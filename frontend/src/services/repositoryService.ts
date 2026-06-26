import type { AnalysisHistoryItem, AnalysisReport, RepoChatMessage, RepositoryFileSummary, RepositorySummary } from '../types';
import { api } from './api';

export async function fetchRepositories() {
  const { data } = await api.get<RepositorySummary[]>('/repositories');
  return data;
}

export async function importRepository(githubUrl: string) {
  const { data } = await api.post<RepositorySummary>('/repositories/import', { githubUrl });
  return data;
}

export async function fetchRepositoryFiles(repositoryId: number) {
  const { data } = await api.get<RepositoryFileSummary[]>(`/repositories/${repositoryId}/files`);
  return data;
}

export async function fetchRepoChat(repositoryId: number) {
  const { data } = await api.get<RepoChatMessage[]>(`/repositories/${repositoryId}/chat`);
  return data;
}

export async function askRepoQuestion(repositoryId: number, question: string) {
  const { data } = await api.post<RepoChatMessage>(`/repositories/${repositoryId}/chat/ask`, { question });
  return data;
}

export async function fetchAnalysisHistory(repositoryId: number) {
  const { data } = await api.get<AnalysisHistoryItem[]>(`/repositories/${repositoryId}/analyses`);
  return data;
}

export async function fetchAnalysis(repositoryId: number) {
  const { data } = await api.get<AnalysisReport>(`/repositories/${repositoryId}/analysis`);
  return data;
}

export async function reanalyzeRepository(repositoryId: number) {
  const { data } = await api.post<AnalysisReport>(`/repositories/${repositoryId}/reanalyze`);
  return data;
}