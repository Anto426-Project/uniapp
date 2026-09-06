'use client';

import React, { useState, useEffect } from 'react';
import { Navbar } from '@/components/Navbar';
import { Hero } from '@/components/Hero';
import { ScreenshotsGallery } from '@/components/ScreenshotsGallery';
import { FeaturesGrid } from '@/components/FeaturesGrid';
import { Architecture } from '@/components/Architecture';
import { DownloadHub } from '@/components/DownloadHub';
import { SecuritySection } from '@/components/SecuritySection';
import { Footer } from '@/components/Footer';
import { DEFAULT_MANIFEST, SCREENSHOTS_DATA } from '@/data/default-manifest';
import { UpdateManifest } from '@/data/types';
import { Background } from '@/components/Background';
import { withBasePath } from '@/utils/basePath';

export default function HomePage() {
  const [theme, setTheme] = useState('violet');
  const [manifest, setManifest] = useState<UpdateManifest>(DEFAULT_MANIFEST);

  // Initialize theme from localStorage and manage body class
  useEffect(() => {
    const savedTheme = localStorage.getItem('uniapp-theme');
    if (savedTheme && ['violet', 'sapphire', 'emerald', 'amber'].includes(savedTheme)) {
      setTheme(savedTheme);
      document.body.className = `theme-${savedTheme}`;
    } else {
      document.body.className = 'theme-violet';
    }
  }, []);

  const handleThemeChange = (newTheme: string) => {
    setTheme(newTheme);
    localStorage.setItem('uniapp-theme', newTheme);
    document.body.className = `theme-${newTheme}`;
  };

  // Live fetch update.json if available
  useEffect(() => {
    async function fetchLiveManifest() {
      try {
        const res = await fetch(withBasePath('/update.json'), { cache: 'no-store' });
        if (res.ok) {
          const data = await res.json();
          if (data && data.channels) {
            setManifest(data);
          }
        }
      } catch {
        // Fallback to DEFAULT_MANIFEST
      }
    }
    fetchLiveManifest();
  }, []);

  const betaRelease = manifest.channels?.beta?.release;

  return (
    <>
      {/* Sfondo dinamico con shader Aurora di React Bits e mesh Monet */}
      <Background theme={theme} />

      <Navbar currentTheme={theme} onThemeChange={handleThemeChange} />

      <main id="main-content">
        <Hero release={betaRelease} channel="beta" />
        <ScreenshotsGallery screenshots={SCREENSHOTS_DATA} />
        <FeaturesGrid />
        <Architecture />
        <SecuritySection />
        <DownloadHub manifest={manifest} theme={theme} />
      </main>

      <Footer />
    </>
  );
}
