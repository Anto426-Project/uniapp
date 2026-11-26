'use client';

import React from 'react';
import { Moon, Sun } from 'lucide-react';
import { MONET_THEMES, getScreenshots } from '@/data/screenshots';
import { hapticSnap } from '@/lib/fluid-physics';
import { useSite } from './SiteShell';
import { FluidSegmentedControl } from './FluidSegmentedControl';
import { FluidCarousel } from './FluidCarousel';
import { FluidReveal } from './FluidReveal';

export function ScreenshotsGallery() {
  const { theme, setTheme, appearance, toggleAppearance, reducedMotion, isLightboxOpen, openLightbox } = useSite();
  const screenshots = getScreenshots(theme, appearance);

  return (
    <section id="screenshots" className="section-block">
      <div className="container-shell">
        <FluidReveal>
          <div className="section-header-center">
            <span className="type-eyebrow">Design Liquid Monet</span>
            <h2 className="type-title-1">Vetro ottico, luce e colore su misura.</h2>
            <p className="type-body section-center-sub">
              Motore dinamico Material You, quattro preset curati e Color Lab personalizzabile con sfondi Orbital Pulse, Aurora, Mesh Glow e Radiant Beam.
            </p>
            <div className="gallery-controls-row">
              <FluidSegmentedControl
                options={MONET_THEMES}
                value={theme}
                onChange={setTheme}
                ariaLabel="Scegli il tema delle schermate"
              />
              <button
                type="button"
                className="dock-appearance-pill"
                onClick={() => {
                  hapticSnap(4);
                  toggleAppearance();
                }}
                aria-label={
                  appearance === 'dark'
                    ? 'Passa alle schermate in tema chiaro'
                    : 'Passa alle schermate in tema scuro'
                }
                aria-pressed={appearance === 'light'}
              >
                {appearance === 'dark' ? (
                  <>
                    <Sun className="w-3.5 h-3.5" aria-hidden="true" />
                    <span>Chiaro</span>
                  </>
                ) : (
                  <>
                    <Moon className="w-3.5 h-3.5" aria-hidden="true" />
                    <span>Scuro</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </FluidReveal>

        <FluidReveal delayMs={70} materialize>
          <FluidCarousel
            items={screenshots}
            paused={isLightboxOpen}
            reducedMotion={reducedMotion}
            onOpen={(index, originRect) => openLightbox(index, originRect)}
          />
        </FluidReveal>
      </div>
    </section>
  );
}
