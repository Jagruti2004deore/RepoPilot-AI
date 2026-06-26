import { Clipboard } from 'lucide-react';
import { memo } from 'react';
import type { AnalysisReport } from '../../types';
import { checklistItems } from '../../utils/formatters';

type ReadinessPanelProps = {
  analysis: AnalysisReport;
  onCopy: (value: string, label: string) => void;
};

export const ReadinessPanel = memo(function ReadinessPanel({ analysis, onCopy }: ReadinessPanelProps) {
  const readiness = analysis.readinessScores;

  return (
    <div className="readiness-panel">
      <div className="readiness-hero">
        <span>Project Demo Readiness</span>
        <strong>{Math.round(readiness.overall)}%</strong>
        <p>Scorecard for GitHub, resume, viva, interview, and deployment readiness.</p>
      </div>
      <div className="readiness-metrics">
        <div><span>Resume</span><strong>{Math.round(readiness.resume)}</strong></div>
        <div><span>Interview</span><strong>{Math.round(readiness.interview)}</strong></div>
        <div><span>GitHub</span><strong>{Math.round(readiness.github)}</strong></div>
        <div><span>Deploy</span><strong>{Math.round(readiness.deployment)}</strong></div>
        <div><span>Demo</span><strong>{Math.round(readiness.demo)}</strong></div>
      </div>
      <article className="readiness-checklist checklist-card">
        <h3>Completion Checklist</h3>
        <div className="checklist-list">
          {checklistItems(analysis.readinessChecklist).map((item) => (
            <div className={item.done ? 'checklist-item done' : 'checklist-item'} key={item.label}>
              <span className="checklist-state">{item.done ? 'Done' : 'Todo'}</span>
              <p>{item.label}</p>
            </div>
          ))}
        </div>
      </article>
      <article className="readiness-checklist next-card">
        <div className="card-title-row">
          <h3>Next Move</h3>
          <button type="button" className="copy-button" onClick={() => onCopy(analysis.readinessReport, 'Readiness report')}><Clipboard size={15} /> Copy</button>
        </div>
        <pre>{analysis.readinessReport}</pre>
      </article>
    </div>
  );
});
