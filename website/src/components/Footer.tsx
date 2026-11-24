'use client';

import React from 'react';
import { usePathname } from 'next/navigation';
import { ArrowUp, ExternalLink, Moon, Sun } from 'lucide-react';
import { MONET_THEMES } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';

export function Footer() {
  const { theme, setTheme, appearance, toggleAppearance, release } = useSite();
  const pathname = usePathname() || '/';
  const homePrefix = pathname === '/ios' || pathname.startsWith('/ios/') ? withBasePath('/') : '';
  const versionLabel = release?.latestVersion
    ? release.latestVersion.startsWith('v')
      ? release.latestVersion
      : `v${release.latestVersion}`
    : null;

  return (
    <footer className="site-footer">
      <div className="container-shell">
        <div className="footer-top-strip">
          <div className="footer-brand-row">
            <a href="#" className="nav-brand">
              <img
                src={withBasePath('/assets/uniapp-icon.webp')}
                alt="UniApp"
                width={26}
                height={26}
                className="nav-brand-icon"
              />
              <span className="nav-brand-title">UniApp</span>
            </a>
            {versionLabel && <span className="pill-badge">{versionLabel}</span>}
          </div>

          <div className="footer-controls-row">
            <div className="swatch-cluster" role="group" aria-label="Scegli il tema dal piè di pagina">
              {MONET_THEMES.map((t) => (
                <button
                  key={t.id}
                  type="button"
                  className="swatch-dot"
                  aria-label={`Tema ${t.label}`}
                  aria-pressed={theme === t.id}
                  title={t.label}
                  onClick={() => setTheme(t.id)}
                >
                  <span
                    className="swatch-dot-fill"
                    style={{ backgroundColor: t.swatchHex }}
                    aria-hidden="true"
                  />
                </button>
              ))}
            </div>

            <button
              type="button"
              className="appearance-toggle footer-appearance-btn"
              onClick={() => {
                hapticSnap(5);
                toggleAppearance();
              }}
              aria-label={appearance === 'dark' ? 'Attiva aspetto chiaro' : 'Attiva aspetto scuro'}
              title={appearance === 'dark' ? 'Passa a tema chiaro' : 'Passa a tema scuro'}
            >
              {appearance === 'dark' ? (
                <Sun className="w-3.5 h-3.5" aria-hidden="true" />
              ) : (
                <Moon className="w-3.5 h-3.5" aria-hidden="true" />
              )}
            </button>

            <a href="#" className="footer-back-top" aria-label="Torna all'inizio della pagina">
              <span>Torna su</span>
              <ArrowUp className="w-3.5 h-3.5" aria-hidden="true" />
            </a>
          </div>
        </div>

        <div className="footer-directory">
          <div className="footer-about-col">
            <p className="type-callout">
              App nativa, gratuita e open source per gli studenti dell&apos;Università degli Studi del Molise. Sviluppata in Kotlin Multiplatform, Compose Multiplatform e Liquid Monet.
            </p>
            <div className="footer-account-row">
              <a
                href="https://github.com/Anto426-Project"
                target="_blank"
                rel="noopener noreferrer"
                className="account-chip"
              >
                <img src="https://github.com/Anto426-Project.png" alt="Anto426-Project" width={22} height={22} />
                <span>Anto426-Project</span>
                <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
              </a>

              <a
                href="https://github.com/Anto426"
                target="_blank"
                rel="noopener noreferrer"
                className="account-chip"
              >
                <img src="https://github.com/Anto426.png" alt="Anto426" width={22} height={22} />
                <span>Anto426</span>
                <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
              </a>
            </div>
          </div>

          <div>
            <span className="footer-col-title">Sezioni</span>
            <ul className="footer-links">
              <li><a href={`${homePrefix}#features`}>Funzionalità</a></li>
              <li><a href={`${homePrefix}#screenshots`}>Schermate</a></li>
              <li><a href={`${homePrefix}#architecture`}>Architettura</a></li>
              <li><a href={`${homePrefix}#download`}>Download</a></li>
              <li><a href={withBasePath('/ios/')}>Sideload iOS</a></li>
              <li><a href={`${homePrefix}#changelog`}>Note di rilascio</a></li>
              <li><a href={`${homePrefix}#security`}>Sicurezza</a></li>
            </ul>
          </div>

          <div>
            <span className="footer-col-title">Distribuzione</span>
            <ul className="footer-links">
              <li>
                <a
                  href="https://github.com/Anto426-Project/uniapp-upstream/releases"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <span>Rilasci Android e iOS</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
              <li>
                <a href={withBasePath('/altstore.json')} target="_blank" rel="noopener noreferrer">
                  <span>Sorgente AltStore / SideStore</span>
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

          <div>
            <span className="footer-col-title">Progetto</span>
            <ul className="footer-links">
              <li>
                <a
                  href="https://github.com/Anto426-Project/Uniapp"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <span>Codice su GitHub</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
              <li>
                <a
                  href="https://github.com/Anto426-Project/Liquid-Monet"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <span>Liquid Monet SDK</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
              <li>
                <a
                  href="https://github.com/Anto426-Project/Uniapp/issues"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <span>Segnala un problema</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
              <li>
                <a
                  href="https://github.com/Anto426-Project/Uniapp/pulls"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <span>Contribuisci</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div className="footer-bottom">
          <p className="footer-copyright">
            &copy; 2026{' '}
            <a href="https://github.com/Anto426" target="_blank" rel="noopener noreferrer">
              Anto426
            </a>{' '}
            e{' '}
            <a href="https://github.com/Anto426-Project" target="_blank" rel="noopener noreferrer">
              Anto426-Project
            </a>{' '}
            · Progetto open source indipendente, non affiliato all&apos;Università degli Studi del Molise.
          </p>
          <span className="footer-tech-tag">Kotlin Multiplatform · Liquid Monet</span>
        </div>
      </div>
    </footer>
  );
}
