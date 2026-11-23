'use client';

import React from 'react';
import { Download, ChevronRight } from 'lucide-react';
import { MONET_THEMES, useRelease, useThemeControls } from './SiteShell';
import { withBasePath } from '@/utils/basePath';

export const Hero = () => {
  const release = useRelease();
  const { theme, setTheme } = useThemeControls();

  const description =
    release?.description ||
    'Esperienza nativa multipiattaforma per gli studenti dell’Università degli Studi del Molise, sviluppata con Kotlin Multiplatform, Compose Multiplatform e Liquid Monet.';

  const versionLabel = release?.latestVersion
    ? release.latestVersion.startsWith('v')
      ? release.latestVersion
      : `v${release.latestVersion}`
    : null;

  const activeThemeMeta = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];
  const heroPreviewSrc = withBasePath(`/assets/screenshots/previews/${activeThemeMeta.assetPrefix}-home.webp`);

  return (
    <section className="hero-stage">
      <div className="container">
        <div className="hero-grid">
          {/* Left Column: Editorial Keynote Copy */}
          <div className="hero-editorial">
            <a href="#download" className="hero-status-pill">
              <span className="hero-status-dot" aria-hidden="true" />
              <span>
                {versionLabel ? `UniApp ${versionLabel} · ` : ''}
                Android 10+ &amp; iOS · Open Source
              </span>
              <ChevronRight className="w-3.5 h-3.5 text-[var(--theme-primary)]" aria-hidden="true" />
            </a>

            <h1 className="hero-headline">
              La tua carriera universitaria,{' '}
              <span className="hero-headline-accent">in un unico posto.</span>
            </h1>

            <p className="hero-subcopy">{description}</p>

            <div className="hero-actions">
              <a href="#download" className="btn-apple-primary">
                <Download className="w-4 h-4" aria-hidden="true" />
                <span>Scarica UniApp</span>
              </a>
              <a
                href="https://github.com/Anto426-Project/Uniapp"
                target="_blank"
                rel="noopener noreferrer"
                className="btn-apple-glass"
              >
                <span>Sorgenti su GitHub</span>
                <ChevronRight className="w-4 h-4 opacity-70" aria-hidden="true" />
              </a>
            </div>

            <div className="hero-pigment-row">
              <span className="hero-pigment-label">Prova Liquid Monet</span>
              <div className="segmented-control" role="group" aria-label="Scegli tema cromatico Monet">
                {MONET_THEMES.map((item) => {
                  const isActive = theme === item.id;
                  return (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => setTheme(item.id)}
                      aria-pressed={isActive}
                      className={`segmented-option ${isActive ? 'active' : ''}`}
                    >
                      <span className={`segmented-swatch ${item.className}`} aria-hidden="true" />
                      <span>{item.label}</span>
                    </button>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Right Column: Live Interactive Device Stage */}
          <div className="hero-device-stage">
            <div className="hero-device-glow" aria-hidden="true" />
            <div className="hero-phone-bezel">
              <div className="hero-phone-screen">
                <img
                  src={heroPreviewSrc}
                  alt={`Anteprima schermata Home di UniApp con tema ${activeThemeMeta.fullLabel}`}
                />
              </div>
            </div>
            <div className="hero-device-caption">
              <span>Dashboard &amp; Carriera</span>
              <span>·</span>
              <strong>{activeThemeMeta.fullLabel}</strong>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
