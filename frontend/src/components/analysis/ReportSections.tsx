import { Clipboard, FileText } from 'lucide-react';
import { memo } from 'react';
import type { AnalysisReport } from '../../types';

type ReportKey = keyof Pick<AnalysisReport, 'architectureReport' | 'codeQualityReport' | 'securityReport' | 'recommendations' | 'interviewQuestions' | 'resumeSummary'>;

type ReportSectionItem = {
  title: string;
  key: ReportKey;
  eyebrow: string;
};

type ReportSectionsProps = {
  analysis: AnalysisReport;
  onCopy: (value: string, label: string) => void;
  sections?: ReportSectionItem[];
};

const defaultSections: ReportSectionItem[] = [
  { title: 'Architecture Review', key: 'architectureReport', eyebrow: 'System structure' },
  { title: 'Security Review', key: 'securityReport', eyebrow: 'Risk controls' },
  { title: 'Code Quality Review', key: 'codeQualityReport', eyebrow: 'Maintainability' },
  { title: 'Recommendations', key: 'recommendations', eyebrow: 'Next improvements' },
  { title: 'Interview Questions', key: 'interviewQuestions', eyebrow: 'Viva prep' },
  { title: 'Resume Summary', key: 'resumeSummary', eyebrow: 'Portfolio pitch' },
];

export const reportSectionGroups = {
  architecture: [{ title: 'Architecture Review', key: 'architectureReport', eyebrow: 'System structure' }] as ReportSectionItem[],
  security: [{ title: 'Security Review', key: 'securityReport', eyebrow: 'Risk controls' }] as ReportSectionItem[],
  quality: [
    { title: 'Code Quality Review', key: 'codeQualityReport', eyebrow: 'Maintainability' },
    { title: 'Recommendations', key: 'recommendations', eyebrow: 'Next improvements' },
  ] as ReportSectionItem[],
};

export const ReportSections = memo(function ReportSections({ analysis, onCopy, sections = defaultSections }: ReportSectionsProps) {
  return (
    <div className="accordion-stack fade-in-section">
      {sections.map((section, index) => {
        const value = String(analysis[section.key] ?? '');
        return (
          <details className="report-accordion" key={section.key} open={index === 0}>
            <summary>
              <span><FileText size={18} /> {section.eyebrow}</span>
              <strong>{section.title}</strong>
            </summary>
            <div className="accordion-body">
              <button type="button" className="copy-button" onClick={() => onCopy(value, section.title)}><Clipboard size={15} /> Copy</button>
              <pre className="syntax-block">{value}</pre>
            </div>
          </details>
        );
      })}
    </div>
  );
});
