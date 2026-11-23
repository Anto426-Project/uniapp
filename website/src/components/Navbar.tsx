'use client';

import React, { useEffect, useState } from 'react';
import { Menu, X } from 'lucide-react';
import { withBasePath } from '@/utils/basePath';
import { MONET_THEMES } from './SiteShell';

interface NavbarProps {
  currentTheme: string;
  onThemeChange: (theme: string) => void;
}

export const Navbar: React.FC<NavbarProps> = ({ currentTheme, onThemeChange }) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 820) setMobileMenuOpen(false);
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    document.body.style.overflow = mobileMenuOpen ? 'hidden' : '';
    return () => {
      document.body.style.overflow = '';
    };
  }, [mobileMenuOpen]);

  const navLinks = [
    { href: '#screenshots', label: 'Schermate' },
    { href: '#features', label: 'Funzionalità' },
    { href: '#architecture', label: 'Architettura' },
    { href: '#download', label: 'Download' },
    { href: '#security', label: 'Sicurezza' },
  ];

  const closeMenu = () => setMobileMenuOpen(false);

  return (
    <>
      <header className="apple-nav-bar">
        <nav className="container apple-nav-inner" aria-label="Navigazione principale">
          <a href="#" className="brand-wrapper" onClick={closeMenu}>
            <img
              src={withBasePath('/assets/uniapp-icon.webp')}
              alt="UniApp"
              className="brand-icon-img"
            />
            <span className="brand-name">UniApp</span>
          </a>

          <div className="nav-links">
            {navLinks.map((link) => (
              <a key={link.href} href={link.href} className="nav-link">
                {link.label}
              </a>
            ))}
          </div>

          <div className="nav-actions">
            <div className="palette-dots" role="group" aria-label="Tema cromatico Monet">
              {MONET_THEMES.map((t) => (
                <button
                  key={t.id}
                  type="button"
                  className={`palette-btn ${t.className} ${currentTheme === t.id ? 'active' : ''}`}
                  data-theme={t.id}
                  aria-label={`Tema ${t.fullLabel}`}
                  aria-pressed={currentTheme === t.id}
                  title={t.fullLabel}
                  onClick={() => onThemeChange(t.id)}
                />
              ))}
            </div>

            <a href="#download" className="nav-cta-btn">
              Scarica
            </a>

            <button
              type="button"
              className="mobile-nav-toggle"
              onClick={() => setMobileMenuOpen((prev) => !prev)}
              aria-label={mobileMenuOpen ? 'Chiudi menu' : 'Apri menu'}
              aria-expanded={mobileMenuOpen}
            >
              {mobileMenuOpen ? <X className="w-4 h-4" /> : <Menu className="w-4 h-4" />}
            </button>
          </div>
        </nav>
      </header>

      {mobileMenuOpen && (
        <div
          className="mobile-menu-overlay"
          onClick={closeMenu}
          role="dialog"
          aria-modal="true"
          aria-label="Menu di navigazione"
        >
          <div className="mobile-menu-card" onClick={(e) => e.stopPropagation()}>
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="mobile-menu-link"
                onClick={closeMenu}
              >
                {link.label}
              </a>
            ))}
            <a
              href="https://github.com/Anto426-Project/Uniapp"
              target="_blank"
              rel="noopener noreferrer"
              className="mobile-menu-link"
              onClick={closeMenu}
            >
              GitHub
            </a>

            <div className="mobile-menu-footer">
              <span className="mobile-menu-label">Pigmento Monet</span>
              <div className="palette-dots mobile">
                {MONET_THEMES.map((t) => (
                  <button
                    key={t.id}
                    type="button"
                    className={`palette-btn ${t.className} ${currentTheme === t.id ? 'active' : ''}`}
                    aria-label={`Tema ${t.fullLabel}`}
                    aria-pressed={currentTheme === t.id}
                    onClick={() => onThemeChange(t.id)}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
