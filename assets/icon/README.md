# UniApp — Icon & Brand Export Kit (`Apple Spark U`)

Kit completo vettoriale e raster per il logo ufficiale di **UniApp**: monogramma `"U"` scolpito con terminale arrotondato a sinistra e slancio organico verso una stella a 4 punte concava, su superficie scura ossidiana stile Apple.

## Specifiche colore

| Variante | Sfondo | Glifo |
|---|---|---|
| **Default Dark (Master)** | `#222328` → `#151518` (52%) → `#0B0B0D` | `#FFFFFF` |
| **iOS Dark (OLED)** | `#18181D` → `#0D0D10` (55%) → `#050507` | `#FFFFFF` |
| **iOS Tinted (HIG Grayscale)** | `#2E2E32` → `#141417` | `#FFFFFF` |
| **Light / Porcelain** | `#FFFFFF` → `#ECEEF3` | `#111114` |
| **Android Monet (Monochrome)** | Dinamico di sistema (Material You) | Alpha mask `#000000` (safe-zone `0.72×`) |

---

## Struttura della cartella

### `svg/` — Sorgenti vettoriali pure (`1024×1024`)
- `uniapp-icon-dark.svg` — Master dark ossidiana + glifo bianco
- `uniapp-icon-ios-dark.svg` — Variante iOS 18+ Dark Mode OLED
- `uniapp-icon-light.svg` — Variante Light / invertita
- `uniapp-icon-ios-tinted.svg` — Variante iOS 18+ Tinted grayscale pronta per il tinting di sistema
- `uniapp-icon-background.svg` — Solo layer sfondo ossidiana
- `uniapp-icon-foreground-adaptive.svg` — Layer foreground trasparente scalato `0.72×` nella safe-zone Android `66dp`
- `uniapp-icon-monochrome-adaptive.svg` — Layer monochrome trasparente scalato `0.72×` per Android 13+ Monet
- `uniapp-icon-mark-white.svg` — Solo marchio bianco su sfondo trasparente (full-scale)
- `uniapp-icon-mark-black.svg` — Solo marchio nero (`#111114`) su sfondo trasparente (full-scale)

### `android/` — Risorse Launcher Android drop-in (`res/`)
- `drawable/ic_launcher_background.xml` — VectorDrawable gradiente sfondo
- `drawable/ic_launcher_foreground.xml` — VectorDrawable foreground bianco (`scale 0.72` nella safe-zone `66dp`)
- `drawable/ic_launcher_monochrome.xml` — VectorDrawable monochrome per Android 13+ Themed Icons
- `mipmap-anydpi-v26/ic_launcher.xml` & `ic_launcher_round.xml` — Adaptive Icon completa (`background` + `foreground` + `monochrome`)
- `mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/` — Fallback legacy `ic_launcher.png` (squircle) e `ic_launcher_round.png` (circolare) da `48px` a `192px`
- `adaptive-layers/` — Layer raster `432×432` (`xxxhdpi 108dp`): `ic_launcher_background_432.png`, `ic_launcher_foreground_432.png`, `ic_launcher_monochrome_432.png`

### `android-monet/` — Material You / Monet Themed Icons (Android 13+)
- `drawable/ic_launcher_monochrome.xml` — VectorDrawable pronto per `res/drawable/`
- `ic_launcher_monochrome_1024.png` — Maschera alpha trasparente `1024×1024`
- `ic_launcher_monochrome_432.png` — Maschera alpha trasparente `432×432` (`xxxhdpi`)
- `previews/` — Anteprime circolari `512×512` con palette dinamiche Monet (`sapphire-dark`, `sapphire-light`, `sage-dark`, `rose-dark`, `lavender-dark`)

### `ios/AppIcon.appiconset/` — Xcode Asset Catalog completo
Pronto per sostituire direttamente `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`.
Tutti i **20 PNG** sono esportati rigorosamente in **RGB 24-bit opaco** (nessun canale alpha, conforme a Xcode & App Store Connect):
- `Contents.json` (allineato al catalogo Xcode del progetto)
- Tutti i tagli iPhone (`20@2x/3x`, `29@2x/3x`, `40@2x/3x`, `60@2x/3x`) e iPad (`20@1x/2x`, `29@1x/2x`, `40@1x/2x`, `76@1x/2x`, `83.5@2x`)
- `ios-marketing-1024@1x.png` (`1024×1024`)
- `AppIcon-dark.png` (`1024×1024`, iOS 18+ Dark appearance)
- `AppIcon-tinted.png` (`1024×1024`, iOS 18+ Tinted appearance)

### `ios-color/` — iOS 18+ / iOS 26 Appearances, Icon Composer & Tinted Color
- `AppIcon-default-1024.png` (`1024×1024` RGB opaco)
- `AppIcon-light-1024.png` (`1024×1024` RGB opaco)
- `AppIcon-dark-1024.png` (`1024×1024` RGB opaco)
- `AppIcon-tinted-1024.png` (`1024×1024` RGB opaco grayscale HIG)
- `AppIcon-foreground-layer-1024.png` (`1024×1024` RGBA trasparente — layer glifo per Apple Icon Composer / Liquid Glass)
- `AppIcon-background-layer-1024.png` (`1024×1024` RGB opaco — layer sfondo per Apple Icon Composer)
- `tinted-previews/` — Anteprime squircle iOS `512×512` in 5 tinte (`cobalt`, `crimson`, `emerald`, `amber`, `violet`)

### `play-store/` — Google Play Console
Tutti i file sono esportati in **RGB 24-bit opaco** (senza alpha):
- `play-store-icon-512.png` — Icona app ad alta risoluzione (`512×512`, full-bleed square; Google Play applica la maschera automaticamente)
- `play-store-icon-1024.png` — Master alta risoluzione (`1024×1024`)
- `play-store-feature-graphic-1024x500.png` — Grafica in primo piano (`1024×500`)

### `app-store/` — Apple App Store Connect
Tutti i file sono esportati in **RGB 24-bit opaco** (zero canale alpha):
- `app-store-icon-1024.png` — Icona principale App Store (`1024×1024`)
- `app-store-icon-dark-1024.png` — Master Dark (`1024×1024`)
- `app-store-icon-tinted-1024.png` — Master Tinted (`1024×1024`)
- `app-store-icon-light-1024.png` — Variante Light marketing (`1024×1024`)

### `web/` — Sito Web, PWA & Favicon
- `favicon.svg` — Favicon vettoriale con squircle Apple
- `favicon-32x32.png` — Favicon trasparente `32×32`
- `apple-touch-icon-180x180.png` — Icona Safari iOS `180×180` (RGB opaco)
- `favicon-192x192.png` — Icona PWA `192×192`
- `icon-512x512.png` — Icona PWA `512×512`
