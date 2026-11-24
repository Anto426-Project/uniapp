'use client';

import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { AnimatePresence, motion } from 'motion/react';
import {
  ArrowLeft,
  Check,
  Copy,
  Download,
  ExternalLink,
  FileKey,
  Lock,
  ShieldCheck,
  Smartphone,
  Terminal,
  Usb,
  Zap,
} from 'lucide-react';
import type { MonetThemeId } from '@/data/types';
import { withBasePath } from '@/utils/basePath';
import { hapticSnap } from '@/lib/fluid-physics';
import {
  loginAppleDeveloper,
  readIpaInfo,
  signIpaWithAppleAccount,
  signIpaWithOfflineP12,
  type AppleDeveloperContext,
  type SignedIpaOutput,
  type TwoFactorContext,
} from '@/lib/ios-sideload/apple-auth';
import {
  installSignedIpaOverWebUsb,
  isPairingDialogPendingError,
  isUsbClaimInterfaceError,
  isWebUsbSupported,
  listKnownPairedUdids,
  pairIosDeviceOverWebUsb,
  type PairedIosDevice,
} from '@/lib/ios-sideload/device-pairing';
import {
  buildAltStoreInstallUrl,
  buildAltStoreSourceUrl,
  buildSideStoreInstallUrl,
  buildSideStoreSourceUrl,
  downloadOfficialIpaVerified,
  resolveOfficialIosArtifact,
} from '@/lib/ios-sideload/ipa-source';
import { IOS_RELAY_URL } from '@/lib/ios-sideload/relay-config';
import type { DirectUsbMuxClient } from '@/lib/ios-sideload/webmuxd/core';
import { useSite } from './SiteShell';
import { FluidSegmentedControl, type SegmentedOption } from './FluidSegmentedControl';
import Aurora from './reactbits/Aurora';
import BlurText from './reactbits/BlurText';
import SpotlightCard from './reactbits/SpotlightCard';

type SideloadMode = 'apple-id' | 'sidestore' | 'p12';

const MODE_OPTIONS: readonly SegmentedOption<SideloadMode>[] = [
  { id: 'apple-id', label: 'Cavo USB' },
  { id: 'sidestore', label: 'SideStore' },
  { id: 'p12', label: 'Certificato .p12' },
];

const AURORA_STOPS: Record<MonetThemeId, string[]> = {
  sapphire: ['#173679', '#4A90D9', '#8CC8FF'],
  emerald: ['#0B5339', '#2ECC71', '#84ECC0'],
  sunset: ['#8C370A', '#E67E22', '#FFC27A'],
  violet: ['#4D1D7A', '#9B59B6', '#DFC2FF'],
};

const SPOTLIGHT_HEX: Record<MonetThemeId, string> = {
  sapphire: '#5AA4F0',
  emerald: '#3BD67F',
  sunset: '#F09038',
  violet: '#B074D4',
};

const SAVED_APPLE_ID_KEY = 'uniapp:ios-last-apple-id';
const SAVED_UDID_KEY = 'uniapp:ios-last-udid';
const IOS_FLUID_EASE: [number, number, number, number] = [0.32, 0.72, 0, 1];

