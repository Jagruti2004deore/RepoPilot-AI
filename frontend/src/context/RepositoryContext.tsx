import { createContext, useCallback, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import {
  askRepoQuestion,
  fetchAnalysis,
  fetchAnalysisHistory,
  fetchRepoChat,
  fetchRepositories,
  fetchRepositoryFiles,
  importRepository,
  reanalyzeRepository,
} from '../services/repositoryService';
import type { AnalysisHistoryItem, AnalysisReport, RepoChatMessage, RepositoryFileSummary, RepositorySummary } from '../types';
import { defaultRepos } from '../utils/constants';
import { apiStatus, errorMessage } from '../utils/helpers';

export type RepositoryContextValue = {
  repositories: RepositorySummary[];
  selectedRepoId: number | null;
  analysis: AnalysisReport | null;
  history: AnalysisHistoryItem[];
  files: RepositoryFileSummary[];
  chatMessages: RepoChatMessage[];
  message: string;
  isBusy: boolean;
  isAsking: boolean;
  setMessage: (message: string) => void;
  resetWorkspace: () => void;
  loadRepositories: (openFirst?: boolean) => Promise<void>;
  loadAnalysis: (repositoryId: number) => Promise<AnalysisReport | null>;
  importAndAnalyze: (githubUrl: string) => Promise<RepositorySummary | null>;
  reanalyze: () => Promise<AnalysisReport | null>;
  askQuestion: (question: string) => Promise<RepoChatMessage | null>;
};

export const RepositoryContext = createContext<RepositoryContextValue | null>(null);

export function RepositoryProvider({ children }: { children: ReactNode }) {
  const [repositories, setRepositories] = useState<RepositorySummary[]>(defaultRepos);
  const [selectedRepoId, setSelectedRepoId] = useState<number | null>(null);
  const [analysis, setAnalysis] = useState<AnalysisReport | null>(null);
  const [history, setHistory] = useState<AnalysisHistoryItem[]>([]);
  const [files, setFiles] = useState<RepositoryFileSummary[]>([]);
  const [chatMessages, setChatMessages] = useState<RepoChatMessage[]>([]);
  const [message, setMessage] = useState('RepoPilot AI is ready. Sign in, import a GitHub repository, and open the generated review.');
  const [isBusy, setIsBusy] = useState(false);
  const [isAsking, setIsAsking] = useState(false);

  const loadRepositoryFiles = useCallback(async (repositoryId: number) => {
    try {
      setFiles(await fetchRepositoryFiles(repositoryId));
    } catch {
      setFiles([]);
    }
  }, []);

  const loadAnalysisHistory = useCallback(async (repositoryId: number) => {
    try {
      setHistory(await fetchAnalysisHistory(repositoryId));
    } catch {
      setHistory([]);
    }
  }, []);

  const loadRepoChat = useCallback(async (repositoryId: number) => {
    try {
      setChatMessages(await fetchRepoChat(repositoryId));
    } catch {
      setChatMessages([]);
    }
  }, []);

  const loadAnalysis = useCallback(
    async (repositoryId: number) => {
      setIsBusy(true);
      setMessage('Loading analysis report...');

      try {
        const report = await fetchAnalysis(repositoryId);
        setAnalysis(report);
        setSelectedRepoId(repositoryId);
        await Promise.all([loadRepositoryFiles(repositoryId), loadAnalysisHistory(repositoryId), loadRepoChat(repositoryId)]);
        setMessage(`${report.owner}/${report.repositoryName} analysis loaded with ${report.importedFileCount} files reviewed.`);
        return report;
      } catch (error) {
        setAnalysis(null);
        setFiles([]);
        setHistory([]);
        setChatMessages([]);

        if (apiStatus(error) === 403) {
          setRepositories((current) => current.filter((repo) => repo.id !== repositoryId));
          setSelectedRepoId(null);
          setMessage('This saved repository belongs to another session. Login again or import it with this account.');
        } else {
          setSelectedRepoId(repositoryId);
          setMessage(errorMessage(error, 'Could not open this analysis. Login again or import the repository with your current account.'));
        }

        return null;
      } finally {
        setIsBusy(false);
      }
    },
    [loadAnalysisHistory, loadRepoChat, loadRepositoryFiles],
  );

  const loadRepositories = useCallback(
    async (openFirst = false) => {
      try {
        const data = await fetchRepositories();
        if (data.length > 0) {
          setRepositories(data);
          setSelectedRepoId(data[0].id);
          if (openFirst) {
            await loadAnalysis(data[0].id);
          }
        }
      } catch (error) {
        setMessage(errorMessage(error, 'Backend session is not available yet. You can still view the local dashboard preview.'));
      }
    },
    [loadAnalysis],
  );

  const importAndAnalyze = useCallback(
    async (githubUrl: string) => {
      setIsBusy(true);
      setMessage('Fetching repository files from GitHub and running analysis...');

      try {
        const repo = await importRepository(githubUrl);
        setRepositories((current) => [repo, ...current.filter((item) => item.id !== repo.id)]);
        setSelectedRepoId(repo.id);
        setMessage(`${repo.owner}/${repo.name} imported. Opening generated analysis...`);
        await loadAnalysis(repo.id);
        return repo;
      } catch (error) {
        setMessage(errorMessage(error, 'Import failed. Check login, backend status, GitHub URL, and internet connection.'));
        return null;
      } finally {
        setIsBusy(false);
      }
    },
    [loadAnalysis],
  );

  const reanalyze = useCallback(async () => {
    if (!analysis) return null;

    setIsBusy(true);
    setMessage('Re-running analysis from saved files...');

    try {
      const report = await reanalyzeRepository(analysis.repositoryId);
      setAnalysis(report);
      await Promise.all([loadRepositoryFiles(report.repositoryId), loadAnalysisHistory(report.repositoryId), loadRepositories(false)]);
      setMessage(`Fresh analysis completed for ${report.owner}/${report.repositoryName}.`);
      return report;
    } catch (error) {
      setMessage(errorMessage(error, 'Re-analysis failed. Try again after the backend is running.'));
      return null;
    } finally {
      setIsBusy(false);
    }
  }, [analysis, loadAnalysisHistory, loadRepositories, loadRepositoryFiles]);

  const askQuestion = useCallback(
    async (question: string) => {
      if (!analysis || !question.trim()) return null;
      setIsAsking(true);

      try {
        const reply = await askRepoQuestion(analysis.repositoryId, question);
        setChatMessages((current) => [...current, reply]);
        return reply;
      } catch (error) {
        setMessage(errorMessage(error, 'Could not answer this repo question right now.'));
        return null;
      } finally {
        setIsAsking(false);
      }
    },
    [analysis],
  );

  const resetWorkspace = useCallback(() => {
    setRepositories(defaultRepos);
    setSelectedRepoId(null);
    setAnalysis(null);
    setHistory([]);
    setFiles([]);
    setChatMessages([]);
    setMessage('Signed out locally.');
  }, []);

  const value = useMemo(
    () => ({
      repositories,
      selectedRepoId,
      analysis,
      history,
      files,
      chatMessages,
      message,
      isBusy,
      isAsking,
      setMessage,
      resetWorkspace,
      loadRepositories,
      loadAnalysis,
      importAndAnalyze,
      reanalyze,
      askQuestion,
    }),
    [analysis, askQuestion, chatMessages, files, history, importAndAnalyze, isAsking, isBusy, loadAnalysis, loadRepositories, message, reanalyze, repositories, resetWorkspace, selectedRepoId],
  );

  return <RepositoryContext.Provider value={value}>{children}</RepositoryContext.Provider>;
}
