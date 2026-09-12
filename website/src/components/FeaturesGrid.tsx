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
      title: 'Badge Digitale & QR Code',
      desc: 'La tua tessera universitaria digitale sempre con te sullo smartphone: genera il QR code per accedere a biblioteche, aule e servizi.',
      icon: <QrCode className="w-5 h-5" />,
    },
    {
      title: 'Tasse & Scadenze PagoPA',
      desc: 'Riepilogo chiaro delle rate universitarie, bollettini PagoPA, importi esatti e promemoria delle scadenze senza brutte sorprese.',
      icon: <CreditCard className="w-5 h-5" />,
    },
    {
      title: 'Navette Campus & Fermate',
      desc: 'Orari delle corse feriali, fermate sul territorio e prenotazione posti per raggiungere comodamente i campus universitari.',
      icon: <Bus className="w-5 h-5" />,
    },
    {
      title: 'Portali di Ateneo a Portata di Mano',
      desc: 'Accesso rapido a Esse3, Moodle, posta elettronica istituzionale Outlook e rubrica dei docenti da un solo punto.',
      icon: <Globe className="w-5 h-5" />,
    },
    {
      title: 'Questionari Didattici ANVUR',
      desc: 'Compilazione guidata dei questionari di valutazione della didattica direttamente nell’app, con salvataggio delle risposte.',
      icon: <ClipboardList className="w-5 h-5" />,
    },
    {
      title: 'Multi-Account & Modalità Offline',
      desc: 'Gestisci profili o percorsi di studio differenti con avatar personalizzati e consulta il tuo libretto anche in aula senza connessione.',
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
