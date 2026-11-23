'use client';

import React, { useCallback, useEffect, useRef, useState } from 'react';

interface CrossfadeImageProps {
  src: string;
  alt: string;
  width?: number;
  height?: number;
  loading?: 'eager' | 'lazy';
  decoding?: 'async' | 'sync' | 'auto';
  draggable?: boolean;
  objectPosition?: string;
  className?: string;
}

/**
 * Smoothly crossfades when `src` changes (e.g. Monet pigment switch) by holding
 * the previous frame as a cover backdrop underneath a single semantic <img>.
 * Handles both uncached network loads and synchronously cached frames reliably.
 */
export function CrossfadeImage({
  src,
  alt,
  width,
  height,
  loading,
  decoding,
  draggable = false,
  objectPosition = 'center center',
  className,
}: CrossfadeImageProps) {
  const imgRef = useRef<HTMLImageElement>(null);
  const [prevSrc, setPrevSrc] = useState<string | null>(null);
  const [fading, setFading] = useState(false);
  const activeSrcRef = useRef(src);
  const cleanupTimerRef = useRef<number | null>(null);
  const rafA = useRef<number | null>(null);
  const rafB = useRef<number | null>(null);

  const clearPending = useCallback(() => {
    if (cleanupTimerRef.current !== null) {
      window.clearTimeout(cleanupTimerRef.current);
      cleanupTimerRef.current = null;
    }
    if (rafA.current !== null) {
      cancelAnimationFrame(rafA.current);
      rafA.current = null;
    }
    if (rafB.current !== null) {
      cancelAnimationFrame(rafB.current);
      rafB.current = null;
    }
  }, []);

  const reveal = useCallback(() => {
    clearPending();
    // Double rAF guarantees the browser paints opacity: 0 before starting the 420ms fade-in
    rafA.current = requestAnimationFrame(() => {
      rafB.current = requestAnimationFrame(() => {
        setFading(false);
        cleanupTimerRef.current = window.setTimeout(() => {
          setPrevSrc(null);
          cleanupTimerRef.current = null;
        }, 460);
      });
    });
  }, [clearPending]);

  useEffect(() => {
    if (src === activeSrcRef.current) return;
    const outgoing = activeSrcRef.current;
    activeSrcRef.current = src;
    clearPending();
    setPrevSrc(outgoing);
    setFading(true);
  }, [src, clearPending]);

  useEffect(() => {
    if (!fading) return;
    const img = imgRef.current;
    if (img && img.complete && img.naturalWidth > 0) {
      reveal();
    }
  }, [fading, src, reveal]);

  useEffect(() => clearPending, [clearPending]);

  return (
    <span className="crossfade-frame">
      <img
        ref={imgRef}
        src={src}
        alt={alt}
        width={width}
        height={height}
        loading={loading}
        decoding={decoding}
        draggable={draggable}
        onLoad={() => {
          if (fading) reveal();
        }}
        onError={() => {
          if (fading) reveal();
        }}
        className={className}
        style={{
          objectPosition,
          opacity: fading ? 0 : 1,
          transition: fading ? 'none' : 'opacity 420ms cubic-bezier(0.22, 1, 0.36, 1)',
        }}
      />
      {prevSrc && (
        <span
          className="crossfade-underlay"
          aria-hidden="true"
          style={{
            backgroundImage: `url("${prevSrc}")`,
            backgroundPosition: objectPosition,
          }}
        />
      )}
    </span>
  );
}
