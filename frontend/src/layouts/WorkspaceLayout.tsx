import { Outlet } from 'react-router-dom';
import { Navbar } from '../components/common/Navbar';

export function WorkspaceLayout() {
  return (
    <main className="app-shell site-shell">
      <section className="hero-band nav-only">
        <Navbar />
      </section>
      <Outlet />
    </main>
  );
}
