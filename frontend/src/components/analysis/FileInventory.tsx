import { FileCode2, Folder, Search } from 'lucide-react';
import { memo, useMemo, useState } from 'react';
import type { RepositoryFileSummary } from '../../types';
import { formatBytes } from '../../utils/formatters';

type SortKey = 'path' | 'language' | 'lines' | 'size';

function fileGroup(path: string) {
  const parts = path.split('/');
  return parts.length > 1 ? parts[0] : 'root';
}

function extension(path: string) {
  const pieces = path.split('.');
  return pieces.length > 1 ? pieces.pop()?.toUpperCase() ?? 'FILE' : 'FILE';
}

export const FileInventory = memo(function FileInventory({ files }: { files: RepositoryFileSummary[] }) {
  const [query, setQuery] = useState('');
  const [language, setLanguage] = useState('all');
  const [role, setRole] = useState('all');
  const [sort, setSort] = useState<SortKey>('path');

  const languages = useMemo(() => Array.from(new Set(files.map((file) => file.language).filter(Boolean))).sort(), [files]);
  const roles = useMemo(() => Array.from(new Set(files.map((file) => file.role).filter(Boolean))).sort(), [files]);

  const filteredFiles = useMemo(() => {
    const normalized = query.toLowerCase();
    return files
      .filter((file) => file.path.toLowerCase().includes(normalized) || file.role.toLowerCase().includes(normalized) || file.language.toLowerCase().includes(normalized))
      .filter((file) => language === 'all' || file.language === language)
      .filter((file) => role === 'all' || file.role === role)
      .sort((a, b) => {
        if (sort === 'lines') return b.lineCount - a.lineCount;
        if (sort === 'size') return b.sizeBytes - a.sizeBytes;
        if (sort === 'language') return a.language.localeCompare(b.language);
        return a.path.localeCompare(b.path);
      });
  }, [files, language, query, role, sort]);

  const grouped = useMemo(() => {
    const map = new Map<string, RepositoryFileSummary[]>();
    filteredFiles.forEach((file) => {
      const group = fileGroup(file.path);
      map.set(group, [...(map.get(group) ?? []), file]);
    });
    return Array.from(map.entries());
  }, [filteredFiles]);

  return (
    <div className="inventory-section file-explorer fade-in-section">
      <div className="inventory-header">
        <div>
          <h3>File Inventory</h3>
          <span>{filteredFiles.length} of {files.length} imported files</span>
        </div>
      </div>

      <div className="file-toolbar">
        <label className="file-search"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search files, roles, languages" /></label>
        <select value={language} onChange={(event) => setLanguage(event.target.value)}>
          <option value="all">All languages</option>
          {languages.map((item) => <option value={item} key={item}>{item}</option>)}
        </select>
        <select value={role} onChange={(event) => setRole(event.target.value)}>
          <option value="all">All roles</option>
          {roles.map((item) => <option value={item} key={item}>{item}</option>)}
        </select>
        <select value={sort} onChange={(event) => setSort(event.target.value as SortKey)}>
          <option value="path">Sort by path</option>
          <option value="language">Sort by language</option>
          <option value="lines">Sort by lines</option>
          <option value="size">Sort by size</option>
        </select>
      </div>

      {filteredFiles.length === 0 ? (
        <div className="empty-inventory">No files match the current filters.</div>
      ) : (
        <div className="file-tree">
          {grouped.map(([group, groupFiles]) => (
            <details className="folder-group" key={group} open>
              <summary><Folder size={17} /> <strong>{group}</strong> <span>{groupFiles.length} files</span></summary>
              <div className="file-list">
                {groupFiles.map((file) => (
                  <div className="file-row modern-file-row" key={file.id}>
                    <span><FileCode2 size={16} /> {file.path}</span>
                    <strong>{file.role}</strong>
                    <small><b>{file.language || extension(file.path)}</b> {file.lineCount} lines - {formatBytes(file.sizeBytes)}</small>
                    <em>{file.signals.length > 0 ? file.signals.join(', ') : 'No special signals'}</em>
                  </div>
                ))}
              </div>
            </details>
          ))}
        </div>
      )}
    </div>
  );
});
