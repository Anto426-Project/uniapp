'use client';

import React, { useState } from 'react';
import { Check, Copy, Download, ExternalLink, FileText, ShieldCheck, Smartphone, Zap } from 'lucide-react';
import type { MonetThemeId } from '@/data/types';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { FluidReveal } from './FluidReveal';
import SpotlightCard from './reactbits/SpotlightCard';

const SPOTLIGHT_HEX: Record<MonetThemeId, string> = {
  sapphire: '#5AA4F0',
  emerald: '#3BD67F',
  sunset: '#F09038',
  violet: '#B074D4',
};

const FALLBACK_CHANGELOG: string[] = [
  '**Liquid Monet 2**: superfici optical glass, palette dinamiche Material You e varianti Dark e Light.',
  '**Didattica e Carriera**: libretto interattivo, simulatore media e laurea, appelli e statistiche CFU.',
  '**UniPass e Presenze QR**: badge digitale 3D reattivo al giroscopio e registrazione rapida delle lezioni.',
  '**Sicurezza Multi-Profilo**: storage cifrato isolato per account, sblocco biometrico e revoca sessioni.',
];

function parseChangelog(notes?: string): string[] {
  if (!notes || !notes.trim()) return [];
  const entries: string[] = [];
  for (let line of notes.split('\n')) {
    line = line.trim();
    if (!line || line.toLowerCase().startsWith('changelog')) continue;
    if (line.startsWith('-') || line.startsWith('*')) {
      line = line.slice(1).trim();
    }
    if (line) entries.push(line);
  }
  return entries;
}

