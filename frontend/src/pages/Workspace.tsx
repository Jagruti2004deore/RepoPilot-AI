import { useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { RepositoryForm } from '../components/workspace/RepositoryForm';
import { RepositoryTable } from '../components/workspace/RepositoryTable';
import { SessionCard } from '../components/workspace/SessionCard';
import { useAuth } from '../hooks/useAuth';
import { useRepositories } from '../hooks/useRepositories';

export default function Workspace() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { repositories, selectedRepoId, isBusy, message, loadRepositories, importAndAnalyze } = useRepositories();

  useEffect(() => {
    void loadRepositories(false);
  }, [loadRepositories]);

  const handleAnalyze = useCallback(
    async (githubUrl: string) => {
      const repo = await importAndAnalyze(githubUrl);
      if (repo) {
        navigate(`/analysis/${repo.id}`);
      }
    },
    [importAndAnalyze, navigate],
  );

  return (
    <section className="workspace workspace-page" id="workspace">
      {user && <SessionCard user={user} />}
      <section className="analysis-panel">
        <RepositoryForm isBusy={isBusy} message={message} onAnalyze={handleAnalyze} />
        <RepositoryTable repositories={repositories} selectedRepoId={selectedRepoId} isBusy={isBusy} onOpen={(repositoryId) => navigate(`/analysis/${repositoryId}`)} />
      </section>
    </section>
  );
}
