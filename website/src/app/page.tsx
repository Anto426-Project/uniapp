import { Hero } from '@/components/Hero';
import { ScreenshotsGallery } from '@/components/ScreenshotsGallery';
import { FeaturesGrid } from '@/components/FeaturesGrid';
import { Architecture } from '@/components/Architecture';
import { DownloadHub } from '@/components/DownloadHub';
import { Footer } from '@/components/Footer';
import { SiteShell } from '@/components/SiteShell';
import { SCREENSHOTS_DATA } from '@/data/default-manifest';
import manifest from '../../public/update.json';
import platforms from '../../public/platforms.json';
import type { PlatformManifest } from '@/data/types';

export default function HomePage() {
  return (
    <SiteShell initialManifest={manifest} initialPlatforms={platforms as PlatformManifest}>
      <main id="main-content">
        <Hero />
        <ScreenshotsGallery screenshots={SCREENSHOTS_DATA} />
        <FeaturesGrid />
        <Architecture />
        <DownloadHub />
      </main>
      <Footer />
    </SiteShell>
  );
}
