export type AuthMode = 'login' | 'register';

export type AuthUser = {
  token: string;
  userId: number;
  name: string;
  email: string;
};

export type AnalysisScores = {
  architecture: number;
  security: number;
  maintainability: number;
  documentation: number;
  testing: number;
  overall: number;
};

export type ReadinessScores = {
  resume: number;
  interview: number;
  github: number;
  deployment: number;
  demo: number;
  overall: number;
};

export type RepositorySummary = {
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

export type AnalysisReport = {
  analysisId: number;
  repositoryId: number;
  repositoryName: string;
  owner: string;
  githubUrl: string;
  status: string;
  defaultBranch: string;
  importedFileCount: number;
  scores: AnalysisScores;
  readinessScores: ReadinessScores;
  architectureReport: string;
  codeQualityReport: string;
  securityReport: string;
  recommendations: string;
  interviewQuestions: string;
  interviewAnswers: string;
  vivaQuestions: string;
  presentationScript: string;
  architectureExplanation: string;
  readinessChecklist: string;
  readinessReport: string;
  resumeSummary: string;
  resumeBullets: string;
  githubProfileTips: string;
  readmeSuggestions: string;
  projectTitleSuggestions: string;
  findings: string[];
  completedAt: string;
};

export type AnalysisHistoryItem = {
  analysisId: number;
  status: string;
  scores: AnalysisScores;
  readinessScores: ReadinessScores;
  findingCount: number;
  completedAt: string;
};

export type RepositoryFileSummary = {
  id: number;
  path: string;
  language: string;
  sizeBytes: number;
  lineCount: number;
  role: string;
  signals: string[];
};

export type RepoChatMessage = {
  id: number;
  question: string;
  answer: string;
  createdAt: string;
};

export type ApiError = {
  response?: {
    status?: number;
    data?: {
      message?: string;
    };
  };
};