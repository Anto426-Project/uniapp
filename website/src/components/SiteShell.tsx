'use client';

import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { Background } from './Background';
import { Navbar } from './Navbar';
import { UpdateManifest, PlatformManifest } from '@/data/types';
import { withBasePath } from '@/utils/basePath';

const ReleaseContext = createContext<UpdateManifest>({});
export const useRelease = () => useContext(ReleaseContext);

const PlatformContext = createContext<PlatformManifest>({ schema: 1, platforms: {} });
export const usePlatforms = () => useContext(PlatformContext);

export const MONET_THEMES = [
  {
    id: 'sapphire',
    label: 'Zaffiro',
    fullLabel: 'Zaffiro',
    className: 'btn-palette-sapphire',
    assetPrefix: 'sapphire',
  },
  {
    id: 'emerald',
    label: 'Smeraldo',
    fullLabel: 'Smeraldo',
    className: 'btn-palette-emerald',
    assetPrefix: 'emerald',
  },
  {
    id: 'sunset',
    label: 'Tramonto',
    fullLabel: 'Tramonto',
    className: 'btn-palette-sunset',
    assetPrefix: 'sunset',
  },
  {
    id: 'violet',
    label: 'Viola',
    fullLabel: 'Viola',
    className: 'btn-palette-violet',
    assetPrefix: 'violet',
  },
] as const;

export type MonetThemeId = (typeof MONET_THEMES)[number]['id'];

interface ThemeContextValue {
  theme: MonetThemeId;
  setTheme: (value: string) => void;
}

const ThemeContext = createContext<ThemeContextValue>({
  theme: 'sapphire',
  setTheme: () => {},
});

export const useCurrentTheme = () => useContext(ThemeContext).theme;
export const useThemeControls = () => useContext(ThemeContext);

const themeIds: string[] = MONET_THEMES.map((t) => t.id);

function normalizeThemeId(raw: string | null): MonetThemeId {
  if (raw === 'amber') return 'sunset';
  if (raw && themeIds.includes(raw)) return raw as MonetThemeId;
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
  const [manifest, setManifest] = useState(initialManifest);
  const [platforms, setPlatforms] = useState(initialPlatforms);

  useEffect(() => {
    try {
      const saved = localStorage.getItem('uniapp-theme');
      if (saved) setThemeState(normalizeThemeId(saved));
    } catch {
      // Storage may be unavailable in private browsing.
    }
  }, []);

  useEffect(() => {
    document.body.classList.remove(...themeIds.map((value) => `theme-${value}`), 'theme-amber');
    document.body.classList.add(`theme-${theme}`);
  }, [theme]);

  const setTheme = useCallback((value: string) => {
    const normalized = normalizeThemeId(value);
    setThemeState(normalized);
    try {
      localStorage.setItem('uniapp-theme', normalized);
    } catch {
      /* Optional persistence. */
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    fetch(withBasePath('/platforms.json'), { cache: 'no-cache', signal: controller.signal })
      .then(async (response) => {
        if (!response.ok) return;
        const data = await response.json();
        if (data?.schema === 1 && data.platforms && typeof data.platforms === 'object') setPlatforms(data);
      })
      .catch(() => {
        /* Keep the platform index embedded in the static export. */
      });
    return () => controller.abort();
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    fetch(withBasePath('/update.json'), { cache: 'no-cache', signal: controller.signal })
      .then(async (response) => {
        if (!response.ok) return;
        const data = await response.json();
        if (data && typeof data.latestVersion === 'string') setManifest(data);
      })
      .catch(() => {
        /* Keep the manifest embedded in the static export. */
      });
    return () => controller.abort();
  }, []);

  return (
    <ThemeContext.Provider value={{ theme, setTheme }}>
      <ReleaseContext.Provider value={manifest}>
        <PlatformContext.Provider value={platforms}>
          <Background theme={theme} />
          <Navbar currentTheme={theme} onThemeChange={setTheme} />
          {children}
        </PlatformContext.Provider>
      </ReleaseContext.Provider>
    </ThemeContext.Provider>
  );
}
