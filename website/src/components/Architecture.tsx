import React from 'react';

export const Architecture: React.FC = () => {
  return (
    <section id="architecture" className="editorial-band">
      <div className="container">
        <div className="editorial-split">
          <div className="editorial-sticky">
            <span className="eyebrow">Architettura</span>
            <h2 className="display-h2">Nativa. Protetta. Aperta.</h2>
            <p className="lead-copy">
              Una base condivisa in Kotlin Multiplatform e Compose Multiplatform con SDK modulari verificati via SHA-256.
            </p>
            <div className="tech-pill-row">
              <span className="tech-pill">Kotlin Multiplatform</span>
              <span className="tech-pill">Compose Multiplatform</span>
              <span className="tech-pill">Liquid Monet</span>
              <span className="tech-pill">Android 10+ &amp; iOS</span>
            </div>
          </div>

          <div className="editorial-chapters">
            <div className="editorial-chapter">
              <span className="chapter-index">01</span>
              <div className="chapter-body">
                <h3>Liquid Monet &amp; Tema Studio</h3>
                <p>
                  Superfici optical glass, palette dinamiche Material You e 4 preset curati (Zaffiro, Smeraldo, Tramonto, Viola) affiancati dal Color Lab. Include icone adattive Android 13+, varianti iOS Default/Dark/Tinted e transizioni unificate che rispettano «Riduci movimento».
                </p>
              </div>
            </div>

            <div className="editorial-chapter">
              <span className="chapter-index">02</span>
              <div className="chapter-body">
                <h3>Sicurezza Biometrica &amp; Storage Cifrato</h3>
                <p>
                  Sblocco dell’app con autenticazione biometrica e PIN a 6 cifre, archiviazione separata e cifrata per ogni account tramite secure-storage-sdk, revoca delle sessioni lato server dai Dispositivi connessi e disabilitazione di backup e traffico in chiaro.
                </p>
              </div>
            </div>

            <div className="editorial-chapter">
              <span className="chapter-index">03</span>
              <div className="chapter-body">
                <h3>100% Open Source &amp; Build Indipendenti</h3>
                <p>
                  Codice sorgente pubblico e ispezionabile su GitHub, zero pubblicità e zero tracciamento. Le build di Android, iOS e sito vengono compilate e pubblicate con workflow indipendenti e impronte SHA-256 trasparenti.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
