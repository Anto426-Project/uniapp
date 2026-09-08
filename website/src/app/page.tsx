import { Hero } from '@/components/Hero';
import { ScreenshotsGallery } from '@/components/ScreenshotsGallery';
import { FeaturesGrid } from '@/components/FeaturesGrid';
import { Architecture } from '@/components/Architecture';
import { DownloadHub } from '@/components/DownloadHub';
import { SecuritySection } from '@/components/SecuritySection';
import { Footer } from '@/components/Footer';
import { SiteShell } from '@/components/SiteShell';
import { SCREENSHOTS_DATA } from '@/data/default-manifest';
import manifest from '../../public/update.json';

export default function HomePage() {
  return (
    <SiteShell initialManifest={manifest}>
      <main id="main-content">
        <Hero />
        <ScreenshotsGallery screenshots={SCREENSHOTS_DATA} />
        <FeaturesGrid />
        <Architecture />
        <SecuritySection />
        <DownloadHub />
      </main>
      <Footer />
    </SiteShell>
  );
}
