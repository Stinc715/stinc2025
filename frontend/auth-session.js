(function () {
  const LEGACY_TOKEN_KEY = 'token';
  const SESSION_TOKEN_KEY = 'clubPortal.authToken';
  const USER_STORAGE_KEY = 'user';
  const LOGOUT_ENDPOINT = '/api/auth/logout';
  const USER_TOKEN_FIELDS = Object.freeze(['token', 'accessToken', 'refreshToken', 'idToken', 'jwt']);
  const rawGetItem = Storage.prototype.getItem;
  const rawSetItem = Storage.prototype.setItem;
  const rawRemoveItem = Storage.prototype.removeItem;
  const rawClear = Storage.prototype.clear;
  let logoutRequestInFlight = false;

  const isLocalStorage = (storage) => {
    try {
      return storage === window.localStorage;
    } catch {
      return false;
    }
  };

  const safeGetToken = () => {
    try {
      return String(rawGetItem.call(window.sessionStorage, SESSION_TOKEN_KEY) || '').trim();
    } catch {
      return '';
    }
  };

  const extractTokenFromUserPayload = (payload) => {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) return '';
    for (const field of USER_TOKEN_FIELDS) {
      const token = String(payload[field] || '').trim();
      if (token) return token;
    }
    return '';
  };

  const sanitizeStoredUserPayload = (payload) => {
    if (!payload || typeof payload !== 'object' || Array.isArray(payload)) return payload;
    const sanitized = { ...payload };
    USER_TOKEN_FIELDS.forEach((field) => {
      delete sanitized[field];
    });
    return sanitized;
  };

  const persistStoredUser = (payload) => {
    try {
      if (!payload || typeof payload !== 'object' || Array.isArray(payload)) {
        rawRemoveItem.call(window.localStorage, USER_STORAGE_KEY);
        return null;
      }
      const sanitized = sanitizeStoredUserPayload(payload);
      rawSetItem.call(window.localStorage, USER_STORAGE_KEY, JSON.stringify(sanitized));
      return sanitized;
    } catch {
      return null;
    }
  };

  const migrateLegacyUser = () => {
    let rawUser = '';
    try {
      rawUser = String(rawGetItem.call(window.localStorage, USER_STORAGE_KEY) || '').trim();
    } catch {
      rawUser = '';
    }
    if (!rawUser) return '';

    try {
      const parsed = JSON.parse(rawUser);
      const embeddedToken = extractTokenFromUserPayload(parsed);
      const sanitized = sanitizeStoredUserPayload(parsed);
      if (JSON.stringify(sanitized) !== rawUser) {
        persistStoredUser(sanitized);
      }
      return embeddedToken;
    } catch {
      return '';
    }
  };

  const dispatchTokenStorageEvent = (oldValue, newValue) => {
    try {
      window.dispatchEvent(new StorageEvent('storage', {
        key: LEGACY_TOKEN_KEY,
        oldValue: oldValue || null,
        newValue: newValue || null,
        storageArea: window.localStorage,
        url: window.location.href
      }));
    } catch {
      const evt = document.createEvent('Event');
      evt.initEvent('storage', false, false);
      evt.key = LEGACY_TOKEN_KEY;
      evt.oldValue = oldValue || null;
      evt.newValue = newValue || null;
      evt.storageArea = window.localStorage;
      evt.url = window.location.href;
      window.dispatchEvent(evt);
    }
  };

  const requestCookieLogout = () => {
    if (logoutRequestInFlight) return;
    logoutRequestInFlight = true;
    fetch(LOGOUT_ENDPOINT, {
      method: 'POST',
      credentials: 'same-origin',
      keepalive: true,
      headers: {
        'X-Requested-With': 'XMLHttpRequest'
      }
    }).catch(() => {
      // Ignore network failures during client-side logout cleanup.
    }).finally(() => {
      logoutRequestInFlight = false;
    });
  };

  const setToken = (token) => {
    const normalized = String(token || '').trim();
    const previous = safeGetToken();
    try {
      if (normalized) {
        rawSetItem.call(window.sessionStorage, SESSION_TOKEN_KEY, normalized);
      } else {
        rawRemoveItem.call(window.sessionStorage, SESSION_TOKEN_KEY);
      }
      rawRemoveItem.call(window.localStorage, LEGACY_TOKEN_KEY);
    } catch {
      // Ignore storage quota or privacy mode errors.
    }
    dispatchTokenStorageEvent(previous, normalized);
  };

  const clearToken = (notifyServer) => {
    const previous = safeGetToken();
    try {
      rawRemoveItem.call(window.sessionStorage, SESSION_TOKEN_KEY);
      rawRemoveItem.call(window.localStorage, LEGACY_TOKEN_KEY);
    } catch {
      // Ignore storage cleanup failures.
    }
    dispatchTokenStorageEvent(previous, '');
    if (notifyServer && previous) {
      requestCookieLogout();
    }
  };

  const migrateLegacyToken = () => {
    let legacyToken = '';
    try {
      legacyToken = String(rawGetItem.call(window.localStorage, LEGACY_TOKEN_KEY) || '').trim();
    } catch {
      legacyToken = '';
    }
    const embeddedUserToken = migrateLegacyUser();
    if (!legacyToken && embeddedUserToken) {
      legacyToken = embeddedUserToken;
    }
    if (!legacyToken) return;
    if (!safeGetToken()) {
      try {
        rawSetItem.call(window.sessionStorage, SESSION_TOKEN_KEY, legacyToken);
      } catch {
        return;
      }
    }
    try {
      rawRemoveItem.call(window.localStorage, LEGACY_TOKEN_KEY);
    } catch {
      // Ignore cleanup failures.
    }
  };

  Storage.prototype.getItem = function (key) {
    if (isLocalStorage(this) && key === LEGACY_TOKEN_KEY) {
      return safeGetToken();
    }
    return rawGetItem.call(this, key);
  };

  Storage.prototype.setItem = function (key, value) {
    if (isLocalStorage(this) && key === LEGACY_TOKEN_KEY) {
      setToken(value);
      return;
    }
    return rawSetItem.call(this, key, value);
  };

  Storage.prototype.removeItem = function (key) {
    if (isLocalStorage(this) && key === LEGACY_TOKEN_KEY) {
      clearToken(true);
      return;
    }
    return rawRemoveItem.call(this, key);
  };

  Storage.prototype.clear = function () {
    if (isLocalStorage(this)) {
      clearToken(true);
    }
    return rawClear.call(this);
  };

  migrateLegacyToken();

  window.AuthSession = Object.freeze({
    getToken: safeGetToken,
    setToken,
    setStoredUser: persistStoredUser,
    sanitizeStoredUser: sanitizeStoredUserPayload,
    clearToken: () => clearToken(false),
    clearAll: () => {
      clearToken(true);
      try {
        rawRemoveItem.call(window.localStorage, 'loggedUser');
        rawRemoveItem.call(window.localStorage, 'user');
        rawRemoveItem.call(window.localStorage, 'selectedClub');
      } catch {
        // Ignore storage cleanup failures.
      }
    },
    logout: async (redirectTo, options) => {
      const config = options && typeof options === 'object' ? options : {};
      clearToken(false);
      try {
        rawRemoveItem.call(window.localStorage, 'loggedUser');
        rawRemoveItem.call(window.localStorage, 'user');
        rawRemoveItem.call(window.localStorage, 'selectedClub');
      } catch {
        // Ignore storage cleanup failures.
      }
      try {
        await fetch(LOGOUT_ENDPOINT, {
          method: 'POST',
          credentials: 'same-origin',
          keepalive: true,
          headers: {
            'X-Requested-With': 'XMLHttpRequest'
          }
        });
      } catch {
        // Ignore logout network failures.
      }
      if (config.reload) {
        window.location.reload();
        return;
      }
      if (redirectTo) {
        if (config.replace) {
          window.location.replace(redirectTo);
          return;
        }
        window.location.href = redirectTo;
        return;
      }
      window.location.reload();
    },
    authFetch: (url, options) => {
      const requestOptions = options && typeof options === 'object' ? { ...options } : {};
      const headers = { ...(requestOptions.headers || {}) };
      const token = safeGetToken();
      if (token && !headers.Authorization) {
        headers.Authorization = 'Bearer ' + token;
      }
      requestOptions.headers = headers;
      requestOptions.credentials = requestOptions.credentials || 'same-origin';
      return fetch(url, requestOptions);
    }
  });
})();
