import { ShieldCheck } from 'lucide-react';
import { memo } from 'react';
import type { ComponentType, CSSProperties } from 'react';
import type { AnalysisReport } from '../../types';

type ScoreCardsProps = {
  modules: Array<{ label: string; score: number; icon: ComponentType<{ size?: number }>; tone: string }>;
  analysis: AnalysisReport;
};

function scoreTone(score: number) {
  if (score >= 90) return 'good';
  if (score >= 70) return 'warn';
  return 'risk';
}

export const ScoreCards = memo(function ScoreCards({ modules, analysis }: ScoreCardsProps) {
  const highFindings = analysis.findings.filter((finding) => finding.startsWith('[HIGH]')).length;
  const mediumFindings = analysis.findings.filter((finding) => finding.startsWith('[MEDIUM]')).length;

  return (
    <div className="dashboard-score-grid fade-in-section">
      {modules.map((item) => {
        const tone = scoreTone(item.score);
        return (
          <article className={`score-orb-card ${tone}`} key={item.label}>
            <div className="score-orb" style={{ '--score': `${item.score * 3.6}deg` } as CSSProperties}>
              <span>{item.score}</span>
            </div>
            <div>
              <item.icon size={19} />
              <strong>{item.label}</strong>
              <small>{tone === 'good' ? 'Production strong' : tone === 'warn' ? 'Needs polish' : 'Needs attention'}</small>
            </div>
            <div className="score-linear"><span style={{ width: `${item.score}%` }} /></div>
          </article>
        );
      })}
      <article className="score-orb-card risk findings-score-card">
        <div className="score-orb finding-orb"><span>{highFindings + mediumFindings}</span></div>
        <div>
          <ShieldCheck size={19} />
          <strong>Risk Findings</strong>
          <small>{highFindings} high, {mediumFindings} medium</small>
        </div>
        <div className="score-linear"><span style={{ width: `${Math.min(100, (highFindings + mediumFindings) * 12)}%` }} /></div>
      </article>
    </div>
  );
});
