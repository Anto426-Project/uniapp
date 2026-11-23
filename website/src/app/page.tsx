import { SiteShell } from '@/components/SiteShell';
import { Hero } from '@/components/Hero';
import { FeaturesSection } from '@/components/FeaturesSection';
import { ScreenshotsGallery } from '@/components/ScreenshotsGallery';
import { ArchitectureSection } from '@/components/ArchitectureSection';
import { DownloadHub } from '@/components/DownloadHub';
import { Footer } from '@/components/Footer';
import manifest from '../../public/update.json';
import platforms from '../../public/platforms.json';
import type { PlatformManifest, UpdateManifest } from '@/data/types';

export default function HomePage() {
  return (
    <SiteShell
      initialManifest={manifest as UpdateManifest}
      initialPlatforms={platforms as PlatformManifest}
    >
      <main id="main-content">
        <Hero />
        <FeaturesSection />
        <ScreenshotsGallery />
        <ArchitectureSection />
        <DownloadHub />
      </main>
      <Footer />
    </SiteShell>
  );
}
