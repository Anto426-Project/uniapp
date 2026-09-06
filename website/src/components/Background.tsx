'use client';

import React, { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';

const Aurora = dynamic(() => import('./reactbits/Aurora'), {
  ssr: false,
});

interface BackgroundProps {
  theme: string;
}

const themeColorStops: Record<string, string[]> = {
  violet: ['#2e1065', '#a855f7', '#ec4899'],
  sapphire: ['#172554', '#3b82f6', '#06b6d4'],
  emerald: ['#022c22', '#10b981', '#34d399'],
  amber: ['#451a03', '#f59e0b', '#fbbf24'],
};

export const Background: React.FC<BackgroundProps> = ({ theme }) => {
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const stops = themeColorStops[theme] || themeColorStops.violet;

  return (
    <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden bg-[#07080c]">
      {/* React Bits Aurora Background (Client-only with SSR disabled) */}
      {mounted && (
        <div className="absolute inset-0 opacity-45 mix-blend-screen transition-opacity duration-700">
          <Aurora
            colorStops={stops}
            blend={0.65}
            amplitude={1.2}
            speed={0.6}
          />
        </div>
      )}

      {/* Bagliori secondari ambientali per profondità Obsidian */}
      <div className="ambient-backdrop">
        <div className="ambient-orb orb-1" />
        <div className="ambient-orb orb-2" />
        <div className="ambient-orb orb-3" />
        <div className="grid-overlay" />
      </div>
    </div>
  );
};
