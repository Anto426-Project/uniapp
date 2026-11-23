'use client';

import React from 'react';
import { ExternalLink, ArrowUp } from 'lucide-react';
import { withBasePath } from '@/utils/basePath';
import { MONET_THEMES, useRelease, useThemeControls } from './SiteShell';

export const Footer: React.FC = () => {
  const release = useRelease();
  const { theme, setTheme } = useThemeControls();
  const versionLabel = release?.latestVersion
    ? release.latestVersion.startsWith('v')
      ? release.latestVersion
      : `v${release.latestVersion}`
    : null;

  return (
    <footer className="site-footer">
      <div className="container">
        {/* Top Apple-style Brand & Theme Utility Strip */}
        <div className="footer-top-bar">
          <a href="#" className="footer-brand-link">
            <img
              src={withBasePath('/assets/uniapp-icon.webp')}
              alt="Icona UniApp"
              className="footer-brand-icon"
            />
            <div className="footer-brand-meta">
              <span className="footer-brand-title">UniApp</span>
              {versionLabel && <span className="footer-version-tag">{versionLabel}</span>}
            </div>
          </a>

          <div className="footer-top-controls">
            <div className="palette-dots" role="group" aria-label="Scegli pigmento Monet">
              {MONET_THEMES.map((t) => (
                <button
                  key={t.id}
                  type="button"
                  className={`palette-btn ${t.className} ${theme === t.id ? 'active' : ''}`}
                  aria-label={`Tema ${t.fullLabel}`}
                  aria-pressed={theme === t.id}
                  title={t.fullLabel}
                  onClick={() => setTheme(t.id)}
                />
              ))}
            </div>

            <a href="#" className="footer-top-link" aria-label="Torna in cima">
              <span>Torna su</span>
              <ArrowUp className="w-3.5 h-3.5" aria-hidden="true" />
            </a>
          </div>
        </div>

        {/* Main 4-Column Apple Directory Grid */}
        <div className="footer-directory-grid">
          <div className="footer-brand-col">
            <p className="footer-brand-desc">
              Applicazione nativa multipiattaforma, libera e 100% open source per gli studenti dell’Università degli Studi del Molise. Sviluppata con Kotlin Multiplatform, Compose Multiplatform e Liquid Monet.
            </p>

            <div className="footer-accounts-container">
              <a
                href="https://github.com/Anto426-Project"
                target="_blank"
                rel="noopener noreferrer"
                className="footer-account-chip"
                title="Organizzazione GitHub Anto426-Project"
              >
                <img
                  src="https://github.com/Anto426-Project.png"
                  alt="Anto426-Project"
                  className="footer-account-avatar"
                />
                <span>Anto426-Project</span>
                <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
              </a>

              <a
                href="https://github.com/Anto426"
                target="_blank"
                rel="noopener noreferrer"
                className="footer-account-chip"
                title="Profilo GitHub di Anto426"
              >
                <img
                  src="https://github.com/Anto426.png"
                  alt="Anto426"
                  className="footer-account-avatar"
                />
                <span>Anto426</span>
                <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
              </a>
            </div>
          </div>

          <div className="footer-nav-col">
            <span className="footer-group-title">Esplora</span>
            <ul className="footer-links-list">
              <li><a href="#screenshots">Schermate</a></li>
              <li><a href="#features">Funzionalità</a></li>
              <li><a href="#architecture">Architettura</a></li>
              <li><a href="#download">Download</a></li>
              <li><a href="#changelog">Note di Rilascio</a></li>
              <li><a href="#security">Sicurezza</a></li>
            </ul>
          </div>

          <div className="footer-nav-col">
            <span className="footer-group-title">Distribuzione</span>
            <ul className="footer-links-list">
              <li>
                <a href="https://github.com/Anto426-Project/uniapp-upstream/releases" target="_blank" rel="noopener noreferrer">
                  <span>Rilasci Android &amp; iOS</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
              <li>
                <a href={withBasePath('/update.json')} target="_blank" rel="noopener noreferrer">
                  <span>Manifest update.json</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
              <li>
                <a href={withBasePath('/platforms.json')} target="_blank" rel="noopener noreferrer">
                  <span>Indice platforms.json</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
            </ul>
          </div>

          <div className="footer-nav-col">
            <span className="footer-group-title">Open Source</span>
            <ul className="footer-links-list">
              <li>
                <a href="https://github.com/Anto426-Project/Uniapp" target="_blank" rel="noopener noreferrer">
                  <span>Repository UniApp</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
              <li>
                <a href="https://github.com/Anto426-Project/Liquid-Monet" target="_blank" rel="noopener noreferrer">
                  <span>Liquid Monet SDK</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
              <li>
                <a href="https://github.com/Anto426-Project/Uniapp/issues" target="_blank" rel="noopener noreferrer">
                  <span>Segnala un problema</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
              <li>
                <a href="https://github.com/Anto426-Project/Uniapp/pulls" target="_blank" rel="noopener noreferrer">
                  <span>Contribuisci</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Legal & Stack Bar */}
        <div className="footer-bottom-row">
          <p className="footer-copyright-text">
            <span>&copy; 2026</span>{' '}
            <a href="https://github.com/Anto426" target="_blank" rel="noopener noreferrer">
              Anto426
            </a>{' '}
            &amp;{' '}
            <a href="https://github.com/Anto426-Project" target="_blank" rel="noopener noreferrer">
              Anto426-Project
            </a>
            <span className="footer-copyright-separator">&bull;</span>
            <span>Progetto open-source indipendente non affiliato all&apos;Ateneo.</span>
          </p>
          <span className="footer-stack-note">Kotlin Multiplatform · Liquid Monet</span>
        </div>
      </div>
    </footer>
  );
};
