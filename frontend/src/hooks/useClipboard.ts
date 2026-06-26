import { useCallback, useState } from 'react';

export function useClipboard() {
  const [copyMessage, setCopyMessage] = useState('');

  const copyText = useCallback(async (value: string, label: string) => {
    if (!value.trim()) return;

    try {
      await navigator.clipboard.writeText(value);
      setCopyMessage(`${label} copied.`);
    } catch {
      setCopyMessage('Copy failed. Select the text manually.');
    }
  }, []);

  return { copyMessage, setCopyMessage, copyText };
}
