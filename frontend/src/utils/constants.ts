import { Layers3, MessageSquareText, ShieldCheck, Sparkles } from 'lucide-react';
import type { AnalysisScores } from '../types';

export const STORAGE_KEYS = {
  token: 'repolens_token',
  user: 'repolens_user',
} as const;
export const defaultScores: AnalysisScores = {
  architecture: 82,
  security: 74,
  maintainability: 88,
  documentation: 63,
  testing: 45,
  overall: 70,
};
export const serviceCards = [
  {
    title: 'Architecture Review',
    text: 'Map controllers, services, repositories, models, security, and configuration so your project is easy to explain.',
    icon: Layers3,
  },
  {
    title: 'Security Scan',
    text: 'Catch risky validation, secrets, CORS, JWT, and authorization patterns before your demo or review.',
    icon: ShieldCheck,
  },
  {
    title: 'Portfolio Coach',
    text: 'Generate resume bullets, viva prep, presentation script, README guidance, and GitHub profile tips.',
    icon: Sparkles,
  },
  {
    title: 'Repo Code Q&A',
    text: 'Ask questions about imported files and get answers with relevant source-file evidence.',
    icon: MessageSquareText,
  },
] as const;

export const workflowSteps = [
  'Sign in',
  'Paste GitHub URL',
  'Import repository files',
  'Review scores and findings',
  'Ask repo/code questions',
  'Export and prepare demo',
] as const;

export const coachTabs = [
  { key: 'interviewAnswers', label: 'Interview Answers' },
  { key: 'vivaQuestions', label: 'Viva Prep' },
  { key: 'presentationScript', label: 'Presentation Script' },
  { key: 'architectureExplanation', label: 'Architecture Talk' },
  { key: 'resumeBullets', label: 'Resume Bullets' },
  { key: 'githubProfileTips', label: 'GitHub Tips' },
  { key: 'readmeSuggestions', label: 'README Coach' },
  { key: 'projectTitleSuggestions', label: 'Project Titles' },
] as const;

export const reportSections = [
  ['Architecture', 'architectureReport'],
  ['Code Quality', 'codeQualityReport'],
  ['Security', 'securityReport'],
] as const;