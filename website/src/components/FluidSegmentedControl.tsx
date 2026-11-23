'use client';

import React, { useCallback, useEffect, useLayoutEffect, useRef } from 'react';
import { SPRING_PILL, SpringDriver, hapticSnap } from '@/lib/fluid-physics';

export interface SegmentedOption<T extends string> {
  id: T;
  label: string;
  swatchHex?: string;
}

interface FluidSegmentedControlProps<T extends string> {
  options: readonly SegmentedOption<T>[];
  value: T;
  onChange: (next: T) => void;
  ariaLabel: string;
  compact?: boolean;
}

/**
 * Apple-style spring-driven segmented control.
 * Indicator anchors cleanly at top: 0; left: 0 and tracks each option's exact
 * offsetLeft/offsetTop/offsetWidth/offsetHeight so there is zero vertical bleed
 * in either single-row desktop capsules or 2x2 mobile grids.
 */
export function FluidSegmentedControl<T extends string>({
  options,
  value,
  onChange,
  ariaLabel,
  compact = false,
}: FluidSegmentedControlProps<T>) {
  const containerRef = useRef<HTMLDivElement>(null);
  const indicatorRef = useRef<HTMLDivElement>(null);
  const buttonRefs = useRef<(HTMLButtonElement | null)[]>([]);

  const springsRef = useRef<{
    x: SpringDriver;
    y: SpringDriver;
    w: SpringDriver;
    h: SpringDriver;
  } | null>(null);
  const initializedRef = useRef(false);
  const draggingRef = useRef(false);

  const applyIndicator = useCallback(() => {
    const indicator = indicatorRef.current;
    const springs = springsRef.current;
    if (!indicator || !springs) return;
    indicator.style.transform = `translate3d(${springs.x.value.toFixed(2)}px, ${springs.y.value.toFixed(2)}px, 0)`;
    indicator.style.width = `${springs.w.value.toFixed(2)}px`;
    indicator.style.height = `${springs.h.value.toFixed(2)}px`;
  }, []);

  const syncToValue = useCallback(
    (immediate: boolean) => {
      const idx = options.findIndex((opt) => opt.id === value);
      const btn = buttonRefs.current[idx >= 0 ? idx : 0];
      const springs = springsRef.current;
      if (!btn || !springs) return;

      const targetX = btn.offsetLeft;
      const targetY = btn.offsetTop;
      const targetW = btn.offsetWidth;
      const targetH = btn.offsetHeight;

      if (immediate) {
        springs.x.setImmediate(targetX);
        springs.y.setImmediate(targetY);
        springs.w.setImmediate(targetW);
        springs.h.setImmediate(targetH);
        applyIndicator();
      } else {
        springs.x.animateTo(targetX, { config: SPRING_PILL });
        springs.y.animateTo(targetY, { config: SPRING_PILL });
        springs.w.animateTo(targetW, { config: SPRING_PILL });
        springs.h.animateTo(targetH, { config: SPRING_PILL });
      }
    },
    [applyIndicator, options, value]
  );

  useEffect(() => {
    const x = new SpringDriver(0, applyIndicator, SPRING_PILL, 0.08);
    const y = new SpringDriver(0, applyIndicator, SPRING_PILL, 0.08);
    const w = new SpringDriver(0, applyIndicator, SPRING_PILL, 0.08);
    const h = new SpringDriver(0, applyIndicator, SPRING_PILL, 0.08);
    springsRef.current = { x, y, w, h };

    return () => {
      x.destroy();
      y.destroy();
      w.destroy();
      h.destroy();
      springsRef.current = null;
    };
  }, [applyIndicator]);

  useLayoutEffect(() => {
    syncToValue(!initializedRef.current);
    initializedRef.current = true;
  }, [syncToValue]);

  useEffect(() => {
    const el = containerRef.current;
    if (!el || typeof ResizeObserver === 'undefined') return;
    const observer = new ResizeObserver(() => syncToValue(true));
    observer.observe(el);
    return () => observer.disconnect();
  }, [syncToValue]);

  const hitTestOption = (clientX: number, clientY: number): T | null => {
    for (let i = 0; i < options.length; i++) {
      const btn = buttonRefs.current[i];
      if (!btn) continue;
      const rect = btn.getBoundingClientRect();
      if (
        clientX >= rect.left &&
        clientX <= rect.right &&
        clientY >= rect.top &&
        clientY <= rect.bottom
      ) {
        return options[i].id;
      }
    }
    return null;
  };

  const handlePointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    if (e.button !== 0 && e.pointerType === 'mouse') return;
    draggingRef.current = true;
    const hit = hitTestOption(e.clientX, e.clientY);
    if (hit && hit !== value) onChange(hit);
  };

  const handlePointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!draggingRef.current) return;
    const hit = hitTestOption(e.clientX, e.clientY);
    if (hit && hit !== value) {
      hapticSnap(4);
      onChange(hit);
    }
  };

  const endDrag = () => {
    draggingRef.current = false;
  };

  return (
    <div
      ref={containerRef}
      className={`fluid-segmented${compact ? ' fluid-segmented--compact' : ''}`}
      role="group"
      aria-label={ariaLabel}
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={endDrag}
      onPointerCancel={endDrag}
      onPointerLeave={endDrag}
    >
      <div ref={indicatorRef} className="fluid-segmented__indicator" aria-hidden="true" />
      {options.map((opt, idx) => {
        const isActive = opt.id === value;
        return (
          <button
            key={opt.id}
            ref={(el) => {
              buttonRefs.current[idx] = el;
            }}
            type="button"
            className="fluid-segmented__option"
            aria-pressed={isActive}
            onClick={() => {
              if (!isActive) onChange(opt.id);
            }}
          >
            {opt.swatchHex && (
              <span
                className="fluid-segmented__swatch"
                style={{ backgroundColor: opt.swatchHex }}
                aria-hidden="true"
              />
            )}
            <span>{opt.label}</span>
          </button>
        );
      })}
    </div>
  );
}
