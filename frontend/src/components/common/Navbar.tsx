import { ChevronDown, GitBranch, LogOut, Radar, UserRound } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useRepositories } from '../../hooks/useRepositories';
import { GITHUB_URL } from '../../utils/constants';

export function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, signOut } = useAuth();
  const { resetWorkspace } = useRepositories();
  const isWorkspace = location.pathname.startsWith('/workspace');

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
            <Link className={isWorkspace ? 'active-link' : ''} to="/workspace">Workspace</Link>
            <details className="profile-menu">
              <summary><UserRound size={16} /> {user.name}<ChevronDown size={14} /></summary>
              <div className="profile-popover">
                <span>{user.email}</span>
                <button type="button" onClick={handleSignOut}><LogOut size={15} /> Sign out</button>
              </div>
            </details>
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
