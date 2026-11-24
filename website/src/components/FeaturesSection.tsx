'use client';

import React from 'react';
import {
  Bus,
  CalendarCheck,
  Users,
} from 'lucide-react';
import type { MonetThemeId } from '@/data/types';
import { MONET_THEMES, getScreenshots, previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { CrossfadeImage } from './CrossfadeImage';
import { FluidReveal } from './FluidReveal';
import SpotlightCard from './reactbits/SpotlightCard';

const SPOTLIGHT_HEX: Record<MonetThemeId, string> = {
  sapphire: '#5AA4F0',
  emerald: '#3BD67F',
  sunset: '#F09038',
  violet: '#B074D4',
};

export function FeaturesSection() {
  const { theme, appearance, openLightbox } = useSite();
  const activeTheme = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];
  const shots = getScreenshots(theme, appearance);
  const didactics = shots[2];
  const badge = shots[3];
  const spotColor = SPOTLIGHT_HEX[theme];

  const handleOpenShot = (index: number, e: React.MouseEvent<HTMLButtonElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    hapticSnap(6);
    openLightbox(index, {
      left: rect.left,
      top: rect.top,
      width: rect.width,
      height: rect.height,
    });
  };

  return (
    <section id="features" className="section-block section-grouped">
      <div className="container-shell">
        <FluidReveal>
          <div className="section-header-split">
            <div>
              <span className="type-eyebrow">Funzionalità</span>
              <h2 className="type-title-1">Nata per chi vive il campus ogni giorno.</h2>
            </div>
            <p className="type-body section-split-sub">
              Precaricamento a tre livelli, cache cifrata e biglietti salvati in locale: tutto resta pronto anche nei corridoi senza segnale.
            </p>
          </div>
        </FluidReveal>

        <FluidReveal delayMs={60}>
          <div className="bento-mosaic">
            {/* Row 1, Left (7 cols): Didattica e carriera */}
            <SpotlightCard
              theme={appearance}
              spotlightColor={spotColor}
              intensity={0.14}
              spotlightSize={320}
              borderGlow={0.65}
              className="bento-spot-tile bento-span-7"
            >
              <div className="bento-hero-split">
                <div className="flex flex-col items-start gap-3">
                  <span className="type-eyebrow">Didattica e carriera</span>
                  <h3 className="type-title-2">
                    Media, CFU e simulatore di laurea sempre allineati.
                  </h3>
                  <p className="type-callout">
                    Dal libretto passi al piano di studi, simuli i voti sugli esami rimanenti e apri le schede insegnamento con programmi completi e link al Course Catalogue CINECA.
                  </p>
                  <div className="bento-metric-chips">
                    <span className="metric-chip">Base di laurea</span>
                    <span className="metric-chip">Course Catalogue</span>
                    <span className="metric-chip">Cache offline</span>
                  </div>
                </div>

                <div className="bento-phone-well">
                  <button
                    type="button"
                    className="bento-phone-trigger"
                    aria-label={`Apri a schermo intero: ${didactics.title}`}
                    onClick={(e) => handleOpenShot(2, e)}
                  >
                    <div className="bento-phone-inner">
                      <CrossfadeImage
                        src={withBasePath(previewPathFor(didactics.file))}
                        alt={`Schermata ${didactics.title} di UniApp con tema ${activeTheme.label}`}
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

            {/* Row 1, Right (5 cols): UniPass e presenze */}
            <SpotlightCard
              theme={appearance}
              spotlightColor={spotColor}
              intensity={0.16}
              spotlightSize={300}
              borderGlow={0.65}
              className="bento-spot-tile bento-span-5 bento-badge-card"
            >
              <div className="flex flex-col items-start gap-2.5">
                <span className="type-eyebrow">UniPass e aula</span>
                <h3 className="type-title-2">Badge 3D e scanner QR nativo.</h3>
                <p className="type-callout">
                  Gira la tessera digitale UniPass per mostrare il QR accademico generato sul dispositivo e registra le presenze in aula con mirino animato e torcia.
                </p>
              </div>

              <div className="bento-phone-well bento-phone-well--compact">
                <button
                  type="button"
                  className="bento-phone-trigger"
                  aria-label={`Apri a schermo intero: ${badge.title}`}
                  onClick={(e) => handleOpenShot(3, e)}
                >
                  <div className="bento-phone-inner">
                    <CrossfadeImage
                      src={withBasePath(previewPathFor(badge.file))}
                      alt={`Schermata ${badge.title} di UniApp con tema ${activeTheme.label}`}
                      width={480}
                      height={880}
                      loading="lazy"
                      objectPosition="top center"
                    />
                  </div>
                </button>
              </div>
            </SpotlightCard>

            {/* Row 2 (3 clean balanced cards: 4 + 4 + 4) */}
            <SpotlightCard
              theme={appearance}
              spotlightColor={spotColor}
              intensity={0.12}
              spotlightSize={260}
              borderGlow={0.55}
              className="bento-spot-tile bento-span-4"
            >
              <div className="bento-compact-body">
                <div className="icon-badge">
                  <CalendarCheck className="w-4 h-4" aria-hidden="true" />
                </div>
                <h3 className="type-headline">Appelli, tasse e OPIS</h3>
                <p className="type-callout">
                  Prenota o annulla appelli, controlla le rate, compila i questionari OPIS in app e apri Esse3, Moodle, Segreteria ed Email di Ateneo.
                </p>
              </div>
            </SpotlightCard>

            <SpotlightCard
              theme={appearance}
              spotlightColor={spotColor}
              intensity={0.12}
              spotlightSize={260}
              borderGlow={0.55}
              className="bento-spot-tile bento-span-4"
            >
              <div className="bento-compact-body">
                <div className="icon-badge">
                  <Bus className="w-4 h-4" aria-hidden="true" />
                </div>
                <h3 className="type-headline">Navette e biglietti offline</h3>
                <p className="type-callout">
                  Prenota corse feriali fino a 15 giorni, consulta le fermate e conserva i biglietti originali con barcode leggibili anche senza rete.
                </p>
              </div>
            </SpotlightCard>

            <SpotlightCard
              theme={appearance}
              spotlightColor={spotColor}
              intensity={0.12}
              spotlightSize={260}
              borderGlow={0.55}
              className="bento-spot-tile bento-span-4"
            >
              <div className="bento-compact-body">
                <div className="icon-badge">
                  <Users className="w-4 h-4" aria-hidden="true" />
                </div>
                <h3 className="type-headline">Multi-account, Color Lab e 10 lingue</h3>
                <p className="type-callout">
                  Più profili e carriere con sessioni cifrate isolate, Area Docente, rubrica sedi, tinta libera HSB, RGB o HEX e 10 lingue disponibili.
                </p>
              </div>
            </SpotlightCard>
          </div>
        </FluidReveal>
      </div>
    </section>
  );
}