export function IosSideloadStudio() {
  const { theme, appearance, reducedMotion, platforms } = useSite();
  const spotColor = SPOTLIGHT_HEX[theme];

  const [mode, setMode] = useState<SideloadMode>('apple-id');
  const [webUsbAvailable, setWebUsbAvailable] = useState(true);
  const [manualUdidOpen, setManualUdidOpen] = useState(false);

  // WebUSB + UDID state
  const usbClientRef = useRef<DirectUsbMuxClient | null>(null);
  const [pairedDevice, setPairedDevice] = useState<PairedIosDevice | null>(null);
  const [targetUdid, setTargetUdid] = useState('');
  const [knownUdids, setKnownUdids] = useState<string[]>([]);
  const [trustPromptPending, setTrustPromptPending] = useState(false);
  const [usbDriverHelp, setUsbDriverHelp] = useState(false);

  // Apple ID auth state
  const [appleId, setAppleId] = useState('');
  const [applePassword, setApplePassword] = useState('');
  const [appleContext, setAppleContext] = useState<AppleDeveloperContext | null>(null);
  const [twoFactorState, setTwoFactorState] = useState<{
    open: boolean;
    ctx: TwoFactorContext | null;
    mode: 'device' | 'sms';
    selectedPhoneId: number | null;
    smsSent: boolean;
    code: string;
    busy: boolean;
    error: string | null;
  }>({
    open: false,
    ctx: null,
    mode: 'device',
    selectedPhoneId: null,
    smsSent: false,
    code: '',
    busy: false,
    error: null,
  });

  // Offline .p12 state
  const [p12File, setP12File] = useState<File | null>(null);
  const [p12Password, setP12Password] = useState('');
  const [provFile, setProvFile] = useState<File | null>(null);

  // IPA + Signing + Install state (official UniApp IPA auto-fetched when needed!)
  const [customIpaOpen, setCustomIpaOpen] = useState(false);
  const [ipaFile, setIpaFile] = useState<File | null>(null);
  const [detectedBundleId, setDetectedBundleId] = useState<string>('');
  const [bundleIdOverride, setBundleIdOverride] = useState('');
  const [signedOutput, setSignedOutput] = useState<SignedIpaOutput | null>(null);
  const [signedDownloadHref, setSignedDownloadHref] = useState<string | null>(null);

  // Busy / Progress / Logs / Errors
  const [busyStep, setBusyStep] = useState<
    null | 'pair' | 'login' | 'sign' | 'install'
  >(null);
  const [progress, setProgress] = useState<{ percent: number; label: string }>({
    percent: 0,
    label: '',
  });
  const [logs, setLogs] = useState<string[]>([]);
  const [logsExpanded, setLogsExpanded] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [copiedSource, setCopiedSource] = useState(false);
  const [copiedLogs, setCopiedLogs] = useState(false);

  const officialIos = useMemo(() => resolveOfficialIosArtifact(platforms), [platforms]);

  const appendLog = useCallback((line: string) => {
    const stamp = new Date().toLocaleTimeString('it-IT', { hour12: false });
    setLogs((prev) => [...prev.slice(-199), `[${stamp}] ${line}`]);
  }, []);

  useEffect(() => {
    const hasUsb = isWebUsbSupported();
    setWebUsbAvailable(hasUsb);
    if (!hasUsb) {
      setManualUdidOpen(true);
    }

    const ua = navigator.userAgent || '';
    if (/iPhone|iPad|iPod/i.test(ua)) {
      setMode('sidestore');
    }

    const udids = listKnownPairedUdids();
    setKnownUdids(udids);

    try {
      const savedEmail = window.localStorage.getItem(SAVED_APPLE_ID_KEY);
      if (savedEmail) setAppleId(savedEmail);
      const savedUdid = window.localStorage.getItem(SAVED_UDID_KEY);
      if (savedUdid) setTargetUdid(savedUdid);
      else if (udids.length > 0) setTargetUdid(udids[0]);
    } catch {
      // Optional storage
    }
  }, []);

  useEffect(() => {
    if (!signedOutput) {
      setSignedDownloadHref(null);
      return;
    }
    const url = URL.createObjectURL(signedOutput.signedFile);
    setSignedDownloadHref(url);
    return () => URL.revokeObjectURL(url);
  }, [signedOutput]);

  const selectIpaFile = useCallback(async (file: File | null) => {
    setSignedOutput(null);
    setErrorMessage(null);
    setIpaFile(file);
    if (!file) {
      setDetectedBundleId('');
      return;
    }
    try {
      const bytes = new Uint8Array(await file.arrayBuffer());
      const info = readIpaInfo(bytes);
      setDetectedBundleId(info.bundleId || '');
    } catch {
      setDetectedBundleId('');
    }
  }, []);

  const handlePairUsb = async () => {
    setErrorMessage(null);
    setTrustPromptPending(false);
    setUsbDriverHelp(false);
    setBusyStep('pair');
    setProgress({ percent: 25, label: 'Connessione a iPhone...' });

    try {
      const info = await pairIosDeviceOverWebUsb({
        clientRef: usbClientRef,
        onLog: appendLog,
        onTrustPromptRequired: () => setTrustPromptPending(true),
        onDisconnected: () => {
          setPairedDevice(null);
          appendLog('usb: dispositivo scollegato');
        },
      });
      hapticSnap(8);
      setPairedDevice(info);
      setTargetUdid(info.udid);
      setKnownUdids(listKnownPairedUdids());
      try {
        window.localStorage.setItem(SAVED_UDID_KEY, info.udid);
      } catch {
        // Optional
      }
      setProgress({ percent: 0, label: '' });
    } catch (err) {
      if (isPairingDialogPendingError(err)) {
        setErrorMessage(
          'Sblocca l’iPhone, tocca «Autorizza» sullo schermo e premi di nuovo «Collega via USB».'
        );
      } else {
        if (isUsbClaimInterfaceError(err)) {
          setUsbDriverHelp(true);
        }
        const rawMsg = err instanceof Error ? err.message : String(err);
        if (!rawMsg.toLowerCase().includes('no device selected')) {
          setErrorMessage(rawMsg);
        }
      }
      setProgress({ percent: 0, label: '' });
    } finally {
      setBusyStep(null);
    }
  };

  const handleAppleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    if (!IOS_RELAY_URL) {
      setErrorMessage(
        'Relay Apple ID non configurato su questa istanza: usa SideStore oppure un certificato .p12.'
      );
      return;
    }

    setBusyStep('login');
    setProgress({ percent: 20, label: 'Accesso Apple ID...' });

    try {
      const ctx = await loginAppleDeveloper({
        appleId,
        password: applePassword,
        relayBaseUrl: IOS_RELAY_URL,
        onLog: appendLog,
        onTwoFactorRequired: (twoFactorCtx) => {
          setTwoFactorState({
            open: true,
            ctx: twoFactorCtx,
            mode: 'device',
            selectedPhoneId: twoFactorCtx.trustedPhoneNumbers[0]?.id ?? null,
            smsSent: false,
            code: '',
            busy: false,
            error: null,
          });
        },
      });

      hapticSnap(8);
      setAppleContext(ctx);
      setApplePassword('');
      setTwoFactorState((prev) => ({ ...prev, open: false, ctx: null, code: '' }));
      try {
        window.localStorage.setItem(SAVED_APPLE_ID_KEY, appleId.trim());
      } catch {
        // Optional
      }
      setProgress({ percent: 0, label: '' });
    } catch (err) {
      setTwoFactorState((prev) => ({ ...prev, open: false, ctx: null }));
      setErrorMessage(err instanceof Error ? err.message : String(err));
      setProgress({ percent: 0, label: '' });
    } finally {
      setBusyStep(null);
    }
  };

  const handleSubmitTwoFactor = async (e: React.FormEvent) => {
    e.preventDefault();
    const { ctx, mode: tfMode, selectedPhoneId, code } = twoFactorState;
    if (!ctx || code.trim().length < 6) return;

    setTwoFactorState((prev) => ({ ...prev, busy: true, error: null }));
    try {
      if (tfMode === 'sms' && selectedPhoneId !== null) {
        await ctx.submitSmsCode(selectedPhoneId, code.trim());
      } else {
        ctx.submitDeviceCode(code.trim());
      }
      setTwoFactorState((prev) => ({ ...prev, open: false, busy: false }));
    } catch (err) {
      setTwoFactorState((prev) => ({
        ...prev,
        busy: false,
        error: err instanceof Error ? err.message : 'Codice non valido.',
      }));
    }
  };

  const handleRequestSms = async () => {
    const { ctx, selectedPhoneId } = twoFactorState;
    if (!ctx || selectedPhoneId === null) return;
    setTwoFactorState((prev) => ({ ...prev, busy: true, error: null }));
    try {
      await ctx.requestSms(selectedPhoneId);
      setTwoFactorState((prev) => ({
        ...prev,
        mode: 'sms',
        smsSent: true,
        busy: false,
        code: '',
      }));
    } catch (err) {
      setTwoFactorState((prev) => ({
        ...prev,
        busy: false,
        error: err instanceof Error ? err.message : 'Invio SMS non riuscito',
      }));
    }
  };

  // Unified One-Click Flow: auto-downloads official IPA if needed, signs in RAM, and flashes via USB if paired!
  const handleSignAndMaybeInstall = async () => {
    setErrorMessage(null);
    setSignedOutput(null);
    setBusyStep('sign');

    try {
      let activeIpa = ipaFile;
      if (!activeIpa) {
        if (!officialIos?.ipaDownload) {
          setCustomIpaOpen(true);
          throw new Error('Seleziona il file .ipa di UniApp.');
        }
        setProgress({ percent: 8, label: 'Download IPA ufficiale...' });
        activeIpa = await downloadOfficialIpaVerified({
          artifact: officialIos.ipaDownload,
          relayBaseUrl: IOS_RELAY_URL,
          onProgress: (pct) =>
            setProgress({ percent: Math.round(pct * 0.45), label: `Download UniApp (${pct}%)...` }),
          onLog: appendLog,
        });
        await selectIpaFile(activeIpa);
      }

      setProgress({ percent: 55, label: 'Firma in locale nel browser...' });

      let signed: SignedIpaOutput;
      if (mode === 'p12') {
        if (!p12File || !provFile) {
          throw new Error('Seleziona sia il certificato .p12 sia il file .mobileprovision.');
        }
        signed = await signIpaWithOfflineP12({
          ipaFile: activeIpa,
          p12File,
          p12Password,
          mobileProvisionFile: provFile,
          bundleIdOverride,
          onLog: appendLog,
        });
      } else {
        if (!appleContext) {
          throw new Error('Accedi prima con il tuo Apple ID.');
        }
        if (!targetUdid.trim()) {
          throw new Error('Collega l’iPhone via USB o inserisci l’UDID.');
        }
        const { result, updatedContext } = await signIpaWithAppleAccount({
          ipaFile: activeIpa,
          context: appleContext,
          deviceUdid: targetUdid,
          deviceName: pairedDevice?.name,
          bundleIdOverride,
          relayBaseUrl: IOS_RELAY_URL,
          onLog: appendLog,
        });
        setAppleContext(updatedContext);
        signed = result;
      }

      setSignedOutput(signed);

      const usbReady =
        Boolean(pairedDevice) &&
        Boolean(usbClientRef.current) &&
        (mode === 'p12' ||
          pairedDevice?.udid.trim().toUpperCase() === targetUdid.trim().toUpperCase());

      if (usbReady && usbClientRef.current) {
        setBusyStep('install');
        setProgress({ percent: 75, label: 'Installazione su iPhone via USB...' });
        await installSignedIpaOverWebUsb({
          client: usbClientRef.current,
          signedFile: signed.signedFile,
          onLog: appendLog,
          onProgress: (pct, stage) =>
            setProgress({ percent: 75 + Math.round(pct * 0.25), label: stage }),
        });
        hapticSnap(12);
        setProgress({ percent: 100, label: 'UniApp installata su iPhone!' });
      } else {
        hapticSnap(10);
        setProgress({ percent: 100, label: 'IPA firmato e pronto al download.' });
      }
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : String(err));
      setProgress({ percent: 0, label: '' });
    } finally {
      setBusyStep(null);
    }
  };

  const handleInstallOverUsb = async () => {
    if (!signedOutput || !usbClientRef.current || !pairedDevice) return;
    setErrorMessage(null);
    setBusyStep('install');
    setProgress({ percent: 15, label: 'Installazione su iPhone...' });

    try {
      await installSignedIpaOverWebUsb({
        client: usbClientRef.current,
        signedFile: signedOutput.signedFile,
        onLog: appendLog,
        onProgress: (pct, stage) => setProgress({ percent: pct, label: stage }),
      });
      hapticSnap(12);
      setProgress({ percent: 100, label: 'UniApp installata su iPhone!' });
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : String(err));
    } finally {
      setBusyStep(null);
    }
  };

  const altSourceAbsoluteUrl = useMemo(() => {
    if (typeof window !== 'undefined') {
      return new URL(withBasePath('/altstore.json'), window.location.origin).toString();
    }
    return 'https://anto426-project.github.io/uniapp-upstream/altstore.json';
  }, []);

  const directIpaUrl = officialIos?.ipaDownload?.url || '';

  const copyAltSourceUrl = () => {
    navigator.clipboard
      .writeText(altSourceAbsoluteUrl)
      .then(() => {
        hapticSnap(6);
        setCopiedSource(true);
        window.setTimeout(() => setCopiedSource(false), 2000);
      })
      .catch(() => setCopiedSource(false));
  };

  const copyConsoleLogs = () => {
    navigator.clipboard
      .writeText(logs.join('\n'))
      .then(() => {
        hapticSnap(6);
        setCopiedLogs(true);
        window.setTimeout(() => setCopiedLogs(false), 2000);
      })
      .catch(() => setCopiedLogs(false));
  };

  const hasIpaSource = Boolean(ipaFile || officialIos?.ipaDownload);
  const canSign =
    busyStep === null &&
    hasIpaSource &&
    (mode === 'p12'
      ? Boolean(p12File && provFile)
      : Boolean(appleContext && targetUdid.trim().length > 0));

  const canFlashUsb =
    busyStep === null &&
    Boolean(signedOutput) &&
    Boolean(pairedDevice) &&
    Boolean(usbClientRef.current) &&
    (mode === 'p12' ||
      pairedDevice?.udid.trim().toUpperCase() === targetUdid.trim().toUpperCase());

  return (
    <>
      <section className="ios-minimal-page">
        <div className="hero-aurora-layer" aria-hidden="true">
          <Aurora
            colorStops={AURORA_STOPS[theme]}
            amplitude={0.85}
            blend={0.62}
            speed={0.45}
            height={0.54}
            glow={0.55}
            brightness={appearance === 'dark' ? 0.85 : 0.42}
            opacity={appearance === 'dark' ? 0.62 : 0.42}
            lightMode={appearance === 'light'}
            paused={reducedMotion}
            mouseInteraction={!reducedMotion}
          />
        </div>

        <div className="container-shell relative z-10">
          {/* Minimal Apple Header */}
          <div className="ios-minimal-header">
            <a href={withBasePath('/')} className="ios-back-link">
              <ArrowLeft className="w-3.5 h-3.5" aria-hidden="true" />
              <span>UniApp</span>
            </a>

            <h1 className="type-title-1 ios-minimal-title">
              {reducedMotion ? (
                <>
                  Installa su <span className="hero-accent-text">iPhone.</span>
                </>
              ) : (
                <BlurText
                  text="Installa su iPhone."
                  animateBy="words"
                  direction="bottom"
                  delay={60}
                  stepDuration={0.3}
                  blur={8}
                  distance={12}
                  trigger="load"
                  className="justify-center"
                />
              )}
            </h1>

            <p className="type-body ios-minimal-sub">
              Firma UniApp in locale con il tuo Apple ID gratuito oppure aprila in SideStore.
            </p>

            <div className="ios-mode-dock">
              <FluidSegmentedControl
                options={MODE_OPTIONS}
                value={mode}
                onChange={(next) => {
                  setErrorMessage(null);
                  setMode(next);
                }}
                ariaLabel="Modalità di installazione iOS"
                compact
              />
            </div>
          </div>

          {/* Single Unified Minimal Studio Card */}
          <div className="ios-sheet-wrap">
            <SpotlightCard
              theme={appearance}
              spotlightColor={spotColor}
              intensity={0.14}
              spotlightSize={360}
              borderGlow={0.6}
              className="ios-sheet-card"
            >
              <AnimatePresence mode="wait" initial={false}>
                {mode === 'sidestore' ? (
                  <motion.div
                    key="sidestore"
                    className="ios-sidestore-pane"
                    initial={reducedMotion ? false : { opacity: 0, y: 6 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={reducedMotion ? undefined : { opacity: 0, y: -6 }}
                    transition={{ duration: reducedMotion ? 0 : 0.18, ease: IOS_FLUID_EASE }}
                  >
                    <div className="ios-app-row">
                      <div className="ios-app-meta">
                        <img
                          src={withBasePath('/assets/uniapp-icon.webp')}
                          alt=""
                          width={48}
                          height={48}
                          className="ios-app-squircle"
                          aria-hidden="true"
                        />
                        <div>
                          <div className="ios-row-title">
                            UniApp per iOS
                            {officialIos && (
                              <span className="ios-mini-pill">v{officialIos.version}</span>
                            )}
                          </div>
                          <p className="ios-row-sub">
                            Rinnovo automatico ogni 7 giorni in Wi-Fi tramite SideStore o AltStore.
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className="ios-sidestore-actions">
                      {directIpaUrl && (
                        <div className="ios-btn-row">
                          <a
                            href={buildSideStoreInstallUrl(directIpaUrl)}
                            className="btn-primary download-cta-btn"
                          >
                            <Zap className="w-4 h-4 shrink-0" aria-hidden="true" />
                            <span>Installa con SideStore</span>
                          </a>
                          <a
                            href={buildAltStoreInstallUrl(directIpaUrl)}
                            className="btn-material download-secondary-btn"
                          >
                            <Smartphone className="w-4 h-4 shrink-0" aria-hidden="true" />
                            <span>Installa con AltStore</span>
                          </a>
                        </div>
                      )}

                      <div className="ios-btn-row">
                        <a
                          href={buildSideStoreSourceUrl(altSourceAbsoluteUrl)}
                          className="btn-material download-secondary-btn"
                        >
                          <span>Aggiungi repo a SideStore</span>
                        </a>
                        <a
                          href={buildAltStoreSourceUrl(altSourceAbsoluteUrl)}
                          className="btn-material download-secondary-btn"
                        >
                          <span>Aggiungi repo ad AltStore</span>
                        </a>
                        <button
                          type="button"
                          className="copy-icon-btn"
                          onClick={copyAltSourceUrl}
                          title={copiedSource ? 'Copiato' : 'Copia URL sorgente'}
                          aria-label={
                            copiedSource ? 'URL sorgente copiato' : 'Copia URL sorgente altstore.json'
                          }
                        >
                          {copiedSource ? (
                            <Check
                              className="w-3.5 h-3.5"
                              style={{ color: 'var(--status-green)' }}
                              aria-hidden="true"
                            />
                          ) : (
                            <Copy className="w-3.5 h-3.5" aria-hidden="true" />
                          )}
                        </button>
                      </div>
                    </div>

                    <div className="ios-pane-foot">
                      <span>Non hai ancora SideStore?</span>
                      <a
                        href="https://sidestore.io"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-link"
                      >
                        <span>sidestore.io</span>
                        <ExternalLink className="w-3 h-3 opacity-60" aria-hidden="true" />
                      </a>
                    </div>
                  </motion.div>
                ) : (
                  <motion.div
                    key={mode}
                    className="ios-steps-list"
                    initial={reducedMotion ? false : { opacity: 0, y: 6 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={reducedMotion ? undefined : { opacity: 0, y: -6 }}
                    transition={{ duration: reducedMotion ? 0 : 0.18, ease: IOS_FLUID_EASE }}
                  >
                    {/* STEP 1: DEVICE */}
                    <div className="ios-step-row">
                      <div className="ios-step-row-main">
                        <div className="ios-step-left">
                          <span
                            className={`ios-step-dot ${pairedDevice || targetUdid.trim() ? 'ios-step-dot--done' : ''}`}
                          >
                            {pairedDevice || targetUdid.trim() ? (
                              <Check className="w-3.5 h-3.5" aria-hidden="true" />
                            ) : (
                              '1'
                            )}
                          </span>
                          <div>
                            <div className="ios-row-title">
                              {pairedDevice ? pairedDevice.name || 'iPhone connesso' : 'iPhone o iPad'}
                            </div>
                            <p className="ios-row-sub">
                              {trustPromptPending
                                ? 'Sblocca l’iPhone, tocca «Autorizza» e riprova.'
                                : pairedDevice
                                  ? `Connesso via USB · ${pairedDevice.udid.slice(0, 12)}…`
                                  : webUsbAvailable
                                    ? 'Collega via cavo su Chrome o Edge per il flash diretto.'
                                    : 'Inserisci l’UDID per scaricare l’IPA firmato.'}
                            </p>
                          </div>
                        </div>

                        <div className="ios-btn-row">
                          {webUsbAvailable && (
                            <button
                              type="button"
                              className={
                                pairedDevice
                                  ? 'btn-material download-secondary-btn'
                                  : 'btn-primary download-cta-btn'
                              }
                              disabled={busyStep !== null}
                              onClick={handlePairUsb}
                            >
                              <Usb className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                              <span>
                                {busyStep === 'pair'
                                  ? 'Connessione...'
                                  : pairedDevice
                                    ? 'Riconnetti'
                                    : 'Collega USB'}
                              </span>
                            </button>
                          )}
                          <button
                            type="button"
                            className="ios-quiet-btn"
                            onClick={() => setManualUdidOpen((prev) => !prev)}
                          >
                            {manualUdidOpen ? 'Chiudi UDID' : 'UDID manuale'}
                          </button>
                        </div>
                      </div>

                      {manualUdidOpen && (
                        <div className="ios-drawer-row">
                          <input
                            id="ios-udid-input"
                            type="text"
                            list="known-udids-list"
                            className="ios-input ios-input--mono"
                            aria-label="UDID dispositivo"
                            placeholder="UDID (es. 00008120-00123456789ABCDE)"
                            value={targetUdid}
                            onChange={(e) => {
                              setTargetUdid(e.target.value);
                              setSignedOutput(null);
                              try {
                                window.localStorage.setItem(SAVED_UDID_KEY, e.target.value);
                              } catch {
                                // Optional
                              }
                            }}
                          />
                          {knownUdids.length > 0 && (
                            <datalist id="known-udids-list">
                              {knownUdids.map((u) => (
                                <option key={u} value={u} />
                              ))}
                            </datalist>
                          )}
                        </div>
                      )}
                    </div>

                    {/* STEP 2: CREDENTIALS */}
                    <div className="ios-step-row">
                      {mode === 'apple-id' ? (
                        <>
                          <div className="ios-step-row-main">
                            <div className="ios-step-left">
                              <span
                                className={`ios-step-dot ${appleContext ? 'ios-step-dot--done' : ''}`}
                              >
                                {appleContext ? (
                                  <Check className="w-3.5 h-3.5" aria-hidden="true" />
                                ) : (
                                  '2'
                                )}
                              </span>
                              <div>
                                <div className="ios-row-title">
                                  {appleContext ? appleContext.appleId : 'Account Apple'}
                                </div>
                                <p className="ios-row-sub">
                                  {appleContext
                                    ? `Team ${appleContext.team.name} · Sessione attiva solo in RAM`
                                    : 'Anche un Apple ID gratuito permette la firma per 7 giorni.'}
                                </p>
                              </div>
                            </div>

                            {appleContext && (
                              <button
                                type="button"
                                className="ios-quiet-btn"
                                disabled={busyStep !== null}
                                onClick={() => {
                                  setAppleContext(null);
                                  setSignedOutput(null);
                                }}
                              >
                                Disconnetti
                              </button>
                            )}
                          </div>

                          {!appleContext && (
                            <form className="ios-inline-login" onSubmit={handleAppleLogin}>
                              <input
                                type="email"
                                autoComplete="username"
                                required
                                className="ios-input"
                                aria-label="Email Apple ID"
                                placeholder="Email Apple ID"
                                value={appleId}
                                onChange={(e) => setAppleId(e.target.value)}
                              />
                              <input
                                type="password"
                                autoComplete="current-password"
                                required
                                className="ios-input"
                                aria-label="Password Apple ID"
                                placeholder="Password"
                                value={applePassword}
                                onChange={(e) => setApplePassword(e.target.value)}
                              />
                              <button
                                type="submit"
                                className="btn-primary download-cta-btn"
                                disabled={
                                  busyStep !== null || !appleId.trim() || applePassword.length === 0
                                }
                              >
                                <Lock className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                                <span>{busyStep === 'login' ? 'Accesso...' : 'Accedi'}</span>
                              </button>
                            </form>
                          )}
                        </>
                      ) : (
                        <>
                          <div className="ios-step-row-main">
                            <div className="ios-step-left">
                              <span
                                className={`ios-step-dot ${p12File && provFile ? 'ios-step-dot--done' : ''}`}
                              >
                                {p12File && provFile ? (
                                  <Check className="w-3.5 h-3.5" aria-hidden="true" />
                                ) : (
                                  '2'
                                )}
                              </span>
                              <div>
                                <div className="ios-row-title">Certificato .p12 e profilo</div>
                                <p className="ios-row-sub">
                                  Firma 100% offline nella memoria del browser.
                                </p>
                              </div>
                            </div>
                          </div>

                          <div className="ios-p12-grid">
                            <label className="ios-file-pill">
                              <input
                                type="file"
                                accept=".p12,.pfx"
                                className="sr-only"
                                aria-label="File certificato (.p12 / .pfx)"
                                onChange={(e) => {
                                  setP12File(e.target.files?.[0] ?? null);
                                  setSignedOutput(null);
                                }}
                              />
                              <FileKey className="w-3.5 h-3.5 shrink-0" style={{ color: 'var(--accent)' }} aria-hidden="true" />
                              <span className="truncate">{p12File ? p12File.name : 'Scegli .p12'}</span>
                            </label>

                            <label className="ios-file-pill">
                              <input
                                type="file"
                                accept=".mobileprovision"
                                className="sr-only"
                                aria-label="Profilo (.mobileprovision)"
                                onChange={(e) => {
                                  setProvFile(e.target.files?.[0] ?? null);
                                  setSignedOutput(null);
                                }}
                              />
                              <ShieldCheck className="w-3.5 h-3.5 shrink-0" style={{ color: 'var(--accent)' }} aria-hidden="true" />
                              <span className="truncate">
                                {provFile ? provFile.name : 'Scegli .mobileprovision'}
                              </span>
                            </label>

                            <input
                              type="password"
                              className="ios-input"
                              aria-label="Password certificato .p12"
                              placeholder="Password .p12 (opzionale)"
                              value={p12Password}
                              onChange={(e) => setP12Password(e.target.value)}
                            />
                          </div>
                        </>
                      )}
                    </div>

                    {/* STEP 3: SIGN & INSTALL */}
                    <div className="ios-step-row">
                      <div className="ios-step-row-main">
                        <div className="ios-step-left">
                          <span
                            className={`ios-step-dot ${signedOutput ? 'ios-step-dot--done' : ''}`}
                          >
                            {signedOutput ? (
                              <Check className="w-3.5 h-3.5" aria-hidden="true" />
                            ) : (
                              '3'
                            )}
                          </span>
                          <div>
                            <div className="ios-row-title">
                              {ipaFile
                                ? ipaFile.name
                                : officialIos
                                  ? `UniApp v${officialIos.version}`
                                  : 'Pacchetto UniApp'}
                            </div>
                            <p className="ios-row-sub">
                              {ipaFile
                                ? `${(ipaFile.size / (1024 * 1024)).toFixed(1)} MB${
                                    detectedBundleId ? ` · ${detectedBundleId}` : ''
                                  }`
                                : officialIos
                                  ? 'Scarica, verifica SHA-256 e firma automaticamente.'
                                  : 'Seleziona un pacchetto .ipa da firmare.'}
                            </p>
                          </div>
                        </div>

                        <div className="ios-btn-row">
                          <button
                            type="button"
                            className="ios-quiet-btn"
                            onClick={() => setCustomIpaOpen((prev) => !prev)}
                          >
                            {customIpaOpen ? 'Chiudi opzioni' : 'Altro .ipa'}
                          </button>

                          <button
                            type="button"
                            className="btn-primary download-cta-btn"
                            disabled={!canSign}
                            onClick={handleSignAndMaybeInstall}
                          >
                            <Zap className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                            <span>
                              {busyStep === 'sign'
                                ? 'Firma in corso...'
                                : busyStep === 'install'
                                  ? 'Installazione...'
                                  : pairedDevice
                                    ? 'Firma e installa'
                                    : 'Firma IPA'}
                            </span>
                          </button>
                        </div>
                      </div>

                      {customIpaOpen && (
                        <div className="ios-drawer-row ios-custom-ipa-row">
                          <label className="ios-file-pill">
                            <input
                              type="file"
                              accept=".ipa"
                              className="sr-only"
                              aria-label="Seleziona file IPA da firmare"
                              onChange={(e) => void selectIpaFile(e.target.files?.[0] ?? null)}
                            />
                            <span className="truncate">
                              {ipaFile ? `File: ${ipaFile.name}` : 'Scegli file .ipa locale…'}
                            </span>
                          </label>

                          <input
                            type="text"
                            className="ios-input ios-input--mono"
                            aria-label="Bundle ID personalizzato"
                            placeholder={detectedBundleId || 'Bundle ID (opzionale)'}
                            value={bundleIdOverride}
                            onChange={(e) => {
                              setBundleIdOverride(e.target.value);
                              setSignedOutput(null);
                            }}
                          />
                        </div>
                      )}

                      {(busyStep !== null || progress.label) && (
                        <div className="ios-progress-strip">
                          <div className="ios-progress-head">
                            <span>{progress.label || 'Operazione in corso...'}</span>
                            <span className="ios-progress-pct">{progress.percent}%</span>
                          </div>
                          <div
                            className="ios-progress-track"
                            role="progressbar"
                            aria-valuenow={progress.percent}
                            aria-valuemin={0}
                            aria-valuemax={100}
                          >
                            <div
                              className="ios-progress-fill"
                              style={{ width: `${progress.percent}%` }}
                            />
                          </div>
                        </div>
                      )}

                      {signedOutput && busyStep === null && (
                        <div className="ios-signed-bar">
                          {canFlashUsb && (
                            <button
                              type="button"
                              className="btn-primary download-cta-btn"
                              onClick={handleInstallOverUsb}
                            >
                              <Usb className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                              <span>Installa via USB</span>
                            </button>
                          )}
                          {signedDownloadHref && (
                            <a
                              href={signedDownloadHref}
                              download={signedOutput.signedFile.name}
                              className="btn-material download-secondary-btn"
                            >
                              <Download className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                              <span>Scarica IPA firmato</span>
                            </a>
                          )}
                        </div>
                      )}

                      {errorMessage && (
                        <div className="ios-error-alert" role="alert">
                          {errorMessage}
                        </div>
                      )}

                      {usbDriverHelp && (
                        <p className="ios-driver-note">
                          Su Windows iTunes blocca l’accesso diretto WebUSB (puoi usare il driver
                          WinUSB con Zadig oppure cliccare «Scarica IPA firmato»). Su macOS la porta
                          è gestita da <code className="mono-code">usbmuxd</code>: scarica l’IPA o
                          usa la scheda SideStore.
                        </p>
                      )}

                      {logs.length > 0 && (
                        <div className="ios-logs-bar">
                          <button
                            type="button"
                            className="ios-quiet-btn"
                            aria-expanded={logsExpanded}
                            onClick={() => setLogsExpanded((prev) => !prev)}
                          >
                            <Terminal className="w-3 h-3" aria-hidden="true" />
                            <span>{logsExpanded ? 'Nascondi log' : `Log (${logs.length})`}</span>
                          </button>
                          {logsExpanded && (
                            <button
                              type="button"
                              className="ios-quiet-btn"
                              onClick={copyConsoleLogs}
                            >
                              {copiedLogs ? 'Copiato' : 'Copia'}
                            </button>
                          )}
                          {logsExpanded && (
                            <pre className="ios-log-console">{logs.join('\n')}</pre>
                          )}
                        </div>
                      )}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </SpotlightCard>

            <p className="ios-minimal-footnote">
              <ShieldCheck className="w-3.5 h-3.5 shrink-0" style={{ color: 'var(--accent)' }} aria-hidden="true" />
              <span>
                Tutto avviene localmente nel browser · Al primo avvio autorizza il profilo in{' '}
                <code className="mono-code">Impostazioni → Generali → VPN e gestione dispositivi</code>.
              </span>
            </p>
          </div>
        </div>
      </section>

      {/* Apple 2FA Verification Sheet */}
      <Dialog.Root
        open={twoFactorState.open}
        onOpenChange={(open) => {
          if (!open && twoFactorState.ctx) {
            twoFactorState.ctx.submitDeviceCode('__CANCELLED__');
            setTwoFactorState((prev) => ({ ...prev, open: false, ctx: null }));
          }
        }}
      >
        <AnimatePresence>
          {twoFactorState.open && (
            <Dialog.Portal forceMount>
              <Dialog.Overlay asChild>
                <motion.div
                  className="modal-scrim"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  transition={{ duration: 0.2, ease: IOS_FLUID_EASE }}
                />
              </Dialog.Overlay>
              <Dialog.Content asChild aria-describedby={undefined}>
                <motion.div
                  className="ios-modal-dialog"
                  initial={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.94, y: 12 }}
                  animate={{ opacity: 1, scale: 1, y: 0 }}
                  exit={reducedMotion ? { opacity: 0 } : { opacity: 0, scale: 0.96, y: 8 }}
                  transition={{ duration: 0.22, ease: IOS_FLUID_EASE }}
                >
                  <Dialog.Title className="type-title-2">Codice di verifica Apple</Dialog.Title>
                  <p className="type-callout">
                    {twoFactorState.mode === 'sms' && twoFactorState.smsSent
                      ? 'Inserisci il codice a 6 cifre ricevuto via SMS.'
                      : 'Inserisci il codice a 6 cifre mostrato sul tuo dispositivo fidato.'}
                  </p>

                  <form onSubmit={handleSubmitTwoFactor} className="ios-modal-form">
                    <input
                      type="text"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      maxLength={6}
                      autoFocus
                      required
                      className="ios-input ios-input--mono ios-otp-input"
                      placeholder="000000"
                      aria-label="Codice di verifica a 6 cifre"
                      value={twoFactorState.code}
                      onChange={(e) =>
                        setTwoFactorState((prev) => ({
                          ...prev,
                          code: e.target.value.replace(/\D/g, '').slice(0, 6),
                        }))
                      }
                    />

                    {twoFactorState.error && (
                      <div className="ios-error-alert">{twoFactorState.error}</div>
                    )}

                    {twoFactorState.ctx && twoFactorState.ctx.trustedPhoneNumbers.length > 0 && (
                      <div className="ios-modal-sms">
                        {twoFactorState.ctx.trustedPhoneNumbers.length > 1 && (
                          <select
                            className="ios-input"
                            aria-label="Numero di telefono fidato per SMS"
                            value={twoFactorState.selectedPhoneId ?? ''}
                            onChange={(e) =>
                              setTwoFactorState((prev) => ({
                                ...prev,
                                selectedPhoneId: Number(e.target.value),
                              }))
                            }
                          >
                            {twoFactorState.ctx.trustedPhoneNumbers.map((p) => (
                              <option key={p.id} value={p.id}>
                                {p.numberWithDialCode || p.obfuscatedNumber}
                              </option>
                            ))}
                          </select>
                        )}

                        <button
                          type="button"
                          className="ios-quiet-btn"
                          disabled={twoFactorState.busy}
                          onClick={handleRequestSms}
                        >
                          {twoFactorState.smsSent ? 'Invia di nuovo SMS' : 'Invia codice via SMS'}
                        </button>
                      </div>
                    )}

                    <div className="ios-btn-row" style={{ justifyContent: 'flex-end' }}>
                      <button
                        type="button"
                        className="btn-material download-secondary-btn"
                        onClick={() => {
                          twoFactorState.ctx?.submitDeviceCode('__CANCELLED__');
                          setTwoFactorState((prev) => ({ ...prev, open: false, ctx: null }));
                        }}
                      >
                        Annulla
                      </button>
                      <button
                        type="submit"
                        className="btn-primary download-cta-btn"
                        disabled={twoFactorState.busy || twoFactorState.code.length < 6}
                      >
                        {twoFactorState.busy ? 'Verifica...' : 'Conferma'}
                      </button>
                    </div>
                  </form>
                </motion.div>
              </Dialog.Content>
            </Dialog.Portal>
          )}
        </AnimatePresence>
      </Dialog.Root>
    </>
  );
}
