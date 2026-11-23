export interface ReleaseData {
  latestVersion?: string;
  latestVersionCode?: number;
  minSupportedVersion?: string;
  minSupportedVersionCode?: number;
  mandatory?: boolean;
  downloadUrl?: string;
  downloadUrlsByAbi?: Record<string, string>;
  notes?: string;
  publishedAt?: string;
  buildCommit?: string;
  appEnabled?: boolean;
  description?: string;
}

export interface UpdateManifest extends ReleaseData {
  sha256ByAbi?: Record<string, string>;
  platforms?: Record<string, ReleaseData>;
}

export interface ScreenshotItem {
  id: string;
  file: string;
  title: string;
  description: string;
}

export interface PlatformDownload {
  name: string;
  os: string;
  url: string;
  sha256: string;
}

export interface PlatformRelease {
  version: string;
  tag: string;
  url: string;
  signing: 'unsigned' | 'release-key';
  variants: Record<string, { version: string; downloads: PlatformDownload[] }>;
}

export interface PlatformManifest {
  schema: number;
  platforms: Partial<Record<'android' | 'ios', PlatformRelease>>;
}

export type MonetThemeId = 'sapphire' | 'emerald' | 'sunset' | 'violet';
export type AppearanceMode = 'dark' | 'light';

export interface OriginRect {
  left: number;
  top: number;
  width: number;
  height: number;
}
