'use client';

import React from 'react';
import GlassSurface from './GlassSurface';

export const FeaturesGrid: React.FC = () => {
  const features = [
    {
      title: 'Libretto & Base di Laurea',
      desc: 'Calcolo automatico della media ponderata, simulazione voti esami futuri e proiezione in tempo reale del voto di laurea.',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5Z" />
          <path d="M6 6h10" />
          <path d="M6 10h10" />
        </svg>
      ),
    },
    {
      title: 'Prenotazione Appelli Istantanea',
      desc: 'Visualizza gli appelli aperti, prenota o cancella iscrizioni con un tocco e ricevi notifiche promemoria.',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <rect width="18" height="18" x="3" y="4" rx="2" ry="2" />
          <line x1="16" y1="2" x2="16" y2="6" />
          <line x1="8" y1="2" x2="8" y2="6" />
          <line x1="3" y1="10" x2="21" y2="10" />
        </svg>
      ),
    },
    {
      title: 'Badge Digitale & QR Code',
      desc: 'Tessera studente sempre a portata di mano con QR generato secondo gli standard ufficiali per aule e biblioteche.',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <rect width="18" height="18" x="3" y="3" rx="2" />
          <path d="M7 7h.01" />
          <path d="M17 7h.01" />
          <path d="M7 17h.01" />
          <path d="M17 17h.01" />
        </svg>
      ),
    },
    {
      title: 'Tasse & Pagamenti PagoPA',
      desc: 'Controllo dello stato dei contributi universitari, bollettini PagoPA, importi e relative scadenze.',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <rect width="20" height="14" x="2" y="5" rx="2" />
          <line x1="2" y1="10" x2="22" y2="10" />
        </svg>
      ),
    },
    {
      title: 'Questionari Didattici ANVUR',
      desc: 'Compilazione fluida e guidata con supporto multipagina e domande obbligatorie/opzionali integrate in-app.',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M9 11l3 3L22 4" />
          <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
        </svg>
      ),
    },
    {
      title: 'Servizio Navette Trasporti',
      desc: 'Orari, disponibilità e prenotazione dei trasporti universitari nei giorni feriali con gestione delle cancellazioni.',
      icon: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M8 6v6" />
          <path d="M15 6v6" />
          <path d="M2 12h19.6" />
          <path d="M18 18h3s.5-1.7.8-2.8c.1-.4.2-.8.2-1.2 0-.6-.4-1.2-.9-1.5l-3.3-1.8A3 3 0 0 0 13.4 10H6.6a3 3 0 0 0-1.4.7L1.9 12.5C1.4 12.8 1 13.4 1 14c0 .4.1.8.2 1.2.3 1.1.8 2.8.8 2.8h3" />
        </svg>
      ),
    },
  ];

  return (
    <section id="features">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">Esperienza Studente</span>
          <h2 className="section-title">Tutto ciò che serve per la tua carriera</h2>
          <p className="section-description">
            Ogni aspetto della vita universitaria organizzato in modo chiaro, rapido e sempre disponibile anche offline.
          </p>
        </div>

        <div className="features-grid">
          {features.map((f, idx) => (
            <GlassSurface
              key={idx}
              width="100%"
              height="auto"
              borderRadius={24}
              className="feature-card as-flex-col"
              distortionScale={-140}
              backgroundOpacity={0.12}
              saturation={1.3}
            >
              <div className="feature-icon-wrapper">{f.icon}</div>
              <h3 className="feature-title">{f.title}</h3>
              <p className="feature-desc">{f.desc}</p>
            </GlassSurface>
          ))}
        </div>
      </div>
    </section>
  );
};
