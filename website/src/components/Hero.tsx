'use client';

import React, { useEffect, useRef } from 'react';
import { ChevronRight, Download } from 'lucide-react';
import { MONET_THEMES, getScreenshots, previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import {
  SPRING_ROTATION,
  Spring2D,
  VelocityTracker,
  hapticSnap,
  rubberband,
} from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { FluidSegmentedControl } from './FluidSegmentedControl';
import { CrossfadeImage } from './CrossfadeImage';

export function Hero() {
  const { theme, setTheme, appearance, release, reducedMotion, openLightbox } = useSite();

  const centerDeviceRef = useRef<HTMLDivElement>(null);
  const sheenRef = useRef<HTMLDivElement>(null);
  const springRef = useRef<Spring2D | null>(null);
  const trackerRef = useRef(new VelocityTracker(90));
  const dragRef = useRef({
    active: false,
    pointerId: -1,
    startX: 0,
    startY: 0,
    moved: false,
  });

  useEffect(() => {
    const spring = new Spring2D(
      0,
      0,
      (x, y) => {
        const el = centerDeviceRef.current;
        if (!el) return;
        const rotY = x * 0.16;
        const rotX = -y * 0.14;
        el.style.transform = `translate3d(${x.toFixed(2)}px, ${y.toFixed(2)}px, 0) rotateX(${rotX.toFixed(2)}deg) rotateY(${rotY.toFixed(2)}deg)`;
        if (sheenRef.current) {
          const angle = 130 + x * 0.45;
          sheenRef.current.style.background = `linear-gradient(${angle.toFixed(1)}deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0) 40%)`;
        }
      },
      SPRING_ROTATION,
      0.08
    );
    springRef.current = spring;
    return () => {
      spring.destroy();
      springRef.current = null;
    };
  }, []);

  const shots = getScreenshots(theme, appearance);
  const homeShot = shots[0];
  const didacticsShot = shots[2];
  const badgeShot = shots[3];
  const activeThemeMeta = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];

  const versionLabel = release?.latestVersion
    ? release.latestVersion.startsWith('v')
      ? release.latestVersion
      : `v${release.latestVersion}`
    : null;

  const description =
    release?.description ||
    "Libretto, simulatore laurea, appelli, presenze QR, badge UniPass e navette dell'Università degli Studi del Molise. Costruita in Kotlin Multiplatform sul motore grafico Liquid Monet, gratuita e senza tracciamento.";

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

  const onCenterPointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    if (e.button !== 0 && e.pointerType === 'mouse') return;
    springRef.current?.stop();
    e.currentTarget.setPointerCapture(e.pointerId);
    dragRef.current = {
      active: true,
      pointerId: e.pointerId,
      startX: e.clientX,
      startY: e.clientY,
      moved: false,
    };
    trackerRef.current.reset(e.clientX, e.clientY);
  };

  const onCenterPointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!dragRef.current.active || dragRef.current.pointerId !== e.pointerId) return;
    const dx = e.clientX - dragRef.current.startX;
    const dy = e.clientY - dragRef.current.startY;
    if (!dragRef.current.moved && Math.hypot(dx, dy) > 10) {
      dragRef.current.moved = true;
    }
    if (!reducedMotion && dragRef.current.moved) {
      trackerRef.current.add(e.clientX, e.clientY);
      springRef.current?.setImmediate(rubberband(dx, 135, 0.45), rubberband(dy, 135, 0.45));
    }
  };

  const onCenterPointerEnd = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!dragRef.current.active || dragRef.current.pointerId !== e.pointerId) return;
    const wasTap = !dragRef.current.moved;
    dragRef.current.active = false;

    if (wasTap) {
      launchFromElement(0, e.currentTarget);
      return;
    }

    const { vx, vy } = trackerRef.current.getVelocity();
    springRef.current?.animateTo(0, 0, {
      vx: vx * 0.28,
      vy: vy * 0.28,
      config: SPRING_ROTATION,
    });
  };

  return (
    <section className="hero-keynote">
      <div className="container-shell">
        <div className="hero-keynote-header">
          <a href="#download" className="hero-status-chip">
            <span className="status-dot" aria-hidden="true" />
            <span>
              {versionLabel ? `UniApp ${versionLabel} · ` : ''}
              Android 10+ e iOS · Open source
            </span>
            <ChevronRight className="w-3.5 h-3.5" style={{ color: 'var(--accent)' }} aria-hidden="true" />
          </a>

          <h1 className="type-hero">
            La tua università,{' '}
            <span className="hero-accent-text">finalmente nativa.</span>
          </h1>

          <p className="hero-subtitle">{description}</p>

          <div className="hero-cta-row">
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
              <span>Esplora il codice</span>
              <ChevronRight className="w-4 h-4 opacity-70" aria-hidden="true" />
            </a>
          </div>
        </div>

        {/* Centered 3-Device Keynote Fan Triptych */}
        <div className="hero-triptych-stage">
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

            <div
              ref={centerDeviceRef}
              className="triptych-center"
              role="button"
              tabIndex={0}
              aria-label={`Apri anteprima ${homeShot.title} (${activeThemeMeta.label})`}
              onPointerDown={onCenterPointerDown}
              onPointerMove={onCenterPointerMove}
              onPointerUp={onCenterPointerEnd}
              onPointerCancel={onCenterPointerEnd}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  launchFromElement(0, e.currentTarget);
                }
              }}
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
                  <div ref={sheenRef} className="device-sheen" aria-hidden="true" />
                </div>
              </div>
            </div>

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
            <span className="type-caption">
              Liquid Monet · <strong style={{ color: 'var(--label-primary)' }}>{activeThemeMeta.label}</strong>
            </span>
          </div>
        </div>
      </div>
    </section>
  );
}
