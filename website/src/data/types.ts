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
