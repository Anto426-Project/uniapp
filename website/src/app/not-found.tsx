'use client';

import React, { useEffect } from 'react';
import Link from 'next/link';
import { Download, Home, SearchX } from 'lucide-react';

export default function NotFound() {
  useEffect(() => {
    try {
      const savedTheme = localStorage.getItem('uniapp-theme');
      const normalized = savedTheme === 'amber' ? 'sunset' : savedTheme;
      if (normalized && ['sapphire', 'emerald', 'sunset', 'violet'].includes(normalized)) {
        document.documentElement.dataset.theme = normalized;
      }
      const savedAppearance = localStorage.getItem('uniapp-appearance');
      if (savedAppearance === 'light' || savedAppearance === 'dark') {
        document.documentElement.dataset.appearance = savedAppearance;
        document.documentElement.style.colorScheme = savedAppearance;
      }
    } catch {
      // Ignore private browsing storage limits.
    }
  }, []);

  return (
    <>
      <div className="ambient-backdrop" aria-hidden="true">
        <div className="ambient-wash-top" />
      </div>

      <main
        style={{
          minHeight: '100dvh',
          display: 'grid',
          placeItems: 'center',
          padding: '3rem 1.25rem',
        }}
      >
        <div
          className="surface-card"
          style={{
            width: 'min(100%, 30rem)',
            textAlign: 'center',
            alignItems: 'center',
            padding: '2.75rem 2rem',
          }}
        >
          <div className="icon-badge" style={{ width: '3.25rem', height: '3.25rem', borderRadius: '0.95rem' }}>
            <SearchX className="w-6 h-6" aria-hidden="true" />
          </div>

          <span className="type-eyebrow">Pagina non trovata</span>

          <h1 className="type-hero" style={{ fontSize: 'clamp(3rem, 9vw, 4.25rem)' }}>
            <span className="hero-accent-text">404</span>
          </h1>

          <p className="type-body" style={{ maxWidth: '22rem' }}>
            L&apos;indirizzo richiesto non esiste o è cambiato. Torna alla pagina iniziale oppure vai subito ai download.
          </p>

          <div className="hero-cta-row" style={{ width: '100%' }}>
            <Link href="/" className="btn-primary">
              <Home className="w-4 h-4" aria-hidden="true" />
              <span>Torna alla home</span>
            </Link>
            <Link href="/#download" className="btn-material">
              <Download className="w-4 h-4" aria-hidden="true" />
              <span>Vai ai download</span>
            </Link>
          </div>
        </div>
      </main>
    </>
  );
}