function renderInlineMarkdown(text: string): React.ReactNode[] {
  const parts = text.split(/(\*\*[^*]+\*\*|`[^`]+`)/g);
  return parts.map((part, idx) => {
    if (part.startsWith('**') && part.endsWith('**') && part.length > 4) {
      return (
        <strong key={idx} style={{ fontWeight: 600, color: 'var(--label-primary)' }}>
          {part.slice(2, -2)}
        </strong>
      );
    }
    if (part.startsWith('`') && part.endsWith('`') && part.length > 2) {
      return (
        <code key={idx} className="mono-code">
          {part.slice(1, -1)}
        </code>
      );
    }
    return <React.Fragment key={idx}>{part}</React.Fragment>;
  });
}

function AppleLogo({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" className={className} aria-hidden="true">
      <path d="M16.365 1.43c0 1.14-.493 2.27-1.177 3.08-.744.9-1.99 1.57-2.987 1.57-.18 0-.36-.02-.53-.06-.01-.18-.04-.56-.04-.95 0-1.15.572-2.27 1.206-2.98.804-.94 2.142-1.64 3.248-1.68.03.32.06.67.06 1.02zm4.565 17.71c-.43.96-.63 1.39-1.18 2.24-.77 1.18-1.85 2.65-3.19 2.66-1.19.01-1.5-.77-3.12-.76-1.62.01-1.96.78-3.15.77-1.34-.01-2.36-1.34-3.13-2.52-2.15-3.28-2.38-7.13-1.05-9.18.95-1.46 2.45-2.32 3.86-2.32 1.44 0 2.34.78 3.53.78 1.15 0 1.85-.78 3.51-.78 1.25 0 2.58.68 3.53 1.86-3.1 1.7-2.6 6.14.39 7.25z" />
    </svg>
  );
}

/**
 * Full-Width Apple Bento Download & Changelog Showcase:
 * - Uses 100% of `.container-shell` (`76rem`), aligned pixel-for-pixel with Features & Architecture grids
 * - Top row: 2 symmetrical SpotlightCard platform tiles (Android | iOS)
 * - Bottom row: Always-visible full-width SpotlightCard `#changelog` with bounded internal scroll
 */
export function DownloadHub() {
  const { theme, appearance, release, platforms } = useSite();
  const [copied, setCopied] = useState(false);

  const iosRelease = platforms.platforms.ios;
  const spotColor = SPOTLIGHT_HEX[theme];

  const hasAndroidVersion = Boolean(release?.latestVersion);
  const version = release?.latestVersion
    ? release.latestVersion.startsWith('v')
      ? release.latestVersion
      : `v${release.latestVersion}`
    : null;

  const parsedNotes = parseChangelog(release?.notes);
  const changelogEntries = parsedNotes.length > 0 ? parsedNotes : FALLBACK_CHANGELOG;

  const directApkUrl = release?.downloadUrlsByAbi?.['arm64-v8a'] || release?.downloadUrl || '';
  const hasDirectApk = Boolean(directApkUrl);
  const androidHref = hasDirectApk
    ? directApkUrl
    : 'https://github.com/Anto426-Project/uniapp-upstream/releases/latest';

  const iosDownloads = iosRelease
    ? Object.values(iosRelease.variants)
        .flatMap((variant) => variant.downloads)
        .sort((a, b) => Number(b.name.endsWith('.ipa')) - Number(a.name.endsWith('.ipa')))
    : [];

  const copyDownloadUrl = () => {
    if (!hasDirectApk) return;
    navigator.clipboard
      .writeText(directApkUrl)
      .then(() => {
        hapticSnap(8);
        setCopied(true);
        window.setTimeout(() => setCopied(false), 2000);
      })
      .catch(() => setCopied(false));
  };

  return (
    <section id="download" className="section-block download-showcase">
      <div className="download-ambient-glow" aria-hidden="true" />

      <div className="container-shell relative z-10">
        <FluidReveal>
          <div className="download-lockup">
            <div className="download-squircle">
              <img
                src={withBasePath('/assets/uniapp-icon.webp')}
                alt="Icona UniApp"
                width={72}
                height={72}
              />
            </div>
            <h2 className="type-title-1">Scarica UniApp.</h2>
            <p className="type-body download-lockup-sub">
              Gratuita, open source e senza tracciamento per Android e iOS.
            </p>
          </div>
        </FluidReveal>

        {/* Full-Width 2-Column Platform Grid (100% of container-shell) */}
        <FluidReveal delayMs={50}>
          <div className="download-platform-grid">
            {/* Tile 1: Android */}
            <SpotlightCard
              theme={appearance}
              spotlightColor={spotColor}
              intensity={0.15}
              spotlightSize={320}
              borderGlow={0.65}
              className="download-platform-card"
            >
              <div className="download-card-inner">
                <div className="download-card-head">
                  <div className="download-platform-brand">
                    <span className="download-os-icon download-os-icon--accent" aria-hidden="true">
                      <Smartphone className="w-4 h-4" />
                    </span>
                    <span className="download-os-name">Android</span>
                  </div>
                  <span className="ver-text">
                    {hasAndroidVersion ? version : 'ARM64 · 64 bit'}
                  </span>
                </div>

                <p id="android-release-hint" className="download-card-desc">
                  Android 10 o successivo · Pacchetto APK firmato a 64 bit pronto da installare, con aggiornamenti rapidi integrati.
                </p>

                <div className="download-card-actions">
                  <a
                    href={androidHref}
                    download={hasDirectApk ? true : undefined}
                    target={hasDirectApk ? undefined : '_blank'}
                    rel={hasDirectApk ? undefined : 'noopener noreferrer'}
                    className="btn-primary download-cta-btn"
                  >
                    <Download className="w-4 h-4 shrink-0" aria-hidden="true" />
                    <span>Scarica APK ARM64</span>
                  </a>

                  {hasDirectApk && (
                    <button
                      type="button"
                      className="copy-icon-btn"
                      onClick={copyDownloadUrl}
                      title={copied ? 'Link copiato' : 'Copia link APK ARM64'}
                      aria-label={copied ? 'Link APK copiato negli appunti' : 'Copia link APK ARM64'}
                    >
                      {copied ? (
                        <Check className="w-3.5 h-3.5" style={{ color: 'var(--status-green)' }} aria-hidden="true" />
                      ) : (
                        <Copy className="w-3.5 h-3.5" aria-hidden="true" />
                      )}
                    </button>
                  )}
                </div>
              </div>
            </SpotlightCard>

            {/* Tile 2: iOS (the sole <article> inside #download) */}
            <article aria-label="Download iOS" className="download-article-wrap">
              <SpotlightCard
                theme={appearance}
                spotlightColor={spotColor}
                intensity={0.14}
                spotlightSize={320}
                borderGlow={0.6}
                className="download-platform-card"
              >
                <div className="download-card-inner">
                  <div className="download-card-head">
                    <div className="download-platform-brand">
                      <span className="download-os-icon" aria-hidden="true">
                        <AppleLogo className="w-4 h-4" />
                      </span>
                      <span className="download-os-name">iOS</span>
                    </div>
                    <span className="download-os-pill">
                      {iosRelease ? `Versione ${iosRelease.version}` : 'iPhone · KMP'}
                    </span>
                  </div>

                  <p className="download-card-desc">
                    L’IPA richiede firma e provisioning: firmalo nel browser col tuo Apple ID via WebUSB o SideStore; il bundle simulatore è separato.
                  </p>

                  <div className="download-card-actions">
                    <a href={withBasePath('/ios/')} className="btn-primary download-cta-btn">
                      <Zap className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                      <span>Sideload Web</span>
                    </a>
                    {iosDownloads.length > 0 ? (
                      iosDownloads.map((file) => (
                        <a key={file.name} className="btn-material download-secondary-btn" href={file.url}>
                          <Download className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                          <span>{file.name.endsWith('.ipa') ? 'IPA iOS' : 'Bundle simulatore'}</span>
                        </a>
                      ))
                    ) : (
                      <a
                        href={iosRelease?.url || 'https://github.com/Anto426-Project/uniapp-upstream/releases'}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="btn-material download-secondary-btn"
                      >
                        <span>Rilasci su GitHub</span>
                        <ExternalLink className="w-3.5 h-3.5 opacity-65" aria-hidden="true" />
                      </a>
                    )}
                  </div>
                </div>
              </SpotlightCard>
            </article>
          </div>
        </FluidReveal>

        {/* Full-Width Bounded Changelog Card (100% of container-shell, always visible) */}
        <FluidReveal delayMs={80}>
          <SpotlightCard
            theme={appearance}
            spotlightColor={spotColor}
            intensity={0.12}
            spotlightSize={360}
            borderGlow={0.55}
            className="changelog-card"
          >
            <div id="changelog" className="changelog-card-inner">
              <div className="changelog-drawer-head">
                <div className="changelog-head-left">
                  <span className="changelog-head-icon" aria-hidden="true">
                    <FileText className="w-3.5 h-3.5" />
                  </span>
                  <span className="changelog-drawer-title">
                    {parsedNotes.length > 0 && version
                      ? `Note di rilascio · ${version}`
                      : 'Note di rilascio'}
                  </span>
                </div>

                <div className="changelog-head-links">
                  <a
                    href="https://github.com/Anto426-Project/uniapp-upstream/releases"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-link"
                  >
                    <ShieldCheck className="w-3.5 h-3.5 opacity-75" style={{ color: 'var(--accent)' }} aria-hidden="true" />
                    <span>SHA-256 e storico rilasci</span>
                    <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                  </a>
                </div>
              </div>

              <div
                className="changelog-scroll-region"
                tabIndex={0}
                aria-label="Elenco delle note di rilascio"
              >
                <ul className="changelog-minimal-list">
                  {changelogEntries.map((entry, idx) => (
                    <li key={idx}>{renderInlineMarkdown(entry)}</li>
                  ))}
                </ul>
              </div>
            </div>
          </SpotlightCard>
        </FluidReveal>
      </div>
    </section>
  );
}
