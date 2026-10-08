'use client';

import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { Background } from './Background';
import { Navbar } from './Navbar';
import { UpdateManifest, PlatformManifest } from '@/data/types';
import { withBasePath } from '@/utils/basePath';

const ReleaseContext = createContext<UpdateManifest>({});
export const useRelease = () => useContext(ReleaseContext);
const PlatformContext = createContext<PlatformManifest>({schema: 1, platforms: {}});
export const usePlatforms = () => useContext(PlatformContext);

const ThemeContext = createContext<string>('violet');
export const useCurrentTheme = () => useContext(ThemeContext);

const themes = ['violet', 'sapphire', 'emerald', 'amber'];

export function SiteShell({ children, initialManifest, initialPlatforms }: {
  children: ReactNode; initialManifest: UpdateManifest; initialPlatforms: PlatformManifest;
}) {
  const [theme, setTheme] = useState('violet');
  const [manifest, setManifest] = useState(initialManifest);
  const [platforms, setPlatforms] = useState(initialPlatforms);

  useEffect(() => {
    try {
      const saved = localStorage.getItem('uniapp-theme');
      if (saved && themes.includes(saved)) setTheme(saved);
    } catch {
      // Storage may be unavailable in private browsing. The default theme still works.
    }
  }, []);

  useEffect(() => {
    document.body.classList.remove(...themes.map((value) => `theme-${value}`));
    document.body.classList.add(`theme-${theme}`);
  }, [theme]);

  useEffect(() => {
    const controller = new AbortController();
    fetch(withBasePath('/platforms.json'), { cache: 'no-cache', signal: controller.signal })
      .then(async (response) => {
        if (!response.ok) return;
        const data = await response.json();
        if (data?.schema === 1 && data.platforms && typeof data.platforms === 'object') setPlatforms(data);
      })
      .catch(() => { /* Keep the platform index embedded in the static export. */ });
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
      .catch(() => { /* Keep the manifest embedded in the static export. */ });
    return () => controller.abort();
  }, []);

  return (
    <ThemeContext.Provider value={theme}>
      <ReleaseContext.Provider value={manifest}>
        <PlatformContext.Provider value={platforms}>
        <Background theme={theme} />
        <Navbar currentTheme={theme} onThemeChange={(value) => {
          if (!themes.includes(value)) return;
          setTheme(value);
          try { localStorage.setItem('uniapp-theme', value); } catch { /* Optional persistence. */ }
        }} />
        {children}
        </PlatformContext.Provider>
      </ReleaseContext.Provider>
    </ThemeContext.Provider>
  );
}
