import { Clipboard, ShieldAlert } from 'lucide-react';
import { memo } from 'react';

type FindingItem = {
  raw: string;
  severity: 'HIGH' | 'MEDIUM' | 'LOW';
  category: string;
  fileName: string;
  description: string;
  fix: string;
};

function parseFinding(finding: string): FindingItem {
  const severity = finding.startsWith('[HIGH]') ? 'HIGH' : finding.startsWith('[MEDIUM]') ? 'MEDIUM' : 'LOW';
  const clean = finding.replace(/^\[(HIGH|MEDIUM|LOW)]\s*/, '');
  const fileMatch = clean.match(/[\w./-]+\.(tsx|ts|jsx|js|java|xml|yml|yaml|properties|json|md)/i);
  const firstColon = clean.indexOf(':');
  const category = firstColon > 0 ? clean.slice(0, firstColon).trim() : severity === 'HIGH' ? 'Critical risk' : severity === 'MEDIUM' ? 'Improvement' : 'Note';

  return {
    raw: finding,
    severity,
    category,
    fileName: fileMatch?.[0] ?? 'Repository level',
    description: firstColon > 0 ? clean.slice(firstColon + 1).trim() : clean,
    fix: severity === 'HIGH'
      ? 'Prioritize this before demo or deployment.'
      : severity === 'MEDIUM'
        ? 'Plan a focused cleanup and add validation or tests.'
        : 'Track this as a polish item for the next iteration.',
  };
}

function groupedFindings(findings: string[]) {
  const parsed = findings.map(parseFinding);
  return {
    HIGH: parsed.filter((finding) => finding.severity === 'HIGH'),
    MEDIUM: parsed.filter((finding) => finding.severity === 'MEDIUM'),
    LOW: parsed.filter((finding) => finding.severity === 'LOW'),
  };
}

type FindingsListProps = {
  findings: string[];
  onCopy: (value: string, label: string) => void;
};

export const FindingsList = memo(function FindingsList({ findings, onCopy }: FindingsListProps) {
  if (findings.length === 0) {
    return <div className="empty-dashboard-state">No findings were reported for this scan.</div>;
  }

  const groups = groupedFindings(findings);

  return (
    <div className="findings-dashboard fade-in-section">
      {(['HIGH', 'MEDIUM', 'LOW'] as const).map((severity) => (
        <section className={`severity-group ${severity.toLowerCase()}`} key={severity}>
          <div className="severity-heading">
            <span><ShieldAlert size={18} /> {severity}</span>
            <b>{groups[severity].length}</b>
          </div>
          <div className="severity-list">
            {groups[severity].length === 0 ? <p>No {severity.toLowerCase()} findings.</p> : groups[severity].map((finding) => (
              <details className="finding-card" key={finding.raw}>
                <summary>
                  <span className={`severity-badge ${finding.severity.toLowerCase()}`}>{finding.severity}</span>
                  <div>
                    <strong>{finding.category}</strong>
                    <small>{finding.fileName}</small>
                  </div>
                </summary>
                <div className="finding-detail-body">
                  <p>{finding.description}</p>
                  <div className="recommended-fix"><b>Recommended fix</b><span>{finding.fix}</span></div>
                  <button type="button" className="copy-button" onClick={() => onCopy(finding.raw, 'Finding')}><Clipboard size={15} /> Copy</button>
                </div>
              </details>
            ))}
          </div>
        </section>
      ))}
    </div>
  );
});
