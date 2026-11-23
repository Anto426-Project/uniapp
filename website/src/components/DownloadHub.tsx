'use client';

import React, { useState } from 'react';
import {
  Calendar,
  Check,
  Copy,
  Download,
  ExternalLink,
  FileCode,
  GitCommit,
  ShieldCheck,
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
  category: string;
  badgeClass: string;
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
    let category = 'Novità';
    let badgeClass = 'change-badge--feature';

    if (
      lower.includes('corretto') ||
      lower.includes('risolto') ||
      lower.includes('fix') ||
      lower.includes('bug')
    ) {
      category = 'Correzione';
      badgeClass = 'change-badge--fix';
    } else if (
      lower.includes('migliorat') ||
      lower.includes('ottimizzat') ||
      lower.includes('uniformat') ||
      lower.includes('velocizzat') ||
      lower.includes('snellit')
    ) {
      category = 'Miglioramento';
      badgeClass = 'change-badge--improvement';
    } else if (
      lower.includes('sicurezza') ||
      lower.includes('crittografia') ||
      lower.includes('privacy')
    ) {
      category = 'Sicurezza';
      badgeClass = 'change-badge--security';
    }

    entries.push({ category, badgeClass, text: content });
  }

  return entries;
}

function renderInlineMarkdown(text: string): React.ReactNode[] {
  const parts = text.split(/(\*\*[^*]+\*\*|`[^`]+`)/g);
  return parts.map((part, idx) => {
    if (part.startsWith('**') && part.endsWith('**') && part.length > 4) {
      return (
        <strong key={idx} style={{ fontWeight: 620, color: 'var(--label-primary)' }}>
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
      ? '1 modifica in questa versione'
      : `${changelogEntries.length} modifiche in questa versione`;

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

        {/* Balanced 1:1 Platform Cards (Android + iOS) */}
        <FluidReveal delayMs={60}>
          <div className="platform-cards-grid">
          {/* Android Card */}
          <div className="surface-card surface-card--featured">
            <div className="card-top-row">
              <span className="platform-chip">
                <span className="status-dot" aria-hidden="true" />
                <span>Android</span>
              </span>
              <span className="pill-badge">ARM64-v8a</span>
            </div>

            <div className="platform-version-block">
              {hasAndroidVersion ? (
                <div className="ver-text">{version}</div>
              ) : (
                <div className="ver-text ver-text--empty">Disponibile su GitHub Releases</div>
              )}

              <div className="meta-inline-row mt-2">
                {dateText ? (
                  <span className="meta-inline">
                    <Calendar className="w-3.5 h-3.5" aria-hidden="true" />
                    <span>Pubblicato il {dateText}</span>
                  </span>
                ) : (
                  <span id="android-release-hint" className="meta-inline">
                    <span>
                      {hasDirectApk
                        ? 'Android 10+ · Pacchetto 64 bit firmato'
                        : 'Il link diretto compare a pubblicazione avvenuta. Nel frattempo puoi scaricare il pacchetto da GitHub.'}
                    </span>
                  </span>
                )}
              </div>
            </div>

            <div className="download-action-row">
              <a
                href={hasDirectApk ? arm64Url : undefined}
                aria-disabled={!hasDirectApk}
                aria-describedby={!hasDirectApk && !dateText ? 'android-release-hint' : undefined}
                download
                className="apk-download-btn"
              >
                <span className="apk-icon-well">
                  <Download className="w-5 h-5" aria-hidden="true" />
                </span>
                <span className="apk-labels">
                  <span className="apk-title">Scarica APK ARM64-v8a</span>
                  <span className="apk-sub">Android 10+ · Architettura 64 bit</span>
                </span>
              </a>

              <button
                type="button"
                className="copy-link-btn"
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

            <div className="card-footer-links">
              <a
                href={commitUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-link"
              >
                <GitCommit className="w-3.5 h-3.5" style={{ color: 'var(--accent)' }} aria-hidden="true" />
                <span>
                  Commit <code className="mono-code">#{commitHash || 'main'}</code>
                </span>
              </a>

              <a
                href={withBasePath('/update.json')}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-link"
              >
                <FileCode className="w-3.5 h-3.5" style={{ color: 'var(--accent)' }} aria-hidden="true" />
                <code className="mono-code">update.json</code>
                <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
              </a>
            </div>
          </div>

          {/* iOS Card (only <article> inside #download) */}
          <article className="surface-card" aria-label="Download iOS">
            <div className="card-top-row">
              <span className="platform-chip">
                <Smartphone className="w-4 h-4" style={{ color: 'var(--accent)' }} aria-hidden="true" />
                <span>iOS</span>
              </span>
              <span className="pill-badge">{iosRelease ? `v${iosRelease.version}` : 'KMP'}</span>
            </div>

            <div className="platform-version-block">
              <div className={iosRelease ? 'ver-text' : 'ver-text ver-text--empty'}>
                {iosRelease ? `v${iosRelease.version}` : 'Disponibile su GitHub Releases'}
              </div>
              <p className="type-callout mt-2">
                {iosRelease
                  ? `Versione ${iosRelease.version}.`
                  : 'Nessun pacchetto nel manifest locale.'}
                {' L’IPA richiede firma e provisioning; il bundle simulatore è separato.'}
              </p>
            </div>

            {iosRelease && (
              <div className="ios-variants">
                {Object.entries(iosRelease.variants).map(([variantKey, variantData]) => (
                  <div key={variantKey} className="ios-variant-box">
                    <ul className="ios-pill-list">
                      {variantData.downloads.map((file) => (
                        <li key={file.name}>
                          <a className="ios-pill-link" href={file.url}>
                            <Download className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                            <span>{file.name.endsWith('.ipa') ? 'IPA iOS' : 'Bundle simulatore'}</span>
                          </a>
                        </li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>
            )}

            <div className="card-footer-links">
              <a
                href={iosRelease?.url || 'https://github.com/Anto426-Project/uniapp-upstream/releases'}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-link"
              >
                <span>Note e checksum su GitHub</span>
                <ExternalLink className="w-3.5 h-3.5 opacity-60" aria-hidden="true" />
              </a>

              <a
                href={withBasePath('/platforms.json')}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-link"
              >
                <FileCode className="w-3.5 h-3.5" style={{ color: 'var(--accent)' }} aria-hidden="true" />
                <code className="mono-code">platforms.json</code>
              </a>
            </div>
          </article>
        </div>
        </FluidReveal>

        {/* Full-Width Bounded Release Notes Card with Internal Scroll */}
        <FluidReveal delayMs={110}>
        <div id="changelog" className="surface-card changelog-panel-bounded">
          <div className="card-top-row">
            <div>
              <div className="flex items-center gap-2.5 flex-wrap">
                <h3 className="type-headline">Note di rilascio</h3>
                {version && <span className="pill-badge">{version}</span>}
              </div>
              <p className="type-caption mt-1">
                {changelogEntries.length > 0
                  ? countLabel
                  : 'Nessuna nota inclusa nel manifest corrente'}
              </p>
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
                    <span className={`change-badge ${entry.badgeClass}`}>{entry.category}</span>
                    <span className="type-callout" style={{ color: 'var(--label-secondary)' }}>
                      {renderInlineMarkdown(entry.text)}
                    </span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="type-callout">
                Le note di rilascio compaiono qui a ogni pubblicazione su GitHub Pages. Puoi consultare lo storico completo su GitHub.
              </p>
            )}
          </div>

          <div className="card-footer-links">
            <span className="inline-link" style={{ cursor: 'default' }}>
              <ShieldCheck className="w-4 h-4" style={{ color: 'var(--status-green)' }} aria-hidden="true" />
              <span>Rilasci verificati su GitHub</span>
            </span>
            <a
              href="https://github.com/Anto426-Project/Uniapp"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-link"
            >
              <span>Codice su GitHub</span>
              <ExternalLink className="w-3.5 h-3.5 opacity-60" aria-hidden="true" />
            </a>
          </div>
        </div>
        </FluidReveal>
      </div>
    </section>
  );
}
