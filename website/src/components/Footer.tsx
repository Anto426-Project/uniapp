import React from 'react';
import { ExternalLink } from 'lucide-react';
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

              />
              <div className="footer-brand-text">
                <span className="footer-brand-title">UniApp</span>
              </div>
            </a>
            <p className="footer-brand-desc">
              Un progetto indipendente per gli utenti dei servizi universitari dell’Università degli Studi del Molise.
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
                <ExternalLink className="w-3 h-3 opacity-60" />
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
                <ExternalLink className="w-3 h-3 opacity-60" />
              </a>
            </div>
          </div>

          {/* Sezione Link Rapidi & Risorse (2 colonne pulite, senza duplicati) */}
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
              <span className="footer-group-title">Sorgenti &amp; API</span>
              <ul className="footer-links-list">
                <li>
                  <a href="https://github.com/Anto426-Project/Uniapp" target="_blank" rel="noopener noreferrer">
                    <span>Progetto UniApp</span>
                    <ExternalLink className="w-3 h-3 opacity-60" />
                  </a>
                </li>
                <li>
                  <a href="https://github.com/Anto426-Project/UniappUpstream" target="_blank" rel="noopener noreferrer">
                    <span>Repository Upstream</span>
                    <ExternalLink className="w-3 h-3 opacity-60" />
                  </a>
                </li>
                <li>
                  <a href={withBasePath('/update.json')} target="_blank" rel="noopener noreferrer">
                    <span>Manifest update.json</span>
                    <ExternalLink className="w-3 h-3 opacity-60" />
                  </a>
                </li>
                <li>
                  <a href="https://github.com/Anto426-Project/Uniapp/issues" target="_blank" rel="noopener noreferrer">
                    <span>Segnala Bug / Feedback</span>
                    <ExternalLink className="w-3 h-3 opacity-60" />
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>

        {/* Barra Inferiore Footer */}
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
        </div>
      </div>
    </footer>
  );
};
