import { Activity, TrendingUp } from 'lucide-react';
import { memo } from 'react';
import type { AnalysisHistoryItem } from '../../types';
import { deltaClass, deltaText } from '../../utils/formatters';

export const HistoryPanel = memo(function HistoryPanel({ history }: { history: AnalysisHistoryItem[] }) {
  if (history.length === 0) return <div className="empty-dashboard-state">No saved analysis history yet.</div>;

  const latestHistory = history[0];
  const previousHistory = history[1];
  const trend = history.slice(0, 7).reverse();

  return (
    <section className="history-panel premium-history fade-in-section">
      <div className="history-title">
        <div>
          <h3>Analysis History</h3>
          <p>Compare the latest scan with previous saved analyses.</p>
        </div>
        <TrendingUp size={20} />
      </div>
      <div className="history-grid">
        <div className="history-card">
          <span>Overall score change</span>
          <strong className={deltaClass(latestHistory?.scores.overall, previousHistory?.scores.overall)}>{deltaText(latestHistory?.scores.overall, previousHistory?.scores.overall)}</strong>
          <small>{latestHistory ? Math.round(latestHistory.scores.overall) : '--'} latest score</small>
        </div>
        <div className="history-card">
          <span>Readiness change</span>
          <strong className={deltaClass(latestHistory?.readinessScores.overall, previousHistory?.readinessScores.overall)}>{deltaText(latestHistory?.readinessScores.overall, previousHistory?.readinessScores.overall)}</strong>
          <small>{latestHistory ? Math.round(latestHistory.readinessScores.overall) : '--'} latest readiness</small>
        </div>
        <div className="history-card">
          <span>Finding count</span>
          <strong>{latestHistory?.findingCount ?? 0}</strong>
          <small>{history.length} saved analysis run{history.length === 1 ? '' : 's'}</small>
        </div>
      </div>

      <div className="trend-chart-card">
        <div><Activity size={17} /> Score trend</div>
        <div className="trend-bars">
          {trend.map((item) => (
            <span key={item.analysisId} style={{ height: `${Math.max(14, item.scores.overall)}%` }} title={`${Math.round(item.scores.overall)}/100`} />
          ))}
        </div>
      </div>

      <div className="history-table">
        {history.slice(0, 5).map((item) => (
          <div className="history-row" key={item.analysisId}>
            <span>#{item.analysisId}</span>
            <strong>{Math.round(item.scores.overall)}/100</strong>
            <b>{Math.round(item.readinessScores.overall)} ready</b>
            <small>{item.findingCount} findings</small>
            <time>{new Date(item.completedAt).toLocaleString()}</time>
          </div>
        ))}
      </div>
    </section>
  );
});
