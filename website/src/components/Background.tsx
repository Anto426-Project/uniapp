'use client';

import React from 'react';

interface BackgroundProps {
  theme: string;
}

export const Background: React.FC<BackgroundProps> = () => {
  return (
    <div className="studio-backdrop" aria-hidden="true">
      <div className="studio-wash" />
      <div className="studio-wash-lower" />
    </div>
  );
};
