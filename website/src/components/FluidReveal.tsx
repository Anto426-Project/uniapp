'use client';

import React from 'react';
import { motion } from 'motion/react';
import { useSite } from './SiteShell';

interface FluidRevealProps {
  children: React.ReactNode;
  className?: string;
  delayMs?: number;
}

export function FluidReveal({ children, className = '', delayMs = 0 }: FluidRevealProps) {
  const { reducedMotion } = useSite();

  return (
    <motion.div
      className={className}
      initial={reducedMotion ? false : { opacity: 0, y: 16 }}
      animate={reducedMotion ? { opacity: 1, y: 0 } : undefined}
      whileInView={reducedMotion ? undefined : { opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.12, margin: '0px 0px -32px 0px' }}
      transition={
        reducedMotion
          ? { duration: 0 }
          : {
              type: 'spring',
              bounce: 0,
              duration: 0.56,
              delay: delayMs / 1000,
            }
      }
    >
      {children}
    </motion.div>
  );
}
