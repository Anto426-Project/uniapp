import React from 'react';

export interface GlassSurfaceProps {
  children?: React.ReactNode;
  id?: string;
  width?: number | string;
  height?: number | string;
  borderRadius?: number;
  borderWidth?: number;
  brightness?: number;
  opacity?: number;
  blur?: number;
  displace?: number;
  backgroundOpacity?: number;
  saturation?: number;
  distortionScale?: number;
  redOffset?: number;
  greenOffset?: number;
  blueOffset?: number;
  xChannel?: 'R' | 'G' | 'B' | 'A';
  yChannel?: 'R' | 'G' | 'B' | 'A';
  mixBlendMode?: string;
  className?: string;
  contentClassName?: string;
  style?: React.CSSProperties;
}

declare const GlassSurface: React.FC<GlassSurfaceProps>;
export default GlassSurface;
