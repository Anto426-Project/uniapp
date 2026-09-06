'use client';

import React, { useEffect, useState } from 'react';
import { Menu, X } from 'lucide-react';
import { withBasePath } from '@/utils/basePath';

interface NavbarProps {
  currentTheme: string;
  onThemeChange: (theme: string) => void;
}

export const Navbar: React.FC<NavbarProps> = ({ currentTheme, onThemeChange }) => {
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Close mobile menu on resize to desktop
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 860) {
        setMobileMenuOpen(false);
      }
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Prevent background scroll when mobile menu is open
  useEffect(() => {
    if (mobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [mobileMenuOpen]);

  const themes = [
    { id: 'violet', label: 'Monet Violet', className: 'btn-palette-violet' },
    { id: 'sapphire', label: 'Monet Sapphire', className: 'btn-palette-sapphire' },
    { id: 'emerald', label: 'Monet Emerald', className: 'btn-palette-emerald' },
    { id: 'amber', label: 'Monet Amber', className: 'btn-palette-amber' },
  ];

  const navLinks = [
    { href: '#screenshots', label: 'Schermate' },
    { href: '#features', label: 'Caratteristiche' },
    { href: '#architecture', label: 'Architettura' },
    { href: '#download', label: 'Download' },
    { href: '#changelog', label: 'Changelog' },
  ];

  const handleLinkClick = () => {
    setMobileMenuOpen(false);
  };

  return (
    <header className={`site-header ${scrolled ? 'scrolled' : ''}`}>
      <nav className="nav-container">
        {/* Brand */}
        <a href="#" className="brand-wrapper" onClick={handleLinkClick}>
          <img
            src={withBasePath('/assets/uniapp-icon.webp')}
            alt="Icona UniApp"
            className="brand-icon-img"
            onError={(e) => {
              (e.currentTarget as HTMLImageElement).src =
                'https://raw.githubusercontent.com/Anto426-Project/UniappUpstream/main/assets/uniapp-icon.webp';
            }}
          />
          <span className="brand-name">UniApp</span>
        </a>

        {/* Desktop Navigation Links */}
        <div className="nav-links">
          {navLinks.map((link) => (
            <a key={link.href} href={link.href} className="nav-link">
              {link.label}
            </a>
          ))}
        </div>

        {/* Actions (Themes, GitHub, Hamburger) */}
        <div className="nav-actions">
          {/* Palette Monet Swapper */}
          <div className="palette-swapper-nav" title="Cambia pigmento cromatico Monet">
            {themes.map((t) => (
              <button
                key={t.id}
                type="button"
                className={`palette-btn ${t.className} ${currentTheme === t.id ? 'active' : ''}`}
                data-theme={t.id}
                aria-label={`Tema ${t.label}`}
                title={t.label}
                onClick={() => onThemeChange(t.id)}
              />
            ))}
          </div>

          {/* GitHub Button */}
          <a
            href="https://github.com/Anto426-Project/Uniapp"
            target="_blank"
            rel="noopener noreferrer"
            className="github-btn"
            title="Repository GitHub UniApp"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path
                fillRule="evenodd"
                clipRule="evenodd"
                d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
              />
            </svg>
            <span className="github-btn-text">GitHub</span>
          </a>

          {/* Mobile Hamburger Toggle Button */}
          <button
            type="button"
            className="mobile-nav-toggle"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label={mobileMenuOpen ? 'Chiudi menu' : 'Apri menu'}
            aria-expanded={mobileMenuOpen}
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </nav>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="mobile-menu-overlay" onClick={() => setMobileMenuOpen(false)}>
          <div
            className="mobile-menu-card"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="mobile-menu-links">
              {navLinks.map((link) => (
                <a
                  key={link.href}
                  href={link.href}
                  className="mobile-menu-link"
                  onClick={handleLinkClick}
                >
                  {link.label}
                </a>
              ))}
            </div>

            <div className="mobile-menu-footer">
              <span className="mobile-menu-label">Pigmento Monet:</span>
              <div className="palette-swapper-nav mobile">
                {themes.map((t) => (
                  <button
                    key={t.id}
                    type="button"
                    className={`palette-btn ${t.className} ${currentTheme === t.id ? 'active' : ''}`}
                    data-theme={t.id}
                    aria-label={`Tema ${t.label}`}
                    title={t.label}
                    onClick={() => {
                      onThemeChange(t.id);
                    }}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>
      )}
    </header>
  );
};
