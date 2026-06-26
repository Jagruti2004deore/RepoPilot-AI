import { Code2 } from 'lucide-react';
import { serviceCards } from '../../utils/constants';

export function Services() {
  return (
    <section className="services-section" id="services">
      <div className="section-kicker"><Code2 size={18} /> Services coders actually use</div>
      <div className="section-heading-large">
        <h2>Everything needed to understand, improve, and present a repository.</h2>
        <p>Built for students, portfolio builders, and developers who want practical code review plus interview-ready explanations.</p>
      </div>
      <div className="service-grid">
        {serviceCards.map((service) => (
          <article className="service-card" key={service.title}>
            <div className="service-icon"><service.icon size={22} /></div>
            <h3>{service.title}</h3>
            <p>{service.text}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
