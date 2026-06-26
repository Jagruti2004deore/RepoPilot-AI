export function formatBytes(sizeBytes: number) {
  if (sizeBytes < 1024) return `${sizeBytes} B`;
  return `${Math.round(sizeBytes / 102.4) / 10} KB`;
}

export function checklistItems(text: string) {
  return text
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => ({
      done: line.startsWith('[DONE]'),
      label: line.replace(/^\[(DONE|TODO)]\s*/, ''),
    }));
}

export function deltaText(current?: number, previous?: number) {
  if (current === undefined || previous === undefined) return 'First run';
  const delta = Math.round((current - previous) * 10) / 10;
  if (delta === 0) return 'No change';
  return `${delta > 0 ? '+' : ''}${delta}`;
}

export function deltaClass(current?: number, previous?: number) {
  if (current === undefined || previous === undefined || current === previous) return 'delta-neutral';
  return current > previous ? 'delta-positive' : 'delta-negative';
}