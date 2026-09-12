import type { Metadata, Viewport } from 'next';
import { Inter, Plus_Jakarta_Sans } from 'next/font/google';
import './globals.css';
import { BASE_PATH } from '@/utils/basePath';
import { cn } from "@/lib/utils";

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
  title: 'UniApp Upstream | Download Android ARM64',
  description: "Sito ufficiale del progetto open source e indipendente UniApp: scarica l'APK per Android, consulta le funzionalità e i rilasci per gli studenti dell'Università degli Studi del Molise.",
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
    title: 'UniApp Upstream | Download Android ARM64',
    description: "L'app 100% open source e indipendente per gli studenti UniMol: libretto, esami, tasse, navette e badge universitario.",
    url: 'https://anto426-project.github.io/uniapp-upstream/',
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
    <html lang="it" className={cn(inter.variable, plusJakartaSans.variable, "font-sans")}>
      <body className="theme-violet">
        {children}
      </body>
    </html>
  );
}
