import { Clipboard, Sparkles } from 'lucide-react';
import { memo, useMemo, useState } from 'react';
import type { AnalysisReport } from '../../types';
import { coachTabs } from '../../utils/constants';

type CoachKey = (typeof coachTabs)[number]['key'];

type CoachPanelProps = {
  analysis: AnalysisReport;
  onCopy: (value: string, label: string) => void;
};

export const CoachPanel = memo(function CoachPanel({ analysis, onCopy }: CoachPanelProps) {
  const [coachTab, setCoachTab] = useState<CoachKey>('interviewAnswers');
  const activeTab = useMemo(() => coachTabs.find((tab) => tab.key === coachTab) ?? coachTabs[0], [coachTab]);
  const activeCoachText = String(analysis[coachTab] ?? '');

  return (
    <section className="coach-panel coach-dashboard fade-in-section">
      <div className="coach-overview-grid">
        {coachTabs.map((tab) => (
          <button key={tab.key} type="button" className={coachTab === tab.key ? 'coach-tile active' : 'coach-tile'} onClick={() => setCoachTab(tab.key)}>
            <Sparkles size={16} />
            <span>{tab.label}</span>
          </button>
        ))}
      </div>
      <article className="report-card coach-card premium-coach-card">
        <div className="card-title-row">
          <div>
            <h3>{activeTab.label}</h3>
            <p>Generated from the latest repository analysis.</p>
          </div>
          <button type="button" className="copy-button" onClick={() => onCopy(activeCoachText, activeTab.label)}><Clipboard size={15} /> Copy</button>
        </div>
        <pre>{activeCoachText}</pre>
      </article>
    </section>
  );
});
