#!/usr/bin/env node
/**
 * UniApp — Cattura automatica (o guidata) di tutte le schermate dal telefono Android via ADB.
 *
 * Cattura tutti i 4 temi (Zaffiro, Smeraldo, Tramonto, Viola) × entrambi gli aspetti
 * (Scuro e Chiaro) × le 8 schermate del sito = 64 screenshot completi:
 *   - Scuro:  website/public/assets/screenshots/{tema}-{schermata}.jpg
 *   - Chiaro: website/public/assets/screenshots/{tema}-light-{schermata}.jpg
 *
 * Include anche Informazioni UniApp (info), Aggiornamenti (aggiornamenti / alias update)
 * e Creatore e crediti (crediti, con scorrimento verso il basso prima dello scatto).
 *
 * Ritaglia automaticamente la barra di stato/notifiche in alto (altezza rilevata dal
 * dispositivo, es. 160px su 1440x3168) e, se l'app è sulla schermata di accesso, effettua
 * il login automatico con le credenziali pubbliche dell'account demo.
 *
 * Al termine rigenera automaticamente le anteprime WebP (520px) e l'indice
 * website/src/data/available-screenshots.json così il sito mostra subito sia
 * gli screenshot scuri che quelli chiari.
 */

import { execFileSync, spawnSync } from 'node:child_process';
import { existsSync, mkdirSync, readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { createRequire } from 'node:module';
import { homedir } from 'node:os';
import { dirname, extname, isAbsolute, join, resolve } from 'node:path';
import { createInterface } from 'node:readline/promises';
import { fileURLToPath } from 'node:url';

const SCRIPT_DIR = dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = resolve(SCRIPT_DIR, '../..');
const WEBSITE_DIR = resolve(REPO_ROOT, 'website');
const SCREENSHOTS_DIR = resolve(WEBSITE_DIR, 'public/assets/screenshots');
const QUICK_DIR = resolve(REPO_ROOT, '.tmp/phone-screenshots');

const THEMES = [
  { id: 'sapphire', label: 'Zaffiro' },
  { id: 'emerald', label: 'Smeraldo' },
  { id: 'sunset', label: 'Tramonto' },
  { id: 'violet', label: 'Viola' },
];

const APPEARANCES = [
  { id: 'dark', label: 'Scuro', suffix: '' },
  { id: 'light', label: 'Chiaro', suffix: '-light' },
];

const SCREENS = [
  { id: 'impostazioni', label: 'Impostazioni' },
  { id: 'aggiornamenti', label: 'Aggiornamenti (Update)' },
  { id: 'info', label: 'Informazioni UniApp' },
  { id: 'crediti', label: 'Creatore e crediti (in basso)' },
  { id: 'home', label: 'Dashboard (UniApp)' },
  { id: 'badge', label: 'Badge UniPass 3D' },
  { id: 'servizi', label: 'Servizi' },
  { id: 'didattica', label: 'Didattica' },
];

const SCREEN_ALIASES = {
  update: 'aggiornamenti',
  updates: 'aggiornamenti',
  credits: 'crediti',
  settings: 'impostazioni',
};

// Public presentation credentials from DemoAccount.kt (local synthetic data only)
const DEMO_CREDENTIALS = {
  username: 'demo@uniapp.local',
  password: 'UniAppDemo2026!',
};

// Default top status-bar ratio fallback (160px on 1440x3168 -> ~5.05%)
const DEFAULT_STATUS_BAR_RATIO = 160 / 3168;

function parseFilterList(raw, aliases = {}) {
  return raw
    .split(',')
    .map((part) => part.trim().toLowerCase())
    .filter(Boolean)
    .map((item) => aliases[item] || item);
}

function filenameFor(themeId, appearanceId, screenId) {
  const suffix = appearanceId === 'light' ? '-light' : '';
  return `${themeId}${suffix}-${screenId}.jpg`;
}

function parseArgs(argv) {
  const opts = {
    mode: 'auto', // auto | manual | quick
    themes: [],
    appearances: [],
    screens: [],
    out: null,
    serial: null,
    delay: 850,
    toastDelay: 3200,
    quality: 92,
    cropTop: 'auto', // 'auto' | number (px) | 0 (disabled)
    cropExisting: false,
    autoDemoLogin: true,
    png: false,
    check: false,
    debug: false,
    cleanStatusBar: false,
    noSync: false,
    help: false,
  };

  for (let i = 0; i < argv.length; i++) {
    const arg = argv[i];
    const next = () => {
      if (i + 1 >= argv.length) throw new Error(`Valore mancante per ${arg}`);
      return argv[++i];
    };

    switch (arg) {
      case '-h':
      case '--help':
        opts.help = true;
        break;
      case '--auto':
        opts.mode = 'auto';
        break;
      case '--manual':
        opts.mode = 'manual';
        break;
      case '--quick':
        opts.mode = 'quick';
        break;
      case '-t':
      case '--theme':
        opts.themes.push(...parseFilterList(next()));
        break;
      case '-a':
      case '--appearance':
        opts.appearances.push(...parseFilterList(next()));
        break;
      case '-s':
      case '--screen':
        opts.screens.push(...parseFilterList(next(), SCREEN_ALIASES));
        break;
      case '-o':
      case '--out':
        opts.out = next();
        break;
      case '--serial':
        opts.serial = next();
        break;
      case '-d':
      case '--delay':
        opts.delay = Math.max(0, Number(next()) || 850);
        break;
      case '--toast-delay':
        opts.toastDelay = Math.max(0, Number(next()) || 3200);
        break;
      case '-q':
      case '--quality':
        opts.quality = Math.min(100, Math.max(60, Number(next()) || 92));
        break;
      case '--crop-top': {
        const val = Number(next());
        if (!Number.isFinite(val) || val < 0) {
          throw new Error('--crop-top richiede un numero di pixel >= 0');
        }
        opts.cropTop = Math.round(val);
        break;
      }
      case '--no-crop-top':
        opts.cropTop = 0;
        break;
      case '--crop-existing':
        opts.cropExisting = true;
        break;
      case '--no-demo-login':
        opts.autoDemoLogin = false;
        break;
      case '--png':
        opts.png = true;
        break;
      case '--check':
        opts.check = true;
        break;
      case '--debug':
        opts.debug = true;
        break;
      case '--clean-status-bar':
        opts.cleanStatusBar = true;
        break;
      case '--no-sync':
        opts.noSync = true;
        break;
      default:
        throw new Error(`Opzione non riconosciuta: ${arg}`);
    }
  }
  return opts;
}

function printHelp() {
  console.log(`
UniApp — Cattura automatica screenshot da telefono Android (tutti i temi + Scuro/Chiaro)

Uso:
  node scripts/media/capture-phone-screenshots.mjs [opzioni]

Modalità:
  (default / --auto)          AUTOMATICA: naviga da sola in UniApp via ADB/UIAutomator e
                              cattura tutti i 4 temi × Scuro & Chiaro × 8 schermate (64 file),
                              poi rigenera anteprime WebP e indice per il sito.
  --manual                    Sessione guidata manuale (premi Invio a ogni schermata)
  --quick                     Scatti rapidi continui in .tmp/phone-screenshots/
  --out <file>                Scatto singolo immediato in un percorso personalizzato
  --crop-existing             Taglia in alto (barra notifiche) tutti gli screenshot già salvati
                              in website/public/assets/screenshots/ e rigenera le WebP (idempotente)
  --check                     Verifica ADB, telefono collegato, risoluzione e pacchetto UniApp

Filtri (combinabili e separabili da virgola, con --auto o --manual):
  --theme <tema>              sapphire | emerald | sunset | violet  (default: tutti e 4)
  --appearance <aspetto>      dark | light                          (default: entrambi)
  --screen <schermata>        home | servizi | didattica | badge | impostazioni |
                              aggiornamenti (alias: update) | info | crediti (in basso)

Opzioni:
  --crop-top <px>             Altezza taglio superiore in pixel (default: auto da barra notifiche, es. 160px)
  --no-crop-top               Disattiva il taglio della barra notifiche in alto
  --no-demo-login             Disattiva il login automatico demo se l'app è sulla schermata di accesso
  --debug                     Usa com.anto426.uniapp.debug invece di com.anto426.uniapp
  --delay <ms>                Attesa dopo ogni cambio schermata (default: 850ms)
  --toast-delay <ms>          Attesa dopo cambio tema/aspetto per far sparire i toast (default: 3200ms)
  --clean-status-bar          Attiva SystemUI Demo Mode durante gli scatti e ripristina alla fine
  --serial <id>               Seleziona un dispositivo specifico
  --quality <60..100>         Qualità JPEG (default: 92)
  --no-sync                   Non esegue website/scripts/prepare-screenshots.mjs al termine

Esempi:
  node scripts/media/capture-phone-screenshots.mjs --check
  node scripts/media/capture-phone-screenshots.mjs
  node scripts/media/capture-phone-screenshots.mjs --crop-existing
  node scripts/media/capture-phone-screenshots.mjs --appearance light
  node scripts/media/capture-phone-screenshots.mjs --screen info,update,crediti
  node scripts/media/capture-phone-screenshots.mjs --theme sapphire --appearance dark --screen crediti
  node scripts/media/capture-phone-screenshots.mjs --manual --theme emerald
`);
}

function readSdkDirFromLocalProperties() {
  const propsPath = resolve(REPO_ROOT, 'local.properties');
  if (!existsSync(propsPath)) return null;
  const content = readFileSync(propsPath, 'utf8');
  for (const rawLine of content.split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith('#')) continue;
    const match = line.match(/^sdk\.dir\s*=\s*(.+)$/);
    if (match) return match[1].replace(/\\:/g, ':').replace(/\\\\/g, '\\').trim();
  }
  return null;
}

