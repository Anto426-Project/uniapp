'use client';

import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { AnimatePresence, motion, useMotionValue, useTransform } from 'motion/react';
import { ChevronLeft, ChevronRight, Moon, Sun, X } from 'lucide-react';
import type { OriginRect, ScreenshotItem } from '@/data/types';
import { MONET_THEMES, fullPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { clamp, hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';

interface FluidLightboxProps {
  isOpen: boolean;
  items: ScreenshotItem[];
  currentIndex: number;
  openedIndex: number;
  stepped: boolean;
  originRect: OriginRect | null;
  onChangeIndex: (nextIndex: number) => void;
  onClose: () => void;
}

const IOS_FLUID_EASE: [number, number, number, number] = [0.32, 0.72, 0, 1];

/**
 * iOS Photos Fullscreen Lightbox (Radix Dialog + Motion):
 * - Physical spring launch from `originRect` to dead-center (`x = 0` locked, zero horizontal drift)
 * - True iOS Photos parallax slide push: outgoing slide parallaxes `-26%` via CSS background underlay
 *   while incoming `<motion.img>` (the single `<img>` in the dialog) glides `100% -> 0%` at full opacity
 *   with ZERO double-exposure ghosting
 * - Vertical `drag="y"` rubber-band swipe-to-dismiss + horizontal pointer swipe navigation
 */
export function FluidLightbox({
  isOpen,
  items,
  currentIndex,
  openedIndex,
  stepped,
  originRect,
  onChangeIndex,
  onClose,
}: FluidLightboxProps) {
  const { theme, setTheme, appearance, toggleAppearance, reducedMotion, setModalDepth } = useSite();
  const [slideState, setSlideState] = useState<{
    dir: 1 | -1;
    outgoingSrc: string | null;
    seq: number;
  }>({ dir: 1, outgoingSrc: null, seq: 0 });

  const wheelLockUntilRef = useRef(0);
  const swipeStartRef = useRef<{ x: number; y: number } | null>(null);

  // Vertical-only drag (`x` is never dragged, guaranteeing 0px horizontal drift!)
  const dragY = useMotionValue(0);
  const interactiveScale = useTransform(dragY, [-180, 0, 220], [0.96, 1, 0.86]);

  const count = items.length;
  const safeIndex = currentIndex < count ? currentIndex : 0;
  const item = items[safeIndex] || items[0];
  const currentFullSrc = item ? withBasePath(fullPathFor(item.file)) : '';

  // Sync background depth push-back and reset state on close
  useEffect(() => {
    setModalDepth(isOpen ? 1 : 0);
    if (!isOpen) {
      dragY.set(0);
      setSlideState({ dir: 1, outgoingSrc: null, seq: 0 });
    }
    return () => {
      setModalDepth(0);
    };
  }, [isOpen, setModalDepth, dragY]);

  // Compute physical launch vector ONCE per open session from `originRect`
  const originVector = useMemo(() => {
    if (!isOpen || !originRect || typeof window === 'undefined') return null;
    const vw = window.innerWidth;
    const vh = window.innerHeight;
    const targetWidth = Math.min(vw * 0.86, 368, (vh * 0.72 * 1440) / 3008);
    const originCenterX = originRect.left + originRect.width / 2;
    const originCenterY = originRect.top + originRect.height / 2;
    return {
      x: originCenterX - vw / 2,
      y: originCenterY - vh / 2,
      scale: clamp(originRect.width / Math.max(targetWidth, 180), 0.42, 0.92),
    };
  }, [isOpen, originRect]);

  // Preload adjacent full-resolution images for instant zero-latency swipes
  useEffect(() => {
    if (!isOpen || count <= 1) return;
    const prevIdx = safeIndex > 0 ? safeIndex - 1 : count - 1;
    const nextIdx = safeIndex < count - 1 ? safeIndex + 1 : 0;
    for (const idx of [prevIdx, nextIdx]) {
      const target = items[idx];
      if (!target) continue;
      const img = new Image();
      img.src = withBasePath(fullPathFor(target.file));
    }
  }, [isOpen, safeIndex, count, items]);

  const stepTo = useCallback(
    (nextIdx: number, dir: 1 | -1) => {
      if (count <= 1) return;
      const wrapped = ((nextIdx % count) + count) % count;
      if (wrapped === safeIndex) return;
      hapticSnap(4);
      setSlideState((prev) => ({
        dir,
        outgoingSrc: currentFullSrc,
        seq: prev.seq + 1,
      }));
      onChangeIndex(wrapped);
    },
    [count, safeIndex, currentFullSrc, onChangeIndex]
  );

  const goPrev = useCallback(() => stepTo(safeIndex - 1, -1), [safeIndex, stepTo]);
  const goNext = useCallback(() => stepTo(safeIndex + 1, 1), [safeIndex, stepTo]);

  useEffect(() => {
    if (!isOpen) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'ArrowLeft') {
        e.preventDefault();
        goPrev();
      } else if (e.key === 'ArrowRight') {
        e.preventDefault();
        goNext();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [isOpen, goPrev, goNext]);

  if (!item) return null;

  const onWheel = (e: React.WheelEvent<HTMLDivElement>) => {
    if (count <= 1) return;
    const now = performance.now();
    if (now < wheelLockUntilRef.current) return;
    const delta = Math.abs(e.deltaX) >= Math.abs(e.deltaY) ? e.deltaX : e.deltaY;
    if (Math.abs(delta) < 28) return;
    wheelLockUntilRef.current = now + 320;
    if (delta > 0) goNext();
    else goPrev();
  };

  const frameInitial = reducedMotion
    ? { opacity: 0 }
    : originVector
      ? {
          x: originVector.x,
          y: originVector.y,
          scale: originVector.scale,
          opacity: 0.85,
        }
      : { x: 0, y: 20, scale: 0.9, opacity: 0 };

  const frameExit = reducedMotion
    ? { opacity: 0, transition: { duration: 0.1 } }
    : originVector && safeIndex === openedIndex
      ? {
          x: originVector.x,
          y: originVector.y,
          scale: originVector.scale,
          opacity: 0,
          transition: { duration: 0.25, ease: IOS_FLUID_EASE },
        }
      : {
          x: 0,
          y: 18,
          scale: 0.88,
          opacity: 0,
          transition: { duration: 0.22, ease: IOS_FLUID_EASE },
        };

  return (
    <Dialog.Root
      open={isOpen}
      onOpenChange={(open) => {
        if (!open) onClose();
      }}
    >
      <AnimatePresence>
        {isOpen && (
          <Dialog.Portal forceMount>
            <Dialog.Overlay asChild>
              <motion.div
                className="lightbox-backdrop"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                transition={{
                  duration: reducedMotion ? 0.1 : 0.26,
                  ease: IOS_FLUID_EASE,
                }}
              />
            </Dialog.Overlay>

            <Dialog.Content
              className="lightbox-layer"
              aria-describedby={undefined}
              onWheel={onWheel}
              onClick={(e) => {
                if (e.target === e.currentTarget) onClose();
              }}
            >
              <Dialog.Title className="sr-only">{item.title}</Dialog.Title>

              {/* Floating Frosted Studio Bar */}
              <motion.div
                className="lightbox-toolbar"
                initial={reducedMotion ? { opacity: 0 } : { opacity: 0, y: -12, scale: 0.96 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                exit={reducedMotion ? { opacity: 0 } : { opacity: 0, y: -8, scale: 0.96 }}
                transition={{
                  duration: reducedMotion ? 0.1 : 0.24,
                  delay: reducedMotion ? 0 : 0.03,
                  ease: IOS_FLUID_EASE,
                }}
              >
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
                          setSlideState((prev) => ({ ...prev, outgoingSrc: null }));
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
                    setSlideState((prev) => ({ ...prev, outgoingSrc: null }));
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

                <Dialog.Close asChild>
                  <button
                    type="button"
                    className="lightbox-close-btn"
                    aria-label="Chiudi anteprima"
                  >
                    <X className="w-4 h-4" aria-hidden="true" />
                  </button>
                </Dialog.Close>
              </motion.div>

              {/* Center Studio Stage — Strictly Symmetrical */}
              <div
                className="lightbox-stage"
                onClick={(e) => {
                  if (e.target === e.currentTarget) onClose();
                }}
              >
                {count > 1 && (
                  <motion.button
                    type="button"
                    className="lightbox-nav-btn"
                    aria-label="Schermata precedente"
                    onClick={goPrev}
                    initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.88 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.88 }}
                    transition={{
                      duration: reducedMotion ? 0.1 : 0.22,
                      delay: reducedMotion ? 0 : 0.04,
                      ease: IOS_FLUID_EASE,
                    }}
                  >
                    <ChevronLeft className="w-5 h-5" aria-hidden="true" />
                  </motion.button>
                )}

                <motion.div
                  className="lightbox-frame"
                  initial={frameInitial}
                  animate={{ x: 0, y: 0, scale: 1, opacity: 1 }}
                  exit={frameExit}
                  transition={
                    reducedMotion
                      ? { duration: 0.12 }
                      : {
                          type: 'spring',
                          stiffness: 330,
                          damping: 32,
                          mass: 0.85,
                        }
                  }
                >
                  <motion.div
                    className="lightbox-drag-carrier"
                    style={reducedMotion ? undefined : { y: dragY, scale: interactiveScale }}
                    drag={reducedMotion ? false : 'y'}
                    dragConstraints={{ top: 0, bottom: 0 }}
                    dragElastic={0.28}
                    onPointerDown={(e) => {
                      swipeStartRef.current = { x: e.clientX, y: e.clientY };
                    }}
                    onPointerUp={(e) => {
                      const start = swipeStartRef.current;
                      swipeStartRef.current = null;
                      if (!start || count <= 1) return;
                      const dx = e.clientX - start.x;
                      const dy = e.clientY - start.y;
                      if (Math.abs(dx) > 46 && Math.abs(dx) > Math.abs(dy) * 1.25) {
                        if (dx < 0) goNext();
                        else goPrev();
                      }
                    }}
                    onDragEnd={(_, info) => {
                      if (info.offset.y > 95 || info.velocity.y > 440) {
                        hapticSnap(6);
                        onClose();
                      }
                    }}
                  >
                    <div className="lightbox-device-halo" aria-hidden="true" />
                    <div className="lightbox-device">
                      <div className="lightbox-device-screen">
                        {/* Outgoing slide parallax underlay (div — zero extra <img> tags!) */}
                        {!reducedMotion && stepped && slideState.outgoingSrc && (
                          <motion.div
                            key={`underlay-${slideState.seq}`}
                            className="lightbox-outgoing-underlay"
                            style={{ backgroundImage: `url("${slideState.outgoingSrc}")` }}
                            initial={{ x: '0%', opacity: 1 }}
                            animate={{ x: `${-slideState.dir * 26}%`, opacity: 0.35 }}
                            transition={{ duration: 0.34, ease: IOS_FLUID_EASE }}
                            aria-hidden="true"
                          />
                        )}

                        {/* Incoming full-resolution slide (sole <img> inside dialog) */}
                        <motion.img
                          key={item.file}
                          src={currentFullSrc}
                          alt={item.title}
                          draggable={false}
                          initial={
                            reducedMotion || !stepped || !slideState.outgoingSrc
                              ? false
                              : { x: `${slideState.dir * 100}%` }
                          }
                          animate={{ x: '0%' }}
                          transition={
                            reducedMotion
                              ? { duration: 0 }
                              : { duration: 0.34, ease: IOS_FLUID_EASE }
                          }
                        />
                        <div className="lightbox-device-sheen" aria-hidden="true" />
                      </div>
                    </div>
                  </motion.div>
                </motion.div>

                {count > 1 && (
                  <motion.button
                    type="button"
                    className="lightbox-nav-btn"
                    aria-label="Schermata successiva"
                    onClick={goNext}
                    initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.88 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.88 }}
                    transition={{
                      duration: reducedMotion ? 0.1 : 0.22,
                      delay: reducedMotion ? 0 : 0.04,
                      ease: IOS_FLUID_EASE,
                    }}
                  >
                    <ChevronRight className="w-5 h-5" aria-hidden="true" />
                  </motion.button>
                )}
              </div>

              {/* Caption + Screen Dots */}
              <motion.div
                className="lightbox-footer"
                initial={reducedMotion ? { opacity: 0 } : { opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                exit={reducedMotion ? { opacity: 0 } : { opacity: 0, y: 8 }}
                transition={{
                  duration: reducedMotion ? 0.1 : 0.24,
                  delay: reducedMotion ? 0 : 0.03,
                  ease: IOS_FLUID_EASE,
                }}
              >
                <div className="lightbox-footer-head">
                  <span className="lightbox-footer-title">{item.title}</span>
                  <span className="lightbox-footer-count">
                    {safeIndex + 1} / {count}
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
                        aria-selected={idx === safeIndex}
                        aria-label={`Mostra ${entry.title}`}
                        className="lightbox-dot"
                        data-active={idx === safeIndex}
                        onClick={() => stepTo(idx, idx >= safeIndex ? 1 : -1)}
                      />
                    ))}
                  </div>
                )}
              </motion.div>
            </Dialog.Content>
          </Dialog.Portal>
        )}
      </AnimatePresence>
    </Dialog.Root>
  );
}
