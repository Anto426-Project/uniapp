'use client';

import React, { useEffect, useRef, useState } from 'react';
import { Activity, Check, ExternalLink, Lock, ShieldCheck } from 'lucide-react';
import { MONET_THEMES, getScreenshots, previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { SPRING_UI, SpringDriver, hapticSnap } from '@/lib/fluid-physics';
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
      'Liquid Monet governa palette dinamiche, rifrazione del vetro, transizioni tra schermate, icone adattive Apple Spark U su Android 13+ e varianti Default, Dark e Tinted su iOS.',
    points: [
      'Colore dinamico Material You, quattro preset (Zaffiro, Smeraldo, Tramonto, Viola) e Color Lab in HSB, RGB e HEX.',
      'Sfondi Orbital Pulse, Aurora, Mesh Glow e Radiant Beam con intensità, velocità e qualità ottica regolabili.',
      "Transizioni gestite interamente dall'SDK, barra di navigazione a scomparsa e rispetto nativo di «Riduci movimento».",
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
    role: 'Motore optical glass, temi dinamici, fisica e componenti Compose condivisi.',
  },
  {
    name: 'uni-sdk',
    role: 'Client KMP per Cineca Esse3, trasporti, biglietti offline e 131 codici di errore stabili.',
  },
  {
    name: 'secure-storage-sdk',
    role: 'Storage cifrato separato per account su Android Keystore e Apple Keychain.',
  },
  {
    name: 'firebase-connector-sdk',
    role: 'Notifiche push e gestione del consenso per account attivo su Android e iOS.',
  },
] as const;

