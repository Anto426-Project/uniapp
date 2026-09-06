'use client';

import React from 'react';
import { ReleaseChannelData } from '@/data/types';

interface HeroProps {
  release?: ReleaseChannelData;
  channel: string;
}

export const Hero: React.FC<HeroProps> = ({ release }) => {
  const description =
    release?.description ||
    'Stanchi della vecchia app universitaria? Scopri UniApp, l’app non ufficiale per gli studenti dell’Università degli Studi del Molise, sviluppata in autonomia da Anto426. Completamente riscritta in Kotlin nativo, utilizza le più recenti tecnologie Material 3 Design e Jetpack Compose per offrire un’esperienza moderna, fluida e intuitiva. Gestisci la tua carriera, consulta il libretto, prenota gli esami e accedi rapidamente alle informazioni più importanti, tutto in un’unica interfaccia veloce e curata.';

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
