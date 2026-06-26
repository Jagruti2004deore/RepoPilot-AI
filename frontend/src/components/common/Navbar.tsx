import { GitBranch, Radar } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useRepositories } from '../../hooks/useRepositories';
import { GITHUB_URL } from '../../utils/constants';

export function Navbar() {
  const navigate = useNavigate();
  const { user, signOut } = useAuth();
  const { resetWorkspace } = useRepositories();

  function handleSignOut() {
    signOut();
    resetWorkspace();
    navigate('/');
  }

  return (
    <nav className="topbar" aria-label="Primary navigation">
      <Link className="brand-lockup" to="/">
        <div className="brand-mark"><Radar size={22} /></div>
        <span>RepoPilot AI</span>
      </Link>
      <div className="topbar-actions">
        <a href="/#services">Services</a>
        <a href="/#how-it-works">How it works</a>
        <a href={GITHUB_URL} target="_blank" rel="noreferrer"><GitBranch size={17} /> GitHub</a>
        {user ? (
          <>
            <Link to="/workspace">Workspace</Link>
            <button type="button" onClick={handleSignOut}>Sign out</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link className="nav-primary" to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
