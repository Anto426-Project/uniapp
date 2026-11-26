'use client';

import React, { useRef, useState } from 'react';
import { AnimatePresence, motion, useScroll, useSpring, useTransform } from 'motion/react';
import { Bell, Check, Cpu, ExternalLink, Layers, Maximize2, ShieldCheck } from 'lucide-react';
import { MONET_THEMES, getScreenshots, previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { FluidSegmentedControl } from './FluidSegmentedControl';
import { CrossfadeImage } from './CrossfadeImage';
import { FluidReveal } from './FluidReveal';

type ArchTabId = 'monet' | 'security' | 'opensource';

const ARCH_TABS = [
  { id: 'monet' as const, label: 'Liquid Monet' },
  { id: 'security' as const, label: 'Sicurezza' },
  { id: 'opensource' as const, label: 'Open source' },
] as const;

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
      'Flussi incrementali non bloccanti su uni-sdk con cache additiva e 131 codici di errore tipizzati.',
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
  const panelRef = useRef<HTMLDivElement>(null);

  const { scrollYProgress } = useScroll({
    target: panelRef,
    offset: ['start end', 'end start'],
  });
  const smoothScroll = useSpring(scrollYProgress, { stiffness: 130, damping: 24, mass: 0.45 });
  const phoneParallaxY = useTransform(smoothScroll, [0, 1], [20, -18]);

  const activeTheme = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];
  const pillar = PILLAR_CONTENT[activeTab];
  const shots = getScreenshots(theme, appearance);
  const shot = shots[pillar.shotIndex] || shots[0];

  return (
    <section id="architecture" className="section-block section-grouped">
      <div className="container-shell">
        <FluidReveal>
          <div className="section-header-split">
            <div className="section-header-lead">
              <span className="type-eyebrow">Architettura</span>
              <h2 className="type-title-1">Kotlin Multiplatform sotto la superficie.</h2>
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

        <FluidReveal delayMs={45} materialize>
          <div ref={panelRef} className="arch-panel">
            <div className="arch-panel-grid">
              <AnimatePresence mode="wait" initial={false}>
                <motion.div
                  key={activeTab}
                  className="arch-panel-copy"
                  initial={reducedMotion ? false : { opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={reducedMotion ? undefined : { opacity: 0, y: -10 }}
                  transition={
                    reducedMotion
                      ? { duration: 0.1 }
                      : { type: 'spring', stiffness: 220, damping: 26 }
                  }
                >
                  <span className="type-eyebrow">{pillar.eyebrow}</span>
                  <h3 className="type-title-2">{pillar.title}</h3>
                  <p className="type-body">{pillar.summary}</p>
                  <ul className="arch-spec-list">
                    {pillar.points.map((pt, pointIndex) => (
                      <motion.li
                        key={pt}
                        initial={reducedMotion ? false : { opacity: 0, x: -10 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={
                          reducedMotion
                            ? { duration: 0 }
                            : {
                                type: 'spring',
                                stiffness: 210,
                                damping: 25,
                                delay: 0.04 + pointIndex * 0.055,
                              }
                        }
                      >
                        <span className="arch-check-dot" aria-hidden="true">
                          <Check className="w-3 h-3" />
                        </span>
                        <span>{pt}</span>
                      </motion.li>
                    ))}
                  </ul>
                </motion.div>
              </AnimatePresence>

              <div className="bento-device-stage bento-device-stage--arch">
                <motion.button
                  type="button"
                  className="bento-device-trigger bento-device-trigger--arch"
                  style={reducedMotion ? undefined : { y: phoneParallaxY }}
                  whileHover={reducedMotion ? undefined : { y: -6 }}
                  whileTap={reducedMotion ? undefined : { scale: 0.985 }}
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
                  <div className="bento-device-shell">
                    <CrossfadeImage
                      src={withBasePath(previewPathFor(shot.file))}
                      alt={`Schermata ${shot.title} di UniApp (${activeTheme.label})`}
                      width={480}
                      height={880}
                      loading="lazy"
                      objectPosition="top center"
                    />
                    <span className="bento-zoom-pill" aria-hidden="true">
                      <Maximize2 className="w-3.5 h-3.5" />
                    </span>
                  </div>
                </motion.button>
              </div>
            </div>
          </div>
        </FluidReveal>

        {/* Modular SDK Spec Strip */}
        <div
          id="security"
          className="sdk-spec-grid"
          aria-label="Moduli SDK e sicurezza"
          style={{ scrollMarginTop: '6rem' }}
        >
          {SDKS.map((sdk, index) => {
            const Icon = sdk.icon;
            return (
              <FluidReveal key={sdk.name} delayMs={65 + index * 55} materialize className="flex">
                <div className="sdk-spec-card w-full">
                  <div className="sdk-spec-top">
                    <span className="icon-badge icon-badge--sm" aria-hidden="true">
                      <Icon className="w-4 h-4" />
                    </span>
                    {sdk.href ? (
                      <a
                        href={sdk.href}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="sdk-tag sdk-tag--link"
                      >
                        <span>{sdk.tag}</span>
                        <ExternalLink className="w-3 h-3 opacity-70" aria-hidden="true" />
                      </a>
                    ) : (
                      <span className="sdk-tag">{sdk.tag}</span>
                    )}
                  </div>
                  <div className="sdk-spec-body">
                    <span className="sdk-name">{sdk.name}</span>
                    <p className="sdk-role">{sdk.role}</p>
                  </div>
                </div>
              </FluidReveal>
            );
          })}
        </div>
      </div>
    </section>
  );
}