function findAdbBinary() {
  const isWin = process.platform === 'win32';
  const exe = isWin ? 'adb.exe' : 'adb';
  const candidates = [];

  if (process.env.ADB) candidates.push(process.env.ADB);
  const sdkFromProps = readSdkDirFromLocalProperties();
  if (sdkFromProps) candidates.push(join(sdkFromProps, 'platform-tools', exe));
  if (process.env.ANDROID_HOME) candidates.push(join(process.env.ANDROID_HOME, 'platform-tools', exe));
  if (process.env.ANDROID_SDK_ROOT) candidates.push(join(process.env.ANDROID_SDK_ROOT, 'platform-tools', exe));
  if (isWin && process.env.LOCALAPPDATA) {
    candidates.push(join(process.env.LOCALAPPDATA, 'Android', 'Sdk', 'platform-tools', exe));
  }
  candidates.push(join(homedir(), 'Library', 'Android', 'sdk', 'platform-tools', exe));
  candidates.push(join(homedir(), 'Android', 'Sdk', 'platform-tools', exe));

  for (const candidate of candidates) {
    if (candidate && existsSync(candidate)) return candidate;
  }

  const probe = spawnSync(exe, ['version'], { encoding: 'utf8' });
  if (probe.status === 0) return exe;

  throw new Error('ADB non trovato. Verifica Android Platform-Tools o local.properties.');
}

function runAdb(adb, serial, args, options = {}) {
  const fullArgs = serial ? ['-s', serial, ...args] : args;
  return execFileSync(adb, fullArgs, { maxBuffer: 64 * 1024 * 1024, ...options });
}

function listConnectedDevices(adb) {
  const output = execFileSync(adb, ['devices', '-l'], { encoding: 'utf8' });
  const devices = [];
  for (const line of output.split(/\r?\n/).slice(1)) {
    const trimmed = line.trim();
    if (!trimmed) continue;
    const parts = trimmed.split(/\s+/);
    if (parts.length < 2) continue;
    const [serial, state, ...rest] = parts;
    const modelMatch = rest.join(' ').match(/model:(\S+)/);
    devices.push({ serial, state, model: modelMatch ? modelMatch[1] : 'Android' });
  }
  return devices;
}

