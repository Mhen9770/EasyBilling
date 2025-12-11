'use client';

import { useState, useEffect } from 'react';
import { Modal } from './Modal';

interface Shortcut {
  keys: string[];
  description: string;
  action?: () => void;
}

interface ShortcutGroup {
  title: string;
  shortcuts: Shortcut[];
}

export interface KeyboardShortcutsProps {
  shortcuts: ShortcutGroup[];
  onShortcut?: (shortcut: Shortcut) => void;
}

export const KeyboardShortcuts: React.FC<KeyboardShortcutsProps> = ({
  shortcuts,
  onShortcut,
}) => {
  const [showHelp, setShowHelp] = useState(false);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Show help modal with ?
      if (e.key === '?' && !e.ctrlKey && !e.metaKey) {
        e.preventDefault();
        setShowHelp(true);
        return;
      }

      // Check for registered shortcuts
      shortcuts.forEach((group) => {
        group.shortcuts.forEach((shortcut) => {
          const isMatch = shortcut.keys.every((key) => {
            if (key === 'Ctrl' || key === 'Control') return e.ctrlKey;
            if (key === 'Alt') return e.altKey;
            if (key === 'Shift') return e.shiftKey;
            if (key === 'Meta' || key === 'Cmd') return e.metaKey;
            return e.key.toLowerCase() === key.toLowerCase();
          });

          if (isMatch) {
            e.preventDefault();
            if (shortcut.action) {
              shortcut.action();
            }
            if (onShortcut) {
              onShortcut(shortcut);
            }
          }
        });
      });
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [shortcuts, onShortcut]);

  const formatKeys = (keys: string[]) => {
    return keys.map((key) => {
      const displayKey = key
        .replace('Control', 'Ctrl')
        .replace('Meta', 'Cmd');
      return displayKey;
    }).join(' + ');
  };

  return (
    <>
      {/* Help Button */}
      <button
        onClick={() => setShowHelp(true)}
        className="fixed bottom-4 right-4 bg-blue-600 text-white rounded-full w-12 h-12 flex items-center justify-center shadow-lg hover:bg-blue-700 transition-colors z-40"
        title="Keyboard Shortcuts (Press ?)"
      >
        <span className="text-xl">⌨️</span>
      </button>

      {/* Help Modal */}
      <Modal
        isOpen={showHelp}
        onClose={() => setShowHelp(false)}
        title="Keyboard Shortcuts"
        size="lg"
      >
        <div className="space-y-6">
          {shortcuts.map((group, index) => (
            <div key={index}>
              <h3 className="text-lg font-semibold text-gray-900 mb-3">
                {group.title}
              </h3>
              <div className="space-y-2">
                {group.shortcuts.map((shortcut, idx) => (
                  <div
                    key={idx}
                    className="flex items-center justify-between py-2 px-3 bg-gray-50 rounded-lg"
                  >
                    <span className="text-sm text-gray-700">
                      {shortcut.description}
                    </span>
                    <div className="flex gap-1">
                      {shortcut.keys.map((key, keyIdx) => (
                        <kbd
                          key={keyIdx}
                          className="px-2 py-1 bg-white border border-gray-300 rounded text-xs font-mono shadow-sm"
                        >
                          {key.replace('Control', 'Ctrl').replace('Meta', 'Cmd')}
                        </kbd>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
          
          <div className="border-t pt-4">
            <p className="text-sm text-gray-600">
              <strong>Tip:</strong> Press <kbd className="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-xs font-mono">?</kbd> anytime to view this help.
            </p>
          </div>
        </div>
      </Modal>
    </>
  );
};
