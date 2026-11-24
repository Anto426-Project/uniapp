'use client';

import React from 'react';
import { motion, useMotionValue, useSpring, useTransform } from 'motion/react';
import { ChevronRight, Download } from 'lucide-react';
import type { MonetThemeId } from '@/data/types';
import { MONET_THEMES, getScreenshots, previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { FluidSegmentedControl } from './FluidSegmentedControl';
import { CrossfadeImage } from './CrossfadeImage';
import Aurora from './reactbits/Aurora';
import BlurText from './reactbits/BlurText';

const AURORA_STOPS: Record<MonetThemeId, string[]> = {
  sapphire: ['#173679', '#4A90D9', '#8CC8FF'],
  emerald: ['#0B5339', '#2ECC71', '#84ECC0'],
  sunset: ['#8C370A', '#E67E22', '#FFC27A'],
  violet: ['#4D1D7A', '#9B59B6', '#DFC2FF'],
};

export function Hero() {
  const { theme, setTheme, appearance, reducedMotion, openLightbox } = useSite();

  const pointerX = useMotionValue(0);
  const pointerY = useMotionValue(0);
  const springX = useSpring(pointerX, { stiffness: 220, damping: 24, mass: 0.7 });
  const springY = useSpring(pointerY, { stiffness: 220, damping: 24, mass: 0.7 });
  const rotateY = useTransform(springX, [-0.5, 0.5], [-6, 6]);
  const rotateX = useTransform(springY, [-0.5, 0.5], [5, -5]);

  const shots = getScreenshots(theme, appearance);
  const homeShot = shots[0];
  const didacticsShot = shots[2];
  const badgeShot = shots[3];
  const activeThemeMeta = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];

  const description =
    "Libretto, simulatore laurea, appelli, presenze QR, badge UniPass e navette dell'Università degli Studi del Molise. Costruita in Kotlin Multiplatform su Liquid Monet, gratuita e senza tracciamento.";

  const launchFromElement = (index: number, el: HTMLElement) => {
    const rect = el.getBoundingClientRect();
    hapticSnap(6);
    openLightbox(index, {
      left: rect.left,
      top: rect.top,
      width: rect.width,
      height: rect.height,
    });
  };

  const handleStageMove = (e: React.MouseEvent<HTMLDivElement>) => {
    if (reducedMotion) return;
    const rect = e.currentTarget.getBoundingClientRect();
    const nx = (e.clientX - rect.left) / rect.width - 0.5;
    const ny = (e.clientY - rect.top) / rect.height - 0.5;
    pointerX.set(nx);
    pointerY.set(ny);
  };

  const handleStageLeave = () => {
    pointerX.set(0);
    pointerY.set(0);
  };

  return (
    <section className="hero-keynote">
      {/* Liquid Monet WebGL Aurora Shader Layer — seamless from y = 0 behind Navbar */}
      <div className="hero-aurora-layer" aria-hidden="true">
        <Aurora
          colorStops={AURORA_STOPS[theme]}
          amplitude={0.85}
          blend={0.62}
          speed={0.45}
          height={0.56}
          glow={0.55}
          brightness={appearance === 'dark' ? 0.85 : 0.42}
          opacity={appearance === 'dark' ? 0.65 : 0.45}
          lightMode={appearance === 'light'}
          paused={reducedMotion}
          mouseInteraction={!reducedMotion}
        />
      </div>

      <div className="container-shell relative z-2">
        <div className="hero-keynote-header">
          <h1 className="type-hero">
            {reducedMotion ? (
              <>
                La tua università, <span className="hero-accent-text">finalmente nativa.</span>
              </>
            ) : (
              <>
                <BlurText
                  text="La tua università,"
                  animateBy="words"
                  direction="bottom"
                  delay={60}
                  stepDuration={0.32}
                  blur={8}
                  distance={14}
                  trigger="load"
                  className="justify-center"
                />
                <span className="hero-accent-text block">
                  <BlurText
                    text="finalmente nativa."
                    animateBy="words"
                    direction="bottom"
                    delay={90}
                    stepDuration={0.36}
                    blur={8}
                    distance={14}
                    trigger="load"
                    className="justify-center"
                  />
                </span>
              </>
            )}
          </h1>

          <motion.p
            className="hero-subtitle"
            initial={reducedMotion ? false : { opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ type: 'spring', bounce: 0, duration: 0.55, delay: 0.1 }}
          >
            {description}
          </motion.p>

          <motion.div
            className="hero-cta-row"
            initial={reducedMotion ? false : { opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ type: 'spring', bounce: 0, duration: 0.55, delay: 0.16 }}
          >
            <a href="#download" className="btn-primary">
              <Download className="w-4 h-4" aria-hidden="true" />
              <span>Scarica UniApp</span>
            </a>
            <a
              href="https://github.com/Anto426-Project/Uniapp"
              target="_blank"
              rel="noopener noreferrer"
              className="btn-material"
            >
              <span>Codice su GitHub</span>
              <ChevronRight className="w-4 h-4 opacity-70" aria-hidden="true" />
            </a>
          </motion.div>
        </div>

        {/* Centered 3-Device Keynote Fan Triptych with Motion Springs */}
        <motion.div
          className="hero-triptych-stage"
          onMouseMove={handleStageMove}
          onMouseLeave={handleStageLeave}
          initial={reducedMotion ? false : { opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ type: 'spring', bounce: 0, duration: 0.68, delay: 0.14 }}
        >
          <div className="hero-triptych-glow" aria-hidden="true" />

          <div className="hero-triptych">
            <button
              type="button"
              className="triptych-flank triptych-flank--left"
              aria-label={`Apri anteprima ${didacticsShot.title} (${activeThemeMeta.label})`}
              onClick={(e) => launchFromElement(2, e.currentTarget)}
            >
              <div className="device-screen">
                <CrossfadeImage
                  src={withBasePath(previewPathFor(didacticsShot.file))}
                  alt={`Schermata ${didacticsShot.title} di UniApp (${activeThemeMeta.label})`}
                  width={480}
                  height={1003}
                  draggable={false}
                />
              </div>
            </button>

            <motion.button
              type="button"
              className="triptych-center"
              style={
                reducedMotion
                  ? undefined
                  : { rotateX, rotateY, transformPerspective: 1200 }
              }
              whileTap={reducedMotion ? undefined : { scale: 0.985 }}
              aria-label={`Apri anteprima ${homeShot.title} (${activeThemeMeta.label})`}
              onClick={(e) => launchFromElement(0, e.currentTarget)}
            >
              <div className="device-frame">
                <div className="device-screen">
                  <CrossfadeImage
                    src={withBasePath(previewPathFor(homeShot.file))}
                    alt={`Schermata ${homeShot.title} di UniApp (${activeThemeMeta.label})`}
                    width={480}
                    height={1003}
                    draggable={false}
                  />
                  <div className="device-sheen" aria-hidden="true" />
                </div>
              </div>
            </motion.button>

            <button
              type="button"
              className="triptych-flank triptych-flank--right"
              aria-label={`Apri anteprima ${badgeShot.title} (${activeThemeMeta.label})`}
              onClick={(e) => launchFromElement(3, e.currentTarget)}
            >
              <div className="device-screen">
                <CrossfadeImage
                  src={withBasePath(previewPathFor(badgeShot.file))}
                  alt={`Schermata ${badgeShot.title} di UniApp (${activeThemeMeta.label})`}
                  width={480}
                  height={1003}
                  draggable={false}
                />
              </div>
            </button>
          </div>

          <div className="hero-pigment-dock">
            <FluidSegmentedControl
              options={MONET_THEMES}
              value={theme}
              onChange={setTheme}
              ariaLabel="Scegli il tema dell'anteprima"
            />
          </div>
        </motion.div>
      </div>
    </section>
  );
}
