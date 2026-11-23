'use client';

import React, { useCallback, useEffect, useRef } from 'react';
import { ChevronLeft, ChevronRight, Moon, Sun, X } from 'lucide-react';
import type { OriginRect, ScreenshotItem } from '@/data/types';
import { MONET_THEMES, fullPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import {
  SPRING_MOMENTUM,
  SPRING_SHEET,
  SPRING_UI,
  Spring2D,
  SpringDriver,
  VelocityTracker,
  clamp,
  hapticSnap,
  project,
  rubberband,
} from '@/lib/fluid-physics';
import { useSite } from './SiteShell';

interface FluidLightboxProps {
  isOpen: boolean;
  items: ScreenshotItem[];
  currentIndex: number;
  originRect: OriginRect | null;
  onChangeIndex: (nextIndex: number) => void;
  onClose: () => void;
}

/**
 * Apple Studio Fullscreen Preview:
 * - Deep frosted studio backdrop (Dark Pro obsidian / Light frosted alabaster) with zero background bleed
 * - Compact centered glass toolbar with live Monet theme swatches, Dark/Light toggle, and close action
 * - Framed concentric phone bezel with snug flanking navigation arrows and interactive dot strip
 * - Anchored spring open/close, 1:1 drag-to-dismiss, horizontal flick switching, and full keyboard support
 */
export function FluidLightbox({
  isOpen,
  items,
  currentIndex,
  originRect,
  onChangeIndex,
  onClose,
}: FluidLightboxProps) {
  const { theme, setTheme, appearance, toggleAppearance, reducedMotion, setModalDepth } = useSite();

  const scrimRef = useRef<HTMLDivElement>(null);
  const frameRef = useRef<HTMLDivElement>(null);
  const closeBtnRef = useRef<HTMLButtonElement>(null);

  const posSpringRef = useRef<Spring2D | null>(null);
  const scaleSpringRef = useRef<SpringDriver | null>(null);
  const trackerRef = useRef(new VelocityTracker(95));
  const closingRef = useRef(false);

  const dragRef = useRef<{
    active: boolean;
    pointerId: number;
    axis: 'none' | 'x' | 'y';
    startX: number;
    startY: number;
    baseX: number;
    baseY: number;
  }>({
    active: false,
    pointerId: -1,
    axis: 'none',
    startX: 0,
    startY: 0,
    baseX: 0,
    baseY: 0,
  });

  const item = items[currentIndex] || items[0];
  const count = items.length;

  const renderFrame = useCallback(() => {
    const frame = frameRef.current;
    const scrim = scrimRef.current;
    const pos = posSpringRef.current;
    const scaleSpring = scaleSpringRef.current;
    if (!frame || !pos || !scaleSpring) return;

    const x = pos.x.value;
    const y = pos.y.value;
    const baseScale = scaleSpring.value;

    const dragProgress = clamp(Math.max(0, y) / 380, 0, 1);
    const liveScale = baseScale * (1 - dragProgress * 0.16);
    const scrimOpacity = clamp((baseScale - 0.5) * 2, 0, 1) * (1 - dragProgress * 0.85);

    if (reducedMotion) {
      frame.style.transform = 'translate3d(0, 0, 0)';
      frame.style.opacity = clamp((baseScale - 0.5) * 2, 0, 1).toFixed(3);
    } else {
      frame.style.transform = `translate3d(${x.toFixed(2)}px, ${y.toFixed(2)}px, 0) scale(${liveScale.toFixed(4)})`;
      frame.style.opacity = clamp((baseScale - 0.35) / 0.65, 0, 1).toFixed(3);
    }

    if (scrim) {
      scrim.style.opacity = scrimOpacity.toFixed(3);
    }
    setModalDepth(scrimOpacity);
  }, [reducedMotion, setModalDepth]);

  const dismissAnimated = useCallback(
    (vy = 0) => {
      if (closingRef.current) return;
      closingRef.current = true;
      const pos = posSpringRef.current;
      const scale = scaleSpringRef.current;
      if (!pos || !scale || reducedMotion) {
        onClose();
        return;
      }

      if (Math.abs(pos.y.value) > 20 || Math.abs(vy) > 120) {
        pos.animateTo(0, Math.max(window.innerHeight * 0.65, pos.y.value + 220), {
          vy,
          config: { damping: 1.0, response: 0.26 },
          onRest: () => {
            if (closingRef.current) onClose();
          },
        });
        scale.animateTo(0.75, { config: { damping: 1.0, response: 0.26 } });
      } else if (originRect) {
        const dx = originRect.left + originRect.width / 2 - window.innerWidth / 2;
        const dy = originRect.top + originRect.height / 2 - window.innerHeight / 2;
        pos.animateTo(dx, dy, {
          config: { damping: 1.0, response: 0.26 },
          onRest: () => {
            if (closingRef.current) onClose();
          },
        });
        scale.animateTo(0.48, { config: { damping: 1.0, response: 0.26 } });
      } else {
        scale.animateTo(0.55, {
          config: { damping: 1.0, response: 0.24 },
          onRest: () => {
            if (closingRef.current) onClose();
          },
        });
      }
    },
    [onClose, originRect, reducedMotion]
  );

  const goPrev = useCallback(() => {
    if (count <= 1) return;
    hapticSnap(4);
    onChangeIndex(currentIndex > 0 ? currentIndex - 1 : count - 1);
  }, [count, currentIndex, onChangeIndex]);

  const goNext = useCallback(() => {
    if (count <= 1) return;
    hapticSnap(4);
    onChangeIndex(currentIndex < count - 1 ? currentIndex + 1 : 0);
  }, [count, currentIndex, onChangeIndex]);

  useEffect(() => {
    if (!isOpen) return;
    closingRef.current = false;
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    const startX =
      !reducedMotion && originRect
        ? originRect.left + originRect.width / 2 - window.innerWidth / 2
        : 0;
    const startY =
      !reducedMotion && originRect
        ? originRect.top + originRect.height / 2 - window.innerHeight / 2
        : reducedMotion
          ? 0
          : 24;
    const startScale = reducedMotion ? 1 : originRect ? 0.62 : 0.92;

    const pos = new Spring2D(startX, startY, renderFrame, SPRING_UI, 0.25);
    const scale = new SpringDriver(startScale, renderFrame, SPRING_UI, 0.004);
    posSpringRef.current = pos;
    scaleSpringRef.current = scale;

    renderFrame();
    pos.animateTo(0, 0, { config: SPRING_UI });
    scale.animateTo(1, { config: SPRING_UI });

    closeBtnRef.current?.focus();

    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') dismissAnimated();
      else if (e.key === 'ArrowLeft') goPrev();
      else if (e.key === 'ArrowRight') goNext();
    };
    window.addEventListener('keydown', onKeyDown);

    return () => {
      document.body.style.overflow = prevOverflow;
      window.removeEventListener('keydown', onKeyDown);
      pos.destroy();
      scale.destroy();
      posSpringRef.current = null;
      scaleSpringRef.current = null;
      setModalDepth(0);
    };
  }, [isOpen, originRect, reducedMotion, renderFrame, dismissAnimated, goPrev, goNext, setModalDepth]);

  if (!isOpen || !item) return null;

  const onPointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    if (e.button !== 0 && e.pointerType === 'mouse') return;
    const pos = posSpringRef.current;
    const scale = scaleSpringRef.current;
    if (!pos || !scale) return;

    closingRef.current = false;
    const liveX = pos.x.value;
    const liveY = pos.y.value;
    pos.stop();
    scale.animateTo(1, { config: SPRING_UI });

    e.currentTarget.setPointerCapture(e.pointerId);
    dragRef.current = {
      active: true,
      pointerId: e.pointerId,
      axis: 'none',
      startX: e.clientX,
      startY: e.clientY,
      baseX: liveX,
      baseY: liveY,
    };
    trackerRef.current.reset(e.clientX, e.clientY);
  };

  const onPointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!dragRef.current.active || dragRef.current.pointerId !== e.pointerId) return;
    const pos = posSpringRef.current;
    if (!pos) return;

    const dx = e.clientX - dragRef.current.startX;
    const dy = e.clientY - dragRef.current.startY;
    trackerRef.current.add(e.clientX, e.clientY);

    if (dragRef.current.axis === 'none') {
      if (Math.hypot(dx, dy) < 10) return;
      dragRef.current.axis = Math.abs(dy) >= Math.abs(dx) ? 'y' : 'x';
    }

    if (dragRef.current.axis === 'y') {
      const rawY = dragRef.current.baseY + dy;
      const nextY = rawY < 0 ? rubberband(rawY, 220, 0.55) : rawY;
      pos.setImmediate(0, nextY);
    } else {
      const rawX = dragRef.current.baseX + dx;
      pos.setImmediate(rawX, 0);
    }
  };

  const onPointerEnd = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!dragRef.current.active || dragRef.current.pointerId !== e.pointerId) return;
    const axis = dragRef.current.axis;
    dragRef.current.active = false;
    const pos = posSpringRef.current;
    if (!pos) return;

    const { vx, vy } = trackerRef.current.getVelocity();

    if (axis === 'y') {
      const projectedY = pos.y.value + project(vy, 0.992);
      if (projectedY > 150 || pos.y.value > 125) {
        hapticSnap(6);
        dismissAnimated(Math.max(vy, 250));
      } else {
        pos.animateTo(0, 0, { vy, config: SPRING_SHEET });
      }
      return;
    }

    if (axis === 'x') {
      const projectedX = pos.x.value + project(vx, 0.992);
      if (projectedX < -110 && count > 1) {
        goNext();
        pos.setImmediate(70, 0);
        pos.animateTo(0, 0, { vx: Math.min(vx * 0.35, -180), config: SPRING_MOMENTUM });
      } else if (projectedX > 110 && count > 1) {
        goPrev();
        pos.setImmediate(-70, 0);
        pos.animateTo(0, 0, { vx: Math.max(vx * 0.35, 180), config: SPRING_MOMENTUM });
      } else {
        pos.animateTo(0, 0, { vx, config: SPRING_MOMENTUM });
      }
    }
  };

  return (
    <>
      <div ref={scrimRef} className="lightbox-backdrop" aria-hidden="true" />
      <div
        className="lightbox-layer"
        role="dialog"
        aria-modal="true"
        aria-label={item.title}
        onClick={(e) => {
          if (e.target === e.currentTarget) dismissAnimated();
        }}
      >
        {/* Compact Centered Studio Control Bar */}
        <div className="lightbox-toolbar">
          <div
            className="lightbox-swatches"
            role="group"
            aria-label="Cambia tema nell'anteprima a schermo intero"
          >
            {MONET_THEMES.map((option) => {
              const active = option.id === theme;
              return (
                <button
                  key={option.id}
                  type="button"
                  className="lightbox-swatch-btn"
                  aria-label={`Tema ${option.label}`}
                  aria-pressed={active}
                  title={option.label}
                  onClick={() => {
                    hapticSnap(4);
                    setTheme(option.id);
                  }}
                >
                  <span
                    className="lightbox-swatch-dot"
                    style={{ backgroundColor: option.swatchHex }}
                    aria-hidden="true"
                  />
                </button>
              );
            })}
          </div>

          <span className="lightbox-toolbar-sep" aria-hidden="true" />

          <button
            type="button"
            className="lightbox-mode-btn"
            aria-label={
              appearance === 'dark'
                ? "Passa all'aspetto chiaro nell'anteprima"
                : "Passa all'aspetto scuro nell'anteprima"
            }
            onClick={() => {
              hapticSnap(4);
              toggleAppearance();
            }}
          >
            {appearance === 'dark' ? (
              <>
                <Sun className="w-3.5 h-3.5" aria-hidden="true" />
                <span>Chiaro</span>
              </>
            ) : (
              <>
                <Moon className="w-3.5 h-3.5" aria-hidden="true" />
                <span>Scuro</span>
              </>
            )}
          </button>

          <span className="lightbox-toolbar-sep" aria-hidden="true" />

          <button
            ref={closeBtnRef}
            type="button"
            className="lightbox-close-btn"
            aria-label="Chiudi anteprima"
            onClick={() => dismissAnimated()}
          >
            <X className="w-4 h-4" aria-hidden="true" />
          </button>
        </div>

        {/* Center Studio Stage: Flanking Arrows + Framed Phone */}
        <div
          className="lightbox-stage"
          onClick={(e) => {
            if (e.target === e.currentTarget) dismissAnimated();
          }}
        >
          {count > 1 && (
            <button
              type="button"
              className="lightbox-nav-btn"
              aria-label="Schermata precedente"
              onClick={goPrev}
            >
              <ChevronLeft className="w-5 h-5" aria-hidden="true" />
            </button>
          )}

          <div
            ref={frameRef}
            className="lightbox-frame"
            onPointerDown={onPointerDown}
            onPointerMove={onPointerMove}
            onPointerUp={onPointerEnd}
            onPointerCancel={onPointerEnd}
          >
            <div className="lightbox-device">
              <div className="lightbox-device-screen">
                <img
                  src={withBasePath(fullPathFor(item.file))}
                  alt={item.title}
                  draggable={false}
                />
              </div>
            </div>
          </div>

          {count > 1 && (
            <button
              type="button"
              className="lightbox-nav-btn"
              aria-label="Schermata successiva"
              onClick={goNext}
            >
              <ChevronRight className="w-5 h-5" aria-hidden="true" />
            </button>
          )}
        </div>

        {/* Compact Caption + Interactive Dot Strip */}
        <div className="lightbox-footer">
          <div className="lightbox-footer-head">
            <span className="lightbox-footer-title">{item.title}</span>
            <span className="lightbox-footer-count">
              {currentIndex + 1} / {count}
            </span>
          </div>
          <p className="lightbox-caption-desc">{item.description}</p>
          {count > 1 && (
            <div className="lightbox-dots" role="tablist" aria-label="Schermate nell'anteprima">
              {items.map((entry, idx) => (
                <button
                  key={entry.id}
                  type="button"
                  role="tab"
                  aria-selected={idx === currentIndex}
                  aria-label={`Mostra ${entry.title}`}
                  className="screenshot-carousel__dot"
                  data-active={idx === currentIndex}
                  onClick={() => {
                    hapticSnap(4);
                    onChangeIndex(idx);
                  }}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
}
