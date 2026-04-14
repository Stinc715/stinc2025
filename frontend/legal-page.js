(function () {
  if (window.ClubPortalLegalPage) return;

  const safe = (value) => (value == null ? '' : String(value).trim());

  const applyText = (selector, value) => {
    document.querySelectorAll(selector).forEach((node) => {
      node.textContent = value;
    });
  };

  const applyEmailLink = (selector, email, fallbackText) => {
    document.querySelectorAll(selector).forEach((node) => {
      if (!(node instanceof HTMLAnchorElement)) return;
      const trimmedEmail = safe(email);
      if (trimmedEmail) {
        node.textContent = trimmedEmail;
        node.href = `mailto:${trimmedEmail}`;
        node.removeAttribute('aria-disabled');
        return;
      }
      node.textContent = fallbackText;
      node.removeAttribute('href');
      node.setAttribute('aria-disabled', 'true');
    });
  };

  const applyConfig = (config) => {
    const controllerName = safe(config?.dataControllerName) || 'Club Portal deployment operator';
    const privacyEmail = safe(config?.privacyContactEmail);
    const retentionSummary = safe(config?.retentionSummary);
    const processorsSummary = safe(config?.processorsSummary);

    applyText('[data-legal-controller-name]', controllerName);

    if (processorsSummary) {
      applyText('[data-legal-processors-summary]', processorsSummary);
    }
    if (retentionSummary) {
      applyText('[data-legal-retention-summary]', retentionSummary);
    }

    applyEmailLink('[data-legal-privacy-email]', privacyEmail, 'Contact the platform administrator');
    applyEmailLink('[data-legal-privacy-email-inline]', privacyEmail, 'the platform administrator');
  };

  const init = () => {
    fetch('/api/public/config', { credentials: 'same-origin' })
      .then((res) => (res.ok ? res.json() : null))
      .then((config) => applyConfig(config || {}))
      .catch(() => {});
  };

  window.ClubPortalLegalPage = { init };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init, { once: true });
  } else {
    init();
  }
})();
