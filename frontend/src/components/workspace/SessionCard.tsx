import { KeyRound } from 'lucide-react';
import { memo } from 'react';
import type { AuthUser } from '../../types';

export const SessionCard = memo(function SessionCard({ user }: { user: AuthUser }) {
  return (
    <aside className="auth-card">
      <div className="section-heading">
        <KeyRound size={20} />
        <div>
          <h2>Session</h2>
          <p>{user.email}</p>
        </div>
      </div>
      <div className="signed-in">
        <div className="avatar">{user.name.charAt(0).toUpperCase()}</div>
        <div>
          <strong>{user.name}</strong>
          <span>JWT session stored locally</span>
        </div>
      </div>
    </aside>
  );
});
