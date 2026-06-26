import { createContext, useCallback, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { authenticate } from '../services/authService';
import type { AuthMode, AuthUser } from '../types';
import { STORAGE_KEYS } from '../utils/constants';

export type AuthContextValue = {
  user: AuthUser | null;
  isAuthenticated: boolean;
  signIn: (mode: AuthMode, payload: { name?: string; email: string; password: string }) => Promise<AuthUser>;
  signOut: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

function readStoredUser() {
  const raw = localStorage.getItem(STORAGE_KEYS.user);
  if (!raw) return null;

  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    localStorage.removeItem(STORAGE_KEYS.user);
    localStorage.removeItem(STORAGE_KEYS.token);
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(readStoredUser);

  const signIn = useCallback(async (mode: AuthMode, payload: { name?: string; email: string; password: string }) => {
    const authUser = await authenticate(mode, payload);
    localStorage.setItem(STORAGE_KEYS.token, authUser.token);
    localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(authUser));
    setUser(authUser);
    return authUser;
  }, []);

  const signOut = useCallback(() => {
    localStorage.removeItem(STORAGE_KEYS.token);
    localStorage.removeItem(STORAGE_KEYS.user);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, isAuthenticated: Boolean(user), signIn, signOut }),
    [signIn, signOut, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
