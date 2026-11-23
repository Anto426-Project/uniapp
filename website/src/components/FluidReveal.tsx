'use client';

import React, { useEffect, useRef, useState, type ReactNode } from 'react';

interface FluidRevealProps {
  children: ReactNode;
  delayMs?: number;
  className?: string;
}

/**
 * Restrained Apple-style scroll reveal:
 * subtle 16px upward settle + opacity fade triggered once on viewport entry,
 * immediately static under prefers-reduced-motion.
 */
export function FluidReveal({ children, delayMs = 0, className = '' }: FluidRevealProps) {
  const ref = useRef<HTMLDivElement>(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    if (
      typeof window === 'undefined' ||
      window.matchMedia('(prefers-reduced-motion: reduce)').matches ||
      typeof IntersectionObserver === 'undefined'
    ) {
      setVisible(true);
      return;
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setVisible(true);
          observer.disconnect();
        }
      },
      { threshold: 0.08, rootMargin: '0px 0px -32px 0px' }
    );

    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  return (
    <div
      ref={ref}
      className={`fluid-reveal ${className}`.trim()}
      data-visible={visible}
      style={delayMs > 0 ? { transitionDelay: `${delayMs}ms` } : undefined}
    >
      {children}
    </div>
  );
}
