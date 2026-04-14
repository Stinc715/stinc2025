(function () {
  if (window.ClubPortalInfoModal) return;

  const fallbackInfoMap = {
    privacy: {
      title: 'Privacy',
      body: `
        <p>Read the full privacy details for this deployment.</p>
        <p class="info-link-row"><a href="privacy.html">Read full Privacy Notice</a></p>
      `
    },
    terms: {
      title: 'Terms',
      body: `
        <p>Read the full terms for this deployment.</p>
        <p class="info-link-row"><a href="terms.html">Read full Terms</a></p>
      `
    },
    help: {
      title: 'Help',
      body: `
        <p>Contact the platform administrator or club staff if you need help with this deployment.</p>
      `
    }
  };

  const createInfoMap = (options = {}) =>
    window.ClubPortalInfoContent?.createInfoMap?.(options) || fallbackInfoMap;

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
    let activeKey = '';

    const renderInfo = (key) => {
      const data = createInfoMap()[key];
      if (!data) return;
      if (titleEl) titleEl.textContent = data.title;
      if (bodyEl) bodyEl.innerHTML = data.body;
    };

    const closeInfo = () => {
      activeKey = '';
      overlay.classList.remove('open');
      overlay.setAttribute('aria-hidden', 'true');
      document.documentElement.classList.remove('no-scroll');
      document.body.classList.remove('no-scroll');
    };

    const openInfo = (key) => {
      activeKey = key;
      renderInfo(key);
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
    window.addEventListener('clubportal:public-config-ready', () => {
      if (overlay.classList.contains('open') && activeKey) {
        renderInfo(activeKey);
      }
    });
  };

  window.ClubPortalInfoModal = { init, createInfoMap };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
