'use client';

import React from 'react';
import {
  Check,
  CalendarCheck,
  CreditCard,
  ClipboardList,
  Newspaper,
  Users,
  Palette,
} from 'lucide-react';
import { MONET_THEMES, useCurrentTheme } from './SiteShell';
import { withBasePath } from '@/utils/basePath';

export const FeaturesGrid: React.FC = () => {
  const theme = useCurrentTheme();
  const activeTheme = MONET_THEMES.find((t) => t.id === theme) || MONET_THEMES[0];
  const didacticsImg = withBasePath(`/assets/screenshots/previews/${activeTheme.assetPrefix}-didattica.webp`);
  const badgeImg = withBasePath(`/assets/screenshots/previews/${activeTheme.assetPrefix}-badge.webp`);

  const matrixItems = [
    {
      title: 'Appelli & Storico',
      desc: 'Consulta gli appelli aperti, prenota o annulla la partecipazione con un tocco e controlla lo storico completo.',
      icon: <CalendarCheck className="w-4 h-4" aria-hidden="true" />,
    },
    {
      title: 'Tasse & Scadenze',
      desc: 'Situazione pagamenti tra rate pagate e in sospeso, avanzamento complessivo e prossima scadenza sempre chiari.',
      icon: <CreditCard className="w-4 h-4" aria-hidden="true" />,
    },
    {
      title: 'Questionari OPIS',
      desc: 'Compilazione guidata dei questionari di valutazione della didattica direttamente in app con salvataggio immediato.',
      icon: <ClipboardList className="w-4 h-4" aria-hidden="true" />,
    },
    {
      title: 'Notizie & Rubrica',
      desc: 'Notizie di Ateneo con immagini, lettura formattata e condivisione, più rubrica contatti filtrabile per sede e categoria.',
      icon: <Newspaper className="w-4 h-4" aria-hidden="true" />,
    },
    {
      title: 'Area Docente & Multi-Account',
      desc: 'Supporto a carriere studente e docente (Insegnamenti, Appelli, Tesi, Verbali), cambio profilo rapido e gestione Dispositivi connessi.',
      icon: <Users className="w-4 h-4" aria-hidden="true" />,
    },
    {
      title: 'Tema Studio & 10 Lingue',
      desc: 'Color Lab con Spettro, HSB/RGB e Preset, sfondi animati (Orbital Pulse predefinito), notifiche push e interfaccia tradotta in 10 lingue.',
      icon: <Palette className="w-4 h-4" aria-hidden="true" />,
    },
  ];

  return (
    <section id="features">
      <div className="container">
        <div className="split-section-header">
          <div className="split-header-copy">
            <span className="eyebrow">Funzionalità</span>
            <h2 className="display-h2">Progettata attorno all’esperienza reale.</h2>
            <p className="lead-copy">
              Dalla didattica quotidiana ai trasporti, ogni sezione dell’app è pensata per rispondere in un attimo.
            </p>
          </div>
        </div>

        {/* Alternating Apple Editorial Spotlights */}
        <div className="spotlights-stack">
          {/* Spotlight 1: Visual Left · Copy Right */}
          <div className="spotlight-row">
            <div className="spotlight-visual">
              <div className="spotlight-phone-crop">
                <img
                  src={didacticsImg}
                  alt={`Schermata Didattica di UniApp con tema ${activeTheme.fullLabel}`}
                />
              </div>
            </div>

            <div className="spotlight-copy">
              <span className="eyebrow">Carriera &amp; Didattica</span>
              <h3 className="spotlight-title">
                Libretto, statistiche e schede insegnamento in una sola vista.
              </h3>
              <p className="lead-copy">
                Passa dagli esami sostenuti al Piano di studi con un tocco, simula il voto di laurea e consulta i programmi completi dei corsi.
              </p>
              <ul className="spotlight-list">
                <li>
                  <Check className="w-4 h-4 spotlight-check" aria-hidden="true" />
                  <span>Media ponderata e aritmetica, avanzamento CFU e scenari sui CFU rimanenti salvati per carriera.</span>
                </li>
                <li>
                  <Check className="w-4 h-4 spotlight-check" aria-hidden="true" />
                  <span>Schede insegnamento in pagina unica con docenti, sezioni espandibili e link al Course Catalogue CINECA.</span>
                </li>
                <li>
                  <Check className="w-4 h-4 spotlight-check" aria-hidden="true" />
                  <span>Precaricamento prioritario e cache offline per consultare i dati anche senza rete in aula.</span>
                </li>
              </ul>
            </div>
          </div>

          {/* Spotlight 2: Copy Left · Visual Right */}
          <div className="spotlight-row spotlight-row--reversed">
            <div className="spotlight-visual">
              <div className="spotlight-phone-crop">
                <img
                  src={badgeImg}
                  alt={`Schermata Badge Universitario di UniApp con tema ${activeTheme.fullLabel}`}
                />
              </div>
            </div>

            <div className="spotlight-copy">
              <span className="eyebrow">In Aula &amp; In Movimento</span>
              <h3 className="spotlight-title">
                Badge 3D, scanner QR presenze e navette sempre pronti.
              </h3>
              <p className="lead-copy">
                Dalla rilevazione presenze in aula alla prenotazione delle navette universitarie, tutto è integrato nativamente.
              </p>
              <ul className="spotlight-list">
                <li>
                  <Check className="w-4 h-4 spotlight-check" aria-hidden="true" />
                  <span>Tessera studente interattiva 3D con QR code generato nativamente sul dispositivo.</span>
                </li>
                <li>
                  <Check className="w-4 h-4 spotlight-check" aria-hidden="true" />
                  <span>Scanner QR Liquid Monet personalizzato con mirino animato e torcia per registrare le presenze.</span>
                </li>
                <li>
                  <Check className="w-4 h-4 spotlight-check" aria-hidden="true" />
                  <span>Prenotazione navette Andata, Ritorno e A/R con calendario dei giorni feriali e titoli di viaggio.</span>
                </li>
              </ul>
            </div>
          </div>
        </div>

        {/* Unified 6-Cell Apple Hairline Spec Matrix */}
        <div className="spec-matrix">
          {matrixItems.map((item) => (
            <div key={item.title} className="spec-cell">
              <div className="spec-icon">{item.icon}</div>
              <h3 className="spec-cell-title">{item.title}</h3>
              <p className="spec-cell-desc">{item.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};
