import { useContext } from 'react';
import { RepositoryContext } from '../context/RepositoryContext';

export function useRepositories() {
  const context = useContext(RepositoryContext);

  if (!context) {
    throw new Error('useRepositories must be used inside RepositoryProvider');
  }

  return context;
}
