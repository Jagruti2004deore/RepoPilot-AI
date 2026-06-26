import { ArrowRight, Route, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

export function Hero() {
  const { user } = useAuth();

  return (
    <section className="hero-band landing-hero">
      <div className="hero-grid">
        <div className="hero-copy">
          <div className="eyebrow"><Sparkles size={16} /> GitHub code review, project readiness, and repo Q&amp;A</div>
          <h1>Ship a portfolio project that reads like production code.</h1>
          <p>
            Import a GitHub repository, review architecture and security, generate interview-ready guidance, and ask questions about the actual codebase.
          </p>
          <div className="hero-actions">
            <Link className="primary-action" to={user ? '/workspace' : '/register'}>
              Analyze repo <ArrowRight size={18} />
            </Link>
            <a className="secondary-action" href="#how-it-works">See workflow <Route size={18} /></a>
          </div>
        </div>
        <div className="coder-console" aria-label="RepoPilot AI product preview">
          <div className="console-dots"><span /><span /><span /></div>
          <pre>{`repo-pilot review https://github.com/user/project

services:
  architecture_review: ready
  security_scan: ready
  portfolio_coach: ready
  repo_code_qna: ready

next: sign in and import your repository`}</pre>
        </div>
      </div>
    </section>
  );
}
