import { ArrowRight, Bot, GitBranch } from 'lucide-react';
import { memo, useState } from 'react';
import type { FormEvent } from 'react';

type RepositoryFormProps = {
  isBusy: boolean;
  message: string;
  onAnalyze: (githubUrl: string) => Promise<void>;
};

export const RepositoryForm = memo(function RepositoryForm({ isBusy, message, onAnalyze }: RepositoryFormProps) {
  const [githubUrl, setGithubUrl] = useState('');

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await onAnalyze(githubUrl);
  }

  return (
    <>
      <div className="section-heading">
        <GitBranch size={22} />
        <div>
          <h2>Repository Analysis</h2>
          <p>Paste any public GitHub repository URL to generate a review.</p>
        </div>
      </div>
      <form className="repo-form" onSubmit={handleSubmit}>
        <input value={githubUrl} onChange={(event) => setGithubUrl(event.target.value)} placeholder="https://github.com/owner/repository" />
        <button type="submit" disabled={isBusy || !githubUrl.trim()}>Analyze <ArrowRight size={17} /></button>
      </form>
      <div className="message-line"><Bot size={17} /> {message}</div>
    </>
  );
});