'use client';

import React, { useRef } from 'react';
import { motion, useMotionValue, useScroll, useSpring, useTransform } from 'motion/react';
import { ChevronRight, Download } from 'lucide-react';
import { MONET_THEMES, getScreenshots, previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { FluidSegmentedControl } from './FluidSegmentedControl';
import { CrossfadeImage } from './CrossfadeImage';
import BlurText from './reactbits/BlurText';
import Magnet from './Magnet';

const HERO_PROOF_ITEMS = [
  {
    value: 'Kotlin Multiplatform',
    label: 'Un solo cuore nativo per Android e iOS',
  },
  {
    value: 'Offline-first',
    label: 'Cache cifrata e caricamento progressivo',
  },
  {
    value: 'Zero tracker',
    label: 'Open source, senza pubblicità né profilazione',
  },
  {
    value: 'Liquid Monet',
    label: 'Vetro ottico e quattro palette dinamiche',
  },
] as const;

export function Hero() {
  const { theme, setTheme, appearance, reducedMotion, release, openLightbox } = useSite();
  const stageRef = useRef<HTMLDivElement>(null);

  // Critically damped pointer-responsive 3D stage (Apple §4 — no overshoot)
  const pointerX = useMotionValue(0);
  const pointerY = useMotionValue(0);
  const springX = useSpring(pointerX, { stiffness: 190, damping: 28, mass: 0.75 });
  const springY = useSpring(pointerY, { stiffness: 190, damping: 28, mass: 0.75 });
  const rotateY = useTransform(springX, [-0.5, 0.5], [-6, 6]);
  const rotateX = useTransform(springY, [-0.5, 0.5], [5, -5]);
  const sheenX = useTransform(springX, [-0.5, 0.5], ['-14%', '14%']);
  const sheenY = useTransform(springY, [-0.5, 0.5], ['-8%', '8%']);

  // Continuous Apple Keynote scroll-linked depth & fan parallax (compositor-only transform)
  const { scrollYProgress } = useScroll({
    target: stageRef,
    offset: ['start end', 'end start'],
  });
  const smoothScroll = useSpring(scrollYProgress, { stiffness: 140, damping: 26, mass: 0.4 });
  const centerParallaxY = useTransform(smoothScroll, [0, 1], [24, -34]);
  const centerScale = useTransform(smoothScroll, [0, 0.45, 1], [0.97, 1.01, 0.99]);
  const flankParallaxY = useTransform(smoothScroll, [0, 1], [44, -18]);
  const leftFanX = useTransform(smoothScroll, [0, 0.5, 1], [18, 0, -14]);
  const rightFanX = useTransform(smoothScroll, [0, 0.5, 1], [-18, 0, 14]);

  const shots = getScreenshots(theme, appearance);
  const homeShot = shots[0];
  const didacticsShot = shots[2];
  const badgeShot = shots[3];
  const activeThemeMeta = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];
  const versionTag = release?.latestVersion
    ? release.latestVersion.startsWith('v')
      ? release.latestVersion
      : `v${release.latestVersion}`
    : null;

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

  const springReveal = (delay = 0) =>
    reducedMotion
      ? { duration: 0 }
      : { type: 'spring' as const, stiffness: 190, damping: 26, mass: 0.85, delay };

  return (
    <section className="hero-stage">
      <div className="container-shell relative z-2">
        <div className="hero-editorial">
          <motion.a
            href="#changelog"
            className="hero-release-pill"
            initial={reducedMotion ? false : { opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={springReveal(0)}
          >
            <span className="hero-release-dot" aria-hidden="true" />
            <span>Università degli Studi del Molise</span>
            {versionTag && (
              <>
                <span className="hero-release-sep" aria-hidden="true">·</span>
                <span className="hero-release-ver">{versionTag}</span>
              </>
            )}
            <ChevronRight className="w-3.5 h-3.5 opacity-60" aria-hidden="true" />
          </motion.a>

          <h1 className="type-hero hero-headline">
            {reducedMotion ? (
              <>
                La tua università.
                <span className="hero-accent-text block">Finalmente nativa.</span>
              </>
            ) : (
              <>
                <BlurText
                  text="La tua università."
                  animateBy="words"
                  direction="bottom"
                  delay={60}
                  stepDuration={0.34}
                  blur={8}
                  distance={16}
                  trigger="load"
                  className="justify-center"
                />
                <span className="hero-accent-text block">
                  <BlurText
                    text="Finalmente nativa."
                    animateBy="words"
                    direction="bottom"
                    delay={90}
                    stepDuration={0.38}
                    blur={8}
                    distance={16}
                    trigger="load"
                    className="justify-center"
                  />
                </span>
              </>
            )}
          </h1>

          <motion.p
            className="hero-subtitle"
            initial={reducedMotion ? false : { opacity: 0, y: 14 }}
            animate={{ opacity: 1, y: 0 }}
            transition={springReveal(0.12)}
          >
            Libretto, simulatore di laurea, appelli, badge UniPass 3D, presenze QR e navette sempre pronte anche offline. Progettata in Kotlin Multiplatform su Liquid Monet.
          </motion.p>

          <motion.div
            className="hero-cta-row"
            initial={reducedMotion ? false : { opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={springReveal(0.18)}
          >
            <Magnet
              padding={40}
              magnetStrength={5}
              maxOffset={5}
              disabled={reducedMotion}
              wrapperClassName="hero-cta-magnet"
              innerClassName="w-full"
            >
              <a href="#download" className="btn-primary btn-lg w-full">
                <Download className="w-4 h-4" aria-hidden="true" />
                <span>Scarica UniApp</span>
              </a>
            </Magnet>
            <Magnet
              padding={36}
              magnetStrength={6}
              maxOffset={5}
              disabled={reducedMotion}
              wrapperClassName="hero-cta-magnet"
              innerClassName="w-full"
            >
              <a
                href="https://github.com/Anto426-Project/Uniapp"
                target="_blank"
                rel="noopener noreferrer"
                className="btn-material btn-lg w-full"
              >
                <span>Codice su GitHub</span>
                <ChevronRight className="w-4 h-4 opacity-70" aria-hidden="true" />
              </a>
            </Magnet>
          </motion.div>
        </div>

        {/* Sculpted 3-Device Keynote Stage with Continuous Scroll Parallax */}
        <motion.div
          ref={stageRef}
          className="hero-triptych-stage"
          onMouseMove={handleStageMove}
          onMouseLeave={handleStageLeave}
          initial={reducedMotion ? false : { opacity: 0, y: 32, scale: 0.97 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={springReveal(0.2)}
        >
          <div className="hero-triptych">
            <motion.div
              className="hero-triptych-flanks"
              style={reducedMotion ? undefined : { y: flankParallaxY }}
            >
              <motion.div
                className="triptych-flank-carrier"
                style={reducedMotion ? undefined : { x: leftFanX }}
              >
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
              </motion.div>

              <motion.div
                className="triptych-flank-carrier"
                style={reducedMotion ? undefined : { x: rightFanX }}
              >
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
              </motion.div>
            </motion.div>

            <motion.button
              type="button"
              className="triptych-center"
              style={
                reducedMotion
                  ? undefined
                  : { rotateX, rotateY, y: centerParallaxY, scale: centerScale, transformPerspective: 1200 }
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
                    loading="eager"
                    draggable={false}
                  />
                  <motion.div
                    className="device-sheen"
                    aria-hidden="true"
                    style={reducedMotion ? undefined : { x: sheenX, y: sheenY }}
                  />
                </div>
              </div>
            </motion.button>
          </div>

          {/* Liquid Glass Pigment Studio Dock */}
          <div className="hero-control-dock">
            <FluidSegmentedControl
              options={MONET_THEMES}
              value={theme}
              onChange={setTheme}
              ariaLabel="Scegli il tema dell'anteprima"
            />
          </div>
        </motion.div>

        {/* Staggered Swiss-Apple Key Highlights Strip */}
        <dl className="hero-proof-strip" aria-label="Punti chiave di UniApp">
          {HERO_PROOF_ITEMS.map((item, idx) => (
            <motion.div
              key={item.value}
              className="hero-proof-item"
              initial={reducedMotion ? false : { opacity: 0, y: 18 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, amount: 0.3 }}
              transition={springReveal(idx * 0.055)}
            >
              <dt className="hero-proof-title">{item.value}</dt>
              <dd className="hero-proof-desc">{item.label}</dd>
            </motion.div>
          ))}
        </dl>
      </div>
    </section>
  );
}
