'use client';

import React from 'react';

export const Architecture: React.FC = () => {
  return (
    <section id="architecture">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">Architettura Nativa</span>
          <h2 className="section-title">Design Moderno &amp; Sicurezza</h2>
          <p className="section-description">
            Tecnologie native all&apos;avanguardia con Liquid-Monet e protezione hardware sul dispositivo per garantire massima fluidità e privacy.
          </p>
        </div>

        <div className="arch-cards-grid">
          {/* 1. Liquid-Monet SDK */}
          <div className="glass-card">
            <div className="card-icon-bubble">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="13.5" cy="6.5" r=".5" fill="currentColor" />
                <circle cx="17.5" cy="10.5" r=".5" fill="currentColor" />
                <circle cx="8.5" cy="7.5" r=".5" fill="currentColor" />
                <circle cx="6.5" cy="12.5" r=".5" fill="currentColor" />
                <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z" />
              </svg>
            </div>
            <h3 className="card-title">Liquid-Monet SDK</h3>
            <p className="card-text">
              Il design system nativo sviluppato con Jetpack Compose e Compose Multiplatform. Gestisce i componenti grafici in vetro ottico (Optical Glass), le animazioni fluide e la palette dinamica Monet che si sincronizza con i pigmenti selezionati (Violet, Sapphire, Emerald, Amber).
            </p>
            <div className="card-tags">
              <span className="arch-tag highlight">Liquid-Monet</span>
              <span className="arch-tag">Optical Glass</span>
              <span className="arch-tag">Palette Dinamiche</span>
              <span className="arch-tag">Compose Multiplatform</span>
            </div>
          </div>

          {/* 2. Sicurezza Zero-Trust e Keystore Hardware */}
          <div className="glass-card">
            <div className="card-icon-bubble">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </div>
            <h3 className="card-title">Sicurezza Zero-Trust &amp; Keystore Hardware</h3>
            <p className="card-text">
              Protezione hardware-backed direttamente sul dispositivo: le sessioni e l&apos;accesso biometrico (impronta o volto) sono custoditi nel chip sicuro dello smartphone, con comunicazioni dirette e cifrate verso i server universitari senza proxy intermedi e senza credenziali in chiaro.
            </p>
            <div className="card-tags">
              <span className="arch-tag highlight">Android Keystore</span>
              <span className="arch-tag">Autenticazione Biometrica</span>
              <span className="arch-tag">Zero Proxy Server</span>
              <span className="arch-tag">Zero Testo in Chiaro</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
