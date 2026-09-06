'use client';

import React from 'react';
import GlassSurface from './GlassSurface';

export const SecuritySection: React.FC = () => {
  return (
    <section id="security">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">Trasparenza</span>
          <h2 className="section-title">Integrità &amp; Sicurezza</h2>
          <p className="section-description">
            La privacy dei tuoi dati accademici è la priorità assoluta. Nessun intermediario e codice trasparente.
          </p>
        </div>

        <GlassSurface
          width="100%"
          height="auto"
          borderRadius={28}
          className="security-glass-box as-block"
          distortionScale={-140}
          backgroundOpacity={0.12}
          saturation={1.3}
        >
          <div className="sec-left-col">
            <div className="sec-shield">
              <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.3" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                <path d="m9 12 2 2 4-4" />
              </svg>
            </div>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'white' }}>VirusTotal Clean</h3>
            <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: '6px' }}>
              Report di scansione automatizzata alla compilazione.
            </p>
            <a
              href="https://www.virustotal.com/gui/file-analysis/NzQ4MmEyM2JhODg0NzdjMjc1NTE1NmYyNDEyZWRiMDY6MTc3OTcxMzA4MQ==/detection"
              target="_blank"
              rel="noopener noreferrer"
              className="btn-vt-report"
            >
              <span>Report VirusTotal</span>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                <polyline points="15 3 21 3 21 9" />
                <line x1="10" y1="14" x2="21" y2="3" />
              </svg>
            </a>
          </div>

          <ul className="sec-points">
            <li className="sec-point-item">
              <div className="sec-item-icon">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                  <rect width="18" height="11" x="3" y="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </div>
              <div className="sec-item-text">
                <h4>Keystore Hardware &amp; Biometria</h4>
                <p>Le credenziali non vengono salvate in chiaro e restano custodite nella memoria sicura protetta del dispositivo.</p>
              </div>
            </li>

            <li className="sec-point-item">
              <div className="sec-item-icon">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                  <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
                </svg>
              </div>
              <div className="sec-item-text">
                <h4>Connessione Diretta Ateneo</h4>
                <p>Le chiamate API verso Esse3 e Moodle avvengono direttamente dal telefono senza transitare da server intermedi non autorizzati.</p>
              </div>
            </li>

            <li className="sec-point-item">
              <div className="sec-item-icon">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2">
                  <path d="M2 12h20" />
                  <path d="M20 12v6a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-6" />
                </svg>
              </div>
              <div className="sec-item-text">
                <h4>Nessun Tracciamento</h4>
                <p>Nessun modulo pubblicitario, nessun dato ceduto a terzi e codice sorgente verificabile.</p>
              </div>
            </li>
          </ul>
        </GlassSurface>
      </div>
    </section>
  );
};
