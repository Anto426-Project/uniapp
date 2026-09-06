export interface ReleaseChannelData {
  latestVersion?: string;
  latestVersionCode?: number;
  minSupportedVersion?: string;
  minSupportedVersionCode?: number;
  mandatory?: boolean;
  releaseChannel?: string;
  downloadUrl?: string;
  downloadUrlsByAbi?: Record<string, string>;
  notes?: string;
  publishedAt?: string;
  buildCommit?: string;
  appEnabled?: boolean;
  description?: string;
}

export interface UpdateManifest {
  channels?: {
    stable?: {
      release?: ReleaseChannelData;
    };
    beta?: {
      release?: ReleaseChannelData;
    };
    [key: string]: {
      release?: ReleaseChannelData;
    } | undefined;
  };
}

export interface ScreenshotItem {
  file: string;
  title: string;
  description: string;
}
