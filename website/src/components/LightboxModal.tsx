'use client';

import React from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { X } from 'lucide-react';

interface LightboxModalProps {
  isOpen: boolean;
  imageUrl: string;
  title: string;
  description: string;
  onClose: () => void;
}

export const LightboxModal: React.FC<LightboxModalProps> = ({
  isOpen,
  imageUrl,
  title,
  description,
  onClose,
}) => {
  return (
    <Dialog.Root open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-50 bg-black/80 transition-opacity motion-safe:animate-in fade-in duration-200" />
        <Dialog.Content className="fixed left-1/2 top-1/2 z-50 -translate-x-1/2 -translate-y-1/2 w-[92vw] max-w-4xl max-h-[90dvh] flex flex-col items-center justify-center p-2 sm:p-4 outline-none focus:outline-none motion-safe:animate-in zoom-in-95 duration-200">
          <div className="relative w-full max-h-[85dvh] flex flex-col items-center justify-center bg-slate-950/85 border border-white/15 rounded-2xl sm:rounded-3xl p-3 sm:p-6 shadow-2xl overflow-hidden">
            <Dialog.Close
              asChild
              className="absolute top-3 right-3 sm:top-5 sm:right-5 z-20 p-2 rounded-full bg-white/10 hover:bg-white/20 text-white transition-colors cursor-pointer"
            >
              <button type="button" aria-label="Chiudi anteprima">
                <X className="w-5 h-5" />
              </button>
            </Dialog.Close>

            {imageUrl && (
              <div className="relative max-h-[65dvh] sm:max-h-[72dvh] w-auto flex items-center justify-center overflow-hidden rounded-xl">
                <img
                  src={imageUrl}
                  alt={title}
                  className="max-h-[65dvh] sm:max-h-[72dvh] w-auto object-contain rounded-xl shadow-lg"
                />
              </div>
            )}

            <div className="mt-3 sm:mt-4 text-center px-2">
              <Dialog.Title className="text-base sm:text-lg font-bold text-white tracking-wide">
                {title}
              </Dialog.Title>
              <Dialog.Description className="text-xs sm:text-sm text-slate-300 mt-1">
                {description}
              </Dialog.Description>
            </div>
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
};