export function ArchitectureSection() {
  const { theme, appearance, reducedMotion, openLightbox } = useSite();
  const [activeTab, setActiveTab] = useState<ArchTabId>('monet');

  const copyRef = useRef<HTMLDivElement>(null);
  const phoneRef = useRef<HTMLButtonElement>(null);
  const prevTabIndexRef = useRef(0);
  const shiftSpringRef = useRef<SpringDriver | null>(null);
  const opacitySpringRef = useRef<SpringDriver | null>(null);
  const scaleSpringRef = useRef<SpringDriver | null>(null);

  useEffect(() => {
    const apply = () => {
      const copyEl = copyRef.current;
      const phoneEl = phoneRef.current;
      const shift = shiftSpringRef.current?.value ?? 0;
      const opacity = opacitySpringRef.current?.value ?? 1;
      const scale = scaleSpringRef.current?.value ?? 1;

      if (copyEl) {
        copyEl.style.transform = `translate3d(${shift.toFixed(2)}px, 0, 0)`;
        copyEl.style.opacity = opacity.toFixed(3);
      }
      if (phoneEl) {
        phoneEl.style.transform = `scale(${scale.toFixed(4)})`;
      }
    };

    const shift = new SpringDriver(0, apply, SPRING_UI, 0.1);
    const opacity = new SpringDriver(1, apply, { damping: 1.0, response: 0.3 }, 0.005);
    const scale = new SpringDriver(1, apply, { damping: 1.0, response: 0.36 }, 0.001);
    shiftSpringRef.current = shift;
    opacitySpringRef.current = opacity;
    scaleSpringRef.current = scale;

    return () => {
      shift.destroy();
      opacity.destroy();
      scale.destroy();
      shiftSpringRef.current = null;
      opacitySpringRef.current = null;
      scaleSpringRef.current = null;
    };
  }, []);

  const handleTabChange = (next: ArchTabId) => {
    if (next === activeTab) return;
    const nextIdx = ARCH_TABS.findIndex((t) => t.id === next);
    const dir = nextIdx >= prevTabIndexRef.current ? 1 : -1;
    prevTabIndexRef.current = nextIdx;

    hapticSnap(4);
    setActiveTab(next);

    if (reducedMotion) {
      opacitySpringRef.current?.setImmediate(0.45);
      opacitySpringRef.current?.animateTo(1, { immediate: false, config: { damping: 1.0, response: 0.2 } });
      return;
    }

    // Interruptible directional spring glide along tab travel axis (§3, §7)
    shiftSpringRef.current?.setImmediate(dir * 26);
    shiftSpringRef.current?.animateTo(0, { config: { damping: 1.0, response: 0.34 } });

    opacitySpringRef.current?.setImmediate(0.12);
    opacitySpringRef.current?.animateTo(1, { config: { damping: 1.0, response: 0.3 } });

    scaleSpringRef.current?.setImmediate(0.968);
    scaleSpringRef.current?.animateTo(1, { config: { damping: 0.9, response: 0.36 } });
  };

  const activeTheme = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];
  const pillar = PILLAR_CONTENT[activeTab];
  const shot = getScreenshots(theme, appearance)[pillar.shotIndex];

  return (
    <section id="architecture" className="section-block section-grouped">
      <div className="container-shell">
        <FluidReveal>
          <div className="section-header-center">
            <span className="type-eyebrow">Architettura</span>
            <h2 className="type-title-1">Kotlin Multiplatform e Liquid Monet sotto il cofano.</h2>
            <p className="type-body">
              Un&apos;unica base nativa condivisa tra Android 10+ e iOS, con motore grafico proprietario, transizioni unificate e quattro SDK verificati via SHA-256.
            </p>
            <div className="mt-2">
              <FluidSegmentedControl
                options={ARCH_TABS}
                value={activeTab}
                onChange={handleTabChange}
                ariaLabel="Sezioni dell'architettura"
                compact
              />
            </div>
          </div>
        </FluidReveal>

        <FluidReveal delayMs={70}>
          <div className="arch-panel">
            <div ref={copyRef} className="arch-panel-copy" style={{ willChange: 'transform, opacity' }}>
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
            </div>

            <div className="bento-phone-well" style={{ minHeight: '21rem' }}>
              <button
                ref={phoneRef}
                type="button"
                className="bento-phone-trigger"
                style={{ width: 'min(14rem, 76%)', willChange: 'transform' }}
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
        </FluidReveal>

        {/* Hairline 4-Column SDK Spec Ledger */}
        <FluidReveal delayMs={110}>
          <div className="sdk-ledger" aria-label="Moduli SDK integrati">
            {SDKS.map((sdk) => (
              <div key={sdk.name} className="sdk-ledger-col">
                <span className="sdk-name">{sdk.name}</span>
                <p className="type-caption" style={{ color: 'var(--label-secondary)' }}>
                  {sdk.role}
                </p>
              </div>
            ))}
          </div>
        </FluidReveal>

        {/* Unified Horizontal Security Trust Ribbon (#security anchor) */}
        <FluidReveal delayMs={130}>
          <div id="security" className="trust-ribbon" style={{ scrollMarginTop: '6rem' }}>
            <div className="trust-ribbon-head">
              <div className="flex flex-col gap-1">
                <span className="type-eyebrow">Trasparenza</span>
                <h3 className="type-title-2">Sicurezza verificabile, non promessa.</h3>
              </div>
              <a
                href="https://github.com/Anto426-Project/uniapp-upstream/releases"
                target="_blank"
                rel="noopener noreferrer"
                className="btn-material"
              >
                <span>Verifica impronte SHA-256</span>
                <ExternalLink className="w-4 h-4 opacity-70" aria-hidden="true" />
              </a>
            </div>

            <div className="trust-ribbon-grid">
              <div className="trust-col">
                <div className="icon-badge">
                  <Lock className="w-4 h-4" aria-hidden="true" />
                </div>
                <h4 className="type-headline">Archiviazione isolata</h4>
                <p className="type-callout">
                  Credenziali, ticket e cache locale restano cifrati e separati per profilo, protetti da biometria o PIN a 6 cifre.
                </p>
              </div>

              <div className="trust-col">
                <div className="icon-badge">
                  <Activity className="w-4 h-4" aria-hidden="true" />
                </div>
                <h4 className="type-headline">Connessioni cifrate</h4>
                <p className="type-callout">
                  Tutte le richieste viaggiano su HTTPS con traffico in chiaro disabilitato a livello di piattaforma, senza pubblicità né tracciamento di terze parti.
                </p>
              </div>

              <div className="trust-col">
                <div className="icon-badge">
                  <ShieldCheck className="w-4 h-4" aria-hidden="true" />
                </div>
                <h4 className="type-headline">Integrità controllata</h4>
                <p className="type-callout">
                  Ogni rilascio su GitHub pubblica commit, manifest JSON e impronte SHA-256; l&apos;updater Android verifica la firma del pacchetto dopo il download.
                </p>
              </div>
            </div>
          </div>
        </FluidReveal>
      </div>
    </section>
  );
}
