import type { Metadata, Viewport } from 'next';
import { Inter, Plus_Jakarta_Sans, Geist } from 'next/font/google';
import './globals.css';
import { BASE_PATH } from '@/utils/basePath';
import { cn } from "@/lib/utils";

const geist = Geist({subsets:['latin'],variable:'--font-sans'});

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-inter',
  display: 'swap',
});

const plusJakartaSans = Plus_Jakarta_Sans({
  subsets: ['latin'],
  variable: '--font-plus-jakarta',
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'UniApp Upstream | Distribuzione Ufficiale Android',
  description: "Sito di distribuzione ufficiale per UniApp: scarica l'APK nativo per Android per sistemi a 64 bit, consulta i changelog e le specifiche dell'applicazione.",
  authors: [{ name: 'Anto426' }],
  icons: {
    icon: [
      { url: `${BASE_PATH}/favicon.ico`, sizes: 'any' },
      { url: `${BASE_PATH}/favicon-32x32.png`, sizes: '32x32', type: 'image/png' },
      { url: `${BASE_PATH}/favicon-192x192.png`, sizes: '192x192', type: 'image/png' },
    ],
    apple: [
      { url: `${BASE_PATH}/apple-touch-icon.png`, sizes: '180x180', type: 'image/png' },
    ],
  },
  openGraph: {
    type: 'website',
    title: 'UniApp Upstream | Distribuzione Ufficiale Android',
    description: "Scarica l'APK di UniApp per Android con design Liquid Monet e Kotlin nativo per gli studenti UniMol.",
    url: 'https://anto426-project.github.io/UniappUpstream/',
    siteName: 'UniApp Upstream',
  },
};

export const viewport: Viewport = {
  themeColor: '#07080c',
  width: 'device-width',
  initialScale: 1,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="it" className={cn(inter.variable, plusJakartaSans.variable, "font-sans", geist.variable)}>
      <body className="theme-violet">
        {children}
      </body>
    </html>
  );
}