function resolveDevice(adb, requestedSerial) {
  const devices = listConnectedDevices(adb);
  const ready = devices.filter((d) => d.state === 'device');

  if (requestedSerial) {
    const found = ready.find((d) => d.serial === requestedSerial);
    if (!found) throw new Error(`Dispositivo "${requestedSerial}" non trovato o non autorizzato.`);
    return found;
  }
  if (ready.length === 0) {
    const unauth = devices.find((d) => d.state === 'unauthorized');
    if (unauth) {
      throw new Error(`Dispositivo ${unauth.serial} non autorizzato: conferma il popup Debug USB sul telefono.`);
    }
    throw new Error('Nessun telefono Android collegato via ADB.');
  }
  if (ready.length > 1) {
    throw new Error(`Più dispositivi collegati. Specifica --serial:\n${ready.map((d) => `  - ${d.serial} (${d.model})`).join('\n')}`);
  }
  return ready[0];
}

function getScreenDimensions(adb, serial) {
  const raw = runAdb(adb, serial, ['shell', 'wm', 'size'], { encoding: 'utf8' });
  const matches = [...raw.matchAll(/(\d+)x(\d+)/g)];
  const last = matches[matches.length - 1];
  if (!last) return { width: 1080, height: 2400 };
  return { width: Number(last[1]), height: Number(last[2]) };
}

function getStatusBarHeight(adb, serial, screenSize) {
  try {
    const raw = runAdb(adb, serial, ['shell', 'dumpsys', 'window', 'displays'], { encoding: 'utf8' });
    const candidates = [];
    for (const match of raw.matchAll(/type=(?:statusBars|displayCutout)[^\r\n]*frame=\[0,0\]\[\d+,(\d+)\]/g)) {
      const h = Number(match[1]);
      if (h > 0 && h < screenSize.height * 0.15) candidates.push(h);
    }
    if (candidates.length > 0) return Math.max(...candidates);
  } catch {}
  return Math.round(screenSize.height * DEFAULT_STATUS_BAR_RATIO);
}

