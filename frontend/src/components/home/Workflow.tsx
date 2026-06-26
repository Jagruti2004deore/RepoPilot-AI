import { Pause, Play, RotateCcw, TerminalSquare } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { workflowSteps } from '../../utils/constants';

const workflowDetails = [
  {
    label: 'JWT workspace',
    command: 'POST /api/auth/login',
    result: '200 OK - reviewer token stored',
    note: 'A protected workspace is created before repo import starts.',
    metric: 'Auth ready',
  },
  {
    label: 'Public GitHub URL',
    command: 'repo-pilot import https://github.com/user/project',
    result: 'URL parsed - owner/user and repository/project detected',
    note: 'RepoPilot validates the URL and prepares the import request.',
    metric: 'Repo detected',
  },
  {
    label: 'Source snapshot',
    command: 'POST /api/repositories/import',
    result: 'Files indexed - controllers, services, configs, docs',
    note: 'Important source files are stored so analysis and Q&A can use them later.',
    metric: 'Files mapped',
  },
  {
    label: 'Architecture, security, docs',
    command: 'GET /api/repositories/{id}/analysis',
    result: 'Scores generated - architecture, security, docs, tests',
    note: 'The report turns code signals into practical review sections.',
    metric: 'Review ready',
  },
  {
    label: 'Code-aware answers',
    command: 'POST /api/repositories/{id}/chat/ask',
    result: 'Answer returned with repo context and source evidence',
    note: 'Users ask project questions and get explanations tied to imported code.',
    metric: 'Q&A active',
  },
  {
    label: 'Markdown and demo prep',
    command: 'EXPORT repopilot-report.md',
    result: 'Portfolio coach, README tips, viva prep, and resume bullets exported',
    note: 'The final output helps users explain and present their project confidently.',
    metric: 'Demo ready',
  },
] as const;

export function Workflow() {
  const [activeStep, setActiveStep] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const active = workflowDetails[activeStep];
  const progress = useMemo(() => ((activeStep + 1) / workflowDetails.length) * 100, [activeStep]);

  useEffect(() => {
    if (!isPlaying) return undefined;

    const timer = window.setInterval(() => {
      setActiveStep((current) => {
        if (current === workflowDetails.length - 1) {
          setIsPlaying(false);
          return current;
        }
        return current + 1;
      });
    }, 1400);

    return () => window.clearInterval(timer);
  }, [isPlaying]);

  function restartDemo() {
    setActiveStep(0);
    setIsPlaying(true);
  }

  return (
    <section className="how-section coder-flow-section interactive-workflow" id="how-it-works">
      <div className="how-copy">
        <div className="section-kicker"><TerminalSquare size={18} /> Interactive usage guide</div>
        <h2>Click through the review pipeline like a live developer workflow.</h2>
        <p>Choose a step or run the demo to see how RepoPilot AI moves from authentication to code-aware Q&amp;A and export-ready project material.</p>

        <div className="workflow-controls" aria-label="Workflow controls">
          <button type="button" className="primary-action mini-action" onClick={() => setIsPlaying((current) => !current)}>
            {isPlaying ? <Pause size={16} /> : <Play size={16} />} {isPlaying ? 'Pause flow' : 'Run flow'}
          </button>
          <button type="button" className="secondary-action mini-action" onClick={restartDemo}>
            <RotateCcw size={16} /> Restart
          </button>
        </div>

        <div className="workflow-progress" aria-label={`Workflow progress ${Math.round(progress)} percent`}>
          <span style={{ width: `${progress}%` }} />
        </div>

        <div className="workflow-terminal live-terminal">
          <div className="console-dots"><span /><span /><span /></div>
          <div className="terminal-toolbar">
            <strong>step {String(activeStep + 1).padStart(2, '0')}</strong>
            <span>{active.metric}</span>
          </div>
          <pre>{`$ ${active.command}
> ${active.result}

# ${active.note}`}</pre>
        </div>
      </div>

      <div className="pipeline-board interactive-board" aria-label="RepoPilot AI workflow diagram">
        {workflowSteps.map((step, index) => (
          <button
            className={index === activeStep ? 'pipeline-step active' : index < activeStep ? 'pipeline-step complete' : 'pipeline-step'}
            key={step}
            type="button"
            onClick={() => {
              setActiveStep(index);
              setIsPlaying(false);
            }}
            aria-pressed={index === activeStep}
          >
            <span>{String(index + 1).padStart(2, '0')}</span>
            <div>
              <strong>{step}</strong>
              <small>{workflowDetails[index].label}</small>
            </div>
          </button>
        ))}
      </div>
    </section>
  );
}
