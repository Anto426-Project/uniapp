'use client';

import React, { useState } from 'react';
import {
  Download,
  Sparkles,
  Copy,
  Check,
  ExternalLink,
  Calendar,
  GitCommit,
  FileCode,
  ShieldCheck,
} from 'lucide-react';
import { useRelease } from './SiteShell';
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
      lower.includes('velocizzat')
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

export const DownloadHub = () => {
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const releaseData = useRelease();

  const version = releaseData?.latestVersion
    ? releaseData.latestVersion.startsWith('v')
      ? releaseData.latestVersion
      : `v${releaseData.latestVersion}`
    : 'Versione non disponibile';

  const dateText = formatDate(releaseData?.publishedAt);
  const changelogItems = parseChangelog(releaseData?.notes);

  const copyToClipboard = (url: string, id: string) => {
    if (!url || url === '#') return;
    navigator.clipboard.writeText(url).then(() => {
      setCopiedId(id);
      setTimeout(() => setCopiedId(null), 2000);
    }).catch(() => setCopiedId(null));
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
    <section id="download" className="py-20 md:py-28 relative">
      <div className="container">
        {/* Intestazione Sezione */}
        <div className="section-header text-center mb-12">
          <span className="section-tag">Download UniApp</span>
          <h2 className="section-title">APK Android e note di rilascio</h2>
          <p className="section-description max-w-2xl mx-auto">
            Le nuove versioni richiedono Android 10 o successivo e un sistema a 64 bit. Sono supportati solo dispositivi ARM64; ARMv7, x86 e x86_64 sono esclusi.
          </p>
        </div>

        {/* 2-Column Release Deck */}
        <div className="release-deck-grid">
          {/* Left Column: Download Console */}
          <div className="release-console-card">
            {/* Console Header / Switcher */}
            <div className="console-header-bar">
              <span>Ultima versione pubblicata</span>
            </div>

            {/* Version & Highlights */}
            <div className="console-version-row">
              <div className="console-version-tag">
                <span className="ver-text">{version}</span>
              </div>
              <div className="console-pub-date">
                <Calendar className="w-3.5 h-3.5" />
                <span>Rilasciato il {dateText}</span>
              </div>
            </div>

            {/* Hero Download Action (ARM64-v8a) */}
            <div className="hero-download-block">
              <div className="hero-download-action-group">
                <a
                  href={arm64Url === '#' ? undefined : arm64Url}
                  aria-disabled={arm64Url === '#'}
                  download
                  className="btn-hero-download"
                >
                  <div className="btn-hero-icon-box">
                    <Download className="w-5 h-5" />
                  </div>
                  <div className="btn-hero-labels">
                    <span className="btn-hero-title">Scarica APK ARM64-v8a</span>
                    <span className="btn-hero-subtitle">Android 10+ • Sistema a 64 bit</span>
                  </div>
                </a>
                <button
                  type="button"
                  onClick={() => copyToClipboard(arm64Url, 'arm64')}
                  disabled={arm64Url === '#'}
                  className="btn-hero-copy"
                  title="Copia link download ARM64"
                >
                  {copiedId === 'arm64' ? (
                    <Check className="w-4 h-4 text-emerald-400" />
                  ) : (
                    <Copy className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>

            {/* Console Footer */}
            <div className="console-footer-bar">
              <a
                href={commitUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="console-link"
              >
                <GitCommit className="w-3.5 h-3.5 text-[var(--theme-primary)]" />
                <span>Commit <code className="console-code">#{commitHash}</code></span>
              </a>

              <a
                href={withBasePath('/update.json')}
                target="_blank"
                rel="noopener noreferrer"
                className="console-link"
              >
                <FileCode className="w-3.5 h-3.5 text-[var(--theme-primary)]" />
                <span>Manifest <code className="console-code">update.json</code></span>
                <ExternalLink className="w-3 h-3 ml-0.5 opacity-60" />
              </a>
            </div>
          </div>

          {/* Right Column: Release Notes / Changelog */}
          <div className="release-changelog-card">
            {/* Changelog Header */}
            <div className="changelog-deck-header">
              <div className="changelog-deck-title-wrap">
                <div className="changelog-deck-icon">
                  <Sparkles className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="changelog-deck-title">Note di Rilascio</h3>
                  <p className="changelog-deck-subtitle">
                    Novità ed ottimizzazioni introdotte in {version}
                  </p>
                </div>
              </div>
              <span className="cl-deck-badge">
                Android 64 bit
              </span>
            </div>

            {/* Scrollable Items Feed (Scrollbar hidden) */}
            <div className="release-changelog-scroll">
              <ul className="deck-changelog-list">
                {changelogItems.map((item, idx) => (
                  <li key={idx} className="deck-changelog-item">
                    <span className={`deck-badge ${item.badgeClass}`}>
                      {item.category}
                    </span>
                    <span className="deck-item-text">
                      {item.text}
                    </span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Changelog Footer */}
            <div className="changelog-deck-footer">
              <div className="verified-build-pill">
                <ShieldCheck className="w-4 h-4 text-emerald-400" />
                <span>Rilasci pubblicati su GitHub</span>
              </div>
              <a
                href="https://github.com/Anto426-Project/Uniapp"
                target="_blank"
                rel="noopener noreferrer"
                className="console-link"
              >
                <span>Vedi sorgente</span>
                <ExternalLink className="w-3 h-3 ml-0.5 opacity-60" />
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
