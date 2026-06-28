import { Clipboard, Download, Eye, Sparkles } from 'lucide-react';
import { memo, useMemo, useState } from 'react';
import type { AnalysisReport } from '../../types';
import { coachTabs } from '../../utils/constants';

type CoachKey = (typeof coachTabs)[number]['key'];

type CoachPanelProps = {
  analysis: AnalysisReport;
  onCopy: (value: string, label: string) => void;
};

const coachDescriptions: Record<CoachKey, string> = {
  interviewAnswers: 'Strong answers for common technical interview questions.',
  vivaQuestions: 'Viva-style questions to practice before project review.',
  presentationScript: 'A polished walkthrough script for demos and college presentations.',
  architectureExplanation: 'A clean architecture story you can explain confidently.',
  resumeBullets: 'Portfolio-ready resume bullets based on the repository.',
  githubProfileTips: 'GitHub profile and repository presentation improvements.',
  readmeSuggestions: 'README improvements for clarity, setup, and recruiter review.',
  projectTitleSuggestions: 'Sharper project names and title options for portfolio use.',
};

function downloadTextFile(fileName: string, value: string) {
  const blob = new Blob([value], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}

export const CoachPanel = memo(function CoachPanel({ analysis, onCopy }: CoachPanelProps) {
  const [coachTab, setCoachTab] = useState<CoachKey>('interviewAnswers');
  const activeTab = useMemo(() => coachTabs.find((tab) => tab.key === coachTab) ?? coachTabs[0], [coachTab]);
  const activeCoachText = String(analysis[coachTab] ?? '');
  const fileName = `repopilot-${analysis.repositoryName}-${activeTab.key}.txt`;

  return (
    <section className="coach-panel coach-dashboard fade-in-section">
      <div className="coach-header-block">
        <div>
          <div className="section-kicker compact-kicker"><Sparkles size={16} /> Portfolio coach</div>
          <h3>Turn analysis into interview-ready material</h3>
          <p>Each card uses the latest repository analysis and keeps the generated output ready to copy or download.</p>
        </div>
      </div>

      <div className="coach-overview-grid">
        {coachTabs.map((tab) => (
          <button key={tab.key} type="button" className={coachTab === tab.key ? 'coach-tile active' : 'coach-tile'} onClick={() => setCoachTab(tab.key)}>
            <Sparkles size={16} />
            <span>{tab.label}</span>
            <small>{coachDescriptions[tab.key]}</small>
            <b><Eye size={14} /> View generated</b>
          </button>
        ))}
      </div>

      <article className="report-card coach-card premium-coach-card">
        <div className="card-title-row">
          <div>
            <h3>{activeTab.label}</h3>
            <p>{coachDescriptions[coachTab]}</p>
          </div>
          <div className="coach-action-row">
            <button type="button" className="copy-button" onClick={() => onCopy(activeCoachText, activeTab.label)}><Clipboard size={15} /> Copy</button>
            <button type="button" className="copy-button" onClick={() => downloadTextFile(fileName, activeCoachText)}><Download size={15} /> Download</button>
          </div>
        </div>
        <pre>{activeCoachText}</pre>
      </article>
    </section>
  );
});
