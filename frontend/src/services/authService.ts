import type { AuthMode, AuthUser } from '../types';
import { api } from './api';

export type AuthPayload = {
  name?: string;
  email: string;
  password: string;
};

export async function authenticate(mode: AuthMode, payload: AuthPayload) {
  const { data } = await api.post<AuthUser>(`/auth/${mode}`, payload);
  return data;
}