'use client';

import React, { useState, useMemo } from 'react';
import { ScreenshotItem } from '@/data/types';
import { withBasePath } from '@/utils/basePath';
import { LightboxModal } from './LightboxModal';
import { ScreenshotCarousel, CarouselScreenshot } from './ScreenshotCarousel';

interface ScreenshotsGalleryProps {
  screenshots: ScreenshotItem[];
}

export const ScreenshotsGallery: React.FC<ScreenshotsGalleryProps> = ({ screenshots }) => {
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [isLightboxOpen, setIsLightboxOpen] = useState(false);

  const carouselItems: CarouselScreenshot[] = useMemo(() => {
    return screenshots.map((item) => ({
      image: withBasePath(`/assets/screenshots/previews/${item.file.replace(/\.[^.]+$/, '.webp')}`),
      alt: item.title || '',
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
            Scorri le schermate di UniApp e tocca un’immagine per ingrandirla.
          </p>
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
        isOpen={isLightboxOpen && Boolean(screenshots[currentIndex])}
        imageUrl={screenshots[currentIndex] ? withBasePath(`/assets/screenshots/${screenshots[currentIndex].file}`) : ''}
        title={screenshots[currentIndex]?.title || ''}
        description={screenshots[currentIndex]?.description || ''}
        onClose={() => setIsLightboxOpen(false)}
      />
    </section>
  );
};
