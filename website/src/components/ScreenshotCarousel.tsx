'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import useEmblaCarousel from 'embla-carousel-react';
import Autoplay from 'embla-carousel-autoplay';
import { ChevronLeft, ChevronRight, Pause, Play } from 'lucide-react';
import './ScreenshotCarousel.css';

export interface CarouselScreenshot {
  image: string;
  alt: string;
  title?: string;
  description?: string;
}

interface ScreenshotCarouselProps {
  items: CarouselScreenshot[];
  paused: boolean;
  onOpen: (index: number) => void;
}

export function ScreenshotCarousel({ items, paused, onOpen }: ScreenshotCarouselProps) {
  const rootRef = useRef<HTMLDivElement>(null);
  const [selected, setSelected] = useState(0);
  const [playing, setPlaying] = useState(true);
  const [reducedMotion, setReducedMotion] = useState(false);
  const autoplay = useMemo(() => Autoplay({
    delay: 4500,
    playOnInit: false,
    stopOnInteraction: true,
    stopOnMouseEnter: true,
    stopOnFocusIn: true,
  }), []);
  const [viewportRef, api] = useEmblaCarousel({
    loop: items.length > 1,
    align: 'center',
    containScroll: false,
    duration: reducedMotion ? 0 : 25,
  }, [autoplay]);

  useEffect(() => {
    const media = window.matchMedia('(prefers-reduced-motion: reduce)');
    const sync = () => setReducedMotion(media.matches);
    sync();
    media.addEventListener('change', sync);
    return () => media.removeEventListener('change', sync);
  }, []);

  useEffect(() => {
    if (!api) return;
    const select = () => setSelected(api.selectedScrollSnap());
    const stop = () => setPlaying(false);
    select();
    api.on('select', select).on('reInit', select).on('pointerDown', stop).on('slideFocusStart', stop);
    return () => {
      api.off('select', select).off('reInit', select).off('pointerDown', stop).off('slideFocusStart', stop);
    };
  }, [api]);

  useEffect(() => {
    const root = rootRef.current;
    if (!root || !api) return;
    let visible = false;
    const sync = () => {
      if (visible && !document.hidden && playing && !paused && !reducedMotion && items.length > 1) {
        autoplay.play();
      } else {
        autoplay.stop();
      }
    };
    const observer = new IntersectionObserver(([entry]) => {
      visible = entry.isIntersecting;
      sync();
    }, { threshold: 0.25 });
    observer.observe(root);
    document.addEventListener('visibilitychange', sync);
    return () => {
      observer.disconnect();
      document.removeEventListener('visibilitychange', sync);
      autoplay.stop();
    };
  }, [api, autoplay, items.length, paused, playing, reducedMotion]);

  if (!items.length) return null;

  const navigate = (direction: number) => {
    setPlaying(false);
    if (direction < 0) api?.scrollPrev(reducedMotion);
    else api?.scrollNext(reducedMotion);
  };

  const getSlidePosition = (index: number) => {
    const count = items.length;
    if (count <= 1) return { offset: 0, position: 'center', distance: 0 };
    let diff = index - selected;
    if (diff > count / 2) diff -= count;
    else if (diff <= -count / 2) diff += count;
    const position = diff === 0 ? 'center' : diff < 0 ? 'left' : 'right';
    return { offset: diff, position, distance: Math.abs(diff) };
  };

  const activeItem = items[selected] || items[0];

  return (
    <div
      ref={rootRef}
      className="screenshot-carousel"
      role="region"
      aria-roledescription="carosello"
      aria-label="Schermate di UniApp"
      onKeyDown={(event) => {
        if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return;
        event.preventDefault();
        navigate(event.key === 'ArrowLeft' ? -1 : 1);
      }}
    >
      <div className="screenshot-carousel__viewport" ref={viewportRef}>
        <div className="screenshot-carousel__track">
          {items.map((item, index) => {
            const isSelected = selected === index;
            const { offset, position, distance } = getSlidePosition(index);

            return (
              <div
                key={item.image}
                className="screenshot-carousel__slide"
                role="group"
                aria-roledescription="schermata"
                aria-label={`${index + 1} di ${items.length}`}
                data-selected={isSelected}
                data-position={position}
                data-offset={offset}
                data-distance={distance}
              >
                <button
                  type="button"
                  className="screenshot-carousel__card"
                  tabIndex={isSelected ? 0 : -1}
                  aria-label={isSelected ? `Ingrandisci: ${item.alt}` : `Mostra: ${item.alt}`}
                  onClick={() => {
                    setPlaying(false);
                    if (isSelected) {
                      onOpen(index);
                    } else {
                      api?.scrollTo(index, reducedMotion);
                    }
                  }}
                >
                  <img
                    src={item.image}
                    alt={item.alt}
                    loading={index < 2 ? 'eager' : 'lazy'}
                    width={480}
                    height={1056}
                    decoding="async"
                    draggable={false}
                  />
                </button>
              </div>
            );
          })}
        </div>
      </div>

      {items.length > 1 && (
        <div className="screenshot-carousel__controls">
          <button type="button" aria-label="Schermata precedente" onClick={() => navigate(-1)}>
            <ChevronLeft size={20} aria-hidden="true" />
          </button>
          <span className="screenshot-carousel__position" aria-live={playing ? 'off' : 'polite'} aria-atomic="true">
            {selected + 1} / {items.length}
          </span>
          <button type="button" aria-label="Schermata successiva" onClick={() => navigate(1)}>
            <ChevronRight size={20} aria-hidden="true" />
          </button>
          {!reducedMotion && (
            <button
              type="button"
              aria-label={playing ? 'Pausa scorrimento automatico' : 'Avvia scorrimento automatico'}
              onClick={() => setPlaying(!playing)}
            >
              {playing ? <Pause size={18} aria-hidden="true" /> : <Play size={18} aria-hidden="true" />}
            </button>
          )}
        </div>
      )}
    </div>
  );
}
