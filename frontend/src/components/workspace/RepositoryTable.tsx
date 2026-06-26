import { GitBranch } from 'lucide-react';
import { memo } from 'react';
import type { RepositorySummary } from '../../types';

type RepositoryTableProps = {
  repositories: RepositorySummary[];
  selectedRepoId: number | null;
  isBusy: boolean;
  onOpen: (repositoryId: number) => void;
};

export const RepositoryTable = memo(function RepositoryTable({ repositories, selectedRepoId, isBusy, onOpen }: RepositoryTableProps) {
  return (
    <div className="repo-table">
      <div className="table-head"><span>Repository</span><span>Status</span><span>Files</span><span>Score</span></div>
      {repositories.map((repo) => (
        <button className={repo.id === selectedRepoId ? 'repo-row active' : 'repo-row'} type="button" key={`${repo.id}-${repo.importedAt}`} onClick={() => onOpen(repo.id)} disabled={isBusy}>
          <span><GitBranch size={16} /> {repo.owner}/{repo.name}</span>
          <strong>{repo.status}</strong>
          <small>{repo.importedFileCount ?? 0}</small>
          <b>{Math.round(repo.scores?.overall ?? 0) || '--'}</b>
        </button>
      ))}
    </div>
  );
});
