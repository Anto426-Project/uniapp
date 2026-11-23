'use client';

import React, { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { ChevronLeft, ChevronRight, Maximize2 } from 'lucide-react';
import type { OriginRect, ScreenshotItem } from '@/data/types';
import { previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import {
  SPRING_MOMENTUM,
  SPRING_REPOSITION,
  SpringDriver,
  VelocityTracker,
  clamp,
  hapticSnap,
  project,
} from '@/lib/fluid-physics';
import { CrossfadeImage } from './CrossfadeImage';

interface FluidCarouselProps {
  items: ScreenshotItem[];
  paused: boolean;
  reducedMotion: boolean;
  onOpen: (index: number, originRect: OriginRect) => void;
}

/** Normalize any integer into [0, count - 1] */
function wrapIndex(value: number, count: number): number {
  if (count <= 0) return 0;
  return ((value % count) + count) % count;
}

/** Shortest signed modular distance from continuous `pos` to slide `index` in (-count/2, +count/2] */
function circularDelta(index: number, pos: number, count: number): number {
  let delta = (((index - pos) % count) + count) % count;
  if (delta > count / 2) delta -= count;
  return delta;
}

/**
 * Infinite Circular Apple Fluid Interfaces Carousel:
 * - True continuous infinite loop in both directions (zero end-stops, zero wrap jumps)
 * - Direct 1:1 pointer tracking with 10px directional hysteresis (vertical page scroll never blocked)
 * - Interruptible mid-flight from the live presentation value
 * - Apple exponential-decay momentum projection + seamless drag-to-spring velocity handoff
 * - Compositor-only transform & opacity updates via requestAnimationFrame
 */
export function FluidCarousel({
  items,
  paused,
  reducedMotion,
  onOpen,
}: FluidCarouselProps) {
  const rootRef = useRef<HTMLDivElement>(null);
  const viewportRef = useRef<HTMLDivElement>(null);
  const slideRefs = useRef<(HTMLDivElement | null)[]>([]);

  const [selected, setSelected] = useState(0);
  const [playing, setPlaying] = useState(true);

  /** Unbounded continuous virtual index (e.g. ..., -2, -1, 0, 1, ..., 7, 8, ...) */
  const targetVirtualRef = useRef(0);
  const strideRef = useRef(260);
  const springRef = useRef<SpringDriver | null>(null);
  const trackerRef = useRef(new VelocityTracker(95));
  const suppressClickUntilRef = useRef(0);

  const dragRef = useRef<{
    active: boolean;
    pointerId: number;
    mode: 'undecided' | 'horizontal' | 'vertical';
    startX: number;
    startY: number;
    startPos: number;
  }>({
    active: false,
    pointerId: -1,
    mode: 'undecided',
    startX: 0,
    startY: 0,
    startPos: 0,
  });

  const count = items.length;

  const measureStride = useCallback(() => {
    const vpWidth = viewportRef.current?.clientWidth || 1000;
    const cardEl = slideRefs.current[0];
    const cardWidth = cardEl?.offsetWidth || 256;
    // Generous flat gallery spacing so flanking screens sit cleanly beside the protagonist without crowding
    strideRef.current = clamp(Math.min(cardWidth * 1.18, vpWidth * 0.72), 196, 332);
  }, []);

  const renderSlides = useCallback(
    (pos: number, velocitySlidePerSec: number) => {
      if (count === 0) return;
      const stride = strideRef.current;
      const speedStretch = reducedMotion
        ? 0
        : clamp(Math.abs(velocitySlidePerSec) * 0.0025, 0, 0.012);

      for (let i = 0; i < count; i++) {
        const el = slideRefs.current[i];
        if (!el) continue;

        const delta = circularDelta(i, pos, count);
        const absDelta = Math.abs(delta);

        // Flat, distortion-free Apple plane (zero 3D rotateY skew)
        const tx = delta * stride;

        // Calm depth hierarchy: crisp 1.0 protagonist + 0.84 adjacent flanks; distant slides fade out cleanly
        const scale = reducedMotion
          ? absDelta < 0.5
            ? 1
            : 0.88
          : clamp(1 - absDelta * 0.16, 0.76, 1);

        let opacity = 0;
        if (absDelta <= 1) {
          opacity = 1 - absDelta * 0.54; // 1.0 center -> 0.46 immediate flanks
        } else if (absDelta < 1.65) {
          opacity = 0.46 * (1 - (absDelta - 1) / 0.65); // smooth fade to 0 before outer edge
        }

        const zIndex = Math.round(50 - absDelta * 12);
        const scaleX = scale * (1 + speedStretch);
        const scaleY = scale * (1 - speedStretch * 0.5);

        el.style.transform = `translate3d(calc(-50% + ${tx.toFixed(2)}px), -50%, 0) scale3d(${scaleX.toFixed(4)}, ${scaleY.toFixed(4)}, 1)`;
        el.style.opacity = opacity.toFixed(3);
        el.style.zIndex = String(zIndex);
        el.style.pointerEvents = absDelta < 1.35 ? 'auto' : 'none';
      }
    },
    [count, reducedMotion]
  );

  const springToVirtual = useCallback(
    (nextVirtual: number, velocity = 0, useMomentum = false) => {
      targetVirtualRef.current = nextVirtual;
      setSelected(wrapIndex(Math.round(nextVirtual), count));
      springRef.current?.animateTo(nextVirtual, {
        velocity,
        config: useMomentum ? SPRING_MOMENTUM : SPRING_REPOSITION,
        immediate: reducedMotion,
      });
    },
    [count, reducedMotion]
  );

  const stepBy = useCallback(
    (delta: -1 | 1, fromUser = true) => {
      if (count <= 1) return;
      if (fromUser) {
        setPlaying(false);
        hapticSnap(4);
      }
      springToVirtual(targetVirtualRef.current + delta, 0, false);
    },
    [count, springToVirtual]
  );

  const goToIndexShortest = useCallback(
    (index: number) => {
      const current = springRef.current?.value ?? targetVirtualRef.current;
      const shortest = circularDelta(index, Math.round(current), count);
      springToVirtual(Math.round(current) + shortest, 0, false);
    },
    [count, springToVirtual]
  );

  useEffect(() => {
    const driver = new SpringDriver(
      targetVirtualRef.current,
      (val, vel) => renderSlides(val, vel),
      SPRING_REPOSITION,
      0.002
    );
    springRef.current = driver;
    measureStride();
    renderSlides(driver.value, 0);

    return () => {
      driver.destroy();
      springRef.current = null;
    };
  }, [measureStride, renderSlides]);

  useLayoutEffect(() => {
    measureStride();
    if (springRef.current) {
      renderSlides(springRef.current.value, 0);
    }
  }, [measureStride, renderSlides, items]);

  useEffect(() => {
    const vp = viewportRef.current;
    if (!vp || typeof ResizeObserver === 'undefined') return;
    const observer = new ResizeObserver(() => {
      measureStride();
      if (springRef.current) renderSlides(springRef.current.value, 0);
    });
    observer.observe(vp);
    return () => observer.disconnect();
  }, [measureStride, renderSlides]);

  // Continuous infinite autoplay (disabled under prefers-reduced-motion)
  useEffect(() => {
    const root = rootRef.current;
    if (!root || count <= 1 || reducedMotion || paused || !playing) return;

    let visible = false;
    let timerId: number | null = null;

    const clearTimer = () => {
      if (timerId !== null) {
        window.clearInterval(timerId);
        timerId = null;
      }
    };

    const syncTimer = () => {
      clearTimer();
      if (visible && !document.hidden) {
        timerId = window.setInterval(() => {
          stepBy(1, false);
        }, 4500);
      }
    };

    const observer = new IntersectionObserver(
      ([entry]) => {
        visible = entry.isIntersecting;
        syncTimer();
      },
      { threshold: 0.25 }
    );

    observer.observe(root);
    document.addEventListener('visibilitychange', syncTimer);

    return () => {
      clearTimer();
      observer.disconnect();
      document.removeEventListener('visibilitychange', syncTimer);
    };
  }, [count, paused, playing, reducedMotion, stepBy]);

  const onPointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    if (e.button !== 0 && e.pointerType === 'mouse') return;
    const spring = springRef.current;
    if (!spring) return;

    // Interrupt mid-flight at the exact live presentation value (§3)
    const livePos = spring.value;
    if (spring.isAnimating) spring.stop();

    dragRef.current = {
      active: true,
      pointerId: e.pointerId,
      mode: 'undecided',
      startX: e.clientX,
      startY: e.clientY,
      startPos: livePos,
    };
    trackerRef.current.reset(e.clientX, e.clientY);
  };

  const onPointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    const drag = dragRef.current;
    if (!drag.active || drag.pointerId !== e.pointerId) return;

    const dx = e.clientX - drag.startX;
    const dy = e.clientY - drag.startY;

    if (drag.mode === 'undecided') {
      if (Math.hypot(dx, dy) < 10) return;
      if (Math.abs(dx) > Math.abs(dy)) {
        drag.mode = 'horizontal';
        setPlaying(false);
        try {
          e.currentTarget.setPointerCapture(e.pointerId);
        } catch {
          // Synthetic touch streams may not require capture.
        }
      } else {
        drag.mode = 'vertical';
        drag.active = false;
        springToVirtual(Math.round(springRef.current?.value ?? targetVirtualRef.current), 0, false);
        return;
      }
    }

    if (drag.mode === 'horizontal') {
      trackerRef.current.add(e.clientX, e.clientY);
      const stride = strideRef.current;
      // Infinite unconstrained 1:1 continuous tracking!
      const nextPos = drag.startPos - dx / stride;
      springRef.current?.setImmediate(nextPos);
    }
  };

  const finishDrag = (e: React.PointerEvent<HTMLDivElement>) => {
    const drag = dragRef.current;
    if (!drag.active || drag.pointerId !== e.pointerId) return;
    const wasHorizontal = drag.mode === 'horizontal';
    const startPos = drag.startPos;
    drag.active = false;
    drag.mode = 'undecided';

    const spring = springRef.current;
    if (!spring) return;

    if (!wasHorizontal) {
      if (Math.abs(spring.value - targetVirtualRef.current) > 0.02) {
        springToVirtual(Math.round(spring.value), 0, false);
      }
      return;
    }

    suppressClickUntilRef.current = performance.now() + 260;

    const stride = strideRef.current;
    const { vx } = trackerRef.current.getVelocity();
    const vSlide = -vx / stride;

    // Apple momentum projection around the infinite circular track (§6)
    const projectedSlideDelta = project(-vx, 0.992) / stride;
    let target = Math.round(spring.value + projectedSlideDelta);

    const traveled = spring.value - startPos;
    if (target === Math.round(startPos) && Math.abs(traveled) > 0.2) {
      target = Math.round(startPos) + Math.sign(traveled);
    }

    if (wrapIndex(target, count) !== selected) {
      hapticSnap(5);
    }

    springToVirtual(target, vSlide, true);
  };

  if (!count) return null;
  const activeItem = items[selected] || items[0];

  return (
    <div
      ref={rootRef}
      className="screenshot-carousel"
      role="region"
      aria-roledescription="carosello"
      aria-label="Schermate di UniApp"
      onKeyDown={(e) => {
        if (e.key === 'ArrowLeft') {
          e.preventDefault();
          stepBy(-1, true);
        } else if (e.key === 'ArrowRight') {
          e.preventDefault();
          stepBy(1, true);
        }
      }}
    >
      <div className="screenshot-carousel__shell">
        {count > 1 && (
          <>
            <button
              type="button"
              className="carousel-edge-btn carousel-edge-btn--left"
              aria-label="Schermata precedente"
              onClick={() => stepBy(-1, true)}
            >
              <ChevronLeft className="w-5 h-5" aria-hidden="true" />
            </button>
            <button
              type="button"
              className="carousel-edge-btn carousel-edge-btn--right"
              aria-label="Schermata successiva"
              onClick={() => stepBy(1, true)}
            >
              <ChevronRight className="w-5 h-5" aria-hidden="true" />
            </button>
          </>
        )}

        <div
          ref={viewportRef}
          className="screenshot-carousel__viewport"
          onPointerDown={onPointerDown}
          onPointerMove={onPointerMove}
          onPointerUp={finishDrag}
          onPointerCancel={finishDrag}
        >
          <div className="screenshot-carousel__halo" aria-hidden="true" />
          <div className="screenshot-carousel__stage">
            {items.map((item, index) => {
              const isSelected = index === selected;
              return (
                <div
                  key={item.id}
                  ref={(el) => {
                    slideRefs.current[index] = el;
                  }}
                  className="screenshot-carousel__slide"
                  role="group"
                  aria-roledescription="schermata"
                  aria-label={`${index + 1} di ${count}`}
                  data-selected={isSelected}
                >
                  <button
                    type="button"
                    className="screenshot-carousel__card"
                    tabIndex={isSelected ? 0 : -1}
                    aria-label={
                      isSelected
                        ? `Apri a schermo intero: ${item.title}`
                        : `Mostra ${item.title}`
                    }
                    onClick={(e) => {
                      if (performance.now() < suppressClickUntilRef.current) return;
                      setPlaying(false);
                      if (isSelected) {
                        const rect = e.currentTarget.getBoundingClientRect();
                        hapticSnap(6);
                        onOpen(index, {
                          left: rect.left,
                          top: rect.top,
                          width: rect.width,
                          height: rect.height,
                        });
                      } else {
                        hapticSnap(4);
                        goToIndexShortest(index);
                      }
                    }}
                  >
                    <div className="screenshot-carousel__screen">
                      <CrossfadeImage
                        src={withBasePath(previewPathFor(item.file))}
                        alt={item.title}
                        width={480}
                        height={1003}
                        loading={index < 3 ? 'eager' : 'lazy'}
                        decoding="async"
                        draggable={false}
                      />
                      {isSelected && (
                        <span className="screenshot-carousel__zoom-hint" aria-hidden="true">
                          <Maximize2 className="w-3.5 h-3.5" />
                        </span>
                      )}
                    </div>
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <div className="screenshot-carousel__meta">
        <div className="screenshot-carousel__meta-head">
          <h3 className="type-headline">{activeItem.title}</h3>
          <span
            className="screenshot-carousel__position"
            aria-live={playing ? 'off' : 'polite'}
            aria-atomic="true"
          >
            {selected + 1} / {count}
          </span>
        </div>
        <p className="type-callout">{activeItem.description}</p>
      </div>

      {count > 1 && (
        <div className="screenshot-carousel__dots" role="tablist" aria-label="Scegli schermata">
          {items.map((item, index) => (
            <button
              key={item.id}
              type="button"
              role="tab"
              aria-selected={index === selected}
              aria-label={`Vai a ${item.title}`}
              className="screenshot-carousel__dot"
              data-active={index === selected}
              onClick={() => {
                setPlaying(false);
                hapticSnap(4);
                goToIndexShortest(index);
              }}
            />
          ))}
        </div>
      )}
    </div>
  );
}
