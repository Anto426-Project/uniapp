'use client';

import React from 'react';
import {
  Bus,
  CalendarCheck,
  CreditCard,
  Palette,
  Users,
} from 'lucide-react';
import { MONET_THEMES, getScreenshots, previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { CrossfadeImage } from './CrossfadeImage';
import { FluidReveal } from './FluidReveal';

export function FeaturesSection() {
  const { theme, setTheme, appearance, openLightbox } = useSite();
  const activeTheme = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];
  const shots = getScreenshots(theme, appearance);
  const didactics = shots[2];
  const badge = shots[3];

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
          <div className="section-header-center">
            <span className="type-eyebrow">Funzionalità</span>
            <h2 className="type-title-1">Nata per chi vive il campus ogni giorno.</h2>
            <p className="type-body">
              Precaricamento a tre livelli, cache cifrata e biglietti salvati in locale: i dati restano pronti anche nei corridoi senza segnale.
            </p>
          </div>
        </FluidReveal>

        <FluidReveal delayMs={70}>
          <div className="bento-mosaic">
          {/* Row 1, Left (7 cols): Didattica e carriera */}
          <div className="bento-tile bento-span-7">
            <div className="bento-hero-split">
              <div className="flex flex-col items-start gap-3">
                <span className="type-eyebrow">Didattica e carriera</span>
                <h3 className="type-title-2">
                  Media, CFU e simulatore di laurea sempre sincronizzati.
                </h3>
                <p className="type-callout">
                  Dal libretto passi al piano di studi, salvi scenari sugli esami rimanenti per carriera e apri le schede insegnamento con SSD, programmi completi e link al Course Catalogue CINECA.
                </p>
                <div className="bento-metric-chips">
                  <span className="metric-chip">Base di laurea</span>
                  <span className="metric-chip">Course Catalogue CINECA</span>
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
          </div>

          {/* Row 1, Right (5 cols): UniPass e presenze */}
          <div className="bento-tile bento-span-5 bento-badge-card">
            <div className="flex flex-col items-start gap-2.5">
              <span className="type-eyebrow">UniPass e aula</span>
              <h3 className="type-title-2">Badge 3D e scanner QR Liquid Monet.</h3>
              <p className="type-callout">
                Gira la tessera digitale UniPass per mostrare il QR accademico generato sul dispositivo e registra le presenze in aula con mirino animato e torcia integrata.
              </p>
            </div>

            <div className="bento-badge-crop">
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
          </div>

          {/* Row 2 (4 + 4 + 4) */}
          <div className="bento-tile bento-span-4">
            <div className="bento-compact-body">
              <div className="icon-badge">
                <CalendarCheck className="w-4 h-4" aria-hidden="true" />
              </div>
              <h3 className="type-headline">Appelli e storico esami</h3>
              <p className="type-callout">
                Controlla le date aperte, prenota o annulla un appello senza passare dal browser e tieni sott&apos;occhio l&apos;intero storico degli esami.
              </p>
            </div>
          </div>

          <div className="bento-tile bento-span-4">
            <div className="bento-compact-body">
              <div className="icon-badge">
                <Bus className="w-4 h-4" aria-hidden="true" />
              </div>
              <h3 className="type-headline">Navette e biglietti offline</h3>
              <p className="type-callout">
                Prenota andata, ritorno o A/R nei feriali fino a 15 giorni, verifica le fermate e conserva i titoli di viaggio originali con barcode leggibili anche senza rete.
              </p>
            </div>
          </div>

          <div className="bento-tile bento-span-4">
            <div className="bento-compact-body">
              <div className="icon-badge">
                <CreditCard className="w-4 h-4" aria-hidden="true" />
              </div>
              <h3 className="type-headline">Tasse, OPIS e portali UniMol</h3>
              <p className="type-callout">
                Rate pagate, scadenze, questionari OPIS compilabili in app e accesso immediato a Esse3, Moodle, Segreteria ed Email di Ateneo.
              </p>
            </div>
          </div>

          {/* Row 3 (6 + 6) */}
          <div className="bento-tile bento-span-6">
            <div className="bento-compact-body">
              <div className="icon-badge">
                <Palette className="w-4 h-4" aria-hidden="true" />
              </div>
              <h3 className="type-headline">Tema Studio, Color Lab e 10 lingue</h3>
              <p className="type-callout">
                Colore dinamico di sistema, quattro preset o tinta libera in HSB, RGB e HEX. Regola intensità del vetro, qualità ottica, sfondi Orbital Pulse, barra a scomparsa e scegli tra 10 lingue.
              </p>
              <div className="bento-palette-bar" role="group" aria-label="Selezione rapida dei temi">
                {MONET_THEMES.map((item) => (
                  <button
                    key={item.id}
                    type="button"
                    className="palette-chip-btn"
                    aria-pressed={theme === item.id}
                    onClick={() => setTheme(item.id)}
                  >
                    <span
                      className="fluid-segmented__swatch"
                      style={{ backgroundColor: item.swatchHex }}
                      aria-hidden="true"
                    />
                    <span>{item.label}</span>
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="bento-tile bento-span-6">
            <div className="bento-compact-body">
              <div className="icon-badge">
                <Users className="w-4 h-4" aria-hidden="true" />
              </div>
              <h3 className="type-headline">Multi-account, area docente e rubrica</h3>
              <p className="type-callout">
                Gestisci più profili e carriere sullo stesso dispositivo con sessioni isolate, accedi all&apos;Area Docente, leggi le notizie e cerca contatti o uffici per sede.
              </p>
            </div>
          </div>
        </div>
        </FluidReveal>
      </div>
    </section>
  );
}
