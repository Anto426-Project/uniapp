import React from 'react';
import {
  GraduationCap,
  CalendarCheck,
  QrCode,
  CreditCard,
  ClipboardList,
  Bus,
  Globe,
  Users,
} from 'lucide-react';

export const FeaturesGrid: React.FC = () => {
  const features = [
    {
      title: 'Libretto & Simulatore Laurea',
      desc: 'Media ponderata e aritmetica sempre aggiornate, conteggio CFU e proiezione in tempo reale del voto di laurea con simulazione esami.',
      icon: <GraduationCap className="w-5 h-5" />,
    },
    {
      title: 'Prenotazione Appelli Istantanea',
      desc: 'Consulta gli appelli d’esame aperti, prenota o cancella la tua partecipazione con un tocco e non perdere mai una sessione.',
      icon: <CalendarCheck className="w-5 h-5" />,
    },
    {
      title: 'Badge Digitale & Scanner QR Presenze',
      desc: 'Tessera universitaria digitale sempre pronta sul dispositivo e scanner QR Liquid Glass personalizzato per registrare le presenze in aula in un istante.',
      icon: <QrCode className="w-5 h-5" />,
    },
    {
      title: 'Tasse & Scadenze PagoPA',
      desc: 'Riepilogo chiaro delle rate universitarie, bollettini PagoPA, importi esatti e promemoria delle scadenze senza brutte sorprese.',
      icon: <CreditCard className="w-5 h-5" />,
    },
    {
      title: 'Navette Campus & Prenotazione Multipla',
      desc: 'Selettore Andata, Ritorno e A/R, calendario interattivo dei giorni feriali prenotabili, corse garantite e titoli di viaggio sempre a portata di mano.',
      icon: <Bus className="w-5 h-5" />,
    },
    {
      title: 'Schede Insegnamenti & Portali',
      desc: 'Programmi completi con sezioni espandibili, moduli CFU, bibliografie e collegamento diretto al Course Catalogue CINECA, oltre a Esse3, Moodle e rubrica docenti.',
      icon: <Globe className="w-5 h-5" />,
    },
    {
      title: 'Questionari Didattici OPIS',
      desc: 'Compilazione guidata dei questionari di valutazione della didattica direttamente nell’app, con salvataggio immediato delle risposte.',
      icon: <ClipboardList className="w-5 h-5" />,
    },
    {
      title: 'Multi-Account, Offline & 10 Lingue',
      desc: 'Gestisci più carriere con caricamento prioritario intelligente, cache offline per consultare il libretto in aula e interfaccia tradotta in 10 lingue.',
      icon: <Users className="w-5 h-5" />,
    },
  ];

  return (
    <section id="features">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">Funzionalità per Studenti</span>
          <h2 className="section-title">Tutto ciò che serve per la tua carriera</h2>
          <p className="section-description">
            Strumenti pratici pensati per rendere la vita universitaria all’UniMol più semplice, ordinata e immediata.
          </p>
        </div>

        <div className="features-grid">
          {features.map((f, idx) => (
            <div key={idx} className="feature-card">
              <div className="feature-icon-wrapper">{f.icon}</div>
              <h3 className="feature-title">{f.title}</h3>
              <p className="feature-desc">{f.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};
