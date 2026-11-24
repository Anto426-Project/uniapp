'use client';

import React, { useState } from 'react';
import { AnimatePresence, motion } from 'motion/react';
import { Bell, Check, Cpu, ExternalLink, Layers, ShieldCheck } from 'lucide-react';
import type { MonetThemeId } from '@/data/types';
import { MONET_THEMES, getScreenshots, previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { FluidSegmentedControl } from './FluidSegmentedControl';
import { CrossfadeImage } from './CrossfadeImage';
import { FluidReveal } from './FluidReveal';
import SpotlightCard from './reactbits/SpotlightCard';

type ArchTabId = 'monet' | 'security' | 'opensource';

const ARCH_TABS = [
  { id: 'monet' as const, label: 'Liquid Monet' },
  { id: 'security' as const, label: 'Sicurezza' },
  { id: 'opensource' as const, label: 'Open source' },
] as const;

const SPOTLIGHT_HEX: Record<MonetThemeId, string> = {
  sapphire: '#5AA4F0',
  emerald: '#3BD67F',
  sunset: '#F09038',
  violet: '#B074D4',
};

const PILLAR_CONTENT: Record<
  ArchTabId,
  {
    eyebrow: string;
    title: string;
    summary: string;
    points: string[];
    shotIndex: number;
  }
> = {
  monet: {
    eyebrow: 'Design system proprietario',
    title: 'Superfici optical glass, fisica unificata e controllo su ogni dettaglio.',
    summary:
      'Liquid Monet governa palette dinamiche, rifrazione del vetro, transizioni tra schermate, icone adattive su Android 13+ e varianti Default, Dark e Tinted su iOS.',
    points: [
      'Colore dinamico Material You, quattro preset curati e Color Lab in HSB, RGB e HEX.',
      'Sfondi Orbital Pulse, Aurora, Mesh Glow e Radiant Beam con intensità e qualità regolabili.',
      "Transizioni gestite interamente dall'SDK, barra a scomparsa e rispetto nativo di «Riduci movimento».",
    ],
    shotIndex: 4,
  },
  security: {
    eyebrow: 'Protezione account',
    title: 'Sessioni isolate, biometria e revoca remota.',
    summary:
      'Ogni profilo conserva credenziali, token e cache in uno storage cifrato dedicato, al riparo da backup non cifrati e traffico in chiaro.',
    points: [
      'Sblocco con impronta o volto e PIN a 6 cifre sugli account protetti.',
      'Elenco Dispositivi connessi con revoca lato server delle sessioni e «Disconnetti tutti».',
      'Backup automatico e connessioni in chiaro disabilitati a livello di piattaforma.',
    ],
    shotIndex: 5,
  },
  opensource: {
    eyebrow: 'Codice e verificabilità',
    title: 'Sorgente aperto, zero tracker e build indipendenti.',
    summary:
      'UniApp e Liquid Monet sono pubblici su GitHub. Le build Android, iOS e sito seguono pipeline separate con manifest e impronte verificabili.',
    points: [
      'Zero pubblicità, zero profilazione e zero SDK di tracciamento.',
      'Protocollo UniMol e Cineca Esse3 gestito da uni-sdk con 131 codici di errore tipizzati.',
      'Ogni rilascio pubblica commit sorgente, manifest JSON e checksum SHA-256.',
    ],
    shotIndex: 6,
  },
};

const SDKS = [
  {
    name: 'liquid-monet',
    tag: 'UI Engine',
    icon: Layers,
    href: 'https://github.com/Anto426-Project/Liquid-Monet',
    role: 'Motore optical glass, temi dinamici, fisica e componenti Compose condivisi.',
  },
  {
    name: 'uni-sdk',
    tag: 'Core KMP',
    icon: Cpu,
    href: undefined,
    role: 'Client per Cineca Esse3, trasporti, biglietti offline e 131 codici di errore tipizzati.',
  },
  {
    name: 'secure-storage',
    tag: 'Crypto',
    icon: ShieldCheck,
    href: undefined,
    role: 'Archivi cifrati isolati per account su Android Keystore e Apple Keychain.',
  },
  {
    name: 'push-connector',
    tag: 'Notifiche',
    icon: Bell,
    href: undefined,
    role: 'Notifiche in tempo reale e consenso granulare per singolo profilo su Android e iOS.',
  },
] as const;

export function ArchitectureSection() {
  const { theme, appearance, reducedMotion, openLightbox } = useSite();
  const [activeTab, setActiveTab] = useState<ArchTabId>('monet');

  const activeTheme = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];
  const pillar = PILLAR_CONTENT[activeTab];
  const shots = getScreenshots(theme, appearance);
  const shot = shots[pillar.shotIndex] || shots[0];
  const spotColor = SPOTLIGHT_HEX[theme];

  return (
    <section id="architecture" className="section-block section-grouped">
      <div className="container-shell">
        <FluidReveal>
          <div className="section-header-split">
            <div>
              <span className="type-eyebrow">Architettura</span>
              <h2 className="type-title-1">Kotlin Multiplatform sotto il cofano.</h2>
            </div>
            <div className="arch-header-controls">
              <FluidSegmentedControl
                options={ARCH_TABS}
                value={activeTab}
                onChange={setActiveTab}
                ariaLabel="Sezioni dell'architettura"
                compact
              />
            </div>
          </div>
        </FluidReveal>

        <FluidReveal delayMs={50}>
          <SpotlightCard
            theme={appearance}
            spotlightColor={spotColor}
            intensity={0.14}
            spotlightSize={360}
            borderGlow={0.6}
            className="arch-spot-panel"
          >
            <div className="arch-panel-grid">
              <AnimatePresence mode="wait" initial={false}>
                <motion.div
                  key={activeTab}
                  className="arch-panel-copy"
                  initial={reducedMotion ? false : { opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={reducedMotion ? undefined : { opacity: 0, y: -8 }}
                  transition={{ duration: reducedMotion ? 0 : 0.22, ease: [0.22, 1, 0.36, 1] }}
                >
                  <span className="type-eyebrow">{pillar.eyebrow}</span>
                  <h3 className="type-title-2">{pillar.title}</h3>
                  <p className="type-body">{pillar.summary}</p>
                  <ul className="arch-spec-list">
                    {pillar.points.map((pt) => (
                      <li key={pt}>
                        <Check className="w-4 h-4 shrink-0 mt-1" style={{ color: 'var(--accent)' }} aria-hidden="true" />
                        <span>{pt}</span>
                      </li>
                    ))}
                  </ul>
                </motion.div>
              </AnimatePresence>

              <div className="bento-phone-well" style={{ minHeight: '20.5rem' }}>
                <button
                  type="button"
                  className="bento-phone-trigger"
                  style={{ width: 'min(13.75rem, 76%)' }}
                  aria-label={`Apri a schermo intero: ${shot.title}`}
                  onClick={(e) => {
                    const rect = e.currentTarget.getBoundingClientRect();
                    hapticSnap(6);
                    openLightbox(pillar.shotIndex, {
                      left: rect.left,
                      top: rect.top,
                      width: rect.width,
                      height: rect.height,
                    });
                  }}
                >
                  <div className="bento-phone-inner">
                    <CrossfadeImage
                      src={withBasePath(previewPathFor(shot.file))}
                      alt={`Schermata ${shot.title} di UniApp (${activeTheme.label})`}
                      width={480}
                      height={880}
                      loading="lazy"
                      objectPosition="top center"
                    />
                  </div>
                </button>
              </div>
            </div>
          </SpotlightCard>
        </FluidReveal>

        {/* Cohesive 4-Tile Bento SDK Strip (same surface language & grid width as above) */}
        <div
          id="security"
          className="sdk-bento-grid"
          aria-label="Moduli SDK e sicurezza"
          style={{ scrollMarginTop: '6rem' }}
        >
          {SDKS.map((sdk, index) => {
            const Icon = sdk.icon;
            return (
              <FluidReveal key={sdk.name} delayMs={70 + index * 35}>
                <SpotlightCard
                  theme={appearance}
                  spotlightColor={spotColor}
                  intensity={0.13}
                  spotlightSize={240}
                  borderGlow={0.55}
                  className="sdk-bento-card"
                >
                  <div className="sdk-bento-top">
                    <span className="sdk-bento-icon" aria-hidden="true">
                      <Icon className="w-4 h-4" />
                    </span>
                    {sdk.href ? (
                      <a
                        href={sdk.href}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="sdk-bento-badge sdk-bento-badge--link"
                      >
                        <span>{sdk.tag}</span>
                        <ExternalLink className="w-3 h-3 opacity-70" aria-hidden="true" />
                      </a>
                    ) : (
                      <span className="sdk-bento-badge">{sdk.tag}</span>
                    )}
                  </div>
                  <div className="sdk-bento-body">
                    <span className="sdk-name">{sdk.name}</span>
                    <p className="sdk-role">{sdk.role}</p>
                  </div>
                </SpotlightCard>
              </FluidReveal>
            );
          })}
        </div>
      </div>
    </section>
  );
}
