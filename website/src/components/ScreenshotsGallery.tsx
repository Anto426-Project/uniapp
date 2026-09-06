'use client';

import React, { useState, useMemo } from 'react';
import { ScreenshotItem } from '@/data/types';
import { withBasePath } from '@/utils/basePath';
import { LightboxModal } from './LightboxModal';
// @ts-ignore
import DepthCarousel from './DepthCarousel';

interface DepthCarouselItem {
  image: string;
  alt: string;
  title?: string;
  description?: string;
  raw?: ScreenshotItem;
}

interface ScreenshotsGalleryProps {
  screenshots: ScreenshotItem[];
}

export const ScreenshotsGallery: React.FC<ScreenshotsGalleryProps> = ({ screenshots }) => {
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [isLightboxOpen, setIsLightboxOpen] = useState(false);

  const carouselItems: DepthCarouselItem[] = useMemo(() => {
    return screenshots.map((item) => ({
      image: withBasePath(`/assets/screenshots/${item.file}`),
      alt: item.title || '',
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
        <div
          className="relative w-full max-w-5xl mx-auto flex items-center justify-center"
          style={{ height: '560px', position: 'relative', margin: '0 auto' }}
        >
          <DepthCarousel
            items={carouselItems}
            depth={180}
            spread={100}
            tilt={22}
            tiltDirection="center"
            perspective={1400}
            visibleCards={2.5}
            falloff={0.2}
            blur={4}
            autoplay={true}
            autoplayDelay={3200}
            loop={true}
            cardWidth={260}
            cardHeight={480}
            radius={22}
            tint="#05060a"
            duration={700}
            ease="power3.out"
            showControls={false}
            showIndicators={false}
            onChange={(idx: number) => setCurrentIndex(idx)}
            onItemClick={() => setIsLightboxOpen(true)}
          />
        </div>
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
