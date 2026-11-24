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

  if (reducedMotion) {
    return <div className={className}>{children}</div>;
  }

  return (
    <motion.div
      className={className}
      initial={{ opacity: 0, y: 16 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.12, margin: '0px 0px -32px 0px' }}
      transition={{
        type: 'spring',
        bounce: 0,
        duration: 0.56,
        delay: delayMs / 1000,
      }}
    >
      {children}
    </motion.div>
  );
}
