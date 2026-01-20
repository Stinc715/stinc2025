(() => {
  const overlay = document.createElement('div');
  overlay.className = 'auth-overlay';
  overlay.innerHTML = `
    <div class="auth-dialog" role="dialog" aria-modal="true" aria-label="Sign in">
      <button class="auth-close" type="button" aria-label="Close">×</button>
      <iframe class="auth-frame" title="Sign in" src="login.html#login"></iframe>
    </div>
  `;

  const openAuthModal = (mode = 'login') => {
    const frame = overlay.querySelector('.auth-frame');
    const hash = mode === 'register' ? '#register' : '#login';
    if (frame) frame.src = `login.html${hash}`;
    overlay.classList.add('open');
    document.body.classList.add('no-scroll');
  };

  const closeAuthModal = () => {
    overlay.classList.remove('open');
    document.body.classList.remove('no-scroll');
  };

  const ensureMounted = () => {
    const root = document.documentElement;
    if (!root.contains(overlay)) {
      root.appendChild(overlay);
    }
  };

  const handleMessage = (event) => {
    const data = event && event.data;
    if (!data || typeof data !== 'object') return;
    if (data.type === 'auth-success') {
      closeAuthModal();
      if (data.target) {
        window.location.href = data.target;
      } else {
        window.location.reload();
      }
    }
  };

  document.addEventListener('DOMContentLoaded', () => {
    ensureMounted();
    overlay.addEventListener('click', (evt) => {
      if (evt.target === overlay) closeAuthModal();
    });
    overlay.querySelector('.auth-close')?.addEventListener('click', closeAuthModal);
    document.addEventListener('keydown', (evt) => {
      if (evt.key === 'Escape' && overlay.classList.contains('open')) {
        closeAuthModal();
      }
    });
  });

  window.openAuthModal = (mode) => {
    ensureMounted();
    openAuthModal(mode);
  };
  window.closeAuthModal = closeAuthModal;
  window.addEventListener('message', handleMessage);
})();
