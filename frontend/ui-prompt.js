(() => {
  if (window.AppPrompt) return;

  const style = document.createElement('style');
  style.textContent = `
    .app-toast-stack {
      position: fixed;
      top: 18px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 5000;
      display: grid;
      gap: 8px;
      pointer-events: none;
    }
    .app-toast {
      min-width: 220px;
      max-width: min(620px, 92vw);
      border-radius: 10px;
      padding: 10px 14px;
      color: #fff;
      box-shadow: 0 12px 28px rgba(15, 23, 42, 0.28);
      font-size: 13px;
      opacity: 0;
      transform: translateY(-8px);
      transition: opacity 0.2s ease, transform 0.2s ease;
    }
    .app-toast.show { opacity: 1; transform: translateY(0); }
    .app-toast.info { background: rgba(17, 24, 39, 0.94); }
    .app-toast.warn { background: rgba(146, 64, 14, 0.94); }
    .app-toast.error { background: rgba(127, 29, 29, 0.95); }

    .app-prompt-overlay {
      position: fixed;
      inset: 0;
      display: none;
      align-items: center;
      justify-content: center;
      padding: 24px;
      overflow-y: auto;
      overscroll-behavior: contain;
      background: rgba(10, 12, 16, 0.5);
      z-index: 5100;
    }
    .app-prompt-overlay.open { display: flex; }
    .app-prompt-card {
      width: min(540px, 92vw);
      background: #fff;
      border: 1px solid #e5e7eb;
      border-radius: 14px;
      box-shadow: none;
      padding: 16px;
      color: #111827;
    }
    .app-prompt-title {
      margin: 0 0 10px;
      font-size: 18px;
      font-weight: 700;
    }
    .app-prompt-body {
      display: grid;
      gap: 6px;
      margin-bottom: 14px;
      font-size: 14px;
      line-height: 1.45;
    }
    .app-prompt-body p { margin: 0; }
    .app-prompt-field {
      display: none;
      gap: 6px;
      margin-bottom: 14px;
    }
    .app-prompt-field.show {
      display: grid;
    }
    .app-prompt-label {
      font-size: 12px;
      font-weight: 600;
      color: #6b7280;
    }
    .app-prompt-input {
      width: 100%;
      border: 1px solid #e5e7eb;
      background: #fff;
      color: #111827;
      border-radius: 10px;
      padding: 10px 12px;
      font-size: 14px;
      outline: none;
    }
    .app-prompt-input:focus {
      border-color: #2563eb;
    }
    .app-prompt-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
    .app-prompt-btn {
      border: 1px solid #e5e7eb;
      background: #fff;
      color: #111827;
      border-radius: 10px;
      min-width: 88px;
      padding: 8px 12px;
      font-size: 13px;
      cursor: pointer;
    }
    .app-prompt-btn.primary {
      background: #2563eb;
      border-color: #2563eb;
      color: #fff;
    }
  `;
  document.head.appendChild(style);

  const toastStack = document.createElement('div');
  toastStack.className = 'app-toast-stack';
  toastStack.setAttribute('aria-live', 'polite');
  toastStack.setAttribute('aria-atomic', 'false');
  document.body.appendChild(toastStack);

  const overlay = document.createElement('div');
  overlay.className = 'app-prompt-overlay';
  overlay.setAttribute('aria-hidden', 'true');
  overlay.innerHTML = `
    <div class="app-prompt-card" role="dialog" aria-modal="true" aria-labelledby="appPromptTitle">
      <h3 id="appPromptTitle" class="app-prompt-title">Confirm</h3>
      <div id="appPromptBody" class="app-prompt-body"></div>
      <label id="appPromptField" class="app-prompt-field" for="appPromptInput">
        <span id="appPromptLabel" class="app-prompt-label">Value</span>
        <input id="appPromptInput" class="app-prompt-input" type="text" />
      </label>
      <div class="app-prompt-actions">
        <button id="appPromptCancel" class="app-prompt-btn" type="button">Cancel</button>
        <button id="appPromptOk" class="app-prompt-btn primary" type="button">Confirm</button>
      </div>
    </div>
  `;
  document.body.appendChild(overlay);

  const titleEl = overlay.querySelector('#appPromptTitle');
  const bodyEl = overlay.querySelector('#appPromptBody');
  const fieldEl = overlay.querySelector('#appPromptField');
  const labelEl = overlay.querySelector('#appPromptLabel');
  const inputEl = overlay.querySelector('#appPromptInput');
  const okBtn = overlay.querySelector('#appPromptOk');
  const cancelBtn = overlay.querySelector('#appPromptCancel');

  let resolver = null;

  const getMode = () => overlay.dataset.mode === 'prompt' ? 'prompt' : 'confirm';
  const hideInputField = () => {
    fieldEl?.classList.remove('show');
    if (inputEl) {
      inputEl.value = '';
      inputEl.placeholder = '';
      inputEl.type = 'text';
    }
    if (labelEl) labelEl.textContent = 'Value';
  };

  const setPromptOpen = (open) => {
    overlay.classList.toggle('open', Boolean(open));
    overlay.setAttribute('aria-hidden', open ? 'false' : 'true');
    document.documentElement.classList.toggle('no-scroll', Boolean(open));
    document.body.classList.toggle('no-scroll', Boolean(open));
    if (open) {
      overlay.scrollTop = 0;
    }
  };

  const closePrompt = (result) => {
    const mode = getMode();
    setPromptOpen(false);
    overlay.dataset.mode = 'confirm';
    hideInputField();
    const done = resolver;
    resolver = null;
    if (done) {
      if (mode === 'prompt') {
        done(result === false ? null : String(result ?? ''));
      } else {
        done(Boolean(result));
      }
    }
  };

  okBtn?.addEventListener('click', () => {
    if (getMode() === 'prompt') {
      closePrompt(String(inputEl?.value ?? ''));
      return;
    }
    closePrompt(true);
  });
  cancelBtn?.addEventListener('click', () => closePrompt(false));
  overlay.addEventListener('click', (evt) => {
    if (evt.target === overlay) closePrompt(false);
  });
  document.addEventListener('keydown', (evt) => {
    if (evt.key === 'Escape' && overlay.classList.contains('open')) {
      closePrompt(false);
    }
  });
  inputEl?.addEventListener('keydown', (evt) => {
    if (evt.key === 'Enter' && overlay.classList.contains('open') && getMode() === 'prompt') {
      evt.preventDefault();
      closePrompt(String(inputEl.value || ''));
    }
  });

  const escapeHtml = (s) => String(s ?? '').replace(/[&<>"']/g, (ch) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[ch]));

  const toast = (message, options = {}) => {
    const text = String(message || '').trim();
    if (!text) return;
    const type = ['info', 'warn', 'error'].includes(options.type) ? options.type : 'info';
    const duration = Number.isFinite(options.duration) ? Math.max(1200, Number(options.duration)) : 2600;
    const item = document.createElement('div');
    item.className = `app-toast ${type}`;
    item.textContent = text;
    toastStack.appendChild(item);
    requestAnimationFrame(() => item.classList.add('show'));
    window.setTimeout(() => {
      item.classList.remove('show');
      window.setTimeout(() => item.remove(), 220);
    }, duration);
  };

  const confirm = (options = {}) => {
    const opts = typeof options === 'string' ? { message: options } : (options || {});
    const title = String(opts.title || 'Confirm');
    const message = String(opts.message || '').trim();
    const details = Array.isArray(opts.details) ? opts.details.map((v) => String(v || '').trim()).filter(Boolean) : [];
    const okText = String(opts.okText || 'Confirm');
    const cancelText = String(opts.cancelText || 'Cancel');
    const showCancel = opts.showCancel !== false;

    if (titleEl) titleEl.textContent = title;
    if (bodyEl) {
      const lines = [];
      if (message) lines.push(`<p>${escapeHtml(message)}</p>`);
      details.forEach((line) => lines.push(`<p>${escapeHtml(line)}</p>`));
      bodyEl.innerHTML = lines.join('') || '<p></p>';
    }
    if (okBtn) okBtn.textContent = okText;
    if (cancelBtn) {
      cancelBtn.textContent = cancelText;
      cancelBtn.style.display = showCancel ? 'inline-flex' : 'none';
    }

    overlay.dataset.mode = 'confirm';
    hideInputField();
    setPromptOpen(true);
    okBtn?.focus();

    return new Promise((resolve) => {
      resolver = resolve;
    });
  };

  const promptBox = (options = {}) => {
    const opts = typeof options === 'string' ? { message: options } : (options || {});
    const title = String(opts.title || 'Enter value');
    const message = String(opts.message || '').trim();
    const label = String(opts.label || 'Value');
    const defaultValue = String(opts.defaultValue ?? '');
    const placeholder = String(opts.placeholder || '');
    const okText = String(opts.okText || 'Save');
    const cancelText = String(opts.cancelText || 'Cancel');
    const inputType = String(opts.inputType || 'text');

    if (titleEl) titleEl.textContent = title;
    if (bodyEl) {
      bodyEl.innerHTML = message ? `<p>${escapeHtml(message)}</p>` : '<p></p>';
    }
    if (labelEl) labelEl.textContent = label;
    if (inputEl) {
      inputEl.type = inputType;
      inputEl.value = defaultValue;
      inputEl.placeholder = placeholder;
    }
    fieldEl?.classList.add('show');
    if (okBtn) okBtn.textContent = okText;
    if (cancelBtn) {
      cancelBtn.textContent = cancelText;
      cancelBtn.style.display = 'inline-flex';
    }

    overlay.dataset.mode = 'prompt';
    setPromptOpen(true);
    inputEl?.focus();
    inputEl?.select();

    return new Promise((resolve) => {
      resolver = resolve;
    });
  };

  const alertBox = (message, options = {}) => {
    const opts = typeof options === 'string' ? { title: options } : (options || {});
    return confirm({
      title: opts.title || 'Notice',
      message,
      okText: opts.okText || 'OK',
      showCancel: false
    }).then(() => true);
  };

  window.AppPrompt = Object.freeze({
    toast,
    confirm,
    prompt: promptBox,
    alert: alertBox
  });
})();
