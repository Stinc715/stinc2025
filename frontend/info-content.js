(function () {
  if (window.ClubPortalInfoContent) return;

  const defaultPublicConfig = Object.freeze({
    privacyContactEmail: '',
    dataControllerName: '',
    retentionSummary: '',
    processorsSummary: ''
  });

  let publicConfig = { ...defaultPublicConfig };
  let publicConfigPromise = null;

  const safe = (value) => (value == null ? '' : String(value).trim());

  const escapeHtml = (value) => safe(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');

  const normalizePublicConfig = (raw) => ({
    privacyContactEmail: safe(raw?.privacyContactEmail),
    dataControllerName: safe(raw?.dataControllerName),
    retentionSummary: safe(raw?.retentionSummary),
    processorsSummary: safe(raw?.processorsSummary)
  });

  const readPublicConfig = () => ({ ...publicConfig });

  const buildContactLink = (email, fallbackText) => {
    const trimmedEmail = safe(email);
    if (!trimmedEmail) {
      return escapeHtml(fallbackText);
    }
    return `<a href="mailto:${escapeHtml(trimmedEmail)}">${escapeHtml(trimmedEmail)}</a>`;
  };

  const dispatchPublicConfigReady = () => {
    window.dispatchEvent(new CustomEvent('clubportal:public-config-ready', {
      detail: readPublicConfig()
    }));
  };

  const loadPublicConfig = () => {
    if (publicConfigPromise) return publicConfigPromise;

    publicConfigPromise = fetch('/api/public/config', { credentials: 'same-origin' })
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => {
        publicConfig = normalizePublicConfig(data);
        dispatchPublicConfigReady();
        return readPublicConfig();
      })
      .catch(() => readPublicConfig());

    return publicConfigPromise;
  };

  const createPrivacyBody = () => {
    const config = readPublicConfig();
    const controllerName = config.dataControllerName || 'the deployment operator';
    const processorsSummary = config.processorsSummary
      || 'Configured third-party services can include Google sign-in or Maps, Stripe payments, and AI-assisted chat.';
    const retentionSummary = config.retentionSummary
      || 'Account, booking, membership, payment, and chat records can be retained for service operation, dispute handling, security, and audit until a formal retention schedule is applied.';

    return `
      <p><strong>Controller:</strong> ${escapeHtml(controllerName)}</p>
      <p>We collect the account, profile, booking, membership, payment, chat, and community data needed to run Club Portal and support clubs and members.</p>
      <p>${escapeHtml(processorsSummary)}</p>
      <p>${escapeHtml(retentionSummary)}</p>
      <p>You can update some profile details in your account settings. For correction or deletion requests, contact ${buildContactLink(config.privacyContactEmail, 'the platform administrator')}.</p>
      <p class="info-link-row"><a href="privacy.html">Read full Privacy Notice</a></p>
    `;
  };

  const termsBody = `
    <p>Use accurate account details, protect your login credentials, and follow published club rules and facility policies.</p>
    <p>Bookings, memberships, and payments are subject to club capacity, availability, and confirmation in the portal. Benefits only apply after the system records the purchase or membership.</p>
    <p>Chat, community, and club features must be used respectfully. Abuse, harassment, fraud, or repeated no-shows may lead to restrictions.</p>
    <p>Some chat flows can begin with AI assistance and may be handed to club staff when human support is needed.</p>
    <p class="info-link-row"><a href="terms.html">Read full Terms</a></p>
  `;

  const createDefaultHelpBody = () => {
    const config = readPublicConfig();
    return `
      <p>Need help? Start by checking your club page, bookings, memberships, or account settings.</p>
      <p>If you cannot sign in or need support with club data, booking issues, or payments, contact ${buildContactLink(config.privacyContactEmail, 'the platform administrator')} or relevant club staff.</p>
    `;
  };

  const createInfoMap = (options = {}) => ({
    privacy: {
      title: 'Privacy',
      body: createPrivacyBody()
    },
    terms: {
      title: 'Terms',
      body: termsBody
    },
    help: {
      title: 'Help',
      body: String(options.helpBody || '').trim() || createDefaultHelpBody()
    }
  });

  loadPublicConfig();

  window.ClubPortalInfoContent = Object.freeze({
    createInfoMap,
    loadPublicConfig,
    readPublicConfig,
    defaultPublicConfig
  });
})();
