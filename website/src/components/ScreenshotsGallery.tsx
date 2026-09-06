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
        <div className="relative w-full max-w-5xl mx-auto" style={{ height: '580px', position: 'relative' }}>
          <DepthCarousel
            items={carouselItems}
            depth={200}
            spread={90}
            tilt={22}
            tiltDirection="right"
            perspective={1400}
            visibleCards={4}
            falloff={0.2}
            blur={6}
            cardWidth={260}
            cardHeight={520}
            radius={24}
            autoplay
            autoplayDelay={3200}
            loop
            showControls
            showIndicators
            onChange={(idx: number) => setCurrentIndex(idx)}
          />
        </div>

        {/* Info Schermata Attiva */}
        {screenshots[currentIndex] && (
          <div className="text-center mt-6">
            <h3 className="text-lg font-semibold text-white/90">
              {screenshots[currentIndex].title}
            </h3>
            <p className="text-sm text-white/60 max-w-md mx-auto mt-1">
              {screenshots[currentIndex].description}
            </p>
            <button
              type="button"
              onClick={() => setIsLightboxOpen(true)}
              className="mt-3 inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium bg-white/10 hover:bg-white/15 text-white/80 border border-white/10 transition-colors"
            >
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7" />
              </svg>
              Ingrandisci schermata
            </button>
          </div>
        )}
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
