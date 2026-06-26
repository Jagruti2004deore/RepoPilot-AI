import { Bot, Download, ExternalLink, FileCode2, RefreshCcw, ShieldAlert, Sparkles, TimerReset } from 'lucide-react';
import { memo, useMemo } from 'react';
import type { CSSProperties } from 'react';
import type { AnalysisHistoryItem, AnalysisReport, RepositoryFileSummary } from '../../types';

type DashboardOverviewProps = {
  analysis: AnalysisReport;
  files: RepositoryFileSummary[];
  history: AnalysisHistoryItem[];
  isBusy: boolean;
  onReanalyze: () => void;
  onExport: () => void;
  onAskAi: () => void;
};

function scoreTone(score: number) {
  if (score >= 90) return 'good';
  if (score >= 70) return 'warn';
  return 'risk';
}

function techStack(files: RepositoryFileSummary[]) {
  const languages = files.map((file) => file.language).filter(Boolean);
  return Array.from(new Set(languages)).slice(0, 5);
}

function languageDistribution(files: RepositoryFileSummary[]) {
  const counts = new Map<string, number>();
  files.forEach((file) => counts.set(file.language || 'Other', (counts.get(file.language || 'Other') ?? 0) + 1));
  return Array.from(counts.entries())
    .map(([language, count]) => ({ language, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 5);
}

function RadarChart({ analysis }: { analysis: AnalysisReport }) {
  const metrics = [
    ['Architecture', analysis.scores.architecture],
    ['Security', analysis.scores.security],
    ['Testing', analysis.scores.testing],
    ['Docs', analysis.scores.documentation],
    ['Maintain', analysis.scores.maintainability],
  ] as const;
  const center = 92;
  const maxRadius = 66;
  const points = metrics.map(([, value], index) => {
    const angle = -Math.PI / 2 + (index * Math.PI * 2) / metrics.length;
    const radius = (value / 100) * maxRadius;
    return `${center + Math.cos(angle) * radius},${center + Math.sin(angle) * radius}`;
  }).join(' ');

  return (
    <div className="mini-chart radar-card">
      <svg viewBox="0 0 184 184" role="img" aria-label="Analysis radar chart">
        {[0.35, 0.7, 1].map((scale) => (
          <polygon key={scale} points={metrics.map(([,], index) => {
            const angle = -Math.PI / 2 + (index * Math.PI * 2) / metrics.length;
            const radius = maxRadius * scale;
            return `${center + Math.cos(angle) * radius},${center + Math.sin(angle) * radius}`;
          }).join(' ')} />
        ))}
        <polygon className="radar-fill" points={points} />
      </svg>
      <div className="chart-legend compact-legend">
        {metrics.map(([label, value]) => <span key={label}>{label} <b>{Math.round(value)}</b></span>)}
      </div>
    </div>
  );
}

function ReadinessDonut({ score }: { score: number }) {
  return (
    <div className="donut-chart" style={{ '--score': `${score * 3.6}deg` } as CSSProperties}>
      <span>{Math.round(score)}%</span>
    </div>
  );
}

function TrendLine({ history }: { history: AnalysisHistoryItem[] }) {
  const data = history.length > 0 ? history.slice(0, 6).reverse() : [];
  const points = data.map((item, index) => {
    const x = data.length === 1 ? 50 : (index / (data.length - 1)) * 100;
    const y = 100 - item.scores.overall;
    return `${x},${y}`;
  }).join(' ');

  return (
    <div className="mini-chart trend-card">
      <svg viewBox="0 0 100 100" preserveAspectRatio="none" role="img" aria-label="Analysis score trend">
        <polyline points={points || '0,50 100,50'} />
      </svg>
      <span>{data.length > 1 ? `${data.length} scan trend` : 'First scan'}</span>
    </div>
  );
}

export const DashboardOverview = memo(function DashboardOverview({ analysis, files, history, isBusy, onReanalyze, onExport, onAskAi }: DashboardOverviewProps) {
  const stack = useMemo(() => techStack(files), [files]);
  const distribution = useMemo(() => languageDistribution(files), [files]);
  const findingsCount = analysis.findings.length;
  const overall = Math.round(analysis.scores.overall);
  const readiness = Math.round(analysis.readinessScores.overall);

  return (
    <section className="dashboard-hero fade-in-section">
      <div className="dashboard-title-block">
        <div className="section-kicker"><Sparkles size={17} /> Analysis dashboard</div>
        <h1>{analysis.owner}/{analysis.repositoryName}</h1>
        <p>{analysis.defaultBranch} branch review with architecture, security, readiness, source files, and repo-aware AI assistance.</p>
        <div className="dashboard-actions">
          <button type="button" onClick={onReanalyze} disabled={isBusy}><RefreshCcw size={17} /> Re-analyze</button>
          <button type="button" onClick={onExport}><Download size={17} /> Export Report</button>
          <a href={analysis.githubUrl} target="_blank" rel="noreferrer"><ExternalLink size={17} /> Open Repository</a>
          <button type="button" className="ask-ai-action" onClick={onAskAi}><Bot size={17} /> Ask AI</button>
        </div>
      </div>

      <div className="dashboard-summary-grid">
        <article className={`summary-card score-summary ${scoreTone(overall)}`}>
          <span>Overall Score</span>
          <strong>{overall}</strong>
          <small>Quality, structure, docs, security</small>
        </article>
        <article className={`summary-card score-summary ${scoreTone(readiness)}`}>
          <span>Production Readiness</span>
          <strong>{readiness}%</strong>
          <small>Demo, resume, GitHub, deployment</small>
        </article>
        <article className="summary-card">
          <span>Files Scanned</span>
          <strong>{analysis.importedFileCount}</strong>
          <small><FileCode2 size={14} /> {files.length} indexed in dashboard</small>
        </article>
        <article className="summary-card">
          <span>Findings Count</span>
          <strong>{findingsCount}</strong>
          <small><ShieldAlert size={14} /> Risks and improvement notes</small>
        </article>
        <article className="summary-card tech-card">
          <span>Tech Stack</span>
          <div className="stack-pills">
            {(stack.length ? stack : ['Repository']).map((language) => <b key={language}>{language}</b>)}
          </div>
        </article>
        <article className="summary-card">
          <span>Last Scan Time</span>
          <strong className="time-strong"><TimerReset size={18} /> {new Date(analysis.completedAt).toLocaleDateString()}</strong>
          <small>{new Date(analysis.completedAt).toLocaleTimeString()}</small>
        </article>
      </div>

      <div className="dashboard-chart-grid">
        <RadarChart analysis={analysis} />
        <div className="mini-chart donut-card"><ReadinessDonut score={readiness} /><span>Readiness doughnut</span></div>
        <TrendLine history={history} />
        <div className="mini-chart language-chart">
          {distribution.length === 0 ? <span>No language data yet</span> : distribution.map((item) => (
            <div className="language-bar" key={item.language}>
              <span>{item.language}</span>
              <b style={{ width: `${Math.max(10, (item.count / Math.max(...distribution.map((entry) => entry.count))) * 100)}%` }} />
              <em>{item.count}</em>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
});
