'use client';

import React from 'react';
import { ReleaseData } from '@/data/types';

interface HeroProps {
  release?: ReleaseData;
}

export const Hero: React.FC<HeroProps> = ({ release }) => {
  const description =
    release?.description ||
    'UniApp è un progetto indipendente per consultare la carriera e utilizzare i servizi universitari dell’Università degli Studi del Molise.';

  return (
    <section className="hero-section">
      <div className="hero-ambient-glow" aria-hidden="true" />
      <div className="container hero-container">
        <div className="hero-content">
          <h1 className="hero-title">
            L&apos;esperienza universitaria per Android,{' '}
            <span className="gradient-text">completamente riscritta.</span>
          </h1>

          <p className="hero-description">{description}</p>
        </div>
      </div>

      <a href="#screenshots" className="hero-scroll-btn" aria-label="Scorri per scoprire le funzionalità">
        <span className="scroll-btn-text">Esplora UniApp</span>
        <div className="scroll-btn-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="6 9 12 15 18 9" />
          </svg>
        </div>
      </a>
    </section>
  );
};
