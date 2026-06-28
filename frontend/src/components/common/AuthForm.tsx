import { KeyRound, LockKeyhole, LogIn, UserPlus } from 'lucide-react';
import { memo, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import type { AuthMode } from '../../types';
import { errorMessage } from '../../utils/helpers';

type AuthFormProps = {
  mode: AuthMode;
};

export const AuthForm = memo(function AuthForm({ mode }: AuthFormProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { signIn } = useAuth();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState(mode === 'register' ? 'Create your reviewer workspace.' : 'Sign in to continue your review.');
  const [isBusy, setIsBusy] = useState(false);
  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/workspace';

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsBusy(true);
    setMessage('Securing your workspace...');

    try {
      const payload = mode === 'register' ? { name, email, password } : { email, password };
      const user = await signIn(mode, payload);
      setMessage(`Signed in as ${user.name}.`);
      navigate(from, { replace: true });
    } catch (error) {
      setMessage(errorMessage(error, 'We could not complete this request. Please check your details and try again.'));
    } finally {
      setIsBusy(false);
    }
  }

  return (
    <section className="auth-page auth-route-page" id="auth">
      <aside className="auth-card">
        <div className="section-heading">
          <KeyRound size={20} />
          <div>
            <h2>{mode === 'register' ? 'Create account' : 'Welcome back'}</h2>
            <p>{mode === 'register' ? 'Create your reviewer workspace.' : 'Sign in to continue your review.'}</p>
          </div>
        </div>

        <form className="stack-form" onSubmit={handleSubmit}>
          <div className="segmented">
            <Link className={mode === 'register' ? 'active' : ''} to="/register"><UserPlus size={16} /> Register</Link>
            <Link className={mode === 'login' ? 'active' : ''} to="/login"><LogIn size={16} /> Login</Link>
          </div>
          {mode === 'register' && (
            <label>Name<input value={name} onChange={(event) => setName(event.target.value)} autoComplete="name" required /></label>
          )}
          <label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="email" required /></label>
          <label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete={mode === 'register' ? 'new-password' : 'current-password'} required /></label>
          <button className="submit-button" type="submit" disabled={isBusy}>
            <LockKeyhole size={17} /> {mode === 'register' ? 'Create account' : 'Sign in'}
          </button>
        </form>
        <div className="message-line compact-message">{message}</div>
      </aside>
    </section>
  );
});
