'use client';

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { ExternalLink, Menu, Moon, Sun, X } from 'lucide-react';
import { MONET_THEMES } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import {
  SPRING_PILL,
  SPRING_SHEET,
  SpringDriver,
  VelocityTracker,
  clamp,
  hapticSnap,
  project,
  rubberband,
} from '@/lib/fluid-physics';
import { useSite } from './SiteShell';

const NAV_ITEMS = [
  { id: 'features', href: '#features', label: 'Funzionalità' },
  { id: 'screenshots', href: '#screenshots', label: 'Schermate' },
  { id: 'architecture', href: '#architecture', label: 'Architettura' },
  { id: 'download', href: '#download', label: 'Download' },
] as const;

export function Navbar() {
  const { theme, setTheme, appearance, toggleAppearance, reducedMotion, setModalDepth } = useSite();

  const [scrolled, setScrolled] = useState(false);
  const [activeSection, setActiveSection] = useState<string>('');
  const [sheetMounted, setSheetMounted] = useState(false);

  const pillRef = useRef<HTMLDivElement>(null);
  const linkRefs = useRef<Record<string, HTMLAnchorElement | null>>({});
  const pillSpringsRef = useRef<{ x: SpringDriver; y: SpringDriver; w: SpringDriver; h: SpringDriver } | null>(null);

  const sheetRef = useRef<HTMLDivElement>(null);
  const scrimRef = useRef<HTMLDivElement>(null);
  const sheetSpringRef = useRef<SpringDriver | null>(null);
  const trackerRef = useRef(new VelocityTracker(95));
  const dragRef = useRef({ active: false, pointerId: -1, startY: 0, startOffset: 0 });

  const applyPill = useCallback(() => {
    const pill = pillRef.current;
    const springs = pillSpringsRef.current;
    if (!pill || !springs) return;
    pill.style.transform = `translate3d(${springs.x.value.toFixed(2)}px, ${springs.y.value.toFixed(2)}px, 0)`;
    pill.style.width = `${springs.w.value.toFixed(2)}px`;
    pill.style.height = `${springs.h.value.toFixed(2)}px`;
  }, []);

  useEffect(() => {
    const x = new SpringDriver(0, applyPill, SPRING_PILL, 0.08);
    const y = new SpringDriver(0, applyPill, SPRING_PILL, 0.08);
    const w = new SpringDriver(0, applyPill, SPRING_PILL, 0.08);
    const h = new SpringDriver(0, applyPill, SPRING_PILL, 0.08);
    pillSpringsRef.current = { x, y, w, h };
    return () => {
      x.destroy();
      y.destroy();
      w.destroy();
      h.destroy();
    };
  }, [applyPill]);

  useEffect(() => {
    const pill = pillRef.current;
    const springs = pillSpringsRef.current;
    if (!pill || !springs) return;
    const activeLink = activeSection ? linkRefs.current[activeSection] : null;
    if (!activeLink) {
      pill.style.opacity = '0';
      return;
    }
    pill.style.opacity = '1';
    springs.x.animateTo(activeLink.offsetLeft, { config: SPRING_PILL });
    springs.y.animateTo(activeLink.offsetTop, { config: SPRING_PILL });
    springs.w.animateTo(activeLink.offsetWidth, { config: SPRING_PILL });
    springs.h.animateTo(activeLink.offsetHeight, { config: SPRING_PILL });
  }, [activeSection]);

  useEffect(() => {
    const onScroll = () => {
      setScrolled(window.scrollY > 18);
      const probe = window.scrollY + window.innerHeight * 0.32;
      let current = '';
      for (const item of NAV_ITEMS) {
        const section = document.getElementById(item.id);
        if (section && section.offsetTop <= probe) {
          current = item.id;
        }
      }
      setActiveSection(current);
    };
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  const applySheetTransform = useCallback(
    (offsetY: number) => {
      const sheet = sheetRef.current;
      const scrim = scrimRef.current;
      const sheetHeight = sheet?.offsetHeight || 420;
      const progress = clamp(1 - offsetY / sheetHeight, 0, 1);

      if (sheet) {
        sheet.style.transform = reducedMotion
          ? 'translate3d(0, 0, 0)'
          : `translate3d(0, ${offsetY.toFixed(2)}px, 0)`;
        sheet.style.opacity = reducedMotion ? progress.toFixed(3) : '1';
      }
      if (scrim) scrim.style.opacity = progress.toFixed(3);
      setModalDepth(progress);
    },
    [reducedMotion, setModalDepth]
  );

  const closeSheet = useCallback(
    (velocity = 0) => {
      const spring = sheetSpringRef.current;
      const height = sheetRef.current?.offsetHeight || 440;
      if (!spring || reducedMotion) {
        setSheetMounted(false);
        setModalDepth(0);
        return;
      }
      spring.animateTo(height + 40, {
        velocity,
        config: { damping: 1.0, response: 0.28 },
        onRest: () => {
          setSheetMounted(false);
          setModalDepth(0);
        },
      });
    },
    [reducedMotion, setModalDepth]
  );

  useEffect(() => {
    if (!sheetMounted) return;
    document.body.style.overflow = 'hidden';

    const height = sheetRef.current?.offsetHeight || 440;
    const spring = new SpringDriver(
      reducedMotion ? 0 : height + 24,
      (val) => applySheetTransform(val),
      SPRING_SHEET,
      0.25
    );
    sheetSpringRef.current = spring;
    applySheetTransform(reducedMotion ? 0 : height + 24);
    spring.animateTo(0, { config: { damping: 1.0, response: 0.34 } });

    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') closeSheet();
    };
    const onResize = () => {
      if (window.innerWidth > 960) closeSheet();
    };
    window.addEventListener('keydown', onKeyDown);
    window.addEventListener('resize', onResize);

    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', onKeyDown);
      window.removeEventListener('resize', onResize);
      spring.destroy();
      sheetSpringRef.current = null;
      setModalDepth(0);
    };
  }, [sheetMounted, reducedMotion, applySheetTransform, closeSheet, setModalDepth]);

  const onSheetPointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    if ((e.target as HTMLElement).closest('a, button')) return;
    const spring = sheetSpringRef.current;
    if (!spring) return;
    const liveOffset = spring.value;
    spring.stop();
    e.currentTarget.setPointerCapture(e.pointerId);
    dragRef.current = { active: true, pointerId: e.pointerId, startY: e.clientY, startOffset: liveOffset };
    trackerRef.current.reset(e.clientX, e.clientY);
  };

  const onSheetPointerMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!dragRef.current.active || dragRef.current.pointerId !== e.pointerId) return;
    const spring = sheetSpringRef.current;
    if (!spring) return;
    trackerRef.current.add(e.clientX, e.clientY);
    const rawOffset = dragRef.current.startOffset + (e.clientY - dragRef.current.startY);
    spring.setImmediate(rawOffset < 0 ? rubberband(rawOffset, 220, 0.55) : rawOffset);
  };

  const onSheetPointerEnd = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!dragRef.current.active || dragRef.current.pointerId !== e.pointerId) return;
    dragRef.current.active = false;
    const spring = sheetSpringRef.current;
    if (!spring) return;
    const { vy } = trackerRef.current.getVelocity();
    const height = sheetRef.current?.offsetHeight || 400;
    if (spring.value + project(vy, 0.992) > height * 0.34 || spring.value > height * 0.42) {
      hapticSnap(6);
      closeSheet(Math.max(vy, 200));
    } else {
      spring.animateTo(0, { velocity: vy, config: SPRING_SHEET });
    }
  };

  return (
    <>
      <header className="nav-island-shell" data-scrolled={scrolled}>
        <nav className="nav-island" aria-label="Navigazione principale">
          <a href="#" className="nav-brand">
            <img
              src={withBasePath('/assets/uniapp-icon.webp')}
              alt="UniApp"
              width={26}
              height={26}
              className="nav-brand-icon"
            />
            <span className="nav-brand-title">UniApp</span>
          </a>

          <div className="nav-links-track">
            <div ref={pillRef} className="nav-active-pill" style={{ opacity: 0 }} aria-hidden="true" />
            {NAV_ITEMS.map((item) => (
              <a
                key={item.id}
                ref={(el) => {
                  linkRefs.current[item.id] = el;
                }}
                href={item.href}
                className="nav-link-item"
                data-active={activeSection === item.id}
              >
                {item.label}
              </a>
            ))}
          </div>

          <div className="nav-right">
            <button
              type="button"
              className="appearance-toggle"
              onClick={toggleAppearance}
              aria-label={appearance === 'dark' ? 'Attiva aspetto chiaro' : 'Attiva aspetto scuro'}
              title={appearance === 'dark' ? 'Aspetto chiaro' : 'Aspetto scuro'}
            >
              {appearance === 'dark' ? (
                <Sun className="w-3.5 h-3.5" aria-hidden="true" />
              ) : (
                <Moon className="w-3.5 h-3.5" aria-hidden="true" />
              )}
            </button>

            <a href="#download" className="nav-download-btn">
              Scarica
            </a>

            <button
              type="button"
              className="nav-mobile-trigger"
              onClick={sheetMounted ? () => closeSheet() : () => { hapticSnap(6); setSheetMounted(true); }}
              aria-label={sheetMounted ? 'Chiudi menu' : 'Apri menu'}
              aria-expanded={sheetMounted}
            >
              {sheetMounted ? (
                <X className="w-4 h-4" aria-hidden="true" />
              ) : (
                <Menu className="w-4 h-4" aria-hidden="true" />
              )}
            </button>
          </div>
        </nav>
      </header>

      {sheetMounted && (
        <>
          <div ref={scrimRef} className="modal-scrim" onClick={() => closeSheet()} aria-hidden="true" />
          <div
            ref={sheetRef}
            className="sheet-modal-panel"
            role="dialog"
            aria-modal="true"
            aria-label="Menu di navigazione"
            onPointerDown={onSheetPointerDown}
            onPointerMove={onSheetPointerMove}
            onPointerUp={onSheetPointerEnd}
            onPointerCancel={onSheetPointerEnd}
          >
            <div className="sheet-grabber" aria-hidden="true" />
            <div className="flex items-center justify-between mb-3">
              <span className="type-caption" style={{ textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                Sezioni
              </span>
              <button
                type="button"
                className="appearance-toggle"
                onClick={() => closeSheet()}
                aria-label="Chiudi menu"
              >
                <X className="w-4 h-4" aria-hidden="true" />
              </button>
            </div>

            <div className="flex flex-col">
              {NAV_ITEMS.map((item) => (
                <a
                  key={item.id}
                  href={item.href}
                  onClick={() => closeSheet()}
                  className="flex items-center justify-between py-3.5 text-lg font-semibold"
                  style={{ borderBottom: '1px solid var(--separator)' }}
                >
                  <span>{item.label}</span>
                  <span style={{ color: 'var(--label-tertiary)', fontSize: '0.85rem' }}>→</span>
                </a>
              ))}
              <a
                href="https://github.com/Anto426-Project/Uniapp"
                target="_blank"
                rel="noopener noreferrer"
                onClick={() => closeSheet()}
                className="flex items-center justify-between py-3.5 text-lg font-semibold"
              >
                <span>Codice su GitHub</span>
                <ExternalLink className="w-4 h-4 opacity-60" aria-hidden="true" />
              </a>
            </div>

            <div
              className="flex items-center justify-between gap-3 pt-4 mt-2"
              style={{ borderTop: '1px solid var(--separator)' }}
            >
              <span className="type-caption">Colore del tema</span>
              <div className="swatch-cluster" role="group" aria-label="Scegli il tema dal menu">
                {MONET_THEMES.map((t) => (
                  <button
                    key={t.id}
                    type="button"
                    className="swatch-dot"
                    style={{ backgroundColor: t.swatchHex }}
                    aria-label={`Tema ${t.label}`}
                    aria-pressed={theme === t.id}
                    onClick={() => setTheme(t.id)}
                  />
                ))}
              </div>
            </div>
          </div>
        </>
      )}
    </>
  );
}
