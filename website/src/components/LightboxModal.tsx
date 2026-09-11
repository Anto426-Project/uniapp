'use client';

import React, { useEffect } from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { X, ChevronLeft, ChevronRight } from 'lucide-react';

interface LightboxModalProps {
  isOpen: boolean;
  imageUrl: string;
  title: string;
  description: string;
  onClose: () => void;
  onPrev?: () => void;
  onNext?: () => void;
  currentIndex?: number;
  totalItems?: number;
}

export const LightboxModal: React.FC<LightboxModalProps> = ({
  isOpen,
  imageUrl,
  title,
  description,
  onClose,
  onPrev,
  onNext,
  currentIndex,
  totalItems,
}) => {
  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'ArrowLeft' && onPrev) onPrev();
      else if (e.key === 'ArrowRight' && onNext) onNext();
      else if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onPrev, onNext, onClose]);

  return (
    <Dialog.Root open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-[1000] bg-black/90 backdrop-blur-md transition-opacity motion-safe:animate-in fade-in duration-200" />
        <Dialog.Content
          onClick={onClose}
          className="fixed inset-0 z-[1000] flex flex-col items-center justify-center p-4 sm:p-8 outline-none focus:outline-none select-none motion-safe:animate-in zoom-in-95 duration-200"
        >
          <Dialog.Title className="sr-only">{title}</Dialog.Title>
          {description && <Dialog.Description className="sr-only">{description}</Dialog.Description>}

          {/* Pulsante chiusura in alto a destra */}
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onClose();
            }}
            aria-label="Chiudi anteprima"
            className="fixed top-4 right-4 sm:top-6 sm:right-6 z-[1010] p-2.5 sm:p-3 rounded-full bg-white/10 hover:bg-white/25 active:scale-95 text-white backdrop-blur-md border border-white/15 transition-all cursor-pointer shadow-xl"
          >
            <X className="w-5 h-5 sm:w-6 sm:h-6" />
          </button>

          {/* Frecce di navigazione desktop */}
          {onPrev && (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onPrev();
              }}
              aria-label="Schermata precedente"
              className="fixed left-4 sm:left-8 top-1/2 -translate-y-1/2 z-[1010] p-3 rounded-full bg-white/10 hover:bg-white/25 active:scale-95 text-white backdrop-blur-md border border-white/15 transition-all cursor-pointer shadow-xl hidden sm:flex items-center justify-center"
            >
              <ChevronLeft className="w-6 h-6" />
            </button>
          )}

          {onNext && (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onNext();
              }}
              aria-label="Schermata successiva"
              className="fixed right-4 sm:right-8 top-1/2 -translate-y-1/2 z-[1010] p-3 rounded-full bg-white/10 hover:bg-white/25 active:scale-95 text-white backdrop-blur-md border border-white/15 transition-all cursor-pointer shadow-xl hidden sm:flex items-center justify-center"
            >
              <ChevronRight className="w-6 h-6" />
            </button>
          )}

          {/* Screenshot puro perfettamente incorniciato */}
          {imageUrl && (
            <div
              onClick={(e) => e.stopPropagation()}
              className="relative flex items-center justify-center rounded-[24px] sm:rounded-[36px] overflow-hidden shadow-[0_30px_70px_-15px_rgba(0,0,0,0.9)] border border-white/15 bg-black/60"
            >
              <img
                src={imageUrl}
                alt={title}
                className="max-h-[80dvh] sm:max-h-[84dvh] w-auto object-contain rounded-[22px] sm:rounded-[34px]"
              />
            </div>
          )}
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
};
