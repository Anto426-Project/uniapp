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
  Tag,
  Calendar,
  GitCommit,
  FileCode,
} from 'lucide-react';
import { UpdateManifest, ReleaseChannelData } from '@/data/types';
import { withBasePath } from '@/utils/basePath';
import GlassSurface from './GlassSurface';

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
        badgeClass: 'bg-white/10 text-slate-300 border-white/10',
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
    let badgeClass = 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';

    if (
      lower.includes('corretto') ||
      lower.includes('risolto') ||
      lower.includes('fix') ||
      lower.includes('bug')
    ) {
      category = 'Bug Fix';
      badgeClass = 'bg-rose-500/15 text-rose-400 border-rose-500/30';
    } else if (
      lower.includes('migliorat') ||
      lower.includes('ottimizzat') ||
      lower.includes('uniformat') ||
      lower.includes('velocizzat')
    ) {
      category = 'Miglioramento';
      badgeClass = 'bg-violet-500/15 text-violet-300 border-violet-500/30';
    } else if (
      lower.includes('sicurezza') ||
      lower.includes('crittografia') ||
      lower.includes('privacy')
    ) {
      category = 'Sicurezza';
      badgeClass = 'bg-sky-500/15 text-sky-400 border-sky-500/30';
    }

    items.push({ category, badgeClass, text: content });
  }

  return items.length
    ? items
    : [
        {
          category: 'Info',
          badgeClass: 'bg-white/10 text-slate-300 border-white/10',
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
            Scarica la build nativa compilata per l&apos;architettura del tuo processore. Supporto completo ad aggiornamenti continui e notifiche in-app.
          </p>
        </div>

        {/* Scheda Unificata Download Hub */}
        <GlassSurface
          width="100%"
          height="auto"
          borderRadius={32}
          className="download-unified-card as-block"
          distortionScale={-140}
          backgroundOpacity={0.14}
          saturation={1.35}
          style={{ marginBottom: '40px' }}
        >
          {/* Toolbar Canale & Versione */}
          <div className="download-card-toolbar">
            <div className="channel-switch-wrapper-inline">
              <span className="toolbar-label">Canale di Rilascio:</span>
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
                  Canale Beta
                </button>
                <button
                  type="button"
                  className={`channel-tab ${channel === 'stable' ? 'active' : ''}`}
                  onClick={() => setChannel('stable')}
                >
                  Canale Stabile
                </button>
              </div>
            </div>

            <div className="download-card-meta">
              <span className="meta-pill version">
                <Tag className="w-3.5 h-3.5" />
                <span>{version}</span>
              </span>
              <span className="meta-pill date">
                <Calendar className="w-3.5 h-3.5" />
                <span>{dateText}</span>
              </span>
            </div>
          </div>

          {/* Elenco dei Pacchetti APK */}
          <div className="download-rows-list">
            {/* 1. ARM64-v8a (Consigliato) */}
            <div className="abi-download-row recommended">
              <div className="abi-row-left">
                <div className="abi-row-icon">
                  <Cpu className="w-6 h-6" />
                </div>
                <div className="abi-row-info">
                  <div className="abi-row-title-line">
                    <span className="abi-name">ARM64-v8a</span>
                    <span className="abi-badge-rec">
                      <Sparkles className="w-3 h-3 inline mr-1" />
                      Consigliato
                    </span>
                    <span className="abi-target-pill">64-bit • Android 8.0+</span>
                  </div>
                  <p className="abi-desc-text">
                    Compilazione nativa a 64-bit ottimizzata per oltre il 95% degli smartphone Android moderni. Massima fluidità a 120Hz e consumi ridotti.
                  </p>
                </div>
              </div>

              <div className="abi-row-actions">
                <a
                  href={arm64Url}
                  download
                  className="btn-download-action-compact primary"
                >
                  <Download className="w-4 h-4" />
                  <span>Scarica APK</span>
                </a>
                <button
                  type="button"
                  onClick={() => copyToClipboard(arm64Url, 'arm64')}
                  className="btn-copy-url-compact"
                  title="Copia link diretto di download"
                >
                  {copiedId === 'arm64' ? (
                    <Check className="w-4 h-4 text-emerald-400" />
                  ) : (
                    <Copy className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>

            {/* 2. Universale */}
            <div className="abi-download-row">
              <div className="abi-row-left">
                <div className="abi-row-icon">
                  <Layers className="w-6 h-6" />
                </div>
                <div className="abi-row-info">
                  <div className="abi-row-title-line">
                    <span className="abi-name">Universale</span>
                    <span className="abi-target-pill">Tutte le CPU Android</span>
                  </div>
                  <p className="abi-desc-text">
                    Include le librerie native per tutte le architetture (ARM32, ARM64, x86). Funziona su qualsiasi dispositivo ed emulatore.
                  </p>
                </div>
              </div>

              <div className="abi-row-actions">
                <a
                  href={universalUrl}
                  download
                  className="btn-download-action-compact secondary"
                >
                  <Download className="w-4 h-4" />
                  <span>Scarica APK</span>
                </a>
                <button
                  type="button"
                  onClick={() => copyToClipboard(universalUrl, 'universal')}
                  className="btn-copy-url-compact"
                  title="Copia link diretto di download"
                >
                  {copiedId === 'universal' ? (
                    <Check className="w-4 h-4 text-emerald-400" />
                  ) : (
                    <Copy className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>

            {/* 3. ARMeabi-v7a */}
            <div className="abi-download-row">
              <div className="abi-row-left">
                <div className="abi-row-icon">
                  <Smartphone className="w-6 h-6" />
                </div>
                <div className="abi-row-info">
                  <div className="abi-row-title-line">
                    <span className="abi-name">ARMeabi-v7a</span>
                    <span className="abi-target-pill">Legacy 32-bit</span>
                  </div>
                  <p className="abi-desc-text">
                    Pacchetto leggero per dispositivi Android più datati dotati di architettura processore a 32-bit.
                  </p>
                </div>
              </div>

              <div className="abi-row-actions">
                <a
                  href={armv7Url}
                  download
                  className="btn-download-action-compact secondary"
                >
                  <Download className="w-4 h-4" />
                  <span>Scarica APK</span>
                </a>
                <button
                  type="button"
                  onClick={() => copyToClipboard(armv7Url, 'armeabi-v7a')}
                  className="btn-copy-url-compact"
                  title="Copia link diretto di download"
                >
                  {copiedId === 'armeabi-v7a' ? (
                    <Check className="w-4 h-4 text-emerald-400" />
                  ) : (
                    <Copy className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>
          </div>
        </GlassSurface>

        {/* Scheda Note di Rilascio / Changelog */}
        <GlassSurface
          id="changelog"
          width="100%"
          height="auto"
          borderRadius={32}
          className="changelog-card as-block"
          distortionScale={-140}
          backgroundOpacity={0.14}
          saturation={1.35}
          style={{ marginTop: '40px' }}
        >
          <div className="changelog-header-row">
            <div className="cl-title-wrap">
              <div className="abi-row-icon" style={{ width: 38, height: 38 }}>
                <Sparkles className="w-4 h-4" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-white leading-tight">
                  Note di Rilascio
                </h3>
                <div className="flex items-center gap-2 mt-1">
                  <span className="cl-version-badge">{version}</span>
                  <span className="cl-channel-badge">
                    Canale {channel === 'beta' ? 'Beta' : 'Stabile'}
                  </span>
                </div>
              </div>
            </div>

            <div className="cl-date-meta">
              <span>Pubblicato il {dateText}</span>
            </div>
          </div>

          <ul className="changelog-items-list">
            {changelogItems.map((item, idx) => (
              <li key={idx} className="changelog-item">
                <span
                  className={`text-[0.68rem] font-bold px-2.5 py-0.5 rounded-full border flex-shrink-0 uppercase tracking-wider mt-0.5 ${item.badgeClass}`}
                >
                  {item.category}
                </span>
                <span className="text-slate-200 leading-relaxed text-sm md:text-[0.95rem]">
                  {item.text}
                </span>
              </li>
            ))}
          </ul>

          <div className="changelog-footer-row">
            <a
              href={commitUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="commit-pill"
            >
              <GitCommit className="w-4 h-4 text-[var(--theme-primary)]" />
              <span>Commit di compilazione:</span>
              <span id="cl-commit-link">#{commitHash}</span>
            </a>

            <a
              href={withBasePath('/update.json')}
              target="_blank"
              rel="noopener noreferrer"
              className="commit-pill hover:underline"
            >
              <FileCode className="w-4 h-4 text-[var(--theme-primary)]" />
              <span>Manifest update.json</span>
              <ExternalLink className="w-3.5 h-3.5" />
            </a>
          </div>
        </GlassSurface>
      </div>
    </section>
  );
};
