import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <section className="report-band empty-analysis">
      <h1>Page not found</h1>
      <p>This RepoPilot AI page does not exist.</p>
      <Link className="primary-action" to="/">Back home</Link>
    </section>
  );
}
