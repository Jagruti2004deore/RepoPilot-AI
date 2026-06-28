import { Bot, Clipboard, FileCode2, FileQuestion, MessageSquareText, RefreshCcw, Sparkles, Trash2, UserRound } from 'lucide-react';
import { memo, useEffect, useMemo, useRef, useState } from 'react';
import type { FormEvent, ReactNode } from 'react';
import type { RepoChatMessage } from '../../types';

const suggestions = [
  'Explain the architecture of this repo.',
  'Which files handle authentication?',
  'What should I improve before demo?',
  'Summarize this project for interview.',
  'Where are security risks in this code?',
  'Create a 2 minute project explanation.',
];

type ChatPanelProps = {
  chatMessages: RepoChatMessage[];
  isAsking: boolean;
  onAsk: (question: string) => Promise<boolean>;
};

function fileReferences(text: string) {
  const matches = text.match(/[\w./-]+\.(tsx|ts|jsx|js|java|xml|yml|yaml|properties|json|md|css|html)/gi) ?? [];
  return Array.from(new Set(matches)).slice(0, 4);
}

function renderParagraph(line: string, index: number) {
  const trimmed = line.trim();
  if (!trimmed) return null;
  if (/^[-*]\s+/.test(trimmed)) return <p className="markdown-bullet" key={index}>{trimmed.replace(/^[-*]\s+/, '')}</p>;
  if (/^#{1,3}\s+/.test(trimmed)) return <strong className="markdown-heading" key={index}>{trimmed.replace(/^#{1,3}\s+/, '')}</strong>;
  return <p key={index}>{line}</p>;
}

function MarkdownLite({ text }: { text: string }) {
  const blocks = text.split(/```/g);
  const content = blocks.flatMap<ReactNode>((block, index) => {
    if (index % 2 === 1) {
      const [language, ...code] = block.split('\n');
      return (
        <pre className="chat-code-block" key={`code-${index}`}>
          <span>{language.trim() || 'code'}</span>
          <code>{code.join('\n').trim() || block.trim()}</code>
        </pre>
      );
    }

    return block.split('\n').map((line, lineIndex) => renderParagraph(line, index * 100 + lineIndex)).filter(Boolean);
  });

  return <div className="markdown-lite">{content}</div>;
}

export const ChatPanel = memo(function ChatPanel({ chatMessages, isAsking, onAsk }: ChatPanelProps) {
  const [repoQuestion, setRepoQuestion] = useState('Explain the architecture of this repo.');
  const [clearedBefore, setClearedBefore] = useState(0);
  const [copiedId, setCopiedId] = useState<number | null>(null);
  const listRef = useRef<HTMLDivElement | null>(null);
  const visibleMessages = useMemo(() => chatMessages.slice(clearedBefore), [chatMessages, clearedBefore]);
  const latestQuestion = visibleMessages.at(-1)?.question ?? chatMessages.at(-1)?.question ?? suggestions[0];

  useEffect(() => {
    listRef.current?.scrollTo({ top: listRef.current.scrollHeight, behavior: 'smooth' });
  }, [visibleMessages, isAsking]);

  useEffect(() => {
    if (clearedBefore > chatMessages.length) setClearedBefore(0);
  }, [chatMessages.length, clearedBefore]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const question = repoQuestion.trim();
    if (!question) return;
    const answered = await onAsk(question);
    if (answered) setRepoQuestion('');
  }

  async function copyAnswer(chat: RepoChatMessage) {
    await navigator.clipboard.writeText(chat.answer);
    setCopiedId(chat.id);
    window.setTimeout(() => setCopiedId(null), 1400);
  }

  async function regenerateLatest() {
    if (!latestQuestion || isAsking) return;
    await onAsk(latestQuestion);
  }

  return (
    <section className="repo-chat-panel premium-chat fade-in-section">
      <div className="chat-header">
        <div>
          <div className="section-kicker compact-kicker"><Sparkles size={16} /> Repo-aware assistant</div>
          <h3><FileQuestion size={18} /> Ask about this repo</h3>
          <p>Use natural questions about architecture, files, security, improvements, interviews, or demo preparation.</p>
        </div>
        <div className="chat-toolbar" aria-label="Chat actions">
          <button type="button" onClick={regenerateLatest} disabled={isAsking || !latestQuestion}><RefreshCcw size={15} /> Regenerate</button>
          <button type="button" onClick={() => setClearedBefore(chatMessages.length)} disabled={chatMessages.length === 0}><Trash2 size={15} /> Clear</button>
        </div>
      </div>

      <div className="suggestion-chips" aria-label="Suggested repo questions">
        {suggestions.map((question) => (
          <button type="button" key={question} onClick={() => setRepoQuestion(question)}>{question}</button>
        ))}
      </div>

      <div className="chat-list bubble-list" ref={listRef}>
        {visibleMessages.length === 0 ? (
          <div className="empty-chat">
            <Bot size={24} />
            <strong>Start a repo conversation</strong>
            <span>Ask anything tied to the imported source code, latest analysis, findings, files, or portfolio guidance.</span>
          </div>
        ) : visibleMessages.map((chat) => {
          const references = fileReferences(`${chat.question}\n${chat.answer}`);
          return (
            <article className="chat-turn" key={chat.id}>
              <div className="chat-bubble user-bubble"><UserRound size={16} /><span>{chat.question}</span></div>
              <div className="chat-bubble ai-bubble">
                <Bot size={16} />
                <div className="ai-message-body">
                  <MarkdownLite text={chat.answer} />
                  {references.length > 0 && (
                    <div className="source-badges">
                      {references.map((reference) => <span key={reference}><FileCode2 size={13} /> {reference}</span>)}
                    </div>
                  )}
                  <button type="button" className="message-copy" onClick={() => void copyAnswer(chat)}>
                    <Clipboard size={14} /> {copiedId === chat.id ? 'Copied' : 'Copy answer'}
                  </button>
                </div>
              </div>
            </article>
          );
        })}
        {isAsking && (
          <div className="chat-bubble ai-bubble loading-bubble">
            <Bot size={16} />
            <div className="thinking-line"><span /><span /><span /><b>Thinking with repository context</b></div>
          </div>
        )}
      </div>

      <form className="chat-form" onSubmit={handleSubmit}>
        <input value={repoQuestion} onChange={(event) => setRepoQuestion(event.target.value)} placeholder="Ask a repository question..." aria-label="Repository question" />
        <button type="submit" disabled={isAsking || !repoQuestion.trim()}><MessageSquareText size={17} /> Ask</button>
      </form>
    </section>
  );
});
