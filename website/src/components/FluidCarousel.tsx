'use client';

import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import useEmblaCarousel from 'embla-carousel-react';
import Autoplay from 'embla-carousel-autoplay';
import { ChevronLeft, ChevronRight, Maximize2 } from 'lucide-react';
import type { OriginRect, ScreenshotItem } from '@/data/types';
import { previewPathFor } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { clamp, hapticSnap } from '@/lib/fluid-physics';
import { CrossfadeImage } from './CrossfadeImage';

interface FluidCarouselProps {
  items: ScreenshotItem[];
  paused: boolean;
  reducedMotion: boolean;
  onOpen: (index: number, originRect: OriginRect) => void;
}

/**
 * Apple App Store / iOS Studio Showcase powered by Embla Carousel:
 * - True physical infinite loop (`embla-carousel-react`) with exponential momentum deceleration
 * - Flat, distortion-free titanium frames with smooth proximity scale & opacity
 * - Minimal bottom iOS page-dot indicator (zero bulky top text bar)
 */
export function FluidCarousel({
  items,
  paused,
  reducedMotion,
  onOpen,
}: FluidCarouselProps) {
  const [selected, setSelected] = useState(0);
  const [isInViewport, setIsInViewport] = useState(false);
  const carouselRef = useRef<HTMLDivElement | null>(null);
  const innerRefs = useRef<(HTMLDivElement | null)[]>([]);

  const plugins = useMemo(() => {
    if (reducedMotion) return [];
    return [
      Autoplay({
        delay: 4600,
        playOnInit: false,
        stopOnInteraction: true,
        stopOnMouseEnter: true,
      }),
    ];
  }, [reducedMotion]);

  const [emblaRef, emblaApi] = useEmblaCarousel(
    {
      loop: true,
      align: 'center',
      skipSnaps: false,
      duration: reducedMotion ? 0 : 24,
    },
    plugins
  );

  // Keep autoplay limited to the visible carousel and pause it for the lightbox.
  useEffect(() => {
    const carousel = carouselRef.current;
    if (!carousel) return;

    const observer = new IntersectionObserver(
      ([entry]) => setIsInViewport(entry.isIntersecting),
      { threshold: 0.2 }
    );
    observer.observe(carousel);

    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!emblaApi || reducedMotion) return;
    const autoplay = emblaApi.plugins()?.autoplay;
    if (!autoplay) return;

    if (paused || !isInViewport) {
      autoplay.stop();
    } else {
      autoplay.play();
    }
  }, [emblaApi, isInViewport, paused, reducedMotion]);

  const applySlideStyles = useCallback(() => {
    if (!emblaApi) return;
    const engine = emblaApi.internalEngine();
    const scrollProgress = emblaApi.scrollProgress();
    const snaps = emblaApi.scrollSnapList();

    snaps.forEach((snap, snapIndex) => {
      let diff = snap - scrollProgress;

      if (engine.options.loop) {
        engine.slideLooper.loopPoints.forEach((loopItem) => {
          const target = loopItem.target();
          if (snapIndex === loopItem.index && target !== 0) {
            const sign = Math.sign(target);
            if (sign === -1) diff = snap - (1 + scrollProgress);
            if (sign === 1) diff = snap + (1 - scrollProgress);
          }
        });
      }

      const unitDist = Math.abs(diff) * Math.max(snaps.length, 1);
      const scale = reducedMotion ? 1 : clamp(1 - unitDist * 0.085, 0.88, 1);
      const opacity = clamp(1 - unitDist * 0.42, 0.32, 1);

      const node = innerRefs.current[snapIndex];
      if (node) {
        node.style.transform = `scale3d(${scale.toFixed(4)}, ${scale.toFixed(4)}, 1)`;
        node.style.opacity = opacity.toFixed(3);
      }
    });
  }, [emblaApi, reducedMotion]);

  const onSelect = useCallback(() => {
    if (!emblaApi) return;
    setSelected(emblaApi.selectedScrollSnap());
  }, [emblaApi]);

  useEffect(() => {
    if (!emblaApi) return;
    onSelect();
    applySlideStyles();

    emblaApi.on('select', onSelect);
    emblaApi.on('scroll', applySlideStyles);
    emblaApi.on('reInit', onSelect);
    emblaApi.on('reInit', applySlideStyles);

    return () => {
      emblaApi.off('select', onSelect);
      emblaApi.off('scroll', applySlideStyles);
      emblaApi.off('reInit', onSelect);
      emblaApi.off('reInit', applySlideStyles);
    };
  }, [emblaApi, onSelect, applySlideStyles]);

  const count = items.length;
  if (!count) return null;
  const safeSelected = selected < count ? selected : 0;
  const activeItem = items[safeSelected] || items[0];

  const stepPrev = () => {
    if (!emblaApi) return;
    hapticSnap(4);
    emblaApi.plugins()?.autoplay?.stop();
    emblaApi.scrollPrev();
  };

  const stepNext = () => {
    if (!emblaApi) return;
    hapticSnap(4);
    emblaApi.plugins()?.autoplay?.stop();
    emblaApi.scrollNext();
  };

  const jumpTo = (index: number) => {
    if (!emblaApi) return;
    hapticSnap(4);
    emblaApi.plugins()?.autoplay?.stop();
    emblaApi.scrollTo(index);
  };

  return (
    <div
      ref={carouselRef}
      className="screenshot-carousel"
      role="region"
      aria-roledescription="carosello"
      aria-label="Schermate di UniApp"
      onKeyDown={(e) => {
        if (e.key === 'ArrowLeft') {
          e.preventDefault();
          stepPrev();
        } else if (e.key === 'ArrowRight') {
          e.preventDefault();
          stepNext();
        }
      }}
    >
      {/* Embla Horizontal Studio Rail */}
      <div className="screenshot-carousel__shell">
        <div className="screenshot-carousel__halo" aria-hidden="true" />

        {count > 1 && (
          <>
            <button
              type="button"
              className="carousel-edge-btn carousel-edge-btn--left"
              aria-label="Schermata precedente"
              onClick={stepPrev}
            >
              <ChevronLeft className="w-5 h-5" aria-hidden="true" />
            </button>
            <button
              type="button"
              className="carousel-edge-btn carousel-edge-btn--right"
              aria-label="Schermata successiva"
              onClick={stepNext}
            >
              <ChevronRight className="w-5 h-5" aria-hidden="true" />
            </button>
          </>
        )}

        <div ref={emblaRef} className="screenshot-carousel__viewport">
          <div className="screenshot-carousel__track">
            {items.map((item, index) => {
              const isSelected = index === safeSelected;
              return (
                <div
                  key={item.id}
                  className="screenshot-carousel__slide"
                  role="group"
                  aria-roledescription="schermata"
                  aria-label={`${index + 1} di ${count}`}
                  data-selected={isSelected}
                >
                  <div
                    ref={(el) => {
                      innerRefs.current[index] = el;
                    }}
                    className="screenshot-carousel__slide-inner"
                  >
                    <button
                      type="button"
                      className="screenshot-carousel__card"
                      tabIndex={isSelected ? 0 : -1}
                      aria-label={
                        isSelected
                          ? `Apri a schermo intero: ${item.title}`
                          : `Mostra ${item.title}`
                      }
                      onClick={(e) => {
                        emblaApi?.plugins()?.autoplay?.stop();
                        if (isSelected) {
                          const rect = e.currentTarget.getBoundingClientRect();
                          hapticSnap(6);
                          onOpen(index, {
                            left: rect.left,
                            top: rect.top,
                            width: rect.width,
                            height: rect.height,
                          });
                        } else {
                          jumpTo(index);
                        }
                      }}
                    >
                      <div className="screenshot-carousel__screen">
                        <CrossfadeImage
                          src={withBasePath(previewPathFor(item.file))}
                          alt={item.title}
                          width={480}
                          height={1003}
                          loading={index < 4 ? 'eager' : 'lazy'}
                          decoding="async"
                          draggable={false}
                        />
                        {isSelected && (
                          <span className="screenshot-carousel__zoom-hint" aria-hidden="true">
                            <Maximize2 className="w-3.5 h-3.5" />
                          </span>
                        )}
                      </div>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Active Screen Caption + Quiet iOS Page Dots */}
      <div className="screenshot-carousel__meta">
        <div className="screenshot-carousel__meta-head">
          <h3 className="type-headline">{activeItem.title}</h3>
          <span
            className="screenshot-carousel__position"
            aria-live="polite"
            aria-atomic="true"
          >
            {safeSelected + 1} / {count}
          </span>
        </div>
        <p className="type-callout">{activeItem.description}</p>

        {count > 1 && (
          <div className="screenshot-carousel__dots" role="tablist" aria-label="Scegli schermata">
            {items.map((item, index) => {
              const isActive = index === safeSelected;
              return (
                <button
                  key={item.id}
                  type="button"
                  role="tab"
                  aria-selected={isActive}
                  aria-label={`Vai a ${item.title}`}
                  className="screenshot-carousel__dot"
                  data-active={isActive}
                  onClick={() => jumpTo(index)}
                />
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
