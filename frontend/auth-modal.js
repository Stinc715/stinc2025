(() => {
  const overlay = document.createElement('div');
  overlay.className = 'auth-overlay';
  overlay.innerHTML = `
    <div class="auth-dialog" role="dialog" aria-modal="true" aria-label="Sign in">
      <button class="auth-close" type="button" aria-label="Close">×</button>
      <iframe class="auth-frame" title="Sign in" src="login.html#login"></iframe>
    </div>
  `;

  const resizeDialog = (height) => {
    const dialog = overlay.querySelector('.auth-dialog');
    const frame = overlay.querySelector('.auth-frame');
    if (!dialog || !frame || !height) return;
    const safeHeight = Math.max(320, Math.ceil(height));
    frame.style.height = `${safeHeight}px`;
    dialog.style.height = `${safeHeight}px`;
  };

  const resizeFromFrame = () => {
    const frame = overlay.querySelector('.auth-frame');
    if (!frame) return;
    try {
      const doc = frame.contentDocument || frame.contentWindow?.document;
      if (!doc) return;
      const body = doc.body;
      const html = doc.documentElement;
      const height = Math.max(
        body?.scrollHeight || 0,
        body?.offsetHeight || 0,
        html?.scrollHeight || 0,
        html?.offsetHeight || 0
      );
      resizeDialog(height);
    } catch (err) {
      // Ignore cross-origin or timing errors.
    }
  };

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
    if (data.type === 'auth-resize' && typeof data.height === 'number') {
      resizeDialog(data.height);
      return;
    }
    if (data.type === 'auth-success') {
      closeAuthModal();
      if (data.target) {
        window.location.href = data.target;
      } else {
        window.location.reload();
      }
    }
  };

  const setupEventListeners = () => {
    overlay.addEventListener('click', (evt) => {
      if (evt.target === overlay) closeAuthModal();
    });
    overlay.querySelector('.auth-close')?.addEventListener('click', closeAuthModal);
    overlay.querySelector('.auth-frame')?.addEventListener('load', () => {
      resizeFromFrame();
      setTimeout(resizeFromFrame, 50);
    });
    document.addEventListener('keydown', (evt) => {
      if (evt.key === 'Escape' && overlay.classList.contains('open')) {
        closeAuthModal();
      }
    });
  };

  // 立即暴露全局函数
  window.openAuthModal = (mode) => {
    ensureMounted();
    openAuthModal(mode);
  };
  window.closeAuthModal = closeAuthModal;
  window.addEventListener('message', handleMessage);

  // DOM 加载后设置事件监听
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      ensureMounted();
      setupEventListeners();
    });
  } else {
    ensureMounted();
    setupEventListeners();
  }
})();
