import { Outlet } from 'react-router-dom';
import { Navbar } from '../components/common/Navbar';
import { Footer } from '../components/common/Footer';

export function MainLayout() {
  return (
    <main className="app-shell site-shell">
      <section className="hero-band nav-only">
        <Navbar />
      </section>
      <Outlet />
      <Footer />
    </main>
  );
}
