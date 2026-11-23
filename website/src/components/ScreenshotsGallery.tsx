'use client';

import React from 'react';
import { MONET_THEMES, getScreenshots } from '@/data/screenshots';
import { useSite } from './SiteShell';
import { FluidSegmentedControl } from './FluidSegmentedControl';
import { FluidCarousel } from './FluidCarousel';
import { FluidReveal } from './FluidReveal';

export function ScreenshotsGallery() {
  const { theme, setTheme, appearance, reducedMotion, isLightboxOpen, openLightbox } = useSite();
  const screenshots = getScreenshots(theme, appearance);

  return (
    <section id="screenshots" className="section-block">
      <div className="container-shell">
        <FluidReveal>
          <div className="section-header-center">
            <span className="type-eyebrow">Design Liquid Monet</span>
            <h2 className="type-title-1">Vetro ottico, luce e colore che seguono il tuo stile.</h2>
            <p className="type-body">
              Motore dinamico Material You, quattro preset curati e Color Lab su misura, con sfondi animati Orbital Pulse, Aurora, Mesh Glow e Radiant Beam.
            </p>
            <div className="mt-2">
              <FluidSegmentedControl
                options={MONET_THEMES}
                value={theme}
                onChange={setTheme}
                ariaLabel="Scegli il tema delle schermate"
              />
            </div>
          </div>
        </FluidReveal>

        <FluidReveal delayMs={70}>
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
