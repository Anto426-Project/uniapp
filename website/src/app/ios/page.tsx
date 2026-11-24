import type { Metadata } from 'next';
import './ios-sideload.css';
import { SiteShell } from '@/components/SiteShell';
import { IosSideloadStudio } from '@/components/IosSideloadStudio';
import { Footer } from '@/components/Footer';
import manifest from '../../../public/update.json';
import platforms from '../../../public/platforms.json';
import type { PlatformManifest, UpdateManifest } from '@/data/types';

export const metadata: Metadata = {
  title: 'UniApp per iOS · Firma Apple ID e Sideload WebUSB dal Browser',
  description:
    'Firma l’IPA ufficiale di UniApp con il tuo account Apple direttamente nel browser in WebAssembly e installalo su iPhone o iPad via WebUSB, SideStore o AltStore.',
};

export default function IosSideloadPage() {
  return (
    <SiteShell
      initialManifest={manifest as UpdateManifest}
      initialPlatforms={platforms as PlatformManifest}
    >
      <main id="main-content">
        <IosSideloadStudio />
      </main>
      <Footer />
    </SiteShell>
  );
}
