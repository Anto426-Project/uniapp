'use client';

import React, { useRef, useState } from 'react';
import { motion, useScroll, useSpring, useTransform } from 'motion/react';
import { Check, Copy, Download, ExternalLink, FileText, ShieldCheck, Smartphone, Zap } from 'lucide-react';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { FluidReveal } from './FluidReveal';
import Magnet from './Magnet';

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
        <strong key={idx} className="changelog-strong">
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

export function DownloadHub() {
  const { reducedMotion, release, platforms } = useSite();
  const [copied, setCopied] = useState(false);
  const lockupRef = useRef<HTMLDivElement>(null);

  const { scrollYProgress } = useScroll({
    target: lockupRef,
    offset: ['start end', 'center center'],
  });
  const smoothScroll = useSpring(scrollYProgress, { stiffness: 150, damping: 26, mass: 0.4 });
  const squircleScale = useTransform(smoothScroll, [0, 1], [0.88, 1]);
  const squircleY = useTransform(smoothScroll, [0, 1], [16, 0]);

  const iosRelease = platforms.platforms.ios;

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
      <div className="container-shell relative z-10">
        <FluidReveal materialize>
          <div ref={lockupRef} className="download-lockup">
            <motion.div
              style={reducedMotion ? undefined : { scale: squircleScale, y: squircleY }}
            >
              <Magnet padding={36} magnetStrength={6} maxOffset={5} disabled={reducedMotion}>
                <div className="download-squircle">
                  <img
                    src={withBasePath('/assets/uniapp-icon.webp')}
                    alt="Icona UniApp"
                    width={76}
                    height={76}
                  />
                </div>
              </Magnet>
            </motion.div>
            <h2 className="type-title-1">Scarica UniApp.</h2>
            <p className="type-body download-lockup-sub">
              Gratuita, open source e verificabile su Android e iOS.
            </p>
          </div>
        </FluidReveal>

        {/* Symmetrical 2-Column Platform Cards */}
        <div className="download-platform-grid">
          {/* Android Card (div — preserving <article> strictly for iOS) */}
          <FluidReveal delayMs={40} materialize className="flex">
            <div className="dl-card w-full">
              <div className="dl-card-head">
                <div className="dl-brand">
                  <span className="dl-os-icon dl-os-icon--accent" aria-hidden="true">
                    <Smartphone className="w-4 h-4" />
                  </span>
                  <span className="dl-os-name">Android</span>
                </div>
                <span className="ver-text">
                  {hasAndroidVersion ? version : 'ARM64 · 64 bit'}
                </span>
              </div>

              <p id="android-release-hint" className="dl-card-desc">
                Android 10 o successivo · Pacchetto APK firmato a 64 bit pronto da installare, con aggiornamenti rapidi integrati.
              </p>

              <div className="dl-card-actions">
                <Magnet
                  padding={34}
                  magnetStrength={6}
                  maxOffset={5}
                  disabled={reducedMotion}
                  wrapperClassName="dl-primary-magnet"
                  innerClassName="w-full"
                >
                  <a
                    href={androidHref}
                    download={hasDirectApk ? true : undefined}
                    target={hasDirectApk ? undefined : '_blank'}
                    rel={hasDirectApk ? undefined : 'noopener noreferrer'}
                    className="btn-primary w-full"
                  >
                    <Download className="w-4 h-4 shrink-0" aria-hidden="true" />
                    <span>Scarica APK ARM64</span>
                  </a>
                </Magnet>

                {hasDirectApk && (
                  <button
                    type="button"
                    className="copy-icon-btn"
                    data-copied={copied}
                    onClick={copyDownloadUrl}
                    title={copied ? 'Link copiato' : 'Copia link APK ARM64'}
                    aria-label={copied ? 'Link APK copiato negli appunti' : 'Copia link APK ARM64'}
                  >
                    {copied ? (
                      <>
                        <Check className="w-3.5 h-3.5 shrink-0" style={{ color: 'var(--status-green)' }} aria-hidden="true" />
                        <span className="copy-btn-feedback">Copiato</span>
                      </>
                    ) : (
                      <Copy className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                    )}
                  </button>
                )}
                <span className="sr-only" role="status" aria-live="polite">
                  {copied ? 'Link APK copiato negli appunti.' : ''}
                </span>
              </div>
            </div>
          </FluidReveal>

          {/* iOS Card (sole <article> inside #download) */}
          <FluidReveal delayMs={105} materialize className="flex">
            <article aria-label="Download iOS" className="dl-card w-full">
              <div className="dl-card-head">
                <div className="dl-brand">
                  <span className="dl-os-icon" aria-hidden="true">
                    <AppleLogo className="w-4 h-4" />
                  </span>
                  <span className="dl-os-name">iOS</span>
                </div>
                <span className="dl-version-pill">
                  {iosRelease ? `Versione ${iosRelease.version}` : 'iPhone · KMP'}
                </span>
              </div>

              <p className="dl-card-desc">
                L&apos;IPA richiede firma e provisioning: firmalo nel browser col tuo Apple ID via WebUSB o SideStore; il bundle simulatore è separato.
              </p>

              <div className="dl-card-actions">
                <Magnet
                  padding={34}
                  magnetStrength={6}
                  maxOffset={5}
                  disabled={reducedMotion}
                  wrapperClassName="dl-primary-magnet"
                  innerClassName="w-full"
                >
                  <a href={withBasePath('/ios/')} className="btn-primary w-full">
                    <Zap className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                    <span>Sideload Web</span>
                  </a>
                </Magnet>

                {iosDownloads.length > 0 ? (
                  iosDownloads.map((file) => (
                    <a key={file.name} className="btn-material dl-secondary-btn" href={file.url}>
                      <Download className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                      <span>{file.name.endsWith('.ipa') ? 'IPA iOS' : 'Bundle simulatore'}</span>
                    </a>
                  ))
                ) : (
                  <a
                    href={iosRelease?.url || 'https://github.com/Anto426-Project/uniapp-upstream/releases'}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="btn-material dl-secondary-btn"
                  >
                    <span>Rilasci su GitHub</span>
                    <ExternalLink className="w-3.5 h-3.5 opacity-65" aria-hidden="true" />
                  </a>
                )}
              </div>
            </article>
          </FluidReveal>
        </div>

        {/* Bounded Inset-Grouped Release Notes Sheet */}
        <FluidReveal delayMs={110} materialize>
          <div id="changelog" className="changelog-sheet">
            <div className="changelog-sheet-head">
              <div className="changelog-head-left">
                <span className="changelog-head-icon" aria-hidden="true">
                  <FileText className="w-3.5 h-3.5" />
                </span>
                <span className="changelog-sheet-title">
                  {parsedNotes.length > 0 && version
                    ? `Note di rilascio · ${version}`
                    : 'Note di rilascio'}
                </span>
              </div>

              <a
                href="https://github.com/Anto426-Project/uniapp-upstream/releases"
                target="_blank"
                rel="noopener noreferrer"
                className="inline-link"
              >
                <ShieldCheck className="w-3.5 h-3.5" style={{ color: 'var(--accent)' }} aria-hidden="true" />
                <span>SHA-256 e storico rilasci</span>
                <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
              </a>
            </div>

            <div
              className="changelog-scroll-region"
              tabIndex={0}
              aria-label="Elenco delle note di rilascio"
            >
              <ul className="changelog-list">
                {changelogEntries.map((entry, idx) => (
                  <li key={idx}>{renderInlineMarkdown(entry)}</li>
                ))}
              </ul>
            </div>
          </div>
        </FluidReveal>
      </div>
    </section>
  );
}
