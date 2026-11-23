import React from 'react';
import { Lock, Activity, ShieldCheck, ExternalLink } from 'lucide-react';

export const SecuritySection: React.FC = () => {
  return (
    <section id="security" className="security-strip-band">
      <div className="container">
        <div className="split-section-header" style={{ marginBottom: 0 }}>
          <div className="split-header-copy">
            <span className="eyebrow">Trasparenza</span>
            <h2 className="display-h2">Integrità &amp; Sicurezza.</h2>
          </div>
          <a
            href="https://github.com/Anto426-Project/uniapp-upstream/releases"
            target="_blank"
            rel="noopener noreferrer"
            className="console-link"
          >
            <span>Verifica impronte SHA-256</span>
            <ExternalLink className="w-3.5 h-3.5" aria-hidden="true" />
          </a>
        </div>

        <div className="security-strip-grid">
          <div className="security-strip-item">
            <div className="spec-icon">
              <Lock className="w-4 h-4" aria-hidden="true" />
            </div>
            <h3>Archiviazione protetta</h3>
            <p>
              Credenziali e sessioni usano l’archiviazione protetta della piattaforma con separazione per account, sblocco biometrico e PIN a 6 cifre.
            </p>
          </div>

          <div className="security-strip-item">
            <div className="spec-icon">
              <Activity className="w-4 h-4" aria-hidden="true" />
            </div>
            <h3>Servizi collegati</h3>
            <p>
              Le funzioni accademiche contattano i servizi dell’Ateneo, CINECA e, per alcune richieste, il proxy sync.faufas.it. Notifiche via FCM e servizi Apple.
            </p>
          </div>

          <div className="security-strip-item">
            <div className="spec-icon">
              <ShieldCheck className="w-4 h-4" aria-hidden="true" />
            </div>
            <h3>Build riproducibili</h3>
            <p>
              Ogni versione pubblicata è accompagnata da commit sorgente, manifest JSON e impronte SHA-256 verificabili direttamente su GitHub.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
};
