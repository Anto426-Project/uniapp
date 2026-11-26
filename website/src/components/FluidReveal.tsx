'use client';

import React from 'react';
import { motion } from 'motion/react';
import { useSite } from './SiteShell';

interface FluidRevealProps {
  children: React.ReactNode;
  className?: string;
  delayMs?: number;
  /** Subtle scale-up materialize (Apple §12: materialize, don't just fade). */
  materialize?: boolean;
}

export function FluidReveal({
  children,
  className = '',
  delayMs = 0,
  materialize = false,
}: FluidRevealProps) {
  const { reducedMotion } = useSite();

  const hiddenState = materialize
    ? { opacity: 0, y: 36, scale: 0.965 }
    : { opacity: 0, y: 28 };
  const visibleState = materialize
    ? { opacity: 1, y: 0, scale: 1 }
    : { opacity: 1, y: 0 };

  return (
    <motion.div
      className={className}
      initial={reducedMotion ? false : hiddenState}
      animate={reducedMotion ? visibleState : undefined}
      whileInView={reducedMotion ? undefined : visibleState}
      viewport={{ once: true, amount: 0.16, margin: '0px 0px -44px 0px' }}
      transition={
        reducedMotion
          ? { duration: 0 }
          : {
              type: 'spring',
              stiffness: 175,
              damping: 25,
              mass: 0.9,
              delay: delayMs / 1000,
            }
      }
    >
      {children}
    </motion.div>
  );
}
