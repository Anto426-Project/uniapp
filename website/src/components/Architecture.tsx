import React from 'react';
import { Sparkles, ShieldCheck, Code2 } from 'lucide-react';

export const Architecture: React.FC = () => {
  return (
    <section id="architecture">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">I Valori di UniApp</span>
          <h2 className="section-title">Design Moderno, Privacy &amp; 100% Open Source</h2>
          <p className="section-description">
            Nata dall’esigenza reale di migliorare l’esperienza universitaria: interfaccia fluida, massima protezione dei dati personali e codice completamente trasparente.
          </p>
        </div>

        <div className="arch-cards-grid">
          {/* 1. Design Liquid Monet */}
          <div className="glass-card">
            <div className="card-icon-bubble">
              <Sparkles className="w-6 h-6 text-[var(--theme-primary)]" />
            </div>
            <h3 className="card-title">Design Moderno &amp; Tema Studio</h3>
            <p className="card-text">
              Componenti in vetro ottico Liquid Monet con sfondi animati, Color Lab personalizzabile e palette dinamiche (Violet, Sapphire, Emerald, Amber). Progettata con Kotlin Multiplatform per offrire un’esperienza fluida e nativa su Android e iOS.
            </p>
            <div className="card-tags">
              <span className="arch-tag highlight">Liquid Monet</span>
              <span className="arch-tag">Tema Studio</span>
              <span className="arch-tag">Android &amp; iOS</span>
              <span className="arch-tag">10 Lingue</span>
            </div>
          </div>

          {/* 2. Privacy & Sicurezza */}
          <div className="glass-card">
            <div className="card-icon-bubble">
              <ShieldCheck className="w-6 h-6 text-[var(--theme-primary)]" />
            </div>
            <h3 className="card-title">Privacy &amp; Protezione sul Dispositivo</h3>
            <p className="card-text">
              Le tue credenziali e i dati di sessione sono archiviati in modo cifrato e sicuro direttamente sul tuo smartphone tramite l’hardware di protezione e la biometria (impronta digitale o volto). Nessun dato ceduto a terzi e zero tracciamento.
            </p>
            <div className="card-tags">
              <span className="arch-tag highlight">Protezione Hardware</span>
              <span className="arch-tag">Accesso Biometrico</span>
              <span className="arch-tag">Dati sul Dispositivo</span>
              <span className="arch-tag">Zero Tracciamento</span>
            </div>
          </div>

          {/* 3. 100% Open Source */}
          <div className="glass-card">
            <div className="card-icon-bubble">
              <Code2 className="w-6 h-6 text-[var(--theme-primary)]" />
            </div>
            <h3 className="card-title">100% Libero &amp; Open Source</h3>
            <p className="card-text">
              UniApp è un progetto indipendente e completamente open source. Il codice è pubblico e verificabile da chiunque su GitHub: niente costi, nessuna pubblicità e totale trasparenza, sviluppato con passione per l’intera comunità universitaria.
            </p>
            <div className="card-tags">
              <span className="arch-tag highlight">100% Open Source</span>
              <span className="arch-tag">GitHub Pubblico</span>
              <span className="arch-tag">Nessuna Pubblicità</span>
              <span className="arch-tag">Per gli Studenti</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
