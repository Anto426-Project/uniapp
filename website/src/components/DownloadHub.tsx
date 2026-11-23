'use client';

import React, { useState } from 'react';
import {
  Check,
  Copy,
  Download,
  ExternalLink,
  Smartphone,
} from 'lucide-react';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { FluidReveal } from './FluidReveal';

function formatDate(dateStr?: string): string | null {
  if (!dateStr) return null;
  const parsed = new Date(dateStr);
  if (Number.isNaN(parsed.getTime())) return dateStr;
  return parsed.toLocaleDateString('it-IT', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
}

interface ChangelogEntry {
  dotClass: string;
  text: string;
}

function parseChangelog(notes?: string): ChangelogEntry[] {
  if (!notes || !notes.trim()) return [];

  const entries: ChangelogEntry[] = [];
  for (let line of notes.split('\n')) {
    line = line.trim();
    if (!line || line.toLowerCase().startsWith('changelog')) continue;

    let content = line;
    if (line.startsWith('-') || line.startsWith('*')) {
      content = line.slice(1).trim();
    }
    if (!content) continue;

    const lower = content.toLowerCase();
    let dotClass = 'change-dot--feature';

    if (
      lower.includes('corretto') ||
      lower.includes('risolto') ||
      lower.includes('fix') ||
      lower.includes('bug')
    ) {
      dotClass = 'change-dot--fix';
    } else if (
      lower.includes('migliorat') ||
      lower.includes('ottimizzat') ||
      lower.includes('uniformat') ||
      lower.includes('velocizzat') ||
      lower.includes('snellit')
    ) {
      dotClass = 'change-dot--improvement';
    } else if (
      lower.includes('sicurezza') ||
      lower.includes('crittografia') ||
      lower.includes('privacy')
    ) {
      dotClass = 'change-dot--security';
    }

    entries.push({ dotClass, text: content });
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

export function DownloadHub() {
  const { release, platforms } = useSite();
  const [copied, setCopied] = useState(false);

  const iosRelease = platforms.platforms.ios;

  const hasAndroidVersion = Boolean(release?.latestVersion);
  const version = release?.latestVersion
    ? release.latestVersion.startsWith('v')
      ? release.latestVersion
      : `v${release.latestVersion}`
    : null;

  const dateText = formatDate(release?.publishedAt);
  const changelogEntries = parseChangelog(release?.notes);

  const arm64Url = release?.downloadUrlsByAbi?.['arm64-v8a'] || release?.downloadUrl || '#';
  const hasDirectApk = arm64Url !== '#';
  const commitHash = release?.buildCommit ? release.buildCommit.slice(0, 7) : null;
  const commitUrl = release?.buildCommit
    ? `https://github.com/Anto426-Project/Uniapp/commit/${release.buildCommit}`
    : 'https://github.com/Anto426-Project/Uniapp/commits/main';

  const iosDownloads = iosRelease
    ? Object.values(iosRelease.variants)
        .flatMap((variant) => variant.downloads)
        .sort((a, b) => Number(b.name.endsWith('.ipa')) - Number(a.name.endsWith('.ipa')))
    : [];

  const copyDownloadUrl = () => {
    if (!hasDirectApk) return;
    navigator.clipboard
      .writeText(arm64Url)
      .then(() => {
        hapticSnap(8);
        setCopied(true);
        window.setTimeout(() => setCopied(false), 2000);
      })
      .catch(() => setCopied(false));
  };

  const countLabel =
    changelogEntries.length === 1
      ? '1 modifica'
      : `${changelogEntries.length} modifiche`;

  return (
    <section id="download" className="section-block">
      <div className="container-shell">
        <FluidReveal>
          <div className="section-header-center">
            <span className="type-eyebrow">Download</span>
            <h2 className="type-title-1">Porta UniApp sul tuo telefono.</h2>
            <p className="type-body">
              Gratuita, open source e indipendente. Android e iOS mantengono build e pacchetti separati.
            </p>
          </div>
        </FluidReveal>

        <FluidReveal delayMs={60}>
          <div className="download-sheet">
            {/* Unified 2-Column Platform Band (Android | iOS) */}
            <div className="download-platforms">
              {/* Android Column */}
              <div className="download-col download-col--android">
                <div className="download-col-head">
                  <span className="platform-chip">
                    <span className="status-dot" aria-hidden="true" />
                    <span>Android</span>
                  </span>
                  <span className="download-spec">ARM64-v8a · Android 10+</span>
                </div>

                <div className="download-version-line">
                  {hasAndroidVersion ? (
                    <span className="ver-text">{version}</span>
                  ) : (
                    <span className="ver-text ver-text--empty">GitHub Releases</span>
                  )}
                  {dateText ? (
                    <span className="download-submeta">{dateText}</span>
                  ) : (
                    <span id="android-release-hint" className="download-submeta">
                      {hasDirectApk
                        ? 'Pacchetto 64 bit firmato'
                        : 'Link diretto disponibile a pubblicazione avvenuta'}
                    </span>
                  )}
                </div>

                <div className="download-actions">
                  <a
                    href={hasDirectApk ? arm64Url : undefined}
                    aria-disabled={!hasDirectApk}
                    aria-describedby={!hasDirectApk && !dateText ? 'android-release-hint' : undefined}
                    download
                    className="btn-primary download-cta-btn"
                  >
                    <Download className="w-4 h-4 shrink-0" aria-hidden="true" />
                    <span>Scarica APK ARM64</span>
                  </a>

                  <button
                    type="button"
                    className="copy-icon-btn"
                    disabled={!hasDirectApk}
                    onClick={copyDownloadUrl}
                    title={copied ? 'Link copiato' : 'Copia link APK ARM64'}
                    aria-label={copied ? 'Link APK copiato negli appunti' : 'Copia link APK ARM64'}
                  >
                    {copied ? (
                      <Check className="w-4 h-4" style={{ color: 'var(--status-green)' }} aria-hidden="true" />
                    ) : (
                      <Copy className="w-4 h-4" aria-hidden="true" />
                    )}
                  </button>
                </div>
              </div>

              {/* iOS Column (only <article> inside #download) */}
              <article className="download-col" aria-label="Download iOS">
                <div className="download-col-head">
                  <span className="platform-chip">
                    <Smartphone className="w-4 h-4" style={{ color: 'var(--accent)' }} aria-hidden="true" />
                    <span>iOS</span>
                  </span>
                  <span className="download-spec">
                    {iosRelease ? `Versione ${iosRelease.version}` : 'KMP'}
                  </span>
                </div>

                <p className="download-ios-note">
                  {iosRelease
                    ? 'L’IPA richiede firma e provisioning; il bundle simulatore è separato.'
                    : 'Nessun pacchetto nel manifest locale. L’IPA richiede firma e provisioning; il bundle simulatore è separato.'}
                </p>

                <div className="download-actions">
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
                      <span>Apri GitHub Releases</span>
                      <ExternalLink className="w-3.5 h-3.5 opacity-60" aria-hidden="true" />
                    </a>
                  )}
                </div>
              </article>
            </div>

            {/* Integrated Compact Changelog Band */}
            <div id="changelog" className="download-changelog">
              <div className="download-changelog-head">
                <div className="download-changelog-title">
                  <h3>Note di rilascio</h3>
                  {changelogEntries.length > 0 && (
                    <span className="download-spec">
                      {version ? `${version} · ${countLabel}` : countLabel}
                    </span>
                  )}
                </div>
                <a
                  href="https://github.com/Anto426-Project/uniapp-upstream/releases"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-link"
                >
                  <span>Tutti i rilasci</span>
                  <ExternalLink className="w-3.5 h-3.5 opacity-60" aria-hidden="true" />
                </a>
              </div>

              <div
                className="changelog-scroll-region"
                tabIndex={0}
                aria-label="Elenco delle note di rilascio"
              >
                {changelogEntries.length > 0 ? (
                  <ul className="changelog-list">
                    {changelogEntries.map((entry, idx) => (
                      <li key={idx} className="changelog-row">
                        <span className={`change-dot ${entry.dotClass}`} aria-hidden="true" />
                        <span className="changelog-text">
                          {renderInlineMarkdown(entry.text)}
                        </span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="changelog-text">
                    Le note di rilascio compaiono qui a ogni pubblicazione su GitHub Pages.
                  </p>
                )}
              </div>
            </div>

            {/* Single Quiet Utility Footer Strip */}
            <div className="download-meta-bar">
              <div className="download-meta-group">
                <a
                  href={commitUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-link"
                >
                  <span>Commit #{commitHash || 'main'}</span>
                </a>
                <span className="download-sep" aria-hidden="true">·</span>
                <a
                  href={withBasePath('/update.json')}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-link"
                >
                  <span>update.json</span>
                </a>
                <span className="download-sep" aria-hidden="true">·</span>
                <a
                  href={withBasePath('/platforms.json')}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-link"
                >
                  <span>platforms.json</span>
                </a>
              </div>

              <div className="download-meta-group">
                <a
                  href={iosRelease?.url || 'https://github.com/Anto426-Project/uniapp-upstream/releases'}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-link"
                >
                  <span>Checksum</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
                <span className="download-sep" aria-hidden="true">·</span>
                <a
                  href="https://github.com/Anto426-Project/Uniapp"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-link"
                >
                  <span>Codice sorgente</span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </div>
            </div>
          </div>
        </FluidReveal>
      </div>
    </section>
  );
}
