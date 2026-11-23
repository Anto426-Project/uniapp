'use client';

import React, { useState, useMemo } from 'react';
import { ScreenshotItem } from '@/data/types';
import { withBasePath } from '@/utils/basePath';
import { LightboxModal } from './LightboxModal';
import { ScreenshotCarousel, CarouselScreenshot } from './ScreenshotCarousel';
import { MONET_THEMES, useThemeControls } from './SiteShell';
import { SCREENSHOTS_BY_THEME, SCREENSHOTS_DATA } from '@/data/default-manifest';

interface ScreenshotsGalleryProps {
  screenshots?: ScreenshotItem[];
}

export const ScreenshotsGallery: React.FC<ScreenshotsGalleryProps> = ({ screenshots = SCREENSHOTS_DATA }) => {
  const { theme: currentTheme, setTheme } = useThemeControls();
  const activeScreenshots = SCREENSHOTS_BY_THEME[currentTheme] || screenshots;

  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [isLightboxOpen, setIsLightboxOpen] = useState(false);

  const carouselItems: CarouselScreenshot[] = useMemo(() => {
    return activeScreenshots.map((item) => ({
      image: withBasePath(`/assets/screenshots/previews/${item.file.replace(/\.[^.]+$/, '.webp')}`),
      alt: item.title || '',
      title: item.title,
      description: item.description,
    }));
  }, [activeScreenshots]);

  return (
    <section id="screenshots" className="gallery-band">
      <div className="container">
        <div className="split-section-header">
          <div className="split-header-copy">
            <span className="eyebrow">Interfaccia</span>
            <h2 className="display-h2">Ogni schermata, curata nei dettagli.</h2>
            <p className="lead-copy">
              Scorri le viste reali di UniApp e tocca un’anteprima per aprirla a schermo intero.
            </p>
          </div>

          <div className="segmented-control" role="group" aria-label="Cambia pigmento schermate">
            {MONET_THEMES.map((item) => {
              const isActive = currentTheme === item.id;
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => setTheme(item.id)}
                  aria-pressed={isActive}
                  className={`segmented-option ${isActive ? 'active' : ''}`}
                >
                  <span className={`segmented-swatch ${item.className}`} aria-hidden="true" />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </div>
        </div>

        <ScreenshotCarousel
          items={carouselItems}
          paused={isLightboxOpen}
          onOpen={(index) => {
            setCurrentIndex(index);
            setIsLightboxOpen(true);
          }}
        />
      </div>

      <LightboxModal
        isOpen={isLightboxOpen && Boolean(activeScreenshots[currentIndex])}
        imageUrl={
          activeScreenshots[currentIndex]
            ? withBasePath(`/assets/screenshots/${activeScreenshots[currentIndex].file}`)
            : ''
        }
        title={activeScreenshots[currentIndex]?.title || ''}
        description={activeScreenshots[currentIndex]?.description || ''}
        currentIndex={currentIndex}
        totalItems={activeScreenshots.length}
        onPrev={() => setCurrentIndex((prev) => (prev > 0 ? prev - 1 : activeScreenshots.length - 1))}
        onNext={() => setCurrentIndex((prev) => (prev < activeScreenshots.length - 1 ? prev + 1 : 0))}
        onClose={() => setIsLightboxOpen(false)}
      />
    </section>
  );
};
