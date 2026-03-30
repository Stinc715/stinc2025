(() => {
  const AUTH_PAGE_VERSION = '20260308g';
  const DEFAULT_DIALOG_HEIGHT = 560;
  const MIN_DIALOG_HEIGHT = 320;
  const DEFAULT_DIALOG_WIDTH = 520;
  const MODE_LAYOUT = Object.freeze({
    login: { width: 460, height: 500 },
    register: { width: 520, height: 620 }
  });
  let requestedContentHeight = DEFAULT_DIALOG_HEIGHT;
  let requestedDialogWidth = DEFAULT_DIALOG_WIDTH;

  const overlay = document.createElement('div');
  overlay.className = 'auth-overlay';
  overlay.innerHTML = `
    <div class="auth-dialog" role="dialog" aria-modal="true" aria-label="Sign in">
      <button class="auth-close" type="button" aria-label="Close">&times;</button>
      <iframe class="auth-frame" title="Sign in" src="login.html?v=${AUTH_PAGE_VERSION}#login" allow="identity-credentials-get"></iframe>
    </div>
  `;

  const getViewportHeightLimit = () => {
    const viewportHeight = window.innerHeight || document.documentElement?.clientHeight || 0;
    const styles = window.getComputedStyle(overlay);
    const paddingTop = parseFloat(styles.paddingTop) || 0;
    const paddingBottom = parseFloat(styles.paddingBottom) || 0;
    const limit = Math.floor(viewportHeight - paddingTop - paddingBottom);
    return Math.max(MIN_DIALOG_HEIGHT, limit);
  };

  const resizeDialog = (height) => {
    const dialog = overlay.querySelector('.auth-dialog');
    const frame = overlay.querySelector('.auth-frame');
    if (!dialog || !frame) return;

    if (typeof height === 'number' && Number.isFinite(height) && height > 0) {
      requestedContentHeight = Math.max(MIN_DIALOG_HEIGHT, Math.ceil(height));
    }

    const viewportHeightLimit = getViewportHeightLimit();
    const safeHeight = Math.min(requestedContentHeight, viewportHeightLimit);
    dialog.style.width = `min(${requestedDialogWidth}px, calc(100vw - clamp(20px, 5vw, 40px)))`;
    frame.style.height = `${safeHeight}px`;
    dialog.style.height = `${safeHeight}px`;
    dialog.style.maxHeight = `${viewportHeightLimit}px`;
    dialog.classList.toggle('auth-dialog--clamped', requestedContentHeight > viewportHeightLimit);
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

  const normalizeReturnTo = (value) => {
    const raw = String(value || '').trim();
    if (!raw) return '';
    if (/^[a-z][a-z0-9+.-]*:/i.test(raw)) return '';
    const normalized = raw.replace(/^\.\/+/, '').replace(/^\/+/, '');
    if (!normalized || normalized.includes('..') || /[\r\n]/.test(normalized)) return '';
    return normalized;
  };

  const buildAuthFrameSrc = (mode = 'login', returnTo = '') => {
    const hash = mode === 'register' ? '#register' : '#login';
    const normalizedReturnTo = normalizeReturnTo(returnTo);
    const query = normalizedReturnTo ? `&returnTo=${encodeURIComponent(normalizedReturnTo)}` : '';
    return `login.html?v=${AUTH_PAGE_VERSION}${query}${hash}`;
  };

  const openAuthModal = (mode = 'login', returnTo = '') => {
    const frame = overlay.querySelector('.auth-frame');
    const layout = MODE_LAYOUT[mode] || MODE_LAYOUT.login;
    requestedDialogWidth = layout.width;
    requestedContentHeight = layout.height;
    overlay.dataset.mode = mode;
    if (frame) frame.src = buildAuthFrameSrc(mode, returnTo);
    overlay.classList.add('open');
    document.body.classList.add('no-scroll');
    resizeDialog();
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
    window.addEventListener('resize', () => {
      if (!overlay.classList.contains('open')) return;
      resizeDialog();
      setTimeout(resizeFromFrame, 50);
    });
    document.addEventListener('keydown', (evt) => {
      if (evt.key === 'Escape' && overlay.classList.contains('open')) {
        closeAuthModal();
      }
    });
  };

  window.openAuthModal = (mode, returnTo) => {
    ensureMounted();
    openAuthModal(mode, returnTo);
  };
  window.closeAuthModal = closeAuthModal;
  window.addEventListener('message', handleMessage);

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
