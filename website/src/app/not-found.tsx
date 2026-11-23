'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { Home, Download, SearchX } from 'lucide-react';
import { Background } from '@/components/Background';

export default function NotFound() {
  const [theme, setTheme] = useState('sapphire');

  useEffect(() => {
    try {
      const savedTheme = localStorage.getItem('uniapp-theme');
      const normalized = savedTheme === 'amber' ? 'sunset' : savedTheme;
      if (normalized && ['sapphire', 'emerald', 'sunset', 'violet'].includes(normalized)) {
        setTheme(normalized);
        document.body.className = `theme-${normalized}`;
        return;
      }
    } catch {
      /* Ignore private storage errors. */
    }
    document.body.className = 'theme-sapphire';
  }, []);

  return (
    <>
      <Background theme={theme} />

      <main
        style={{
          minHeight: '100dvh',
          display: 'grid',
          placeItems: 'center',
          padding: '48px 20px',
        }}
      >
        <div
          className="studio-card"
          style={{
            width: 'min(100%, 480px)',
            padding: '44px 32px',
            textAlign: 'center',
            alignItems: 'center',
          }}
        >
          <div className="spec-icon" style={{ width: 52, height: 52, borderRadius: 15, marginBottom: 16 }}>
            <SearchX className="w-6 h-6" aria-hidden="true" />
          </div>

          <span className="eyebrow">Errore 404</span>

          <h1
            className="hero-headline"
            style={{ fontSize: 'clamp(3rem, 9vw, 4.2rem)', marginTop: 8 }}
          >
            <span className="hero-headline-accent">404</span>
          </h1>

          <p className="lead-copy" style={{ marginTop: 12, maxWidth: 360 }}>
            La pagina o risorsa richiesta non esiste oppure è stata spostata.
          </p>

          <div className="hero-actions" style={{ marginTop: 28, justifyContent: 'center', width: '100%' }}>
            <Link href="/" className="btn-apple-primary">
              <Home className="w-4 h-4" aria-hidden="true" />
              <span>Torna alla Home</span>
            </Link>
            <Link href="/#download" className="btn-apple-glass">
              <Download className="w-4 h-4" aria-hidden="true" />
              <span>Download</span>
            </Link>
          </div>
        </div>
      </main>
    </>
  );
}
