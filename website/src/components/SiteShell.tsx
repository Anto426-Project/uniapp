'use client';

import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { Background } from './Background';
import { Navbar } from './Navbar';
import { UpdateManifest } from '@/data/types';
import { withBasePath } from '@/utils/basePath';

const ReleaseContext = createContext<UpdateManifest>({});
export const useRelease = () => useContext(ReleaseContext);

const ThemeContext = createContext<string>('violet');
export const useCurrentTheme = () => useContext(ThemeContext);

const themes = ['violet', 'sapphire', 'emerald', 'amber'];

export function SiteShell({ children, initialManifest }: { children: ReactNode; initialManifest: UpdateManifest }) {
  const [theme, setTheme] = useState('violet');
  const [manifest, setManifest] = useState(initialManifest);

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
        <Background theme={theme} />
        <Navbar currentTheme={theme} onThemeChange={(value) => {
          if (!themes.includes(value)) return;
          setTheme(value);
          try { localStorage.setItem('uniapp-theme', value); } catch { /* Optional persistence. */ }
        }} />
        {children}
      </ReleaseContext.Provider>
    </ThemeContext.Provider>
  );
}
