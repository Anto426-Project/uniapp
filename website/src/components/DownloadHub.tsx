'use client';

import React, { useState } from 'react';
import {
  Download,
  Cpu,
  Layers,
  Smartphone,
  Sparkles,
  Copy,
  Check,
  ExternalLink,
  Calendar,
  GitCommit,
  FileCode,
  ShieldCheck,
} from 'lucide-react';
import { UpdateManifest, ReleaseChannelData } from '@/data/types';
import { withBasePath } from '@/utils/basePath';

interface DownloadHubProps {
  manifest: UpdateManifest;
  theme?: string;
}

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
        text: 'Nessuna nota di rilascio specificata per questo canale.',
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

export const DownloadHub: React.FC<DownloadHubProps> = ({ manifest }) => {
  const [channel, setChannel] = useState<'beta' | 'stable'>('beta');
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const releaseData: ReleaseChannelData | undefined =
    manifest.channels?.[channel]?.release;

  const version = releaseData?.latestVersion
    ? releaseData.latestVersion.startsWith('v')
      ? releaseData.latestVersion
      : `v${releaseData.latestVersion}`
    : 'Release';

  const dateText = formatDate(releaseData?.publishedAt);
  const changelogItems = parseChangelog(releaseData?.notes);

  const copyToClipboard = (url: string, id: string) => {
    if (!url || url === '#') return;
    navigator.clipboard.writeText(url);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const arm64Url =
    releaseData?.downloadUrlsByAbi?.['arm64-v8a'] ||
    releaseData?.downloadUrl ||
    '#';

  const universalUrl =
    releaseData?.downloadUrlsByAbi?.['universal'] ||
    releaseData?.downloadUrl ||
    '#';

  const armv7Url =
    releaseData?.downloadUrlsByAbi?.['armeabi-v7a'] ||
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
          <span className="section-tag">Distribuzione Ufficiale</span>
          <h2 className="section-title">Pacchetti APK &amp; Release</h2>
          <p className="section-description max-w-2xl mx-auto">
            Scarica la build nativa ottimizzata per il tuo dispositivo. Supporto ad aggiornamenti continui e notifiche in-app.
          </p>
        </div>

        {/* 2-Column Release Deck */}
        <div className="release-deck-grid">
          {/* Left Column: Download Console */}
          <div className="release-console-card">
            {/* Console Header / Switcher */}
            <div className="console-header-bar">
              <div className="channel-switch glass-pill">
                <div
                  className="switch-pill"
                  style={{
                    transform: channel === 'beta' ? 'translateX(0)' : 'translateX(100%)',
                  }}
                />
                <button
                  type="button"
                  className={`channel-tab ${channel === 'beta' ? 'active' : ''}`}
                  onClick={() => setChannel('beta')}
                >
                  Beta
                </button>
                <button
                  type="button"
                  className={`channel-tab ${channel === 'stable' ? 'active' : ''}`}
                  onClick={() => setChannel('stable')}
                >
                  Stabile
                </button>
              </div>

              <div className="console-status-indicator">
                <span className="status-live-dot" />
                <span className="status-live-text">
                  Canale {channel === 'beta' ? 'Beta' : 'Stabile'} attivo
                </span>
              </div>
            </div>

            {/* Version & Highlights */}
            <div className="console-version-row">
              <div className="console-version-tag">
                <span className="ver-text">{version}</span>
                <span className="ver-subbadge">Build Ufficiale</span>
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
                  href={arm64Url}
                  download
                  className="btn-hero-download"
                >
                  <div className="btn-hero-icon-box">
                    <Download className="w-5 h-5" />
                  </div>
                  <div className="btn-hero-labels">
                    <span className="btn-hero-title">Scarica APK ARM64-v8a</span>
                    <span className="btn-hero-subtitle">Consigliato • 64-bit • Android 8.0+</span>
                  </div>
                </a>
                <button
                  type="button"
                  onClick={() => copyToClipboard(arm64Url, 'arm64')}
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

            {/* Alternative Architectures (Compact, No Card-in-Card) */}
            <div className="alt-arch-section">
              <span className="alt-arch-label">Architetture alternative</span>

              <div className="alt-arch-list">
                {/* Universale */}
                <div className="alt-arch-row">
                  <div className="alt-arch-info">
                    <Layers className="w-4 h-4 alt-arch-icon" />
                    <div>
                      <span className="alt-arch-name">Universale</span>
                      <span className="alt-arch-note">Tutte le CPU • Emulatori</span>
                    </div>
                  </div>
                  <div className="alt-arch-actions">
                    <a
                      href={universalUrl}
                      download
                      className="btn-alt-download"
                      title="Scarica APK Universale"
                    >
                      <Download className="w-3.5 h-3.5" />
                      <span>APK</span>
                    </a>
                    <button
                      type="button"
                      onClick={() => copyToClipboard(universalUrl, 'universal')}
                      className="btn-alt-copy"
                      title="Copia link Universale"
                    >
                      {copiedId === 'universal' ? (
                        <Check className="w-3.5 h-3.5 text-emerald-400" />
                      ) : (
                        <Copy className="w-3.5 h-3.5" />
                      )}
                    </button>
                  </div>
                </div>

                {/* ARMeabi-v7a */}
                <div className="alt-arch-row">
                  <div className="alt-arch-info">
                    <Smartphone className="w-4 h-4 alt-arch-icon" />
                    <div>
                      <span className="alt-arch-name">ARMeabi-v7a</span>
                      <span className="alt-arch-note">Legacy 32-bit</span>
                    </div>
                  </div>
                  <div className="alt-arch-actions">
                    <a
                      href={armv7Url}
                      download
                      className="btn-alt-download"
                      title="Scarica APK ARMeabi-v7a"
                    >
                      <Download className="w-3.5 h-3.5" />
                      <span>APK</span>
                    </a>
                    <button
                      type="button"
                      onClick={() => copyToClipboard(armv7Url, 'armeabi-v7a')}
                      className="btn-alt-copy"
                      title="Copia link ARMeabi-v7a"
                    >
                      {copiedId === 'armeabi-v7a' ? (
                        <Check className="w-3.5 h-3.5 text-emerald-400" />
                      ) : (
                        <Copy className="w-3.5 h-3.5" />
                      )}
                    </button>
                  </div>
                </div>
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
                Canale {channel === 'beta' ? 'Beta' : 'Stabile'}
              </span>
            </div>

            {/* Scrollable Items Feed */}
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
                <span>Build GitHub Actions Verificata</span>
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
