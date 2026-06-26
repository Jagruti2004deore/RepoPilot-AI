import { useMemo } from 'react';
import { BarChart3, BrainCircuit, CheckCircle2, Radar, ShieldCheck } from 'lucide-react';
import { useRepositories } from './useRepositories';
import { defaultScores } from '../utils/constants';

export function useAnalysis() {
  const repositoryState = useRepositories();
  const selectedRepo = repositoryState.repositories.find((repo) => repo.id === repositoryState.selectedRepoId) ?? repositoryState.repositories[0];
  const activeScores = repositoryState.analysis?.scores ?? selectedRepo?.scores ?? defaultScores;

  const modules = useMemo(
    () => [
      { label: 'Architecture', score: Math.round(activeScores.architecture), icon: BrainCircuit, tone: 'blue' },
      { label: 'Security', score: Math.round(activeScores.security), icon: ShieldCheck, tone: 'green' },
      { label: 'Maintainability', score: Math.round(activeScores.maintainability), icon: Radar, tone: 'violet' },
      { label: 'Docs', score: Math.round(activeScores.documentation), icon: BarChart3, tone: 'amber' },
      { label: 'Tests', score: Math.round(activeScores.testing), icon: CheckCircle2, tone: 'green' },
    ],
    [activeScores],
  );

  return { ...repositoryState, selectedRepo, activeScores, modules };
}
