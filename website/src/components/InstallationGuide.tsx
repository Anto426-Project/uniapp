'use client';

import React from 'react';

export const InstallationGuide: React.FC = () => {
  return (
    <section id="installation">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">Istruzioni Semplici</span>
          <h2 className="section-title">Installazione su Android</h2>
          <p className="section-description">
            Tre rapidi passaggi per installare l&apos;APK direttamente sul tuo smartphone.
          </p>
        </div>

        <div className="steps-grid">
          <div className="step-box">
            <span className="step-idx">01</span>
            <div className="step-box-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="7 10 12 15 17 10" />
                <line x1="12" y1="15" x2="12" y2="3" />
              </svg>
            </div>
            <h3 className="step-box-title">Scarica l&apos;APK</h3>
            <p className="step-box-desc">
              Scegli il pacchetto <strong>ARM64-v8a</strong> per i dispositivi a 64-bit o <strong>Universale</strong> per qualsiasi dispositivo Android.
            </p>
          </div>

          <div className="step-box">
            <span className="step-idx">02</span>
            <div className="step-box-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            </div>
            <h3 className="step-box-title">Consenti Sorgente</h3>
            <p className="step-box-desc">
              Se richiesto dal sistema, abilita temporaneamente <em>Installa app sconosciute</em> nelle impostazioni del browser (es. Chrome).
            </p>
          </div>

          <div className="step-box">
            <span className="step-idx">03</span>
            <div className="step-box-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                <path d="m9 12 2 2 4-4" />
                <circle cx="12" cy="12" r="10" />
              </svg>
            </div>
            <h3 className="step-box-title">Apri e Accedi</h3>
            <p className="step-box-desc">
              Apri UniApp ed effettua il login con le tue credenziali Esse3. I successivi aggiornamenti verranno notificati direttamente in-app.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
};
