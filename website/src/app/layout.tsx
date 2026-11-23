import type { Metadata, Viewport } from 'next';
import './globals.css';
import { BASE_PATH } from '@/utils/basePath';

export const metadata: Metadata = {
  title: 'UniApp · La tua università, finalmente nativa (Android e iOS)',
  description:
    "Libretto, base di laurea, appelli, presenze QR, badge UniPass 3D, tasse e navette dell'Università degli Studi del Molise in un'app nativa open source per Android e iOS.",
  authors: [{ name: 'Anto426' }],
  icons: {
    icon: [
      { url: `${BASE_PATH}/favicon.svg`, type: 'image/svg+xml' },
      { url: `${BASE_PATH}/favicon.ico`, sizes: 'any' },
      { url: `${BASE_PATH}/favicon-32x32.png`, sizes: '32x32', type: 'image/png' },
      { url: `${BASE_PATH}/favicon-192x192.png`, sizes: '192x192', type: 'image/png' },
    ],
    apple: [{ url: `${BASE_PATH}/apple-touch-icon.png`, sizes: '180x180', type: 'image/png' }],
  },
  openGraph: {
    type: 'website',
    title: 'UniApp · La tua università, finalmente nativa',
    description:
      "Libretto, simulatore laurea, badge UniPass 3D, presenze QR, tasse e navette offline per gli studenti dell'Università degli Studi del Molise.",
    url: 'https://anto426-project.github.io/uniapp-upstream/',
    siteName: 'UniApp',
  },
};

export const viewport: Viewport = {
  themeColor: [
    { media: '(prefers-color-scheme: dark)', color: '#000000' },
    { media: '(prefers-color-scheme: light)', color: '#fbfbfd' },
  ],
  width: 'device-width',
  initialScale: 1,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="it" data-appearance="dark" data-theme="sapphire">
      <body>{children}</body>
    </html>
  );
}
