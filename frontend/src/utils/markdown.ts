import type { AnalysisReport } from '../types';

export function buildMarkdownReport(analysis: AnalysisReport) {
  return `# RepoPilot AI Report - ${analysis.owner}/${analysis.repositoryName}

Overall score: ${Math.round(analysis.scores.overall)}/100
Project readiness: ${Math.round(analysis.readinessScores.overall)}/100
Files reviewed: ${analysis.importedFileCount}
Completed: ${new Date(analysis.completedAt).toLocaleString()}

## Architecture
${analysis.architectureReport}

## Security
${analysis.securityReport}

## Recommendations
${analysis.recommendations}

## Resume Summary
${analysis.resumeSummary}

## Resume Bullets
${analysis.resumeBullets}

## Coach Notes
${analysis.interviewAnswers}

## README Suggestions
${analysis.readmeSuggestions}
`;
}