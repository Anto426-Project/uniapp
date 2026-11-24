'use client';

import React, { useEffect, useState } from 'react';
import { usePathname } from 'next/navigation';
import { AnimatePresence, motion } from 'motion/react';
import { Download, Menu, Moon, Sun, X } from 'lucide-react';
import { MONET_THEMES } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { FluidSegmentedControl } from './FluidSegmentedControl';

interface NavSection {
  id: string;
  href: string;
  label: string;
}

const SECTIONS: readonly NavSection[] = [
  { id: 'features', href: '#features', label: 'Funzionalità' },
  { id: 'screenshots', href: '#screenshots', label: 'Schermate' },
  { id: 'architecture', href: '#architecture', label: 'Architettura' },
  { id: 'download', href: '#download', label: 'Download' },
];

export function Navbar() {
  const { theme, setTheme, appearance, toggleAppearance, reducedMotion } = useSite();
  const pathname = usePathname() || '/';
  const isIosPage = pathname === '/ios' || pathname.startsWith('/ios/');
  const homePrefix = isIosPage ? withBasePath('/') : '';
  const [activeId, setActiveId] = useState<string>('');
  const [scrolled, setScrolled] = useState(false);
  const [sheetOpen, setSheetOpen] = useState(false);

  useEffect(() => {
    if (isIosPage) {
      setActiveId('download');
      const onScroll = () => setScrolled(window.scrollY > 20);
      onScroll();
      window.addEventListener('scroll', onScroll, { passive: true });
      return () => window.removeEventListener('scroll', onScroll);
    }

    const updateActive = () => {
      setScrolled(window.scrollY > 20);
      const y = window.scrollY + 180;
      let current = '';
      for (const s of SECTIONS) {
        const el = document.getElementById(s.id);
        if (el && el.offsetTop <= y) {
          current = s.id;
        }
      }
      setActiveId(current);
    };

    updateActive();
    window.addEventListener('scroll', updateActive, { passive: true });
    return () => window.removeEventListener('scroll', updateActive);
  }, [isIosPage]);

  useEffect(() => {
    if (!sheetOpen) return;
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setSheetOpen(false);
    };
    window.addEventListener('keydown', onKey);
    return () => {
      document.body.style.overflow = prevOverflow;
      window.removeEventListener('keydown', onKey);
    };
  }, [sheetOpen]);

  return (
    <>
      <header className="nav-island-wrap">
        <nav
          className="nav-island"
          data-scrolled={scrolled}
          aria-label="Navigazione principale"
        >
          <a href={isIosPage ? withBasePath('/') : '#'} className="nav-brand">
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
            {SECTIONS.map((s) => {
              const isActive = activeId === s.id;
              return (
                <a
                  key={s.id}
                  href={`${homePrefix}${s.href}`}
                  className="nav-link-item"
                  aria-current={isActive ? 'location' : undefined}
                >
                  {isActive && (
                    <motion.span
                      layoutId="navbar-active-pill"
                      className="nav-active-pill"
                      aria-hidden="true"
                      transition={
                        reducedMotion
                          ? { duration: 0.12 }
                          : { type: 'spring', bounce: 0.14, duration: 0.4 }
                      }
                    />
                  )}
                  <span className="relative z-1">{s.label}</span>
                </a>
              );
            })}
          </div>

          <div className="nav-actions">
            <button
              type="button"
              className="appearance-toggle"
              onClick={() => {
                hapticSnap(5);
                toggleAppearance();
              }}
              aria-label={appearance === 'dark' ? 'Attiva aspetto chiaro' : 'Attiva aspetto scuro'}
              title={appearance === 'dark' ? 'Passa a tema chiaro' : 'Passa a tema scuro'}
            >
              {appearance === 'dark' ? (
                <Sun className="w-4 h-4" aria-hidden="true" />
              ) : (
                <Moon className="w-4 h-4" aria-hidden="true" />
              )}
            </button>

            <a href={`${homePrefix}#download`} className="nav-download-cta">
              Scarica
            </a>

            <button
              type="button"
              className="nav-mobile-trigger"
              aria-label={sheetOpen ? 'Chiudi menu' : 'Apri menu di navigazione'}
              aria-expanded={sheetOpen}
              onClick={() => {
                hapticSnap(5);
                setSheetOpen((prev) => !prev);
              }}
            >
              {sheetOpen ? (
                <X className="w-4 h-4" aria-hidden="true" />
              ) : (
                <Menu className="w-4 h-4" aria-hidden="true" />
              )}
            </button>
          </div>
        </nav>
      </header>

      <AnimatePresence>
        {sheetOpen && (
          <>
            <motion.div
              className="modal-scrim"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.2 }}
              onClick={() => setSheetOpen(false)}
              aria-hidden="true"
            />
            <div
              className="sheet-modal-layer"
              role="dialog"
              aria-modal="true"
              aria-label="Menu di navigazione"
              onClick={(e) => {
                if (e.target === e.currentTarget) setSheetOpen(false);
              }}
            >
              <motion.div
                className="sheet-modal-panel"
                initial={reducedMotion ? { opacity: 0 } : { y: '100%' }}
                animate={reducedMotion ? { opacity: 1 } : { y: 0 }}
                exit={reducedMotion ? { opacity: 0 } : { y: '100%' }}
                transition={
                  reducedMotion
                    ? { duration: 0.16 }
                    : { type: 'spring', bounce: 0.08, duration: 0.38 }
                }
                drag={reducedMotion ? false : 'y'}
                dragConstraints={{ top: 0, bottom: 0 }}
                dragElastic={{ top: 0.08, bottom: 0.6 }}
                onDragEnd={(_, info) => {
                  if (info.offset.y > 90 || info.velocity.y > 420) {
                    hapticSnap(5);
                    setSheetOpen(false);
                  }
                }}
              >
                <div className="sheet-grabber-hit" aria-hidden="true">
                  <span className="sheet-grabber-bar" />
                </div>

                <div className="sheet-nav-list">
                  {SECTIONS.map((s) => (
                    <a
                      key={s.id}
                      href={`${homePrefix}${s.href}`}
                      className="sheet-nav-link"
                      aria-current={activeId === s.id ? 'location' : undefined}
                      onClick={() => setSheetOpen(false)}
                    >
                      <span>{s.label}</span>
                    </a>
                  ))}
                </div>

                <div className="mt-4 pt-4" style={{ borderTop: '1px solid var(--separator)' }}>
                  <FluidSegmentedControl
                    options={MONET_THEMES}
                    value={theme}
                    onChange={setTheme}
                    ariaLabel="Scegli il tema dal menu"
                    compact
                  />
                </div>

                <div className="mt-4">
                  <a
                    href={`${homePrefix}#download`}
                    className="btn-primary w-full"
                    onClick={() => setSheetOpen(false)}
                  >
                    <Download className="w-4 h-4" aria-hidden="true" />
                    <span>Scarica UniApp</span>
                  </a>
                </div>
              </motion.div>
            </div>
          </>
        )}
      </AnimatePresence>
    </>
  );
}
