import { Download, GitBranch, History } from 'lucide-react';
import { memo } from 'react';
import type { AnalysisReport } from '../../types';

type ReportActionsProps = {
  analysis: AnalysisReport;
  isBusy: boolean;
  onDownload: () => void;
  onReanalyze: () => void;
};

export const ReportActions = memo(function ReportActions({ analysis, isBusy, onDownload, onReanalyze }: ReportActionsProps) {
  return (
    <div className="report-actions">
      <button type="button" onClick={onReanalyze} disabled={isBusy}><History size={17} /> Re-analyze</button>
      <button type="button" onClick={onDownload}><Download size={17} /> Export MD</button>
      <a href={analysis.githubUrl} target="_blank" rel="noreferrer"><GitBranch size={17} /> Open repo</a>
    </div>
  );
});
