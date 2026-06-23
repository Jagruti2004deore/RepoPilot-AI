import {
  ArrowRight,
  BarChart3,
  Bot,
  BrainCircuit,
  CheckCircle2,
  GitBranch,
  History,
  KeyRound,
  LockKeyhole,
  LogIn,
  Radar,
  ShieldCheck,
  Sparkles,
  UserPlus,
} from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { api } from './lib/api';

type AuthMode = 'login' | 'register';

type AuthUser = {
  token: string;
  userId: number;
  name: string;
  email: string;
};

type AnalysisScores = {
  architecture: number;
  security: number;
  maintainability: number;
  documentation: number;
  testing: number;
  overall: number;
};

type RepositorySummary = {
  id: number;
  analysisId?: number | null;
  githubUrl: string;
  owner: string;
  name: string;
  status: string;
  defaultBranch?: string | null;
  importedFileCount?: number;
  scores?: AnalysisScores | null;
  importedAt: string;
};

type AnalysisReport = {
  analysisId: number;
  repositoryId: number;
  repositoryName: string;
  owner: string;
  githubUrl: string;
  status: string;
  defaultBranch: string;
  importedFileCount: number;
  scores: AnalysisScores;
  architectureReport: string;
  codeQualityReport: string;
  securityReport: string;
  recommendations: string;
  interviewQuestions: string;
  resumeSummary: string;
  findings: string[];
  completedAt: string;
};

type RepositoryFileSummary = {
  id: number;
  path: string;
  language: string;
  sizeBytes: number;
  lineCount: number;
  role: string;
  signals: string[];
};

type ApiError = {
  response?: {
    data?: {
      message?: string;
    };
  };
};

const defaultScores: AnalysisScores = {
  architecture: 82,
  security: 74,
  maintainability: 88,
  documentation: 63,
  testing: 45,
  overall: 70,
};

const defaultRepos: RepositorySummary[] = [
  {
    id: 1,
    githubUrl: 'https://github.com/Jagruti2004deore/RepoLens-AI',
    owner: 'Jagruti2004deore',
    name: 'RepoLens-AI',
    status: 'LOCAL_PREVIEW',
    defaultBranch: 'main',
    importedFileCount: 0,
    scores: defaultScores,
    importedAt: new Date().toISOString(),
  },
];

const buildScope = [
  'GitHub repository import',
  'Static architecture and security scoring',
  'File inventory and hotspot signals',
  'Re-analysis from saved repository files',
];

const reportSections = [
  ['Architecture', 'architectureReport'],
  ['Code Quality', 'codeQualityReport'],
  ['Security', 'securityReport'],
] as const;

function scoreModules(scores: AnalysisScores) {
  return [
    { label: 'Architecture', score: Math.round(scores.architecture), icon: BrainCircuit, tone: 'blue' },
    { label: 'Security', score: Math.round(scores.security), icon: ShieldCheck, tone: 'green' },
    { label: 'Maintainability', score: Math.round(scores.maintainability), icon: Radar, tone: 'violet' },
    { label: 'Docs', score: Math.round(scores.documentation), icon: BarChart3, tone: 'amber' },
    { label: 'Tests', score: Math.round(scores.testing), icon: CheckCircle2, tone: 'green' },
  ];
}

function errorMessage(error: unknown, fallback: string) {
  const apiError = error as ApiError;
  return apiError.response?.data?.message ?? fallback;
}

function formatBytes(sizeBytes: number) {
  if (sizeBytes < 1024) return `${sizeBytes} B`;
  return `${Math.round(sizeBytes / 102.4) / 10} KB`;
}

