'use client';

import React, { useRef } from 'react';
import { motion, useScroll, useSpring, useTransform } from 'motion/react';
import { Bus, CalendarCheck, Maximize2, Palette } from 'lucide-react';
import { MONET_THEMES, getScreenshots, previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { CrossfadeImage } from './CrossfadeImage';
import { FluidReveal } from './FluidReveal';

export function FeaturesSection() {
  const { theme, setTheme, appearance, reducedMotion, openLightbox } = useSite();
  const sectionRef = useRef<HTMLElement>(null);
  const activeTheme = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];
  const shots = getScreenshots(theme, appearance);
  const didactics = shots[2];
  const badge = shots[3];

  // Smooth scroll-driven hardware elevation inside the Bento stages
  const { scrollYProgress } = useScroll({
    target: sectionRef,
    offset: ['start end', 'end start'],
  });
  const smoothScroll = useSpring(scrollYProgress, { stiffness: 130, damping: 24, mass: 0.45 });
  const didacticsY = useTransform(smoothScroll, [0, 1], [18, -16]);
  const badgeY = useTransform(smoothScroll, [0, 1], [24, -10]);

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
    <section ref={sectionRef} id="features" className="section-block section-grouped">
      <div className="container-shell">
        <FluidReveal>
          <div className="section-header-split">
            <div className="section-header-lead">
              <span className="type-eyebrow">Funzionalità</span>
              <h2 className="type-title-1">Progettata per il ritmo del campus.</h2>
            </div>
            <p className="type-body section-split-sub">
              Caricamento progressivo a flusso, cache cifrata additiva e badge Offline integrato: ogni dato resta leggibile anche dove il segnale non arriva.
            </p>
          </div>
        </FluidReveal>

        <div className="bento-grid">
          {/* Tile 1 (Span 7): Didattica e carriera — Hero horizontal Bento */}
          <FluidReveal className="bento-span-7 flex" delayMs={0} materialize>
            <div className="bento-card bento-card--feature-wide">
              <div className="bento-wide-copy">
                <span className="type-eyebrow">Didattica e carriera</span>
                <h3 className="type-title-2">
                  Media, CFU e simulatore di laurea sempre allineati.
                </h3>
                <p className="type-callout">
                  Dal libretto passi al piano di studi, simuli i voti sugli esami rimanenti e apri le schede insegnamento con programmi completi e link diretti al Course Catalogue CINECA.
                </p>
                <div className="bento-tags">
                  <span className="bento-tag">Base di laurea</span>
                  <span className="bento-tag">Course Catalogue</span>
                  <span className="bento-tag">Cache offline</span>
                </div>
              </div>

              <div className="bento-device-stage">
                <motion.button
                  type="button"
                  className="bento-device-trigger"
                  style={reducedMotion ? undefined : { y: didacticsY }}
                  whileHover={reducedMotion ? undefined : { y: -6 }}
                  whileTap={reducedMotion ? undefined : { scale: 0.985 }}
                  aria-label={`Apri a schermo intero: ${didactics.title}`}
                  onClick={(e) => handleOpenShot(2, e)}
                >
                  <div className="bento-device-shell">
                    <CrossfadeImage
                      src={withBasePath(previewPathFor(didactics.file))}
                      alt={`Schermata ${didactics.title} di UniApp con tema ${activeTheme.label}`}
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
          </FluidReveal>

          {/* Tile 2 (Span 5): UniPass 3D e Presenze QR — Tall vertical Bento */}
          <FluidReveal className="bento-span-5 flex" delayMs={75} materialize>
            <div className="bento-card bento-card--feature-tall">
              <div className="bento-tall-copy">
                <span className="type-eyebrow">UniPass e aula</span>
                <h3 className="type-title-2">Badge 3D e scanner QR nativo.</h3>
                <p className="type-callout">
                  Ruota la tessera digitale UniPass per mostrare il QR accademico generato sul dispositivo e registra le presenze in aula con mirino animato e torcia.
                </p>
              </div>

              <div className="bento-device-stage bento-device-stage--compact">
                <motion.button
                  type="button"
                  className="bento-device-trigger"
                  style={reducedMotion ? undefined : { y: badgeY }}
                  whileHover={reducedMotion ? undefined : { y: -6 }}
                  whileTap={reducedMotion ? undefined : { scale: 0.985 }}
                  aria-label={`Apri a schermo intero: ${badge.title}`}
                  onClick={(e) => handleOpenShot(3, e)}
                >
                  <div className="bento-device-shell">
                    <CrossfadeImage
                      src={withBasePath(previewPathFor(badge.file))}
                      alt={`Schermata ${badge.title} di UniApp con tema ${activeTheme.label}`}
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
          </FluidReveal>

          {/* Tile 3 (Span 4): Appelli, notizie live e OPIS */}
          <FluidReveal className="bento-span-4 flex" delayMs={60} materialize>
            <div className="bento-card bento-card--compact">
              <div className="bento-compact-top">
                <span className="icon-badge" aria-hidden="true">
                  <CalendarCheck className="w-4 h-4" />
                </span>
                <span className="bento-meta-pill">Sync 2 min</span>
              </div>
              <div className="bento-compact-copy">
                <h3 className="type-headline">Appelli, notizie live e OPIS</h3>
                <p className="type-callout">
                  Prenota appelli, controlla le tasse, compila i questionari OPIS in app e ricevi gli avvisi di Ateneo senza perdere lo storico in cache.
                </p>
              </div>
            </div>
          </FluidReveal>

          {/* Tile 4 (Span 4): Navette e biglietti offline */}
          <FluidReveal className="bento-span-4 flex" delayMs={120} materialize>
            <div className="bento-card bento-card--compact">
              <div className="bento-compact-top">
                <span className="icon-badge" aria-hidden="true">
                  <Bus className="w-4 h-4" />
                </span>
                <span className="bento-meta-pill">Barcode offline</span>
              </div>
              <div className="bento-compact-copy">
                <h3 className="type-headline">Navette e biglietti offline</h3>
                <p className="type-callout">
                  Prenota corse feriali fino a 15 giorni, consulta fermate e orari e conserva i biglietti originali sempre leggibili anche senza rete.
                </p>
              </div>
            </div>
          </FluidReveal>

          {/* Tile 5 (Span 4): Multi-account, Color Lab e 10 lingue */}
          <FluidReveal className="bento-span-4 flex" delayMs={180} materialize>
            <div className="bento-card bento-card--compact">
              <div className="bento-compact-top">
                <span className="icon-badge" aria-hidden="true">
                  <Palette className="w-4 h-4" />
                </span>
                <div className="bento-swatch-bar" role="group" aria-label="Anteprima rapida palette Monet">
                  {MONET_THEMES.map((item) => (
                    <button
                      key={item.id}
                      type="button"
                      className="bento-swatch-btn"
                      aria-label={`Attiva tema ${item.label}`}
                      aria-pressed={theme === item.id}
                      title={item.label}
                      onClick={() => setTheme(item.id)}
                    >
                      <span
                        className="bento-swatch-dot"
                        style={{ backgroundColor: item.swatchHex }}
                        aria-hidden="true"
                      />
                    </button>
                  ))}
                </div>
              </div>
              <div className="bento-compact-copy">
                <h3 className="type-headline">Multi-account, Color Lab e 10 lingue</h3>
                <p className="type-callout">
                  Più profili e carriere con sessioni cifrate isolate, Area Docente, rubrica sedi, tinta libera HSB, RGB o HEX e 10 lingue complete.
                </p>
              </div>
            </div>
          </FluidReveal>
        </div>
      </div>
    </section>
  );
}
