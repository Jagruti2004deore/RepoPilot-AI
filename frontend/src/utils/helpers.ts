import type { ApiError } from '../types';

export function errorMessage(error: unknown, fallback: string) {
  const apiError = error as ApiError;
  return apiError.response?.data?.message ?? fallback;
}

export function apiStatus(error: unknown) {
  return (error as ApiError).response?.status;
}