function App() {
  const [authMode, setAuthMode] = useState<AuthMode>('register');
  const [name, setName] = useState('Jagruti Deore');
  const [email, setEmail] = useState('jagruti@example.com');
  const [password, setPassword] = useState('password123');
  const [githubUrl, setGithubUrl] = useState('https://github.com/Jagruti2004deore/RepoLens-AI');
  const [user, setUser] = useState<AuthUser | null>(() => {
    const raw = localStorage.getItem('repolens_user');
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  });
  const [repositories, setRepositories] = useState<RepositorySummary[]>(defaultRepos);
  const [selectedRepoId, setSelectedRepoId] = useState<number | null>(null);
  const [analysis, setAnalysis] = useState<AnalysisReport | null>(null);
  const [files, setFiles] = useState<RepositoryFileSummary[]>([]);
  const [message, setMessage] = useState('RepoLensAI is ready. Sign in, import a GitHub repository, and open the generated review.');
  const [isBusy, setIsBusy] = useState(false);

  const selectedRepo = repositories.find((repo) => repo.id === selectedRepoId) ?? repositories[0];
  const activeScores = analysis?.scores ?? selectedRepo?.scores ?? defaultScores;
  const modules = useMemo(() => scoreModules(activeScores), [activeScores]);
  const overall = Math.round(activeScores.overall);
  const highFindings = analysis?.findings.filter((finding) => finding.startsWith('[HIGH]')).length ?? 0;
  const mediumFindings = analysis?.findings.filter((finding) => finding.startsWith('[MEDIUM]')).length ?? 0;

  useEffect(() => {
    if (user) {
      void loadRepositories(true);
    }
  }, [user]);

  async function loadRepositories(openFirst = false) {
    try {
      const { data } = await api.get<RepositorySummary[]>('/repositories');
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
  }

  async function loadRepositoryFiles(repositoryId: number) {
    try {
      const { data } = await api.get<RepositoryFileSummary[]>(`/repositories/${repositoryId}/files`);
      setFiles(data);
    } catch {
      setFiles([]);
    }
  }

  async function loadAnalysis(repositoryId: number) {
    setIsBusy(true);
    setMessage('Loading analysis report...');
    try {
      const { data } = await api.get<AnalysisReport>(`/repositories/${repositoryId}/analysis`);
      setAnalysis(data);
      setSelectedRepoId(repositoryId);
      await loadRepositoryFiles(repositoryId);
      setMessage(`${data.owner}/${data.repositoryName} analysis loaded with ${data.importedFileCount} files reviewed.`);
    } catch (error) {
      setAnalysis(null);
      setFiles([]);
      setSelectedRepoId(repositoryId);
      setMessage(errorMessage(error, 'Analysis is not available for this repository yet. Import it with the backend running.'));
    } finally {
      setIsBusy(false);
    }
  }

  async function handleAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsBusy(true);
    setMessage('Contacting RepoLensAI backend...');

    try {
      const payload = authMode === 'register' ? { name, email, password } : { email, password };
      const { data } = await api.post<AuthUser>(`/auth/${authMode}`, payload);
      localStorage.setItem('repolens_token', data.token);
      localStorage.setItem('repolens_user', JSON.stringify(data));
      setUser(data);
      setMessage(`Signed in as ${data.name}. Repository import is ready.`);
    } catch (error) {
      setMessage(errorMessage(error, 'Could not sign in. Start Spring Boot and PostgreSQL, then try again.'));
    } finally {
      setIsBusy(false);
    }
  }

  async function handleImport(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsBusy(true);
    setMessage('Fetching repository files from GitHub and running analysis...');

    try {
      const { data } = await api.post<RepositorySummary>('/repositories/import', { githubUrl });
      setRepositories((current) => [data, ...current.filter((repo) => repo.id !== data.id)]);
      setSelectedRepoId(data.id);
      setMessage(`${data.owner}/${data.name} imported. Opening generated analysis...`);
      await loadAnalysis(data.id);
    } catch (error) {
      setMessage(errorMessage(error, 'Import failed. Check login, backend status, GitHub URL, and internet connection.'));
    } finally {
      setIsBusy(false);
    }
  }

  async function handleReanalyze() {
    if (!analysis) return;
    setIsBusy(true);
    setMessage('Re-running analysis from saved files...');
    try {
      const { data } = await api.post<AnalysisReport>(`/repositories/${analysis.repositoryId}/reanalyze`);
      setAnalysis(data);
      await loadRepositoryFiles(data.repositoryId);
      await loadRepositories(false);
      setMessage(`Fresh analysis completed for ${data.owner}/${data.repositoryName}.`);
    } catch (error) {
      setMessage(errorMessage(error, 'Re-analysis failed. Try again after the backend is running.'));
    } finally {
      setIsBusy(false);
    }
  }

  function handleSignOut() {
    localStorage.removeItem('repolens_token');
    localStorage.removeItem('repolens_user');
    setUser(null);
    setAnalysis(null);
    setFiles([]);
    setSelectedRepoId(null);
    setRepositories(defaultRepos);
    setMessage('Signed out locally.');
  }

  return (
    <main className="app-shell">
      <section className="hero-band">
        <nav className="topbar" aria-label="Primary navigation">
          <div className="brand-lockup">
            <div className="brand-mark"><Radar size={22} /></div>
            <span>RepoLensAI</span>
          </div>
          <div className="topbar-actions">
            <a href="https://github.com/Jagruti2004deore/RepoLens-AI" target="_blank" rel="noreferrer">
              <GitBranch size={17} /> GitHub
            </a>
            {user && <button type="button" onClick={handleSignOut}>Sign out</button>}
          </div>
        </nav>

        <div className="hero-grid">
          <div className="hero-copy">
            <div className="eyebrow"><Sparkles size={16} /> Intelligent code review and architecture analyzer</div>
            <h1>RepoLensAI</h1>
            <p>
              Import a GitHub repository and generate architecture, security, maintainability, testing, documentation, interview, and resume-ready analysis.
            </p>
            <div className="hero-actions">
              <a className="primary-action" href="#workspace">Open workspace <ArrowRight size={18} /></a>
              <span className="status-pill"><CheckCircle2 size={16} /> Day 2/3 workspace</span>
            </div>
          </div>

          <div className="review-console" aria-label="RepoLensAI score preview">
            <div className="console-header">
              <span>{analysis ? `${analysis.owner}/${analysis.repositoryName}` : 'Overall readiness'}</span>
              <strong>{overall}/100</strong>
            </div>
            <div className="score-ring" style={{ background: `conic-gradient(#80ed99 0 ${overall}%, rgba(255, 255, 255, 0.1) ${overall}% 100%)` }}>
              <span>{overall}</span>
            </div>
            <div className="module-list">
              {modules.map((item) => (
                <div className="module-row" key={item.label} data-tone={item.tone}>
                  <item.icon size={18} />
                  <span>{item.label}</span>
                  <div className="meter"><span style={{ width: `${item.score}%` }} /></div>
                  <strong>{item.score}</strong>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      <section className="workspace" id="workspace">
        <aside className="auth-card">
          <div className="section-heading">
            <KeyRound size={20} />
            <div>
              <h2>{user ? 'Session' : 'Access'}</h2>
              <p>{user ? user.email : 'Create your reviewer workspace.'}</p>
            </div>
          </div>

          {user ? (
            <div className="signed-in">
              <div className="avatar">{user.name.charAt(0).toUpperCase()}</div>
              <div>
                <strong>{user.name}</strong>
                <span>JWT session stored locally</span>
              </div>
            </div>
          ) : (
            <form className="stack-form" onSubmit={handleAuth}>
              <div className="segmented">
                <button type="button" className={authMode === 'register' ? 'active' : ''} onClick={() => setAuthMode('register')}>
                  <UserPlus size={16} /> Register
                </button>
                <button type="button" className={authMode === 'login' ? 'active' : ''} onClick={() => setAuthMode('login')}>
                  <LogIn size={16} /> Login
                </button>
              </div>
              {authMode === 'register' && (
                <label>Name<input value={name} onChange={(event) => setName(event.target.value)} /></label>
              )}
              <label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
              <label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
              <button className="submit-button" type="submit" disabled={isBusy}>
                <LockKeyhole size={17} /> {authMode === 'register' ? 'Create account' : 'Sign in'}
              </button>
            </form>
          )}
        </aside>

        <section className="analysis-panel">
          <div className="section-heading">
            <GitBranch size={22} />
            <div>
              <h2>Repository Analysis</h2>
              <p>Paste a public GitHub URL to generate a review.</p>
            </div>
          </div>

          <form className="repo-form" onSubmit={handleImport}>
            <input value={githubUrl} onChange={(event) => setGithubUrl(event.target.value)} placeholder="https://github.com/owner/repository" />
            <button type="submit" disabled={isBusy || !user}>Analyze <ArrowRight size={17} /></button>
          </form>

          <div className="message-line"><Bot size={17} /> {message}</div>

          <div className="repo-table">
            <div className="table-head"><span>Repository</span><span>Status</span><span>Files</span><span>Score</span></div>
            {repositories.map((repo) => (
              <button className={repo.id === selectedRepoId ? 'repo-row active' : 'repo-row'} type="button" key={`${repo.id}-${repo.importedAt}`} onClick={() => loadAnalysis(repo.id)} disabled={isBusy}>
                <span><GitBranch size={16} /> {repo.owner}/{repo.name}</span>
                <strong>{repo.status}</strong>
                <small>{repo.importedFileCount ?? 0}</small>
                <b>{Math.round(repo.scores?.overall ?? 0) || '--'}</b>
              </button>
            ))}
          </div>
        </section>
      </section>

      {analysis && (
        <section className="report-band">
          <div className="report-header">
            <div className="section-heading">
              <BrainCircuit size={22} />
              <div>
                <h2>Generated Review</h2>
                <p>{analysis.defaultBranch} branch, {analysis.importedFileCount} files, completed {new Date(analysis.completedAt).toLocaleString()}</p>
              </div>
            </div>
            <div className="report-actions">
              <button type="button" onClick={handleReanalyze} disabled={isBusy}><History size={17} /> Re-analyze</button>
              <a href={analysis.githubUrl} target="_blank" rel="noreferrer"><GitBranch size={17} /> Open repo</a>
            </div>
          </div>

          <div className="score-grid">
            {modules.map((item) => (
              <div className="score-card" key={item.label}>
                <item.icon size={19} />
                <span>{item.label}</span>
                <strong>{item.score}</strong>
              </div>
            ))}
            <div className="score-card risk-card">
              <ShieldCheck size={19} />
              <span>Risk Findings</span>
              <strong>{highFindings}H/{mediumFindings}M</strong>
            </div>
          </div>

          <div className="report-grid">
            {reportSections.map(([title, key]) => (
              <article className="report-card" key={key}>
                <h3>{title}</h3>
                <pre>{analysis[key]}</pre>
              </article>
            ))}
          </div>

          <div className="insight-grid">
            <article className="report-card">
              <h3>Recommendations</h3>
              <pre>{analysis.recommendations}</pre>
            </article>
            <article className="report-card">
              <h3>Interview Questions</h3>
              <pre>{analysis.interviewQuestions}</pre>
            </article>
            <article className="report-card wide">
              <h3>Resume Summary</h3>
              <p>{analysis.resumeSummary}</p>
            </article>
          </div>

          <div className="inventory-section">
            <div className="inventory-header">
              <h3>File Inventory</h3>
              <span>{files.length} imported files</span>
            </div>
            {files.length === 0 ? (
              <div className="empty-inventory">No source files were imported from this repository yet.</div>
            ) : (
              <div className="file-list">
                {files.slice(0, 12).map((file) => (
                  <div className="file-row" key={file.id}>
                    <span>{file.path}</span>
                    <strong>{file.role}</strong>
                    <small>{file.language} · {file.lineCount} lines · {formatBytes(file.sizeBytes)}</small>
                    <em>{file.signals.length > 0 ? file.signals.join(', ') : 'No special signals'}</em>
                  </div>
                ))}
              </div>
            )}
          </div>

          {analysis.findings.length > 0 && (
            <div className="findings-list">
              <h3>Findings</h3>
              {analysis.findings.map((finding) => <div className="finding" key={finding}>{finding}</div>)}
            </div>
          )}
        </section>
      )}

      <section className="milestone-band">
        <div className="section-heading">
          <History size={22} />
          <div>
            <h2>MVP Build Scope</h2>
            <p>Core pieces needed for a working RepoLensAI demo.</p>
          </div>
        </div>
        <div className="milestone-grid">
          {buildScope.map((item) => (
            <div className="milestone" key={item}><CheckCircle2 size={18} /> {item}</div>
          ))}
        </div>
      </section>
    </main>
  );
}

export default App;