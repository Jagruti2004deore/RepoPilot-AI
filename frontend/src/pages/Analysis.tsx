import { Clipboard, FileCode2, History, LayoutDashboard, MessageSquareText, ShieldAlert, ShieldCheck, Sparkles, Workflow } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ChatPanel } from '../components/analysis/ChatPanel';
import { CoachPanel } from '../components/analysis/CoachPanel';
import { DashboardOverview } from '../components/analysis/DashboardOverview';
import { FileInventory } from '../components/analysis/FileInventory';
import { FindingsList } from '../components/analysis/FindingsList';
import { HistoryPanel } from '../components/analysis/HistoryPanel';
import { ReadinessPanel } from '../components/analysis/ReadinessPanel';
import { ReportSections, reportSectionGroups } from '../components/analysis/ReportSections';
import { ScoreCards } from '../components/analysis/ScoreCards';
import { Loader } from '../components/common/Loader';
import { useAnalysis } from '../hooks/useAnalysis';
import { useClipboard } from '../hooks/useClipboard';
import { buildMarkdownReport } from '../utils/markdown';

type DashboardTab = 'overview' | 'architecture' | 'security' | 'quality' | 'findings' | 'files' | 'coach' | 'chat' | 'history';

const dashboardTabs = [
  { key: 'overview', label: 'Overview', icon: LayoutDashboard },
  { key: 'architecture', label: 'Architecture', icon: Workflow },
  { key: 'security', label: 'Security', icon: ShieldCheck },
  { key: 'quality', label: 'Code Quality', icon: Sparkles },
  { key: 'findings', label: 'Findings', icon: ShieldAlert },
  { key: 'files', label: 'Files', icon: FileCode2 },
  { key: 'coach', label: 'Portfolio Coach', icon: Sparkles },
  { key: 'chat', label: 'AI Chat', icon: MessageSquareText },
  { key: 'history', label: 'History', icon: History },
] as const;

export default function Analysis() {
  const navigate = useNavigate();
  const { repositoryId } = useParams();
  const { analysis, modules, history, files, chatMessages, isBusy, isAsking, loadAnalysis, reanalyze, askQuestion } = useAnalysis();
  const { copyMessage, setCopyMessage, copyText } = useClipboard();
  const [activeTab, setActiveTab] = useState<DashboardTab>('overview');
  const numericRepositoryId = Number(repositoryId);

  useEffect(() => {
    if (!Number.isFinite(numericRepositoryId)) {
      navigate('/workspace', { replace: true });
      return;
    }

    void loadAnalysis(numericRepositoryId);
  }, [loadAnalysis, navigate, numericRepositoryId]);

  const downloadMarkdown = useCallback(() => {
    if (!analysis) return;
    const blob = new Blob([buildMarkdownReport(analysis)], { type: 'text/markdown;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `repopilot-${analysis.repositoryName}-report.md`;
    link.click();
    URL.revokeObjectURL(url);
    setCopyMessage('Markdown report downloaded.');
  }, [analysis, setCopyMessage]);

  const handleAsk = useCallback(
    async (question: string) => {
      const reply = await askQuestion(question);
      if (reply) {
        setCopyMessage('Repo question answered.');
        return true;
      }
      return false;
    },
    [askQuestion, setCopyMessage],
  );

  if (!analysis) {
    return (
      <section className="report-band empty-analysis">
        {isBusy ? <Loader /> : <p>No analysis is open yet. <Link to="/workspace">Import or open a repository</Link>.</p>}
      </section>
    );
  }

  return (
    <section className="analysis-dashboard-shell">
      <DashboardOverview
        analysis={analysis}
        files={files}
        history={history}
        isBusy={isBusy}
        onReanalyze={() => void reanalyze()}
        onExport={downloadMarkdown}
        onAskAi={() => setActiveTab('chat')}
      />

      {copyMessage && <div className="copy-note dashboard-copy-note"><Clipboard size={16} /> {copyMessage}</div>}

      <nav className="dashboard-tabs" aria-label="Analysis dashboard sections">
        {dashboardTabs.map((tab) => (
          <button key={tab.key} type="button" className={activeTab === tab.key ? 'active' : ''} onClick={() => setActiveTab(tab.key)}>
            <tab.icon size={16} /> {tab.label}
          </button>
        ))}
      </nav>

      <div className="dashboard-tab-panel">
        {activeTab === 'overview' && (
          <>
            <ScoreCards modules={modules} analysis={analysis} />
            <ReadinessPanel analysis={analysis} onCopy={copyText} />
          </>
        )}
        {activeTab === 'architecture' && <ReportSections analysis={analysis} onCopy={copyText} sections={reportSectionGroups.architecture} />}
        {activeTab === 'security' && <ReportSections analysis={analysis} onCopy={copyText} sections={reportSectionGroups.security} />}
        {activeTab === 'quality' && <ReportSections analysis={analysis} onCopy={copyText} sections={reportSectionGroups.quality} />}
        {activeTab === 'findings' && <FindingsList findings={analysis.findings} onCopy={copyText} />}
        {activeTab === 'files' && <FileInventory files={files} />}
        {activeTab === 'coach' && <CoachPanel analysis={analysis} onCopy={copyText} />}
        {activeTab === 'chat' && <ChatPanel chatMessages={chatMessages} isAsking={isAsking} onAsk={handleAsk} />}
        {activeTab === 'history' && <HistoryPanel history={history} />}
      </div>
    </section>
  );
}
