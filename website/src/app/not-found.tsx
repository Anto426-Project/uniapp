'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { Home, Download, SearchX, ExternalLink } from 'lucide-react';
import { Background } from '@/components/Background';
import GlassSurface from '@/components/GlassSurface';

export default function NotFound() {
  const [theme, setTheme] = useState('violet');

  useEffect(() => {
    const savedTheme = localStorage.getItem('uniapp-theme');
    if (savedTheme && ['violet', 'sapphire', 'emerald', 'amber'].includes(savedTheme)) {
      setTheme(savedTheme);
      document.body.className = `theme-${savedTheme}`;
    } else {
      document.body.className = 'theme-violet';
    }
  }, []);

  return (
    <>
      {/* Sfondo dinamico Aurora con la stessa palette scelta dall'utente */}
      <Background theme={theme} />

      <main
        className="not-found-page"
        style={{
          minHeight: '100dvh',
          display: 'grid',
          placeItems: 'center',
          position: 'relative',
          padding: '48px 20px',
          zIndex: 1,
        }}
      >
        <GlassSurface
          width="min(100%, 560px)"
          height="auto"
          borderRadius={28}
          className="as-block"
          distortionScale={-100}
          backgroundOpacity={0.85}
          saturation={1.2}
          style={{
            padding: '52px 36px',
            textAlign: 'center',
            position: 'relative',
            zIndex: 2,
            boxShadow: '0 24px 60px -15px rgba(0, 0, 0, 0.75), 0 0 30px var(--theme-glow-1)',
          }}
        >
          {/* Badge Icona */}
          <div
            style={{
              width: 64,
              height: 64,
              display: 'grid',
              placeItems: 'center',
              margin: '0 auto 24px',
              borderRadius: '20px',
              background: 'var(--theme-card-accent)',
              border: '1px solid rgba(255, 255, 255, 0.15)',
              color: 'var(--theme-primary)',
              boxShadow: '0 0 24px var(--theme-glow-1)',
            }}
          >
            <SearchX className="w-8 h-8" />
          </div>

          <span className="section-tag" style={{ marginBottom: 12 }}>
            Errore 404 • Risorsa Non Trovata
          </span>

          {/* 404 con gradiente testuale Monet */}
          <h1
            style={{
              margin: '16px 0 8px',
              fontSize: 'clamp(3.8rem, 11vw, 5.5rem)',
              fontWeight: 900,
              lineHeight: 1,
              letterSpacing: '-0.03em',
              background: 'linear-gradient(135deg, #ffffff 40%, var(--theme-primary) 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}
          >
            404
          </h1>

          <h2
            style={{
              margin: '12px 0',
              color: '#ffffff',
              fontSize: '1.4rem',
              fontWeight: 700,
            }}
          >
            Questa pagina non è disponibile
          </h2>

          <p
            style={{
              maxWidth: 420,
              margin: '0 auto 32px',
              color: 'var(--text-secondary)',
              fontSize: '0.95rem',
              lineHeight: 1.6,
            }}
          >
            L&apos;indirizzo digitato potrebbe essere cambiato, il link è obsoleto oppure la risorsa è stata trasferita.
          </p>

          {/* Azioni di Navigazione */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 14,
              flexWrap: 'wrap',
            }}
          >
            <Link
              href="/"
              className="btn-download-action-compact primary"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: 8,
                padding: '12px 24px',
                borderRadius: '16px',
                fontWeight: 700,
                fontSize: '0.92rem',
                textDecoration: 'none',
              }}
            >
              <Home className="w-4 h-4" />
              <span>Torna alla Home</span>
            </Link>

            <Link
              href="/#download"
              className="btn-download-action-compact secondary"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: 8,
                padding: '12px 22px',
                borderRadius: '16px',
                fontWeight: 600,
                fontSize: '0.92rem',
                textDecoration: 'none',
              }}
            >
              <Download className="w-4 h-4" />
              <span>Vai ai Download</span>
            </Link>
          </div>

          {/* Footer Card */}
          <div
            style={{
              marginTop: 36,
              paddingTop: 20,
              borderTop: '1px solid rgba(255, 255, 255, 0.08)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              fontSize: '0.82rem',
              color: 'var(--text-tertiary)',
            }}
          >
            <span>UniApp Ecosystem</span>
            <a
              href="https://github.com/Anto426-Project/Uniapp"
              target="_blank"
              rel="noopener noreferrer"
              style={{
                color: 'var(--theme-primary)',
                textDecoration: 'none',
                display: 'inline-flex',
                alignItems: 'center',
                gap: 4,
                fontWeight: 600,
              }}
            >
              <span>GitHub</span>
              <ExternalLink className="w-3.5 h-3.5" />
            </a>
          </div>
        </GlassSurface>
      </main>
    </>
  );
}