function loadSharp() {
  try {
    const req = createRequire(resolve(WEBSITE_DIR, 'package.json'));
    return req('sharp');
  } catch {
    return null;
  }
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

function enableCleanStatusBar(adb, serial) {
  try {
    runAdb(adb, serial, ['shell', 'settings', 'put', 'global', 'sysui_demo_allowed', '1']);
    runAdb(adb, serial, ['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'enter']);
    runAdb(adb, serial, ['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'clock', '-e', 'hhmm', '0941']);
    runAdb(adb, serial, ['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'battery', '-e', 'level', '100', '-e', 'plugged', 'false']);
    runAdb(adb, serial, ['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'network', '-e', 'wifi', 'show', '-e', 'level', '4', '-e', 'mobile', 'hide']);
    runAdb(adb, serial, ['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'notifications', '-e', 'visible', 'false']);
    return true;
  } catch {
    console.warn('Avviso: SystemUI Demo Mode non supportata da questa ROM.');
    return false;
  }
}

function disableCleanStatusBar(adb, serial) {
  try {
    runAdb(adb, serial, ['shell', 'am', 'broadcast', '-a', 'com.android.systemui.demo', '-e', 'command', 'exit']);
  } catch {}
}

function detectInstalledPackage(adb, serial, preferDebug) {
  const raw = runAdb(adb, serial, ['shell', 'pm', 'list', 'packages', 'com.anto426.uniapp'], { encoding: 'utf8' });
  const installed = raw
    .split(/\r?\n/)
    .map((l) => l.replace(/^package:/, '').trim())
    .filter(Boolean);
  if (preferDebug && installed.includes('com.anto426.uniapp.debug')) return 'com.anto426.uniapp.debug';
  if (installed.includes('com.anto426.uniapp')) return 'com.anto426.uniapp';
  if (installed.includes('com.anto426.uniapp.debug')) return 'com.anto426.uniapp.debug';
  return preferDebug ? 'com.anto426.uniapp.debug' : 'com.anto426.uniapp';
}

function launchUniApp(adb, serial, pkg) {
  runAdb(adb, serial, ['shell', 'am', 'start', '-n', `${pkg}/com.anto426.uniapp.android.MainActivity`], {
    stdio: 'ignore',
  });
}

function decodeXmlEntities(str) {
  return str
    .replace(/&quot;/g, '"')
    .replace(/&apos;/g, "'")
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&');
}

/**
 * Reads live accessibility hierarchy via `uiautomator dump` and parses all nodes with bounds.
 */
function dumpUiNodes(adb, serial) {
  const remotePath = '/sdcard/uniapp_ui_dump.xml';
  runAdb(adb, serial, ['shell', 'uiautomator', 'dump', '--compressed', remotePath], { stdio: 'ignore' });
  const xml = runAdb(adb, serial, ['shell', 'cat', remotePath], { encoding: 'utf8' });

  const nodes = [];
  const nodeRegex = /<node\s+([^>]+?)\/?>/g;
  let match;
  while ((match = nodeRegex.exec(xml)) !== null) {
    const attrsRaw = match[1];
    const getAttr = (name) => {
      const m = attrsRaw.match(new RegExp(`${name}="([^"]*)"`));
      return m ? decodeXmlEntities(m[1]) : '';
    };
    const boundsStr = getAttr('bounds');
    const b = boundsStr.match(/\[(\d+),(\d+)\]\[(\d+),(\d+)\]/);
    if (!b) continue;
    const x1 = Number(b[1]);
    const y1 = Number(b[2]);
    const x2 = Number(b[3]);
    const y2 = Number(b[4]);
    nodes.push({
      text: getAttr('text'),
      desc: getAttr('content-desc'),
      className: getAttr('class'),
      password: getAttr('password') === 'true',
      focused: getAttr('focused') === 'true',
      clickable: getAttr('clickable') === 'true',
      x1,
      y1,
      x2,
      y2,
      cx: Math.round((x1 + x2) / 2),
      cy: Math.round((y1 + y2) / 2),
      width: x2 - x1,
      height: y2 - y1,
    });
  }
  return nodes;
}

function tap(adb, serial, x, y) {
  runAdb(adb, serial, ['shell', 'input', 'tap', String(Math.round(x)), String(Math.round(y))]);
}

function swipe(adb, serial, x1, y1, x2, y2, durationMs = 260) {
  runAdb(adb, serial, [
    'shell',
    'input',
    'swipe',
    String(Math.round(x1)),
    String(Math.round(y1)),
    String(Math.round(x2)),
    String(Math.round(y2)),
    String(durationMs),
  ]);
}

function pressBack(adb, serial) {
  runAdb(adb, serial, ['shell', 'input', 'keyevent', '4']);
}

function inputText(adb, serial, text) {
  // Single-quote for Android mksh so characters like ! and @ are delivered literally
  const quoted = `'${String(text).replace(/'/g, "'\\''").replace(/ /g, '%s')}'`;
  runAdb(adb, serial, ['shell', 'input', 'text', quoted]);
}

function clearFocusedField(adb, serial) {
  try {
    runAdb(adb, serial, ['shell', 'input', 'keycombination', '113', '29']); // Ctrl+A
    runAdb(adb, serial, ['shell', 'input', 'keyevent', '67']); // DEL
  } catch {
    // Fallback if older Android does not support keycombination
    for (let i = 0; i < 32; i++) {
      runAdb(adb, serial, ['shell', 'input', 'keyevent', '67']);
    }
  }
}

function findNode(nodes, matcher) {
  const candidates = nodes.filter(matcher).filter((n) => n.width > 0 && n.height > 0);
  if (candidates.length === 0) return null;
  // Prefer smallest concrete matching element so we tap the exact button/label center
  candidates.sort((a, b) => a.width * a.height - b.width * b.height);
  return candidates[0];
}

function textMatches(node, patterns) {
  const text = (node.text || '').trim().toLowerCase();
  const desc = (node.desc || '').trim().toLowerCase();
  for (const p of patterns) {
    if (typeof p === 'string') {
      const target = p.toLowerCase();
      if (text === target || desc === target) return true;
    } else if (p instanceof RegExp) {
      if (p.test(text) || p.test(desc)) return true;
    }
  }
  return false;
}

async function tapNodeByPatterns(adb, serial, patterns, { screenSize, region = null, scrollAttempts = 0, scrollDir = 'down' } = {}) {
  for (let attempt = 0; attempt <= scrollAttempts; attempt++) {
    const nodes = dumpUiNodes(adb, serial);
    const node = findNode(nodes, (n) => {
      if (!textMatches(n, patterns)) return false;
      if (region === 'bottomBar' && screenSize && n.cy < screenSize.height * 0.8) return false;
      if (region === 'content' && screenSize && n.cy > screenSize.height * 0.87) return false;
      return true;
    });
    if (node) {
      tap(adb, serial, node.cx, node.cy);
      return node;
    }
    if (attempt < scrollAttempts && screenSize) {
      const midX = screenSize.width * 0.5;
      if (scrollDir === 'down') {
        swipe(adb, serial, midX, screenSize.height * 0.72, midX, screenSize.height * 0.3, 280);
      } else {
        swipe(adb, serial, midX, screenSize.height * 0.3, midX, screenSize.height * 0.74, 280);
      }
      await sleep(500);
    }
  }
  return null;
}

async function scrollToTop(adb, serial, screenSize) {
  const midX = screenSize.width * 0.5;
  swipe(adb, serial, midX, screenSize.height * 0.28, midX, screenSize.height * 0.8, 220);
  await sleep(350);
  swipe(adb, serial, midX, screenSize.height * 0.28, midX, screenSize.height * 0.8, 220);
  await sleep(350);
}

async function scrollToBottom(adb, serial, screenSize, swipes = 2) {
  // Swipe upward with finger so the screen content scrolls DOWN toward the bottom
  const midX = screenSize.width * 0.5;
  for (let i = 0; i < swipes; i++) {
    swipe(adb, serial, midX, screenSize.height * 0.78, midX, screenSize.height * 0.22, 320);
    await sleep(420);
  }
}

async function waitUntilTextGone(adb, serial, patterns, maxWaitMs = 4500) {
  const deadline = Date.now() + maxWaitMs;
  while (Date.now() < deadline) {
    try {
      const nodes = dumpUiNodes(adb, serial);
      const stillVisible = nodes.some((n) => textMatches(n, patterns));
      if (!stillVisible) return;
    } catch {
      return;
    }
    await sleep(550);
  }
}

async function ensureBottomBarVisible(adb, serial, screenSize) {
  const hasBottomNav = () => {
    const nodes = dumpUiNodes(adb, serial);
    return Boolean(
      findNode(nodes, (n) => textMatches(n, [/^uniapp$/, /^servizi$/, /^didattica$/, /^impostazi/]) && n.cy > screenSize.height * 0.8)
    );
  };

  if (hasBottomNav()) return true;

  // Small upward scroll reveals auto-hidden floating bottom navigation bar if needed
  const midX = screenSize.width * 0.5;
  swipe(adb, serial, midX, screenSize.height * 0.4, midX, screenSize.height * 0.7, 220);
  await sleep(450);
  if (hasBottomNav()) return true;

  // If the app was left inside a nested sub-screen, step back to root navigation
  for (let attempt = 0; attempt < 3; attempt++) {
    pressBack(adb, serial);
    await sleep(550);
    if (hasBottomNav()) return true;
  }
  return false;
}

function isLoginScreen(nodes) {
  return nodes.some((n) =>
    textMatches(n, [
      'Accedi con Esse3',
      /^accedi con esse3/i,
      /credenziali d.?accesso/i,
      'Nome utente o matricola',
      'Password Esse3',
    ])
  );
}

function findEditableFields(nodes) {
  return nodes
    .filter((n) => /EditText/i.test(n.className || '') && n.width > 120 && n.height > 20)
    .sort((a, b) => a.cy - b.cy);
}

async function ensureDemoLogin(adb, serial, screenSize) {
  const initialNodes = dumpUiNodes(adb, serial);
  if (!isLoginScreen(initialNodes)) {
    return false;
  }

  console.log(`Schermata di accesso rilevata: eseguo login automatico con account demo (${DEMO_CREDENTIALS.username})...`);
  await scrollToTop(adb, serial, screenSize);

  // 1. Locate and fill Username field
  let nodes = dumpUiNodes(adb, serial);
  let fields = findEditableFields(nodes);
  const usernameField =
    fields.find((n) => !n.password) ||
    findNode(nodes, (n) => textMatches(n, [/m\.rossi|123456|nome utente o matricola/i]));

  if (!usernameField) {
    throw new Error('Campo username non trovato nella schermata di login.');
  }

  tap(adb, serial, usernameField.cx, usernameField.cy);
  await sleep(400);
  if (usernameField.text && !/m\.rossi|123456|nome utente/i.test(usernameField.text)) {
    clearFocusedField(adb, serial);
    await sleep(180);
  }
  inputText(adb, serial, DEMO_CREDENTIALS.username);
  await sleep(400);

  // 2. Re-dump after IME opens and locate Password field
  nodes = dumpUiNodes(adb, serial);
  fields = findEditableFields(nodes);
  let passwordField =
    fields.find((n) => n.password) ||
    (fields.length >= 2 ? fields[1] : null) ||
    findNode(nodes, (n) => textMatches(n, [/inserisci la tua password|password esse3/i]));

  if (!passwordField) {
    pressBack(adb, serial); // hide keyboard to reveal password field
    await sleep(450);
    nodes = dumpUiNodes(adb, serial);
    fields = findEditableFields(nodes);
    passwordField =
      fields.find((n) => n.password) ||
      (fields.length >= 2 ? fields[1] : null) ||
      findNode(nodes, (n) => textMatches(n, [/inserisci la tua password|password esse3/i]));
  }

  if (!passwordField) {
    throw new Error('Campo password non trovato nella schermata di login.');
  }

  tap(adb, serial, passwordField.cx, passwordField.cy);
  await sleep(400);
  if (passwordField.text && !/inserisci la tua password|password/i.test(passwordField.text)) {
    clearFocusedField(adb, serial);
    await sleep(180);
  }
  inputText(adb, serial, DEMO_CREDENTIALS.password);
  await sleep(400);

  // 3. Hide soft keyboard so "Accedi con Esse3" is clearly tappable, then submit
  pressBack(adb, serial);
  await sleep(450);

  const submitted = await tapNodeByPatterns(
    adb,
    serial,
    ['Accedi con Esse3', /^accedi con esse3/i, /^accedi$/i],
    {
      screenSize,
      scrollAttempts: 2,
      scrollDir: 'down',
    }
  );
  if (!submitted) {
    throw new Error('Pulsante "Accedi con Esse3" non trovato nella schermata di login.');
  }

  // 4. Wait until dashboard / bottom navigation appears (or pick career if dialog opens)
  const deadline = Date.now() + 10000;
  while (Date.now() < deadline) {
    await sleep(650);
    const liveNodes = dumpUiNodes(adb, serial);
    const hasBottomNav = Boolean(
      findNode(
        liveNodes,
        (n) => textMatches(n, [/^uniapp$/, /^servizi$/, /^didattica$/, /^impostazi/]) && n.cy > screenSize.height * 0.8
      )
    );
    if (hasBottomNav) {
      console.log('Login demo completato con successo.');
      await sleep(1200);
      return true;
    }
    const careerDialog = liveNodes.some((n) => textMatches(n, ['Seleziona carriera', /^seleziona carriera/i]));
    if (careerDialog) {
      await tapNodeByPatterns(adb, serial, [/DEMO-2026001/i, /informatica/i], { screenSize });
    }
  }

  throw new Error('Timeout durante il login demo: verifica che le credenziali demo siano attive in questa build.');
}

async function openBottomTab(adb, serial, screenSize, tabPatterns, fallbackFractionX) {
  await ensureBottomBarVisible(adb, serial, screenSize);
  const found = await tapNodeByPatterns(adb, serial, tabPatterns, {
    screenSize,
    region: 'bottomBar',
  });
  if (!found) {
    // Clean proportional fallback inside the floating bottom dock
    tap(adb, serial, screenSize.width * fallbackFractionX, screenSize.height * 0.93);
  }
  await sleep(650);
}

async function applyThemeAndAppearance(adb, serial, screenSize, themeMeta, appearanceMeta, toastDelayMs) {
  // Go to Impostazioni -> Tema e colori
  await openBottomTab(adb, serial, screenSize, [/^impostazi/i], 0.84);
  await scrollToTop(adb, serial, screenSize);

  const openedTheme = await tapNodeByPatterns(adb, serial, ['Tema e colori', /^tema e colori/i], {
    screenSize,
    region: 'content',
    scrollAttempts: 2,
    scrollDir: 'down',
  });
  if (!openedTheme) {
    throw new Error('Voce "Tema e colori" non trovata in Impostazioni. Assicurati di essere loggato in UniApp in lingua Italiana.');
  }
  await sleep(750);

  // Ensure "Stile" tab is active and at top
  await scrollToTop(adb, serial, screenSize);
  await tapNodeByPatterns(adb, serial, ['Stile'], { screenSize, region: 'content' });
  await sleep(350);

  // Select Dark / Light mode
  const tappedMode = await tapNodeByPatterns(adb, serial, [appearanceMeta.label], {
    screenSize,
    region: 'content',
    scrollAttempts: 1,
    scrollDir: 'down',
  });
  if (!tappedMode) {
    throw new Error(`Modalità "${appearanceMeta.label}" non trovata in Tema Studio.`);
  }
  await sleep(650);

  // Select Monet palette (Zaffiro, Smeraldo, Tramonto, Viola)
  const tappedPalette = await tapNodeByPatterns(adb, serial, [themeMeta.label], {
    screenSize,
    region: 'content',
    scrollAttempts: 2,
    scrollDir: 'down',
  });
  if (!tappedPalette) {
    throw new Error(`Tema "${themeMeta.label}" non trovato in Tema Studio.`);
  }

  // Wait for theme transition + toast fade-out before returning to Settings
  if (toastDelayMs > 0) {
    process.stdout.write(`  Tema ${themeMeta.label} (${appearanceMeta.label}) applicato, attendo scomparsa toast (${toastDelayMs}ms)...\n`);
    await sleep(toastDelayMs);
  }

  // Return from Tema Studio to Impostazioni
  pressBack(adb, serial);
  await sleep(700);
}

async function captureToFile({ adb, serial, sharp, targetPath, quality, asPng, cropTop = 0 }) {
  const pngBuffer = runAdb(adb, serial, ['exec-out', 'screencap', '-p']);
  if (!pngBuffer || pngBuffer.length < 100 || pngBuffer[0] !== 0x89 || pngBuffer[1] !== 0x50) {
    throw new Error('Screenshot non valido (schermo bloccato o protetto da FLAG_SECURE).');
  }

  mkdirSync(dirname(targetPath), { recursive: true });
  const wantPng = asPng || extname(targetPath).toLowerCase() === '.png' || !sharp;

  if (!sharp) {
    writeFileSync(targetPath, pngBuffer);
    return pngBuffer;
  }

  let pipeline = sharp(pngBuffer);
  if (cropTop > 0) {
    const meta = await pipeline.metadata();
    const safeCrop = Math.min(Math.max(0, Math.round(cropTop)), Math.floor((meta.height || 0) * 0.2));
    if (safeCrop > 0 && meta.width && meta.height > safeCrop) {
      pipeline = pipeline.extract({
        left: 0,
        top: safeCrop,
        width: meta.width,
        height: meta.height - safeCrop,
      });
    }
  }

  if (wantPng) {
    await pipeline.png().toFile(targetPath);
  } else {
    await pipeline.jpeg({ quality, mozjpeg: true }).toFile(targetPath);
  }
  return pngBuffer;
}

async function cropExistingScreenshots({ sharp, cropTop, quality }) {
  if (!sharp) {
    throw new Error('Sharp non disponibile in website/node_modules: impossibile tagliare gli screenshot esistenti.');
  }
  if (!existsSync(SCREENSHOTS_DIR)) {
    throw new Error(`Cartella screenshot non trovata: ${SCREENSHOTS_DIR}`);
  }

  const files = readdirSync(SCREENSHOTS_DIR)
    .filter((name) => /\.(jpg|jpeg|png)$/i.test(name))
    .sort();

  let croppedCount = 0;
  let skippedCount = 0;

  for (const file of files) {
    const fullPath = resolve(SCREENSHOTS_DIR, file);
    const raw = readFileSync(fullPath);
    const meta = await sharp(raw).metadata();
    if (!meta.width || !meta.height) continue;

    // Idempotency guard when using automatic mode: already cropped phone screenshots have ratio < 2.14
    if (cropTop === 'auto' && meta.height / meta.width < 2.14) {
      skippedCount += 1;
      continue;
    }

    const topPx =
      typeof cropTop === 'number'
        ? cropTop
        : Math.round(meta.height * DEFAULT_STATUS_BAR_RATIO);

    if (topPx <= 0 || topPx >= meta.height * 0.25) {
      skippedCount += 1;
      continue;
    }

    const pipeline = sharp(raw).extract({
      left: 0,
      top: topPx,
      width: meta.width,
      height: meta.height - topPx,
    });
    const outBuf =
      extname(file).toLowerCase() === '.png'
        ? await pipeline.png().toBuffer()
        : await pipeline.jpeg({ quality, mozjpeg: true }).toBuffer();

    writeFileSync(fullPath, outBuf);
    croppedCount += 1;
    console.log(`  Tagliato ${file} (${meta.width}x${meta.height} -> ${meta.width}x${meta.height - topPx}, top -${topPx}px)`);
  }

  console.log(`\nTaglio completato: ${croppedCount} screenshot aggiornati, ${skippedCount} già tagliati/saltati.`);
}

function syncWebsitePreviews() {
  const script = resolve(WEBSITE_DIR, 'scripts/prepare-screenshots.mjs');
  if (!existsSync(script)) return;
  console.log('\nRigenero anteprime WebP e indice del sito...');
  execFileSync(process.execPath, [script], { cwd: WEBSITE_DIR, stdio: 'inherit' });
}

function timestampName(ext) {
  const now = new Date();
  const pad = (n) => String(n).padStart(2, '0');
  return `uniapp-${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}-${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}.${ext}`;
}

async function runAutomaticCapture({ adb, serial, sharp, screenSize, cropTopPx, opts, selectedThemes, selectedAppearances, selectedScreenIds }) {
  const totalCombos = selectedThemes.length * selectedAppearances.length;
  const totalShots = totalCombos * selectedScreenIds.size;
  let captured = 0;

  console.log(
    `\nAvvio cattura automatica: ${selectedThemes.length} temi × ${selectedAppearances.length} aspetti × ${selectedScreenIds.size} schermate = ${totalShots} screenshot.\nNon toccare lo schermo durante la cattura.\n`
  );

  const snap = async (themeMeta, appearanceMeta, screenId) => {
    const filename = filenameFor(themeMeta.id, appearanceMeta.id, screenId);
    const targetPath = resolve(SCREENSHOTS_DIR, filename);
    await sleep(opts.delay);
    await captureToFile({ adb, serial, sharp, targetPath, quality: opts.quality, asPng: false, cropTop: cropTopPx });
    captured += 1;
    console.log(`  [${captured}/${totalShots}] Salvato ${filename}`);
  };

  for (const appearanceMeta of selectedAppearances) {
    for (const themeMeta of selectedThemes) {
      console.log(`\n=== ${themeMeta.label.toUpperCase()} · ${appearanceMeta.label.toUpperCase()} ===`);
      await applyThemeAndAppearance(adb, serial, screenSize, themeMeta, appearanceMeta, opts.toastDelay);

      const needsSettingsSub =
        selectedScreenIds.has('impostazioni') ||
        selectedScreenIds.has('aggiornamenti') ||
        selectedScreenIds.has('info') ||
        selectedScreenIds.has('crediti');

      if (needsSettingsSub) {
        await scrollToTop(adb, serial, screenSize);
      }

      // 1. Impostazioni (already on Impostazioni!)
      if (selectedScreenIds.has('impostazioni')) {
        await snap(themeMeta, appearanceMeta, 'impostazioni');
      }

      // 2. Aggiornamenti / Update (from Impostazioni)
      if (selectedScreenIds.has('aggiornamenti')) {
        const opened = await tapNodeByPatterns(adb, serial, ['Aggiornamenti', /^aggiornamenti$/i], {
          screenSize,
          region: 'content',
          scrollAttempts: 3,
          scrollDir: 'down',
        });
        if (opened) {
          await sleep(500);
          await waitUntilTextGone(adb, serial, [/ricerca aggiornamenti/i, /controllo della versione/i], 4500);
          await snap(themeMeta, appearanceMeta, 'aggiornamenti');
          pressBack(adb, serial);
          await sleep(600);
        } else {
          console.warn('  Avviso: voce "Aggiornamenti" non trovata, salto.');
        }
      }

      // 3 & 4. Informazioni UniApp (info) + Creatore e crediti (crediti, scorrendo in basso!)
      if (selectedScreenIds.has('info') || selectedScreenIds.has('crediti')) {
        const openedInfo = await tapNodeByPatterns(
          adb,
          serial,
          ['Informazioni UniApp', /^informazioni uniapp/i, 'Note legali e crediti'],
          {
            screenSize,
            region: 'content',
            scrollAttempts: 3,
            scrollDir: 'down',
          }
        );
        if (openedInfo) {
          await sleep(550);

          if (selectedScreenIds.has('info')) {
            await snap(themeMeta, appearanceMeta, 'info');
          }

          if (selectedScreenIds.has('crediti')) {
            const openedCredits = await tapNodeByPatterns(
              adb,
              serial,
              ['Creatore e crediti', /^creatore e crediti/i, 'Autore e contributori'],
              {
                screenSize,
                region: 'content',
                scrollAttempts: 2,
                scrollDir: 'down',
              }
            );
            if (openedCredits) {
              await sleep(650);
              await waitUntilTextGone(adb, serial, [/sincronizzazione/i], 4500);
              // Scorre in basso (NON in alto) per mostrare la sezione inferiore con crediti e contributori
              await scrollToBottom(adb, serial, screenSize, 2);
              await snap(themeMeta, appearanceMeta, 'crediti');
              pressBack(adb, serial);
              await sleep(600);
            } else {
              console.warn('  Avviso: voce "Creatore e crediti" non trovata in Informazioni UniApp, salto.');
            }
          }

          pressBack(adb, serial);
          await sleep(600);
        } else {
          console.warn('  Avviso: voce "Informazioni UniApp" non trovata, salto info/crediti.');
        }
      }

      // 4 & 5. Home + Badge UniPass 3D
      if (selectedScreenIds.has('home') || selectedScreenIds.has('badge')) {
        await openBottomTab(adb, serial, screenSize, [/^uniapp$/i], 0.16);
        await scrollToTop(adb, serial, screenSize);

        if (selectedScreenIds.has('home')) {
          await snap(themeMeta, appearanceMeta, 'home');
        }

        if (selectedScreenIds.has('badge')) {
          const openedBadge = await tapNodeByPatterns(adb, serial, ['Badge', /^badge$/i], {
            screenSize,
            region: 'content',
          });
          if (openedBadge) {
            await snap(themeMeta, appearanceMeta, 'badge');
            pressBack(adb, serial);
            await sleep(650);
          } else {
            console.warn('  Avviso: pulsante "Badge" non trovato nella Home, salto.');
          }
        }
      }

      // 6. Servizi
      if (selectedScreenIds.has('servizi')) {
        await openBottomTab(adb, serial, screenSize, [/^servizi$/i], 0.39);
        await scrollToTop(adb, serial, screenSize);
        await snap(themeMeta, appearanceMeta, 'servizi');
      }

      // 7. Didattica
      if (selectedScreenIds.has('didattica')) {
        await openBottomTab(adb, serial, screenSize, [/^didattica$/i], 0.62);
        await scrollToTop(adb, serial, screenSize);
        await snap(themeMeta, appearanceMeta, 'didattica');
      }
    }
  }

  console.log(`\nCattura automatica completata: ${captured}/${totalShots} screenshot salvati.`);
}

async function runManualCapture({ adb, serial, sharp, cropTopPx, opts, selectedThemes, selectedAppearances, selectedScreenIds }) {
  const queue = [];
  for (const appearance of selectedAppearances) {
    for (const theme of selectedThemes) {
      for (const screen of SCREENS) {
        if (selectedScreenIds.has(screen.id)) {
          queue.push({ theme, appearance, screen });
        }
      }
    }
  }

  const rl = createInterface({ input: process.stdin, output: process.stdout });
  try {
    console.log(`\nModalità manuale (${queue.length} screenshot). [Invio] scatta · [r] riscatta · [s] salta · [q] esci\n`);
    let idx = 0;
    let saved = 0;
    let lastGroup = '';

    while (idx < queue.length) {
      const { theme, appearance, screen } = queue[idx];
      const groupKey = `${theme.id}-${appearance.id}`;
      if (groupKey !== lastGroup) {
        console.log(`\n=== TEMA: ${theme.label.toUpperCase()} · ASPETTO: ${appearance.label.toUpperCase()} ===\n`);
        lastGroup = groupKey;
      }

      const filename = filenameFor(theme.id, appearance.id, screen.id);
      const ans = (await rl.question(`[${idx + 1}/${queue.length}] ${screen.label} (${filename}) > `)).trim().toLowerCase();
      if (ans === 'q') break;
      if (ans === 's') {
        idx += 1;
        continue;
      }
      if (ans === 'r') {
        idx = Math.max(0, idx - 1);
        lastGroup = '';
        continue;
      }

      const targetPath = resolve(SCREENSHOTS_DIR, filename);
      await captureToFile({ adb, serial, sharp, targetPath, quality: opts.quality, asPng: false, cropTop: cropTopPx });
      console.log(`  Salvato: ${filename}`);
      saved += 1;
      idx += 1;
    }

    console.log(`\nSessione manuale conclusa: ${saved} screenshot salvati.`);
  } finally {
    rl.close();
  }
}

async function main() {
  const opts = parseArgs(process.argv.slice(2));
  if (opts.help) {
    printHelp();
    return;
  }

  for (const themeId of opts.themes) {
    if (!THEMES.some((t) => t.id === themeId)) {
      throw new Error(`Tema non valido "${themeId}". Usa: ${THEMES.map((t) => t.id).join(', ')}`);
    }
  }
  for (const appearanceId of opts.appearances) {
    if (!APPEARANCES.some((a) => a.id === appearanceId)) {
      throw new Error(`Aspetto non valido "${appearanceId}". Usa: dark | light`);
    }
  }
  for (const screenId of opts.screens) {
    if (!SCREENS.some((s) => s.id === screenId)) {
      throw new Error(
        `Schermata non valida "${screenId}". Usa: ${SCREENS.map((s) => s.id).join(', ')} (alias: update, credits)`
      );
    }
  }

  const sharp = loadSharp();

  // Standalone offline crop mode for already-captured website screenshots
  if (opts.cropExisting) {
    await cropExistingScreenshots({
      sharp,
      cropTop: opts.cropTop,
      quality: opts.quality,
    });
    if (!opts.noSync) {
      syncWebsitePreviews();
    }
    return;
  }

  const adb = findAdbBinary();
  const device = resolveDevice(adb, opts.serial);
  const screenSize = getScreenDimensions(adb, device.serial);
  const cropTopPx =
    opts.cropTop === 'auto' ? getStatusBarHeight(adb, device.serial, screenSize) : opts.cropTop;
  const pkg = detectInstalledPackage(adb, device.serial, opts.debug);

  console.log(`ADB: ${adb}`);
  console.log(`Telefono: ${device.model} (${device.serial}) · ${screenSize.width}x${screenSize.height}`);
  console.log(`Pacchetto UniApp: ${pkg}`);
  console.log(`Sharp (JPEG + WebP): ${sharp ? 'disponibile' : 'non disponibile (solo PNG)'}`);
  console.log(`Taglio barra notifiche (top): ${cropTopPx > 0 ? `${cropTopPx}px` : 'disattivato'}`);
  console.log(`Login automatico demo: ${opts.autoDemoLogin ? `attivo (${DEMO_CREDENTIALS.username})` : 'disattivato'}`);

  if (opts.check) return;

  let demoActive = false;
  const cleanup = () => {
    if (demoActive) {
      disableCleanStatusBar(adb, device.serial);
      demoActive = false;
    }
  };
  process.on('SIGINT', () => {
    cleanup();
    process.exit(130);
  });
  process.on('exit', cleanup);

  if (opts.cleanStatusBar) {
    demoActive = enableCleanStatusBar(adb, device.serial);
  }

  // Single custom output file
  if (opts.out) {
    const targetPath = isAbsolute(opts.out) ? opts.out : resolve(process.cwd(), opts.out);
    await captureToFile({
      adb,
      serial: device.serial,
      sharp,
      targetPath,
      quality: opts.quality,
      asPng: opts.png,
      cropTop: cropTopPx,
    });
    console.log(`Salvato: ${targetPath}`);
    return;
  }

  // Quick continuous mode
  if (opts.mode === 'quick') {
    const rl = createInterface({ input: process.stdin, output: process.stdout });
    try {
      console.log(`\nModalità rapida in ${QUICK_DIR} (Invio = scatta, q = esci)\n`);
      let n = 0;
      while (true) {
        const ans = (await rl.question(`[Scatto #${n + 1}] > `)).trim().toLowerCase();
        if (ans === 'q') break;
        const ext = opts.png || !sharp ? 'png' : 'jpg';
        const targetPath = resolve(QUICK_DIR, timestampName(ext));
        await captureToFile({
          adb,
          serial: device.serial,
          sharp,
          targetPath,
          quality: opts.quality,
          asPng: opts.png,
          cropTop: cropTopPx,
        });
        console.log(`  Salvato: ${targetPath}`);
        n += 1;
      }
    } finally {
      rl.close();
      cleanup();
    }
    return;
  }

  const selectedThemes = opts.themes.length
    ? THEMES.filter((t) => opts.themes.includes(t.id))
    : THEMES;
  const selectedAppearances = opts.appearances.length
    ? APPEARANCES.filter((a) => opts.appearances.includes(a.id))
    : APPEARANCES;
  const selectedScreenIds = new Set(
    opts.screens.length ? opts.screens : SCREENS.map((s) => s.id)
  );

  if (opts.mode === 'auto') {
    launchUniApp(adb, device.serial, pkg);
    await sleep(1200);
    if (opts.autoDemoLogin) {
      await ensureDemoLogin(adb, device.serial, screenSize);
    }
    await runAutomaticCapture({
      adb,
      serial: device.serial,
      sharp,
      screenSize,
      cropTopPx,
      opts,
      selectedThemes,
      selectedAppearances,
      selectedScreenIds,
    });
  } else {
    await runManualCapture({
      adb,
      serial: device.serial,
      sharp,
      cropTopPx,
      opts,
      selectedThemes,
      selectedAppearances,
      selectedScreenIds,
    });
  }

  cleanup();

  if (!opts.noSync) {
    syncWebsitePreviews();
  }
}

main().catch((error) => {
  console.error(`Errore: ${error.message}`);
  process.exit(1);
});
