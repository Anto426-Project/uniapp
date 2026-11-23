'use client';

import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import type {
  AppearanceMode,
  MonetThemeId,
  OriginRect,
  PlatformManifest,
  UpdateManifest,
} from '@/data/types';
import { MONET_THEMES, getScreenshots } from '@/data/screenshots';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import { Navbar } from './Navbar';
import { FluidLightbox } from './FluidLightbox';

interface SiteContextValue {
  theme: MonetThemeId;
  setTheme: (id: MonetThemeId) => void;
  appearance: AppearanceMode;
  toggleAppearance: () => void;
  reducedMotion: boolean;
  release: UpdateManifest;
  platforms: PlatformManifest;
  isLightboxOpen: boolean;
  openLightbox: (index: number, originRect?: OriginRect | null) => void;
  setModalDepth: (progress: number) => void;
}

const SiteContext = createContext<SiteContextValue | null>(null);

export function useSite(): SiteContextValue {
  const ctx = useContext(SiteContext);
  if (!ctx) throw new Error('useSite must be used inside SiteShell');
  return ctx;
}

const VALID_THEMES = new Set<string>(MONET_THEMES.map((t) => t.id));

function normalizeTheme(raw: string | null): MonetThemeId {
  if (raw === 'amber') return 'sunset';
  if (raw && VALID_THEMES.has(raw)) return raw as MonetThemeId;
  return 'sapphire';
}

export function SiteShell({
  children,
  initialManifest,
  initialPlatforms,
}: {
  children: ReactNode;
  initialManifest: UpdateManifest;
  initialPlatforms: PlatformManifest;
}) {
  const [theme, setThemeState] = useState<MonetThemeId>('sapphire');
  const [appearance, setAppearance] = useState<AppearanceMode>('dark');
  const [reducedMotion, setReducedMotion] = useState(false);
  const [release, setRelease] = useState<UpdateManifest>(initialManifest);
  const [platforms, setPlatforms] = useState<PlatformManifest>(initialPlatforms);

  const [lightboxState, setLightboxState] = useState<{
    open: boolean;
    index: number;
    originRect: OriginRect | null;
  }>({ open: false, index: 0, originRect: null });

  const stageRef = useRef<HTMLDivElement>(null);

  // Sync prefers-reduced-motion live
  useEffect(() => {
    const media = window.matchMedia('(prefers-reduced-motion: reduce)');
    const sync = () => setReducedMotion(media.matches);
    sync();
    media.addEventListener('change', sync);
    return () => media.removeEventListener('change', sync);
  }, []);

  // Hydrate persisted Monet theme and Light/Dark appearance
  useEffect(() => {
    try {
      const savedTheme = localStorage.getItem('uniapp-theme');
      if (savedTheme) setThemeState(normalizeTheme(savedTheme));

      const savedAppearance = localStorage.getItem('uniapp-appearance');
      if (savedAppearance === 'light' || savedAppearance === 'dark') {
        setAppearance(savedAppearance);
      }
    } catch {
      // Storage unavailable in private browsing.
    }
  }, []);

  // Apply attributes cleanly to <html>
  useEffect(() => {
    const root = document.documentElement;
    root.dataset.theme = theme;
    root.dataset.appearance = appearance;
    root.style.colorScheme = appearance;
  }, [theme, appearance]);

  const setTheme = useCallback((next: MonetThemeId) => {
    setThemeState(next);
    hapticSnap(5);
    try {
      localStorage.setItem('uniapp-theme', next);
    } catch {
      // Optional persistence.
    }
  }, []);

  const toggleAppearance = useCallback(() => {
    setAppearance((prev) => {
      const next: AppearanceMode = prev === 'dark' ? 'light' : 'dark';
      hapticSnap(6);
      try {
        localStorage.setItem('uniapp-appearance', next);
      } catch {
        // Optional persistence.
      }
      return next;
    });
  }, []);

  // Live modal depth push-back on the background stage (§12 Dim to focus)
  const setModalDepth = useCallback(
    (progress: number) => {
      const el = stageRef.current;
      if (!el) return;
      if (reducedMotion || progress <= 0.001) {
        el.style.transform = '';
        el.style.borderRadius = '';
        return;
      }
      const p = Math.min(1, Math.max(0, progress));
      const scale = 1 - p * 0.032;
      const radius = p * 18;
      el.style.transform = `scale(${scale.toFixed(4)})`;
      el.style.borderRadius = `${radius.toFixed(1)}px`;
    },
    [reducedMotion]
  );

  // Runtime manifest fetches (preserve build-time data on network failure)
  useEffect(() => {
    const controller = new AbortController();
    fetch(withBasePath('/update.json'), { cache: 'no-cache', signal: controller.signal })
      .then(async (res) => {
        if (!res.ok) return;
        const data = await res.json();
        if (data && typeof data.latestVersion === 'string') {
          setRelease(data);
        }
      })
      .catch(() => {});

    fetch(withBasePath('/platforms.json'), { cache: 'no-cache', signal: controller.signal })
      .then(async (res) => {
        if (!res.ok) return;
        const data = await res.json();
        if (data?.schema === 1 && data.platforms && typeof data.platforms === 'object') {
          setPlatforms(data);
        }
      })
      .catch(() => {});

    return () => controller.abort();
  }, []);

  const openLightbox = useCallback((index: number, originRect: OriginRect | null = null) => {
    setLightboxState({ open: true, index, originRect });
  }, []);

  const closeLightbox = useCallback(() => {
    setLightboxState((prev) => ({ ...prev, open: false }));
    setModalDepth(0);
  }, [setModalDepth]);

  const activeScreenshots = useMemo(
    () => getScreenshots(theme, appearance),
    [theme, appearance]
  );

  const contextValue = useMemo<SiteContextValue>(
    () => ({
      theme,
      setTheme,
      appearance,
      toggleAppearance,
      reducedMotion,
      release,
      platforms,
      isLightboxOpen: lightboxState.open,
      openLightbox,
      setModalDepth,
    }),
    [
      theme,
      setTheme,
      appearance,
      toggleAppearance,
      reducedMotion,
      release,
      platforms,
      lightboxState.open,
      openLightbox,
      setModalDepth,
    ]
  );

  return (
    <SiteContext.Provider value={contextValue}>
      <div className="ambient-backdrop" aria-hidden="true">
        <div className="ambient-wash-top" />
        <div className="ambient-wash-bottom" />
      </div>

      <div ref={stageRef} className="page-stage">
        <Navbar />
        {children}
      </div>

      <FluidLightbox
        isOpen={lightboxState.open}
        items={activeScreenshots}
        currentIndex={lightboxState.index}
        originRect={lightboxState.originRect}
        onChangeIndex={(nextIdx) =>
          setLightboxState((prev) => ({ ...prev, index: nextIdx }))
        }
        onClose={closeLightbox}
      />
    </SiteContext.Provider>
  );
}
