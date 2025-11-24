const daySelect = document.querySelector('#day-select');
const scheduleRows = document.querySelectorAll('.schedule__table tbody tr');
const navToggle = document.querySelector('.nav__toggle');
const navLinks = document.querySelector('.nav__links');

daySelect?.addEventListener('change', (event) => {
  const selected = event.target.value;

  scheduleRows.forEach((row) => {
    const day = row.getAttribute('data-day');
    if (selected === 'all' || selected === day) {
      row.classList.remove('is-hidden');
    } else {
      row.classList.add('is-hidden');
    }
  });
});

navToggle?.addEventListener('click', () => {
  const isExpanded = navToggle.getAttribute('aria-expanded') === 'true';
  navToggle.setAttribute('aria-expanded', String(!isExpanded));
  navLinks.classList.toggle('is-active');
});

navLinks?.querySelectorAll('a').forEach((link) => {
  link.addEventListener('click', () => {
    navLinks.classList.remove('is-active');
    navToggle?.setAttribute('aria-expanded', 'false');
  });
});

const observer = new IntersectionObserver(
  (entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible');
        observer.unobserve(entry.target);
      }
    });
  },
  {
    threshold: 0.2,
  }
);

document.querySelectorAll('.card, .profile, .quote').forEach((element, index) => {
  element.style.setProperty('--delay', `${index * 80}ms`);
  observer.observe(element);
});
