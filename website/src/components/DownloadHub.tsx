'use client';

import React, { useState } from 'react';
import {
  Download,
  Copy,
  Check,
  ExternalLink,
  Calendar,
  GitCommit,
  FileCode,
  ShieldCheck,
  Smartphone,
} from 'lucide-react';
import { useRelease, usePlatforms } from './SiteShell';
import { withBasePath } from '@/utils/basePath';

function formatDate(dateStr?: string) {
  if (!dateStr) return 'Non specificata';
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString('it-IT', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
}

interface ChangelogItem {
  category: string;
  badgeClass: string;
  text: string;
}

function parseChangelog(notes?: string): ChangelogItem[] {
  if (!notes || !notes.trim()) {
    return [
      {
        category: 'Info',
        badgeClass: 'badge-info',
        text: 'Nessuna nota di rilascio specificata per questa versione.',
      },
    ];
  }

  const lines = notes.split('\n');
  const items: ChangelogItem[] = [];

  for (let line of lines) {
    line = line.trim();
    if (!line) continue;
    if (line.toLowerCase().startsWith('changelog')) continue;

    let content = line;
    if (line.startsWith('-') || line.startsWith('*')) {
      content = line.substring(1).trim();
    }

    const lower = content.toLowerCase();
    let category = 'Novità';
    let badgeClass = 'badge-feature';

    if (
      lower.includes('corretto') ||
      lower.includes('risolto') ||
      lower.includes('fix') ||
      lower.includes('bug')
    ) {
      category = 'Bug Fix';
      badgeClass = 'badge-fix';
    } else if (
      lower.includes('migliorat') ||
      lower.includes('ottimizzat') ||
      lower.includes('uniformat') ||
      lower.includes('velocizzat') ||
      lower.includes('snellit')
    ) {
      category = 'Miglioramento';
      badgeClass = 'badge-improvement';
    } else if (
      lower.includes('sicurezza') ||
      lower.includes('crittografia') ||
      lower.includes('privacy')
    ) {
      category = 'Sicurezza';
      badgeClass = 'badge-security';
    }

    items.push({ category, badgeClass, text: content });
  }

  return items.length
    ? items
    : [
        {
          category: 'Info',
          badgeClass: 'badge-info',
          text: 'Nessun dettaglio disponibile.',
        },
      ];
}

function renderInlineMarkdown(text: string): React.ReactNode[] {
  const tokenRegex = /(\*\*[^*]+\*\*|`[^`]+`)/g;
  const parts = text.split(tokenRegex);
  return parts.map((part, idx) => {
    if (part.startsWith('**') && part.endsWith('**') && part.length > 4) {
      return (
        <strong key={idx} className="font-semibold text-[var(--text-primary)]">
          {part.slice(2, -2)}
        </strong>
      );
    }
    if (part.startsWith('`') && part.endsWith('`') && part.length > 2) {
      return (
        <code key={idx} className="console-code">
          {part.slice(1, -1)}
        </code>
      );
    }
    return <React.Fragment key={idx}>{part}</React.Fragment>;
  });
}

export const DownloadHub = () => {
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const releaseData = useRelease();
  const { platforms } = usePlatforms();
  const iosRelease = platforms.ios;

  const version = releaseData?.latestVersion
    ? releaseData.latestVersion.startsWith('v')
      ? releaseData.latestVersion
      : `v${releaseData.latestVersion}`
    : 'Versione non disponibile';

  const dateText = formatDate(releaseData?.publishedAt);
  const changelogItems = parseChangelog(releaseData?.notes);

  const copyToClipboard = (url: string, id: string) => {
    if (!url || url === '#') return;
    navigator.clipboard
      .writeText(url)
      .then(() => {
        setCopiedId(id);
        setTimeout(() => setCopiedId(null), 2000);
      })
      .catch(() => setCopiedId(null));
  };

  const arm64Url =
    releaseData?.downloadUrlsByAbi?.['arm64-v8a'] ||
    releaseData?.downloadUrl ||
    '#';

  const commitHash = releaseData?.buildCommit
    ? releaseData.buildCommit.substring(0, 7)
    : 'latest';
  const commitUrl = releaseData?.buildCommit
    ? `https://github.com/Anto426-Project/Uniapp/commit/${releaseData.buildCommit}`
    : 'https://github.com/Anto426-Project/Uniapp/commits/main';

  return (
    <section id="download">
      <div className="container">
        <div className="split-section-header">
          <div className="split-header-copy">
            <span className="eyebrow">Download</span>
            <h2 className="display-h2">Scarica UniApp.</h2>
            <p className="lead-copy">
              Gratuito e indipendente. Android e iOS mantengono versioni e pacchetti separati.
            </p>
          </div>
          <a
            href="https://github.com/Anto426-Project/uniapp-upstream/releases"
            target="_blank"
            rel="noopener noreferrer"
            className="btn-apple-glass"
          >
            <span>Tutti i rilasci su GitHub</span>
            <ExternalLink className="w-4 h-4 opacity-70" aria-hidden="true" />
          </a>
        </div>

        <div className="download-studio-grid">
          {/* Left Column: Android + iOS stacked */}
          <div className="download-platforms-col">
            {/* Android Primary Card */}
            <div className="studio-card studio-card--android">
              <div className="studio-card-top">
                <span className="platform-label">
                  <span className="status-live-dot" aria-hidden="true" />
                  <span>Android</span>
                </span>
                <span className="badge-pill">ARM64-v8a</span>
              </div>

              <div className="version-block">
                <div className="ver-text">{version}</div>
                <div className="pub-date">
                  <Calendar className="w-3.5 h-3.5" aria-hidden="true" />
                  <span>Rilasciato il {dateText}</span>
                </div>
              </div>

              <div className="download-cta-row">
                <a
                  href={arm64Url === '#' ? undefined : arm64Url}
                  aria-disabled={arm64Url === '#'}
                  download
                  className="btn-hero-download"
                >
                  <div className="btn-hero-icon-box">
                    <Download className="w-5 h-5" aria-hidden="true" />
                  </div>
                  <div className="btn-hero-labels">
                    <span className="btn-hero-title">Scarica APK ARM64-v8a</span>
                    <span className="btn-hero-subtitle">Android 10+ · 64 bit</span>
                  </div>
                </a>
                <button
                  type="button"
                  onClick={() => copyToClipboard(arm64Url, 'arm64')}
                  disabled={arm64Url === '#'}
                  className="btn-hero-copy"
                  title="Copia link download ARM64"
                  aria-label="Copia link download ARM64"
                >
                  {copiedId === 'arm64' ? (
                    <Check className="w-4 h-4 text-emerald-400" aria-hidden="true" />
                  ) : (
                    <Copy className="w-4 h-4" aria-hidden="true" />
                  )}
                </button>
              </div>

              <div className="studio-card-footer">
                <a
                  href={commitUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="console-link"
                >
                  <GitCommit className="w-3.5 h-3.5 text-[var(--theme-primary)]" aria-hidden="true" />
                  <span>
                    Commit <code className="console-code">#{commitHash}</code>
                  </span>
                </a>

                <a
                  href={withBasePath('/update.json')}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="console-link"
                >
                  <FileCode className="w-3.5 h-3.5 text-[var(--theme-primary)]" aria-hidden="true" />
                  <span>
                    <code className="console-code">update.json</code>
                  </span>
                  <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                </a>
              </div>
            </div>

            {/* iOS Card (only <article> inside #download) */}
            <article className="studio-card" aria-label="Download iOS">
              <div>
                <div className="studio-card-top">
                  <span className="platform-label">
                    <Smartphone className="w-4 h-4 text-[var(--theme-primary)]" aria-hidden="true" />
                    <span>iOS</span>
                  </span>
                  <span className="badge-pill">
                    {iosRelease ? `v${iosRelease.version}` : 'KMP'}
                  </span>
                </div>

                <p className="ios-note">
                  {iosRelease ? `Versione ${iosRelease.version}.` : 'Nessun download disponibile.'}
                  {' L’IPA richiede firma e provisioning; il bundle simulatore è separato.'}
                </p>

                {iosRelease && (
                  <div className="ios-variants-list">
                    {Object.entries(iosRelease.variants).map(([variant, data]) => (
                      <div key={variant} className="ios-variant-row">
                        <span className="ios-variant-label">iOS · {data.version}</span>
                        <ul className="ios-downloads-pills">
                          {data.downloads.map((file) => (
                            <li key={file.name}>
                              <a className="ios-download-pill" href={file.url}>
                                <Download className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                                <span>
                                  {file.name.endsWith('.ipa') ? 'IPA iOS' : 'Bundle simulatore'}
                                </span>
                              </a>
                            </li>
                          ))}
                        </ul>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="studio-card-footer">
                <a
                  className="console-link"
                  href={iosRelease?.url || 'https://github.com/Anto426-Project/uniapp-upstream/releases'}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <span>Note e checksum su GitHub</span>
                  <ExternalLink className="w-3.5 h-3.5" aria-hidden="true" />
                </a>
              </div>
            </article>
          </div>

          {/* Right Column: Full-Height Editorial Release Notes */}
          <div id="changelog" className="changelog-panel">
            <div className="studio-card-top">
              <div>
                <h3 className="spec-cell-title">Note di Rilascio</h3>
                <p className="pub-date" style={{ marginTop: 4 }}>
                  Novità ed ottimizzazioni introdotte in {version}
                </p>
              </div>
              <span className="badge-pill">Changelog</span>
            </div>

            <ul className="changelog-list">
              {changelogItems.map((item, idx) => (
                <li key={idx} className="changelog-item">
                  <span className={`deck-badge ${item.badgeClass}`}>{item.category}</span>
                  <span className="changelog-text">{renderInlineMarkdown(item.text)}</span>
                </li>
              ))}
            </ul>

            <div className="studio-card-footer">
              <span className="console-link" style={{ cursor: 'default' }}>
                <ShieldCheck className="w-4 h-4 text-emerald-400" aria-hidden="true" />
                <span>Rilasci verificati su GitHub</span>
              </span>
              <a
                href="https://github.com/Anto426-Project/Uniapp"
                target="_blank"
                rel="noopener noreferrer"
                className="console-link"
              >
                <span>Codice Open Source</span>
                <ExternalLink className="w-3.5 h-3.5 opacity-60" aria-hidden="true" />
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
