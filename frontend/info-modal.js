(function () {
  if (window.ClubPortalInfoModal) return;

  const infoMap = {
    privacy: {
      title: 'Privacy',
      body: `
        <p>We store the information needed to run Club Portal features such as sign-in, club setup, bookings, memberships, payments, chat, and community Q&amp;A.</p>
        <p>Depending on how this project is deployed, data may be stored in your browser and on the backend server. Some features can also use configured third-party services such as Google sign-in or Maps, Stripe payments, and AI-assisted chat.</p>
        <p>If you need your account or club data corrected or removed, please contact the platform admin.</p>
      `
    },
    terms: {
      title: 'Terms',
      body: `
        <p>Bookings are first-come, first-served and subject to club capacity.</p>
        <p>Members must follow club rules and respect facility policies.</p>
        <p>Repeated no-shows may result in booking restrictions.</p>
      `
    },
    help: {
      title: 'Help',
      body: `
        <p>Need assistance? Start by searching for a club and selecting a time slot.</p>
        <p>If you cannot log in, double-check your email and password.</p>
        <p>Contact support at support@example.com for further help.</p>
      `
    }
  };

  const ensureOverlay = () => {
    let overlay = document.getElementById('infoOverlay');
    if (overlay) return overlay;

    overlay = document.createElement('div');
    overlay.id = 'infoOverlay';
    overlay.className = 'info-overlay';
    overlay.setAttribute('aria-hidden', 'true');
    overlay.innerHTML = `
      <div class="info-card" role="dialog" aria-modal="true" aria-labelledby="infoTitle">
        <button class="info-close" type="button" aria-label="Close">&times;</button>
        <h3 id="infoTitle">Privacy</h3>
        <div id="infoBody" class="info-body"></div>
      </div>
    `;
    document.body.appendChild(overlay);
    return overlay;
  };

  const init = () => {
    const triggers = Array.from(document.querySelectorAll('.info-trigger'));
    if (!triggers.length) return;

    const overlay = ensureOverlay();
    const titleEl = overlay.querySelector('#infoTitle');
    const bodyEl = overlay.querySelector('#infoBody');
    const closeBtn = overlay.querySelector('.info-close');

    const closeInfo = () => {
      overlay.classList.remove('open');
      overlay.setAttribute('aria-hidden', 'true');
      document.documentElement.classList.remove('no-scroll');
      document.body.classList.remove('no-scroll');
    };

    const openInfo = (key) => {
      const data = infoMap[key];
      if (!data) return;
      if (titleEl) titleEl.textContent = data.title;
      if (bodyEl) bodyEl.innerHTML = data.body;
      overlay.classList.add('open');
      overlay.setAttribute('aria-hidden', 'false');
      document.documentElement.classList.add('no-scroll');
      document.body.classList.add('no-scroll');
    };

    triggers.forEach((btn) => {
      btn.addEventListener('click', () => openInfo(btn.dataset.info));
    });

    closeBtn?.addEventListener('click', closeInfo);
    overlay.addEventListener('click', (event) => {
      if (event.target === overlay) closeInfo();
    });
    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape' && overlay.classList.contains('open')) {
        closeInfo();
      }
    });
  };

  window.ClubPortalInfoModal = { init };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
