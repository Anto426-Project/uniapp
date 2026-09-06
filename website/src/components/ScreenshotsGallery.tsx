'use client';

import React, { useState, useMemo } from 'react';
import { ScreenshotItem } from '@/data/types';
import { withBasePath } from '@/utils/basePath';
import { LightboxModal } from './LightboxModal';
import DepthCarousel, { DepthCarouselItem } from './reactbits/DepthCarousel';

interface ScreenshotsGalleryProps {
  screenshots: ScreenshotItem[];
}

export const ScreenshotsGallery: React.FC<ScreenshotsGalleryProps> = ({ screenshots }) => {
  const [selectedScreenshot, setSelectedScreenshot] = useState<ScreenshotItem | null>(null);

  const carouselItems: DepthCarouselItem[] = useMemo(() => {
    return screenshots.map((item) => ({
      image: withBasePath(`/assets/screenshots/${item.file}`),
      alt: item.title,
      title: item.title,
      description: item.description,
      raw: item,
    }));
  }, [screenshots]);

  return (
    <section id="screenshots" className="py-12 md:py-20 relative overflow-hidden">
      <div className="container mx-auto px-4">
        {/* Intestazione Sezione */}
        <div className="section-header text-center mb-8 md:mb-12">
          <span className="section-tag">Interfaccia Utente</span>
          <h2 className="section-title">Anteprima Schermate</h2>
          <p className="section-description max-w-2xl mx-auto">
            Esplora l'interfaccia Material 3 Expressive e Liquid Monet di UniApp nel carosello 3D interattivo. Clicca sulla schermata per ingrandirla a tutto schermo.
          </p>
        </div>

        {/* 3D Depth Carousel Container */}
        <div className="relative w-full max-w-6xl mx-auto flex items-center justify-center">
          <div className="relative w-full h-[580px] sm:h-[620px] flex items-center justify-center">
            <DepthCarousel
              items={carouselItems}
              cardWidth={260}
              cardHeight={530}
              radius={24}
              tint="#030712"
              depth={160}
              spread={110}
              tilt={24}
              tiltDirection="both"
              perspective={1200}
              visibleCards={2}
              falloff={0.22}
              blur={4}
              duration={650}
              autoplay
              autoplayDelay={3500}
              loop
              showControls
              showIndicators
              onItemClick={(idx) => setSelectedScreenshot(screenshots[idx])}
            />
          </div>
        </div>
      </div>

      <LightboxModal
        isOpen={Boolean(selectedScreenshot)}
        imageUrl={selectedScreenshot ? withBasePath(`/assets/screenshots/${selectedScreenshot.file}`) : ''}
        title={selectedScreenshot?.title || ''}
        description={selectedScreenshot?.description || ''}
        onClose={() => setSelectedScreenshot(null)}
      />
    </section>
  );
};
