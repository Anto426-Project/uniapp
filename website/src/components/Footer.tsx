'use client';

import React from 'react';
import { withBasePath } from '@/utils/basePath';

export const Footer: React.FC = () => {
  return (
    <footer className="site-footer">
      <div className="footer-glow" aria-hidden="true" />
      <div className="container">
        <div className="footer-main-grid">
          {/* Colonna Brand e Progetto */}
          <div className="footer-brand-col">
            <a href="#" className="footer-brand-link">
              <img
                src={withBasePath('/assets/uniapp-icon.webp')}
                alt="Icona UniApp"
                className="footer-brand-icon"
                onError={(e) => {
                  (e.currentTarget as HTMLImageElement).src =
                    'https://raw.githubusercontent.com/Anto426-Project/UniappUpstream/main/assets/uniapp-icon.webp';
                }}
              />
              <div className="footer-brand-text">
                <span className="footer-brand-title">UniApp</span>
              </div>
            </a>
            <p className="footer-brand-desc">
              L&apos;applicazione nativa per gli studenti dell’Università degli Studi del Molise. Progettata con Kotlin Multiplatform, Compose ed elevati standard di privacy.
            </p>

            {/* Account GitHub Ufficiali */}
            <div className="footer-accounts-container">
              <a
                href="https://github.com/Anto426-Project"
                target="_blank"
                rel="noopener noreferrer"
                className="footer-account-chip"
                title="Visita l'organizzazione GitHub Anto426-Project"
              >
                <img
                  src="https://github.com/Anto426-Project.png"
                  alt="Anto426-Project"
                  className="footer-account-avatar"
                />
                <span>Anto426-Project</span>
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                  <polyline points="15 3 21 3 21 9" />
                  <line x1="10" y1="14" x2="21" y2="3" />
                </svg>
              </a>

              <a
                href="https://github.com/Anto426"
                target="_blank"
                rel="noopener noreferrer"
                className="footer-account-chip"
                title="Visita il profilo GitHub di Anto426"
              >
                <img
                  src="https://github.com/Anto426.png"
                  alt="Anto426"
                  className="footer-account-avatar"
                />
                <span>Anto426</span>
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                  <polyline points="15 3 21 3 21 9" />
                  <line x1="10" y1="14" x2="21" y2="3" />
                </svg>
              </a>
            </div>
          </div>

          {/* Sezione Link Rapidi & Risorse */}
          <div className="footer-links-group">
            <div className="footer-nav-col">
              <span className="footer-group-title">Navigazione</span>
              <ul className="footer-links-list">
                <li><a href="#screenshots">Schermate</a></li>
                <li><a href="#features">Caratteristiche</a></li>
                <li><a href="#architecture">Architettura</a></li>
                <li><a href="#download">Download APK</a></li>
                <li><a href="#changelog">Note di Rilascio</a></li>
              </ul>
            </div>

            <div className="footer-nav-col">
              <span className="footer-group-title">GitHub &amp; Profili</span>
              <ul className="footer-links-list">
                <li>
                  <a href="https://github.com/Anto426-Project" target="_blank" rel="noopener noreferrer">
                    <img
                      src="https://github.com/Anto426-Project.png"
                      alt="Anto426-Project"
                      className="footer-inline-avatar"
                    />
                    <span>Anto426-Project</span>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                      <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                      <polyline points="15 3 21 3 21 9" />
                      <line x1="10" y1="14" x2="21" y2="3" />
                    </svg>
                  </a>
                </li>
                <li>
                  <a href="https://github.com/Anto426" target="_blank" rel="noopener noreferrer">
                    <img
                      src="https://github.com/Anto426.png"
                      alt="Anto426"
                      className="footer-inline-avatar"
                    />
                    <span>Anto426</span>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                      <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                      <polyline points="15 3 21 3 21 9" />
                      <line x1="10" y1="14" x2="21" y2="3" />
                    </svg>
                  </a>
                </li>
              </ul>
            </div>

            <div className="footer-nav-col">
              <span className="footer-group-title">Sorgenti &amp; API</span>
              <ul className="footer-links-list">
                <li>
                  <a href="https://github.com/Anto426-Project/Uniapp" target="_blank" rel="noopener noreferrer">
                    <span>Progetto UniApp</span>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                      <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                      <polyline points="15 3 21 3 21 9" />
                      <line x1="10" y1="14" x2="21" y2="3" />
                    </svg>
                  </a>
                </li>
                <li>
                  <a href="https://github.com/Anto426-Project/UniappUpstream" target="_blank" rel="noopener noreferrer">
                    <span>Repository Upstream</span>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                      <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                      <polyline points="15 3 21 3 21 9" />
                      <line x1="10" y1="14" x2="21" y2="3" />
                    </svg>
                  </a>
                </li>
                <li>
                  <a href={withBasePath('/update.json')} target="_blank" rel="noopener noreferrer">
                    <span>Manifest update.json</span>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                      <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                      <polyline points="15 3 21 3 21 9" />
                      <line x1="10" y1="14" x2="21" y2="3" />
                    </svg>
                  </a>
                </li>
                <li>
                  <a href="https://github.com/Anto426-Project/Uniapp/issues" target="_blank" rel="noopener noreferrer">
                    <span>Segnala Bug / Feedback</span>
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                      <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
                      <polyline points="15 3 21 3 21 9" />
                      <line x1="10" y1="14" x2="21" y2="3" />
                    </svg>
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>

        {/* Barra Inferiore Footer */}
        <div className="footer-bottom-row">
          <div className="footer-copyright-text">
            <span>
              &copy; 2026{' '}
              <a href="https://github.com/Anto426" target="_blank" rel="noopener noreferrer" style={{ color: '#fff', textDecoration: 'none', fontWeight: 700 }}>
                Anto426
              </a>{' '}
              &amp;{' '}
              <a href="https://github.com/Anto426-Project" target="_blank" rel="noopener noreferrer" style={{ color: '#fff', textDecoration: 'none', fontWeight: 700 }}>
                Anto426-Project
              </a>{' '}
              • Progetto open-source non ufficiale, indipendente dall&apos;Ateneo.
            </span>
          </div>
          <div className="footer-bottom-badges">
            <span className="footer-pill-tag">
              <span className="footer-monet-dot" />
              Liquid Monet &amp; Compose 1.12
            </span>
          </div>
        </div>
      </div>
    </footer>
  );
};
