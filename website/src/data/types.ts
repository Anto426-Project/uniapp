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

// One published Android release. No channel selection or prerelease fallback.
export interface UpdateManifest extends ReleaseData {
  sha256ByAbi?: Record<string, string>;
  platforms?: Record<string, ReleaseData>;
}

export interface ScreenshotItem {
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
  platforms: Partial<Record<'android' | 'ios' | 'windows' | 'linux' | 'macos', PlatformRelease>>;
}
