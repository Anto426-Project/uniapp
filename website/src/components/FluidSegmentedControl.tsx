'use client';

import React, { useId } from 'react';
import { motion } from 'motion/react';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';

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
 * iOS Segmented Control powered by Motion `layoutId` spring morphing:
 * - Indicator lives inside the active button (exact 0px bounding-box alignment on 1-row and 2x2 mobile grids)
 * - Critically-damped iOS spring glide across segments with instant interruption
 */
export function FluidSegmentedControl<T extends string>({
  options,
  value,
  onChange,
  ariaLabel,
  compact = false,
}: FluidSegmentedControlProps<T>) {
  const { reducedMotion } = useSite();
  const groupId = useId();

  return (
    <div
      className={`fluid-segmented ${compact ? 'fluid-segmented--compact' : ''}`}
      role="group"
      aria-label={ariaLabel}
    >
      {options.map((option) => {
        const active = option.id === value;
        return (
          <button
            key={option.id}
            type="button"
            className="fluid-segmented__btn"
            aria-pressed={active}
            onClick={() => {
              if (option.id !== value) {
                hapticSnap(5);
                onChange(option.id);
              }
            }}
          >
            {active && (
              <motion.span
                layoutId={`seg-pill-${groupId}`}
                className="fluid-segmented__indicator"
                aria-hidden="true"
                transition={
                  reducedMotion
                    ? { duration: 0.12 }
                    : { type: 'spring', bounce: 0.12, duration: 0.38 }
                }
              />
            )}
            <span className="fluid-segmented__content">
              {option.swatchHex && (
                <span
                  className="fluid-segmented__swatch"
                  style={{ backgroundColor: option.swatchHex }}
                  aria-hidden="true"
                />
              )}
              <span>{option.label}</span>
            </span>
          </button>
        );
      })}
    </div>
  );
}
