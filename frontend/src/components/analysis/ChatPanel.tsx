import { Bot, FileQuestion, MessageSquareText, UserRound } from 'lucide-react';
import { memo, useEffect, useRef, useState } from 'react';
import type { FormEvent } from 'react';
import type { RepoChatMessage } from '../../types';

const suggestions = [
  'Explain the architecture of this repo.',
  'Which files handle authentication?',
  'What should I improve before demo?',
  'Summarize this project for interview.',
];

type ChatPanelProps = {
  chatMessages: RepoChatMessage[];
  isAsking: boolean;
  onAsk: (question: string) => Promise<boolean>;
};

function MarkdownLite({ text }: { text: string }) {
  return (
    <div className="markdown-lite">
      {text.split('\n').filter(Boolean).map((line) => {
        if (line.trim().startsWith('```')) return null;
        if (line.trim().startsWith('-')) return <p className="markdown-bullet" key={line}>{line}</p>;
        return <p key={line}>{line}</p>;
      })}
    </div>
  );
}

export const ChatPanel = memo(function ChatPanel({ chatMessages, isAsking, onAsk }: ChatPanelProps) {
  const [repoQuestion, setRepoQuestion] = useState('Explain the architecture of this repo.');
  const listRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    listRef.current?.scrollTo({ top: listRef.current.scrollHeight, behavior: 'smooth' });
  }, [chatMessages, isAsking]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const answered = await onAsk(repoQuestion);
    if (answered) setRepoQuestion('');
  }

  return (
    <section className="repo-chat-panel premium-chat fade-in-section">
      <div className="chat-header">
        <div>
          <h3><FileQuestion size={18} /> Ask about this repo</h3>
          <p>Ask architecture, security, file, or improvement questions based on imported code and the latest analysis.</p>
        </div>
      </div>

      <div className="suggestion-chips">
        {suggestions.map((question) => (
          <button type="button" key={question} onClick={() => setRepoQuestion(question)}>{question}</button>
        ))}
      </div>

      <div className="chat-list bubble-list" ref={listRef}>
        {chatMessages.length === 0 ? (
          <div className="empty-chat">No questions yet. Pick a suggestion or ask something specific about the codebase.</div>
        ) : chatMessages.map((chat) => (
          <article className="chat-turn" key={chat.id}>
            <div className="chat-bubble user-bubble"><UserRound size={16} /><span>{chat.question}</span></div>
            <div className="chat-bubble ai-bubble"><Bot size={16} /><MarkdownLite text={chat.answer} /></div>
          </article>
        ))}
        {isAsking && <div className="chat-bubble ai-bubble loading-bubble"><Bot size={16} /><span /><span /><span /></div>}
      </div>

      <form className="chat-form" onSubmit={handleSubmit}>
        <input value={repoQuestion} onChange={(event) => setRepoQuestion(event.target.value)} placeholder="Example: Which files handle authentication?" />
        <button type="submit" disabled={isAsking || !repoQuestion.trim()}><MessageSquareText size={17} /> Ask</button>
      </form>
    </section>
  );
});
