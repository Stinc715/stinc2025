    document.addEventListener('DOMContentLoaded', () => {
      const infoOverlay = document.getElementById('infoOverlay');
      const infoTitle = document.getElementById('infoTitle');
      const infoBody = document.getElementById('infoBody');
      const getInfoMap = () => window.ClubPortalInfoContent?.createInfoMap?.() || {
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

      const openInfo = (key) => {
        const data = getInfoMap()[key];
        if (!data || !infoOverlay) return;
        if (infoTitle) infoTitle.textContent = data.title;
        if (infoBody) infoBody.innerHTML = data.body;
        infoOverlay.classList.add('open');
        document.body.classList.add('no-scroll');
      };

      const closeInfo = () => {
        if (!infoOverlay) return;
        infoOverlay.classList.remove('open');
        document.body.classList.remove('no-scroll');
      };

      document.querySelectorAll('.info-trigger').forEach((btn) => {
        btn.addEventListener('click', () => openInfo(btn.dataset.info));
      });
      infoOverlay?.addEventListener('click', (evt) => { if (evt.target === infoOverlay) closeInfo(); });
      infoOverlay?.querySelector('.info-close')?.addEventListener('click', closeInfo);

      const panelButtons = Array.from(document.querySelectorAll('.side-link'));
      const panels = Array.from(document.querySelectorAll('.panel'));
      const sideNav = document.querySelector('.side-nav');
      const bookingListEl = document.getElementById('bookingList');
      const bookingErrorEl = document.getElementById('bookingError');
      const bookingModeUpcomingBtn = document.getElementById('bookingModeUpcoming');
      const bookingModeExpiredBtn = document.getElementById('bookingModeExpired');
      const bookingUpcomingCountEl = document.getElementById('bookingUpcomingCount');
      const bookingExpiredCountEl = document.getElementById('bookingExpiredCount');
      const membershipListEl = document.getElementById('membershipList');
      const membershipErrorEl = document.getElementById('membershipError');
      let bookingViewMode = 'upcoming';
      let bookingItemsCache = [];

      const switchPanel = (panelKey) => {
        panelButtons.forEach((btn) => btn.classList.toggle('active', btn.dataset.panel === panelKey));
        panels.forEach((panel) => panel.classList.toggle('active', panel.dataset.panel === panelKey));
        sideNav?.classList.toggle('info-active', panelKey === 'info');
      };

      panelButtons.forEach((btn) => {
        btn.addEventListener('click', () => {
          const panelKey = btn.dataset.panel;
          switchPanel(panelKey);
          if (panelKey === 'bookings') {
            loadBookings();
          } else if (panelKey === 'memberships') {
            loadMemberships();
          }
        });
      });


      const safeParse = (key) => {
        try {
          return JSON.parse(localStorage.getItem(key) || 'null');
        } catch (err) {
          return null;
        }
      };

      const AVATAR_STORAGE_KEY_LEGACY = 'profileAvatar';
      const AVATAR_STORAGE_KEY_PREFIX = 'profileAvatar:';

      const getAvatarStorageKey = () => {
        const loggedUser = safeParse('loggedUser') || {};
        const type = String(loggedUser.type || loggedUser.role || 'user').trim().toLowerCase() || 'user';
        const email = String(loggedUser.email || '').trim().toLowerCase();
        if (email) return `${AVATAR_STORAGE_KEY_PREFIX}${type}:${email}`;
        const name = String(loggedUser.name || '').trim().toLowerCase();
        if (name) return `${AVATAR_STORAGE_KEY_PREFIX}${type}:${name}`;
        return `${AVATAR_STORAGE_KEY_PREFIX}${type}:anonymous`;
      };

      const parseAvatarStorageValue = (raw) => {
        const text = String(raw || '').trim();
        if (!text) return '';
        try {
          const parsed = JSON.parse(text);
          return typeof parsed === 'string' ? parsed.trim() : '';
        } catch {
          // Backward compatibility: older builds stored plain string instead of JSON string.
          return text;
        }
      };

      const readStoredAvatar = () => {
        const scopedKey = getAvatarStorageKey();
        const scoped = parseAvatarStorageValue(localStorage.getItem(scopedKey));
        return scoped || '';
      };

      const persistAvatar = (avatarUrl, { emit = true } = {}) => {
        const normalized = String(avatarUrl || '').trim();
        if (!normalized) return;
        try {
          localStorage.setItem(getAvatarStorageKey(), JSON.stringify(normalized));
          localStorage.removeItem(AVATAR_STORAGE_KEY_LEGACY);
        } catch (err) {
          console.error(err);
        }
        try {
          const profile = safeParse('profile') || {};
          profile.avatarUrl = normalized;
          profile.avatar = normalized;
          localStorage.setItem('profile', JSON.stringify(profile));
        } catch (err) {
          console.error(err);
        }
        if (emit) {
          window.dispatchEvent(new CustomEvent('profile-avatar-updated', { detail: { avatarUrl: normalized } }));
        }
      };

      const applyAvatarPreview = (avatarUrl) => {
        const normalized = String(avatarUrl || '').trim();
        if (!avatarImg || !avatarPlaceholder) return;
        if (normalized) {
          avatarImg.src = normalized;
          avatarImg.style.display = 'block';
          avatarPlaceholder.style.display = 'none';
          return;
        }
        avatarImg.removeAttribute('src');
        avatarImg.style.display = 'none';
        avatarPlaceholder.style.display = 'block';
      };

      const MAX_AVATAR_BYTES = 4 * 1024 * 1024;
      const ALLOWED_AVATAR_TYPES = new Set(['image/jpeg', 'image/png', 'image/gif', 'image/webp']);

      const validateAvatarFile = (file) => {
        if (!file) return 'No avatar file provided.';
        const contentType = String(file.type || '').toLowerCase();
        if (!ALLOWED_AVATAR_TYPES.has(contentType)) {
          return 'Only JPG, PNG, GIF, and WEBP avatars are supported.';
        }
        if ((Number(file.size) || 0) > MAX_AVATAR_BYTES) {
          return 'Avatar must be 4MB or smaller.';
        }
        return '';
      };

      const fileToAvatarDataUrl = async (file) => {
        if (!file) return '';
        const rawDataUrl = await new Promise((resolve) => {
          const reader = new FileReader();
          reader.onload = () => resolve(typeof reader.result === 'string' ? reader.result : '');
          reader.onerror = () => resolve('');
          reader.readAsDataURL(file);
        });
        if (!rawDataUrl) return '';

        try {
          const image = await new Promise((resolve, reject) => {
            const img = new Image();
            img.onload = () => resolve(img);
            img.onerror = () => reject(new Error('Failed to decode image'));
            img.src = rawDataUrl;
          });
          const maxSide = 256;
          const ratio = Math.min(1, maxSide / Math.max(image.width || 1, image.height || 1));
          const width = Math.max(1, Math.round((image.width || 1) * ratio));
          const height = Math.max(1, Math.round((image.height || 1) * ratio));
          const canvas = document.createElement('canvas');
          canvas.width = width;
          canvas.height = height;
          const ctx = canvas.getContext('2d');
          if (!ctx) return rawDataUrl;
          ctx.drawImage(image, 0, 0, width, height);
          return canvas.toDataURL('image/jpeg', 0.84);
        } catch {
          return rawDataUrl;
        }
      };

      const authFetch = (url, options = {}) => {
        const token = localStorage.getItem('token');
        const headers = { ...(options.headers || {}) };
        if (token) headers.Authorization = `Bearer ${token}`;
        return fetch(url, {
          ...options,
          credentials: options.credentials ?? 'include',
          headers
        });
      };

      const showUserAlert = (message, title = 'Notice') => {
        const text = String(message || '').trim();
        if (!text) return Promise.resolve();
        if (window.AppPrompt && typeof window.AppPrompt.alert === 'function') {
          return window.AppPrompt.alert(text, { title, okText: 'OK' });
        }
        console.warn('[user-profile] AppPrompt.alert unavailable', { title, text });
        return Promise.resolve();
      };

      const confirmUserAction = ({ title = 'Confirm', message = '', details = [], okText = 'Confirm', cancelText = 'Cancel' } = {}) => {
        if (window.AppPrompt && typeof window.AppPrompt.confirm === 'function') {
          return window.AppPrompt.confirm({ title, message, details, okText, cancelText });
        }
        console.warn('[user-profile] AppPrompt.confirm unavailable', { title, message, details });
        return Promise.resolve(false);
      };

      const readTextSafe = async (res) => {
        try {
          if (res.status === 413) {
            return 'Upload is too large. Keep avatars under 4MB.';
          }
          const contentType = String(res.headers.get('content-type') || '').toLowerCase();
          if (contentType.includes('application/json')) {
            const data = await res.json();
            if (data && typeof data.message === 'string' && data.message.trim()) {
              return data.message.trim();
            }
            return JSON.stringify(data || {});
          }
          return await res.text();
        } catch {
          return '';
        }
      };

      const persistSessionToken = (token) => {
        const normalized = String(token || '').trim();
        if (!normalized) return;
        try {
          if (window.AuthSession?.setToken) {
            window.AuthSession.setToken(normalized);
            return;
          }
          localStorage.setItem('token', normalized);
        } catch (err) {
          console.error(err);
        }
      };

      const syncLocalProfileState = (payload) => {
        const profilePayload = payload && typeof payload === 'object' ? payload : {};
        const displayName = String(profilePayload.displayName || profilePayload.fullName || profilePayload.name || '').trim();
        const email = String(profilePayload.email || '').trim();
        const avatarUrl = String(profilePayload.avatarUrl || profilePayload.avatar || '').trim();
        const role = String(profilePayload.role || '').trim().toLowerCase() || 'user';
        const authProvider = String(profilePayload.authProvider || '').trim().toLowerCase();

        const profile = safeParse('profile') || {};
        const loggedUser = safeParse('loggedUser') || {};
        const userObj = safeParse('user') || {};

        if (displayName) {
          profile.displayName = displayName;
          profile.fullName = displayName;
          profile.name = displayName;
          loggedUser.name = displayName;
          userObj.fullName = displayName;
          userObj.name = displayName;
        }
        if (email) {
          profile.email = email;
          loggedUser.email = email;
          userObj.email = email;
        }
        if (authProvider) {
          profile.authProvider = authProvider;
          loggedUser.authProvider = authProvider;
          userObj.authProvider = authProvider;
        }
        if (typeof profilePayload.canChangePassword === 'boolean') {
          profile.canChangePassword = profilePayload.canChangePassword;
          loggedUser.canChangePassword = profilePayload.canChangePassword;
          userObj.canChangePassword = profilePayload.canChangePassword;
        }
        if (avatarUrl) {
          profile.avatarUrl = avatarUrl;
          profile.avatar = avatarUrl;
        }
        if (role) {
          profile.role = role;
          loggedUser.type = role;
          userObj.role = role;
        }

        try {
          localStorage.setItem('profile', JSON.stringify(profile));
          localStorage.setItem('loggedUser', JSON.stringify(loggedUser));
          localStorage.setItem('user', JSON.stringify(userObj));
        } catch (err) {
          console.error(err);
        }

        if (avatarUrl) {
          persistAvatar(avatarUrl, { emit: false });
        }
      };

      const escapeHtml = (value) => String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');

      const formatDateTime = (iso) => {
        if (!iso || typeof iso !== 'string') return '--';
        const dt = new Date(iso);
        if (Number.isNaN(dt.getTime())) return iso;
        const y = dt.getFullYear();
        const m = String(dt.getMonth() + 1).padStart(2, '0');
        const d = String(dt.getDate()).padStart(2, '0');
        const hh = String(dt.getHours()).padStart(2, '0');
        const mm = String(dt.getMinutes()).padStart(2, '0');
        return `${y}-${m}-${d} ${hh}:${mm}`;
      };

      const gbpCurrency = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' });
      const formatPrice = (price) => {
        const n = Number(price);
        if (!Number.isFinite(n)) return gbpCurrency.format(0);
        return gbpCurrency.format(n);
      };

      const profileApi = {
        async getMyBookings() {
          const res = await authFetch('/api/my/bookings');
          if (!res.ok) {
            const text = await res.text();
            throw new Error(text || 'Failed to load bookings.');
          }
          return await res.json();
        },
        async getMyMemberships() {
          const res = await authFetch('/api/my/memberships');
          if (!res.ok) {
            const text = await res.text();
            throw new Error(text || 'Failed to load memberships.');
          }
          return await res.json();
        },
        async getProfile() {
          const res = await authFetch('/api/profile');
          if (!res.ok) {
            throw new Error((await readTextSafe(res)) || 'Failed to load profile.');
          }
          return await res.json();
        },
        async updateName(displayName) {
          const res = await authFetch('/api/profile', {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ displayName })
          });
          if (!res.ok) {
            throw new Error((await readTextSafe(res)) || 'Failed to update name.');
          }
          return await res.json();
        },
        async sendEmailCode(email) {
          const res = await authFetch('/api/profile/email/code', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
          });
          if (!res.ok) {
            throw new Error((await readTextSafe(res)) || 'Failed to send verification code.');
          }
          return await res.json();
        },
        async updateEmail(email, code) {
          const res = await authFetch('/api/profile/email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, code })
          });
          if (!res.ok) {
            throw new Error((await readTextSafe(res)) || 'Failed to update email.');
          }
          return await res.json();
        },
        async uploadAvatar(file) {
          const formData = new FormData();
          formData.append('avatar', file);
          const res = await authFetch('/api/profile/avatar', {
            method: 'POST',
            body: formData
          });
          if (!res.ok) {
            throw new Error((await readTextSafe(res)) || 'Failed to upload avatar.');
          }
          return await res.json();
        },
        async updatePassword(currentPassword, newPassword) {
          const res = await authFetch('/api/profile/password', {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ currentPassword, newPassword })
          });
          if (!res.ok) {
            throw new Error((await readTextSafe(res)) || 'Failed to update password.');
          }
          return await res.json();
        },
        async exportProfileData() {
          const res = await authFetch('/api/profile/export');
          if (!res.ok) {
            throw new Error((await readTextSafe(res)) || 'Failed to export profile data.');
          }
          return await res.json();
        },
        async requestDeletion(reason) {
          const res = await authFetch('/api/profile/deletion-request', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reason })
          });
          if (!res.ok) {
            throw new Error((await readTextSafe(res)) || 'Failed to submit deletion request.');
          }
          return await res.json();
        },
        async rotateSession() {
          const res = await authFetch('/api/profile/session/rotate', {
            method: 'POST'
          });
          if (!res.ok) {
            throw new Error((await readTextSafe(res)) || 'Failed to rotate session.');
          }
          return await res.json();
        }
      };

      const bookingStatusClass = (statusRaw) => {
        const normalized = String(statusRaw || '').trim().toLowerCase();
        if (!normalized) return '';
        return `status-${normalized.replace(/[^a-z0-9]+/g, '-')}`;
      };

      const bookingTimeMs = (raw) => {
        const value = Date.parse(String(raw || ''));
        return Number.isFinite(value) ? value : null;
      };

      const splitBookings = (items) => {
        const source = Array.isArray(items) ? items : [];
        const now = Date.now();
        const upcoming = [];
        const expired = [];

        source.forEach((item) => {
          const endMs = bookingTimeMs(item?.endTime);
          const startMs = bookingTimeMs(item?.startTime);
          const isExpired = endMs !== null ? endMs < now : (startMs !== null ? startMs < now : false);
          if (isExpired) expired.push(item);
          else upcoming.push(item);
        });

        upcoming.sort((a, b) => (bookingTimeMs(a?.startTime) ?? Number.MAX_SAFE_INTEGER) - (bookingTimeMs(b?.startTime) ?? Number.MAX_SAFE_INTEGER));
        expired.sort((a, b) => (bookingTimeMs(b?.startTime) ?? 0) - (bookingTimeMs(a?.startTime) ?? 0));
        return { upcoming, expired };
      };

      const renderBookingModeUi = (counts) => {
        const upcomingCount = Number(counts?.upcoming || 0);
        const expiredCount = Number(counts?.expired || 0);
        if (bookingUpcomingCountEl) bookingUpcomingCountEl.textContent = String(upcomingCount);
        if (bookingExpiredCountEl) bookingExpiredCountEl.textContent = String(expiredCount);

        const upcomingActive = bookingViewMode === 'upcoming';
        bookingModeUpcomingBtn?.classList.toggle('active', upcomingActive);
        bookingModeExpiredBtn?.classList.toggle('active', !upcomingActive);
        bookingModeUpcomingBtn?.setAttribute('aria-selected', upcomingActive ? 'true' : 'false');
        bookingModeExpiredBtn?.setAttribute('aria-selected', upcomingActive ? 'false' : 'true');
      };

      const renderBookings = (items) => {
        if (!bookingListEl) return;

        bookingItemsCache = Array.isArray(items) ? items : [];
        const { upcoming, expired } = splitBookings(bookingItemsCache);
        renderBookingModeUi({ upcoming: upcoming.length, expired: expired.length });

        const activeList = bookingViewMode === 'expired' ? expired : upcoming;
        if (!activeList.length) {
          bookingListEl.innerHTML = bookingViewMode === 'expired'
            ? '<div class="booking-empty">No expired bookings yet.</div>'
            : '<div class="booking-empty">No upcoming bookings yet.</div>';
          return;
        }

        const rows = activeList.map((item) => {
          const clubName = escapeHtml(item?.clubName || 'Club');
          const venueName = escapeHtml(item?.venueName || 'Venue');
          const startLabel = formatDateTime(item?.startTime);
          const endLabel = formatDateTime(item?.endTime);
          const verificationCode = String(item?.bookingVerificationCode || '').trim();
          const orderNo = String(item?.orderNo || '').trim();
          const statusRaw = String(item?.status || 'PENDING').trim();
          const statusLabel = escapeHtml(statusRaw || 'PENDING');
          const statusClass = bookingStatusClass(statusRaw);
          const priceLabel = escapeHtml(formatPrice(item?.price));
          const verificationHtml = verificationCode
            ? `
                <div class="booking-code-row">
                  <span class="booking-code-label">Check-in code</span>
                  <span class="booking-code-value">${escapeHtml(verificationCode)}</span>
                </div>
              `
            : '';
          const orderNoHtml = orderNo
            ? `<div class="booking-sub">Order number: ${escapeHtml(orderNo)}</div>`
            : '';
          const clubId = Number(item?.clubId);
          const detailBtn = Number.isFinite(clubId)
            ? `<button class="btn ghost small" type="button" data-action="details" data-club-id="${clubId}">Details</button>`
            : '';
          const timeClass = bookingViewMode === 'expired' ? 'expired' : 'upcoming';
          const timeLabel = bookingViewMode === 'expired' ? 'Expired' : 'Upcoming';

          return `
            <div class="booking-card">
              <div class="booking-meta">
                <div class="booking-title">${clubName}</div>
                <div class="booking-sub">${venueName}</div>
                <div class="booking-meta-row">
                  <span class="booking-pill desktop-badge ${timeClass}">${timeLabel}</span>
                  <span class="booking-pill desktop-badge ${escapeHtml(statusClass)}">${statusLabel}</span>
                </div>
                ${orderNoHtml}
                <div class="booking-sub">${escapeHtml(startLabel)} - ${escapeHtml(endLabel)}</div>
                <div class="booking-sub">Price: ${priceLabel}</div>
                ${verificationHtml}
              </div>
              <div class="booking-actions">${detailBtn}</div>
            </div>
          `;
        }).join('');

        bookingListEl.innerHTML = rows;
        bookingListEl.querySelectorAll('button[data-action="details"]').forEach((btn) => {
          btn.addEventListener('click', () => {
            const clubId = btn.getAttribute('data-club-id');
            if (!clubId) return;
            window.location.href = `club.html?club=${encodeURIComponent(clubId)}`;
          });
        });
      };

      const loadBookings = async () => {
        if (!bookingListEl) return;

        if (bookingErrorEl) {
          bookingErrorEl.style.display = 'none';
          bookingErrorEl.textContent = '';
        }

        bookingListEl.innerHTML = '<div class="muted">Loading bookings...</div>';
        const token = localStorage.getItem('token');
        if (!token) {
          bookingListEl.innerHTML = '<div class="muted">Please log in to view your bookings.</div>';
          return;
        }

        try {
          const items = await profileApi.getMyBookings();
          renderBookings(items);
        } catch (err) {
          console.error(err);
          bookingListEl.innerHTML = '<div class="muted">Unable to load bookings right now.</div>';
          if (bookingErrorEl) {
            bookingErrorEl.textContent = (err && err.message) ? err.message : 'Failed to load bookings.';
            bookingErrorEl.style.display = 'block';
          }
        }
      };

      const membershipStatusClass = (statusRaw) => {
        const normalized = String(statusRaw || '').trim().toLowerCase();
        if (normalized === 'active') return 'membership-active';
        if (normalized === 'scheduled') return 'membership-scheduled';
        return 'membership-expired';
      };

      const renderMemberships = (items) => {
        if (!membershipListEl) return;

        const rows = Array.isArray(items) ? items.slice() : [];
        rows.sort((a, b) => {
          const order = (value) => {
            const key = String(value || '').trim().toUpperCase();
            if (key === 'ACTIVE') return 0;
            if (key === 'SCHEDULED') return 1;
            return 2;
          };
          const first = order(a?.status) - order(b?.status);
          if (first !== 0) return first;
          return String(b?.endDate || '').localeCompare(String(a?.endDate || ''));
        });

        if (!rows.length) {
          membershipListEl.innerHTML = '<div class="booking-empty">No memberships yet.</div>';
          return;
        }

        membershipListEl.innerHTML = rows.map((item) => {
          const clubName = escapeHtml(item?.clubName || 'Club');
          const planName = escapeHtml(item?.planName || 'Membership');
          const statusLabel = escapeHtml(String(item?.status || 'ACTIVE'));
          const statusClass = membershipStatusClass(item?.status);
          const priceLabel = escapeHtml(formatPrice(item?.planPrice));
          const packPlan = String(item?.benefitType || '').toUpperCase() === 'BOOKING_PACK';
          const discountLabel = packPlan
            ? `${escapeHtml(String(item?.remainingBookings ?? 0))}/${escapeHtml(String(item?.includedBookings ?? 0))} credits left`
            : `${escapeHtml(String(item?.discountPercent ?? 0))}% off`;
          const orderNo = String(item?.orderNo || '').trim();
          const startLabel = escapeHtml(String(item?.startDate || '--'));
          const endLabel = escapeHtml(String(item?.endDate || '--'));
          const clubId = Number(item?.clubId);
          const detailBtn = Number.isFinite(clubId)
            ? `<button class="btn ghost small" type="button" data-action="membership-details" data-club-id="${clubId}">Open club</button>`
            : '';

          return `
            <div class="booking-card">
              <div class="booking-meta">
                <div class="booking-title">${clubName}</div>
                <div class="booking-sub">${planName}</div>
                <div class="booking-meta-row">
                  <span class="booking-pill desktop-badge ${statusClass}">${statusLabel}</span>
                  <span class="booking-pill desktop-badge">${discountLabel}</span>
                </div>
                ${orderNo ? `<div class="booking-sub">Order number: ${escapeHtml(orderNo)}</div>` : ''}
                <div class="booking-sub">${startLabel} - ${endLabel}</div>
                <div class="booking-sub">Membership price: ${priceLabel}</div>
              </div>
              <div class="booking-actions">${detailBtn}</div>
            </div>
          `;
        }).join('');

        membershipListEl.querySelectorAll('button[data-action="membership-details"]').forEach((btn) => {
          btn.addEventListener('click', () => {
            const clubId = btn.getAttribute('data-club-id');
            if (!clubId) return;
            window.location.href = `club.html?club=${encodeURIComponent(clubId)}`;
          });
        });
      };

      const loadMemberships = async () => {
        if (!membershipListEl) return;

        if (membershipErrorEl) {
          membershipErrorEl.style.display = 'none';
          membershipErrorEl.textContent = '';
        }

        membershipListEl.innerHTML = '<div class="muted">Loading memberships...</div>';
        const token = localStorage.getItem('token');
        if (!token) {
          membershipListEl.innerHTML = '<div class="booking-empty">Please log in to view your memberships.</div>';
          return;
        }

        try {
          const items = await profileApi.getMyMemberships();
          renderMemberships(items);
        } catch (err) {
          console.error(err);
          membershipListEl.innerHTML = '<div class="booking-empty">Unable to load memberships right now.</div>';
          if (membershipErrorEl) {
            membershipErrorEl.textContent = (err && err.message) ? err.message : 'Failed to load memberships.';
            membershipErrorEl.style.display = 'block';
          }
        }
      };

      const setBookingMode = (mode) => {
        const next = mode === 'expired' ? 'expired' : 'upcoming';
        if (bookingViewMode === next) return;
        bookingViewMode = next;
        renderBookings(bookingItemsCache);
      };

      bookingModeUpcomingBtn?.addEventListener('click', () => setBookingMode('upcoming'));
      bookingModeExpiredBtn?.addEventListener('click', () => setBookingMode('expired'));

      const tabProfile = document.getElementById('tabProfile');
      const tabSecurity = document.getElementById('tabSecurity');
      const profileTab = document.getElementById('profileTab');
      const securityTab = document.getElementById('securityTab');

      const modalNameInput = document.getElementById('modalNameInput');
      const nameRow = document.getElementById('nameRow');
      const modalEmailInput = document.getElementById('modalEmailInput');
      const editNameBtn = document.getElementById('editNameBtn');
      const saveNameBtn = document.getElementById('saveNameBtn');
      const cancelNameBtn = document.getElementById('cancelNameBtn');
      const editEmailBtn = document.getElementById('editEmailBtn');
      const emailOverlay = document.getElementById('emailOverlay');
      const closeEmailModal = document.getElementById('closeEmailModal');
      const cancelEmailBtn = document.getElementById('cancelEmailBtn');
      const sendEmailCodeBtn = document.getElementById('sendEmailCodeBtn');
      const newEmailInput = document.getElementById('newEmailInput');
      const emailCodeInput = document.getElementById('emailCodeInput');
      const emailCodeSlots = Array.from(document.querySelectorAll('#emailOverlay .code-slot'));
      const emailUpdateErr = document.getElementById('emailUpdateErr');
      const nameConfirm = document.getElementById('nameConfirm');
      const confirmNameBtn = document.getElementById('confirmNameBtn');
      const dismissNameBtn = document.getElementById('dismissNameBtn');
      const avatarInput = document.getElementById('avatarInput');
      const avatarImg = document.getElementById('avatarImg');
      const avatarPlaceholder = document.getElementById('avatarPlaceholder');

      const currentPassInput = document.getElementById('currentPassInput');
      const newPassInput = document.getElementById('newPassInput');
      const confirmPassInput = document.getElementById('confirmPassInput');
      const currentPassErr = document.getElementById('currentPassErr');
      const newPassErr = document.getElementById('newPassErr');
      const confirmPassErr = document.getElementById('confirmPassErr');
      const securityFormEl = document.getElementById('securityForm');
      const securityGridEl = securityFormEl?.querySelector('.security-grid');
      const securityFootEl = securityFormEl?.querySelector('.security-foot');
      const passwordProviderCardEl = document.getElementById('passwordProviderCard');
      const passwordProviderCopyEl = document.getElementById('passwordProviderCopy');
      const updatePassBtn = document.getElementById('updatePassBtn');
      const rotateSessionBtn = document.getElementById('rotateSessionBtn');
      const rotateSessionStatusEl = document.getElementById('rotateSessionStatus');
      const exportDataBtn = document.getElementById('exportDataBtn');
      const requestDeletionBtn = document.getElementById('requestDeletionBtn');
      const deletionReasonInput = document.getElementById('deletionReasonInput');
      const dataRightsStatusEl = document.getElementById('dataRightsStatus');
      const dataRightsRetentionSummaryEl = document.getElementById('dataRightsRetentionSummary');
      const dataRightsPrivacyEmailEl = document.getElementById('dataRightsPrivacyEmail');

      let currentName = '';
      let currentEmail = '';
      let codeTimer = null;
      let codeRemaining = 0;
      let emailVerifyInFlight = false;
      let passwordChangeAllowed = true;
      let exportInFlight = false;
      let deletionRequestInFlight = false;
      let rotateSessionInFlight = false;

      const defaultRetentionSummary = 'Account, booking, membership, payment, and chat records can be retained for service operation, dispute handling, security, and audit until a formal retention schedule is applied.';

      const setDataRightsStatus = (text, type = '') => {
        if (!dataRightsStatusEl) return;
        const message = String(text || '').trim();
        if (!message) {
          dataRightsStatusEl.textContent = '';
          dataRightsStatusEl.classList.remove('show', 'is-success', 'is-error', 'is-info');
          return;
        }
        dataRightsStatusEl.textContent = message;
        dataRightsStatusEl.classList.add('show');
        dataRightsStatusEl.classList.toggle('is-success', type === 'success');
        dataRightsStatusEl.classList.toggle('is-error', type === 'error');
        dataRightsStatusEl.classList.toggle('is-info', type === 'info');
      };

      const setRotateSessionStatus = (text, type = '') => {
        if (!rotateSessionStatusEl) return;
        const message = String(text || '').trim();
        if (!message) {
          rotateSessionStatusEl.textContent = '';
          rotateSessionStatusEl.classList.remove('show', 'is-success', 'is-error', 'is-info');
          return;
        }
        rotateSessionStatusEl.textContent = message;
        rotateSessionStatusEl.classList.add('show');
        rotateSessionStatusEl.classList.toggle('is-success', type === 'success');
        rotateSessionStatusEl.classList.toggle('is-error', type === 'error');
        rotateSessionStatusEl.classList.toggle('is-info', type === 'info');
      };

      const applyDataRightsConfig = (rawConfig = {}) => {
        const config = rawConfig && typeof rawConfig === 'object' ? rawConfig : {};
        const retentionSummary = String(config.retentionSummary || '').trim() || defaultRetentionSummary;
        const privacyEmail = String(config.privacyContactEmail || '').trim();

        if (dataRightsRetentionSummaryEl) {
          dataRightsRetentionSummaryEl.textContent = retentionSummary;
        }
        if (dataRightsPrivacyEmailEl) {
          if (privacyEmail) {
            dataRightsPrivacyEmailEl.textContent = privacyEmail;
            dataRightsPrivacyEmailEl.href = `mailto:${privacyEmail}`;
          } else {
            dataRightsPrivacyEmailEl.textContent = 'Contact the platform administrator';
            dataRightsPrivacyEmailEl.href = 'privacy.html';
          }
        }
      };

      const buildExportFilename = () => {
        const dt = new Date();
        const stamp = [
          dt.getFullYear(),
          String(dt.getMonth() + 1).padStart(2, '0'),
          String(dt.getDate()).padStart(2, '0'),
          '-',
          String(dt.getHours()).padStart(2, '0'),
          String(dt.getMinutes()).padStart(2, '0'),
          String(dt.getSeconds()).padStart(2, '0')
        ].join('');
        const accountLabel = String(currentEmail || 'account')
          .toLowerCase()
          .replace(/[^a-z0-9]+/g, '-')
          .replace(/^-+|-+$/g, '')
          .slice(0, 40) || 'account';
        return `club-portal-profile-export-${accountLabel}-${stamp}.json`;
      };

      const downloadJsonFile = (payload, filename) => {
        const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
        const objectUrl = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
      };

      const setEmailVerifyMessage = (text, type = '') => {
        if (!emailUpdateErr) return;
        if (!text) {
          emailUpdateErr.textContent = '';
          emailUpdateErr.classList.remove('show', 'is-error', 'is-success');
          return;
        }
        emailUpdateErr.textContent = text;
        emailUpdateErr.classList.add('show');
        emailUpdateErr.classList.toggle('is-error', type === 'error');
        emailUpdateErr.classList.toggle('is-success', type === 'success');
      };

      const syncEmailCodeSlots = (value) => {
        const digits = String(value || '').replace(/\D/g, '').slice(0, 6);
        if (emailCodeInput) emailCodeInput.value = digits;
        emailCodeSlots.forEach((slot, index) => {
          const next = digits[index] || '';
          if (slot.value !== next) slot.value = next;
        });
        return digits;
      };

      const readCurrentEmailCode = () => String(emailCodeInput?.value || '').trim();

      const focusEmailCodeSlot = (index) => {
        const next = emailCodeSlots[index];
        if (!next) return;
        next.focus();
        next.select?.();
      };

      const clearEmailCodeEntry = (focusFirst = false) => {
        syncEmailCodeSlots('');
        if (focusFirst) focusEmailCodeSlot(0);
      };

      const updateEmailCodeFromSlots = () => syncEmailCodeSlots(
        emailCodeSlots.map((slot) => String(slot.value || '').replace(/\D/g, '').slice(-1)).join('')
      );

      const submitEmailVerification = async ({ auto = false } = {}) => {
        const nextEmail = (newEmailInput?.value || '').trim();
        const code = readCurrentEmailCode();
        if (emailVerifyInFlight) return;
        if (!nextEmail || !code) {
          setEmailVerifyMessage('Please enter the new email and verification code.', 'error');
          return;
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(nextEmail)) {
          setEmailVerifyMessage(auto ? 'Enter a valid email so verification can complete automatically.' : 'Please enter a valid email address.', 'error');
          return;
        }
        if (!/^\d{6}$/.test(code)) {
          setEmailVerifyMessage('Verification code must be 6 digits.', 'error');
          return;
        }

        emailVerifyInFlight = true;
        setEmailVerifyMessage('Verifying code...', '');
        try {
          const updated = await profileApi.updateEmail(nextEmail, code);
          persistSessionToken(updated?.token);
          syncLocalProfileState(updated);
          currentEmail = nextEmail;
          if (modalEmailInput) modalEmailInput.value = nextEmail;
          if (codeTimer) {
            clearInterval(codeTimer);
            codeTimer = null;
          }
          if (sendEmailCodeBtn) {
            sendEmailCodeBtn.disabled = false;
            sendEmailCodeBtn.textContent = 'Send code';
          }
          setEmailVerifyMessage('Email verified and updated.', 'success');
          window.setTimeout(() => {
            closeEmailModalFn();
            clearEmailCodeEntry(false);
            setEmailVerifyMessage('', '');
          }, 280);
        } catch (err) {
          console.error(err);
          setEmailVerifyMessage((err && err.message) ? err.message : 'Verification failed. Please check the code and try again.', 'error');
          clearEmailCodeEntry(true);
        } finally {
          emailVerifyInFlight = false;
        }
      };

      const applyEmailCodeFromIndex = (rawValue, startIndex = 0) => {
        const digits = String(rawValue || '').replace(/\D/g, '');
        if (!emailCodeSlots.length) return syncEmailCodeSlots(digits);
        const nextValues = emailCodeSlots.map((slot) => String(slot.value || '').replace(/\D/g, '').slice(-1));
        let writeIndex = Math.max(0, Math.min(startIndex, emailCodeSlots.length - 1));
        for (const digit of digits) {
          if (writeIndex >= emailCodeSlots.length) break;
          nextValues[writeIndex] = digit;
          writeIndex += 1;
        }
        const combined = syncEmailCodeSlots(nextValues.join(''));
        if (combined.length === 6 && !emailVerifyInFlight) {
          submitEmailVerification({ auto: true });
        } else if (writeIndex < emailCodeSlots.length) {
          focusEmailCodeSlot(writeIndex);
        }
        return combined;
      };

      const toggleConfirm = (show) => {
        if (!nameConfirm) return;
        nameConfirm.classList.toggle('open', show);
        if (show) confirmNameBtn?.focus();
        if (saveNameBtn) saveNameBtn.style.display = show ? 'none' : 'inline-flex';
        if (cancelNameBtn) cancelNameBtn.style.display = show ? 'none' : 'inline-flex';
      };

      const setEditing = (editing) => {
        if (!modalNameInput) return;
        modalNameInput.readOnly = !editing;
        if (editNameBtn) editNameBtn.style.display = editing ? 'none' : 'inline-flex';
        if (saveNameBtn) saveNameBtn.style.display = editing ? 'inline-flex' : 'none';
        if (cancelNameBtn) cancelNameBtn.style.display = editing ? 'inline-flex' : 'none';
        nameRow?.classList.toggle('editing', editing);
        toggleConfirm(false);
        if (editing) modalNameInput.focus();
      };

      const loadAndDisplay = async () => {
        let profile = null;
        try {
          profile = await profileApi.getProfile();
          if (profile) syncLocalProfileState(profile);
        } catch (err) {
          console.error(err);
        }
        const localProfile = safeParse('profile');
        const loggedUser = safeParse('loggedUser');
        const userObj = safeParse('user');
        const avatarData = readStoredAvatar();

        const displayName = (profile && profile.displayName)
          || (localProfile && localProfile.displayName)
          || (loggedUser && loggedUser.name)
          || (userObj && (userObj.fullName || userObj.name))
          || '';
        const email = (profile && profile.email)
          || (loggedUser && loggedUser.email)
          || (userObj && userObj.email)
          || '';
        const serverAvatar = String((profile && (profile.avatarUrl || profile.avatar)) || '').trim();
        if (serverAvatar) {
          persistAvatar(serverAvatar, { emit: false });
        }
        const avatarUrl = serverAvatar || avatarData || '';

        if (modalNameInput) modalNameInput.value = displayName || '';
        if (modalEmailInput) modalEmailInput.value = email || '';
        currentName = displayName || '';
        currentEmail = email || '';
        setEditing(false);
        applyAvatarPreview(avatarUrl);
        applyPasswordChangeAvailability(profile, localProfile, loggedUser, userObj);
      };

      const switchInfoTab = (tab) => {
        const showProfile = tab === 'profile';
        profileTab.classList.toggle('active', showProfile);
        securityTab.classList.toggle('active', !showProfile);
        tabProfile.classList.toggle('active', showProfile);
        tabSecurity.classList.toggle('active', !showProfile);
      };

      const applyPasswordChangeAvailability = (...sources) => {
        let authProvider = '';
        let canChangePassword = null;

        sources.forEach((source) => {
          if (!source || typeof source !== 'object') return;
          if (!authProvider) {
            const nextProvider = String(source.authProvider || '').trim().toLowerCase();
            if (nextProvider) authProvider = nextProvider;
          }
          if (canChangePassword === null && typeof source.canChangePassword === 'boolean') {
            canChangePassword = source.canChangePassword;
          }
        });

        if (!authProvider) authProvider = 'password';
        if (canChangePassword === null) {
          canChangePassword = authProvider !== 'google';
        }

        passwordChangeAllowed = Boolean(canChangePassword);
        securityFormEl?.classList.toggle('password-disabled', !passwordChangeAllowed);
        securityGridEl?.toggleAttribute('hidden', !passwordChangeAllowed);
        securityFootEl?.toggleAttribute('hidden', !passwordChangeAllowed);
        passwordProviderCardEl?.toggleAttribute('hidden', passwordChangeAllowed);

        if (passwordProviderCopyEl) {
          passwordProviderCopyEl.textContent = authProvider === 'google'
            ? 'You signed in with Google. Password changes are managed in your Google account.'
            : '';
        }

        [currentPassInput, newPassInput, confirmPassInput, updatePassBtn].forEach((el) => {
          if (el) el.disabled = !passwordChangeAllowed;
        });

        if (!passwordChangeAllowed) {
          if (currentPassInput) currentPassInput.value = '';
          if (newPassInput) newPassInput.value = '';
          if (confirmPassInput) confirmPassInput.value = '';
          if (currentPassErr) currentPassErr.style.display = 'none';
          if (newPassErr) newPassErr.style.display = 'none';
          if (confirmPassErr) confirmPassErr.style.display = 'none';
        }
      };

      const resetSecurity = () => {
        currentPassInput.value = '';
        newPassInput.value = '';
        confirmPassInput.value = '';
        currentPassErr.style.display = 'none';
        newPassErr.style.display = 'none';
        confirmPassErr.style.display = 'none';
      };

      const handleExportData = async () => {
        if (exportInFlight || !exportDataBtn) return;
        exportInFlight = true;
        exportDataBtn.disabled = true;
        exportDataBtn.textContent = 'Exporting...';
        setDataRightsStatus('Preparing your account export...', 'info');
        try {
          const payload = await profileApi.exportProfileData();
          downloadJsonFile(payload, buildExportFilename());
          setDataRightsStatus('Your account export has been downloaded as a JSON file.', 'success');
        } catch (err) {
          console.error(err);
          setDataRightsStatus((err && err.message) ? err.message : 'Failed to export your data.', 'error');
        } finally {
          exportInFlight = false;
          exportDataBtn.disabled = false;
          exportDataBtn.textContent = 'Export My Data';
        }
      };

      const handleDeletionRequest = async () => {
        if (deletionRequestInFlight || !requestDeletionBtn) return;
        const reason = String(deletionReasonInput?.value || '').trim();
        const confirmed = await confirmUserAction({
          title: 'Request account deletion?',
          message: 'Submit a manual account deletion request for this profile?',
          details: reason ? [`Reason: ${reason}`] : [],
          okText: 'Submit request',
          cancelText: 'Keep account'
        });
        if (!confirmed) return;

        deletionRequestInFlight = true;
        requestDeletionBtn.disabled = true;
        requestDeletionBtn.textContent = 'Submitting...';
        setDataRightsStatus('Submitting your deletion request...', 'info');
        try {
          const payload = await profileApi.requestDeletion(reason);
          const created = Boolean(payload?.created);
          if (created && deletionReasonInput) {
            deletionReasonInput.value = '';
          }
          const message = String(payload?.message || '').trim()
            || (created
              ? 'Your deletion request has been recorded for manual review.'
              : 'A deletion request is already pending for this account.');
          setDataRightsStatus(message, created ? 'success' : 'info');
        } catch (err) {
          console.error(err);
          setDataRightsStatus((err && err.message) ? err.message : 'Failed to submit deletion request.', 'error');
        } finally {
          deletionRequestInFlight = false;
          requestDeletionBtn.disabled = false;
          requestDeletionBtn.textContent = 'Request Account Deletion';
        }
      };

      const handleRotateSession = async () => {
        if (rotateSessionInFlight || !rotateSessionBtn) return;
        const confirmed = await confirmUserAction({
          title: 'Sign out other sessions?',
          message: 'Keep this device signed in and sign out other browsers and devices using this account?',
          okText: 'Sign out others',
          cancelText: 'Cancel'
        });
        if (!confirmed) return;

        rotateSessionInFlight = true;
        rotateSessionBtn.disabled = true;
        rotateSessionBtn.textContent = 'Signing Out Others...';
        setRotateSessionStatus('Rotating your session...', 'info');
        try {
          const payload = await profileApi.rotateSession();
          persistSessionToken(payload?.token);
          setRotateSessionStatus('Other sessions have been signed out. This device stays signed in.', 'success');
        } catch (err) {
          console.error(err);
          setRotateSessionStatus((err && err.message) ? err.message : 'Failed to rotate session.', 'error');
        } finally {
          rotateSessionInFlight = false;
          rotateSessionBtn.disabled = false;
          rotateSessionBtn.textContent = 'Keep This Device, Sign Out Others';
        }
      };

      tabProfile?.addEventListener('click', () => {
        switchPanel('info');
        switchInfoTab('profile');
      });
      tabSecurity?.addEventListener('click', () => {
        switchPanel('info');
        switchInfoTab('security');
      });

      const persistDisplayName = async () => {
        const newName = (modalNameInput.value || '').trim();
        if (newName === currentName) {
          setEditing(false);
          return;
        }
        try {
          const updated = await profileApi.updateName(newName);
          syncLocalProfileState(updated);
        } catch (err) {
          console.error(err);
          await showUserAlert((err && err.message) ? err.message : 'Failed to update name.', 'Name update failed');
          return;
        }

        currentName = newName;
        if (modalNameInput) modalNameInput.value = newName;
        setEditing(false);
        toggleConfirm(false);
      };

      const openEmailModal = () => {
        if (!emailOverlay) return;
        if (newEmailInput) newEmailInput.value = '';
        clearEmailCodeEntry(false);
        setEmailVerifyMessage('', '');
        emailOverlay.classList.add('open');
      };

      const closeEmailModalFn = () => {
        if (!emailOverlay) return;
        emailOverlay.classList.remove('open');
      };

      editNameBtn?.addEventListener('click', () => setEditing(true));
      saveNameBtn?.addEventListener('click', () => toggleConfirm(true));
      confirmNameBtn?.addEventListener('click', persistDisplayName);
      dismissNameBtn?.addEventListener('click', () => toggleConfirm(false));
      cancelNameBtn?.addEventListener('click', () => {
        if (modalNameInput) modalNameInput.value = currentName;
        setEditing(false);
      });
      editEmailBtn?.addEventListener('click', openEmailModal);
      closeEmailModal?.addEventListener('click', closeEmailModalFn);
      cancelEmailBtn?.addEventListener('click', closeEmailModalFn);
      emailOverlay?.addEventListener('click', (event) => {
        if (event.target === emailOverlay) closeEmailModalFn();
      });
      sendEmailCodeBtn?.addEventListener('click', async () => {
        if (!sendEmailCodeBtn || sendEmailCodeBtn.disabled) return;
        const emailValue = (newEmailInput?.value || '').trim();
        const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailValue);
        if (!emailOk) {
          setEmailVerifyMessage('Please enter a valid email before requesting a code.', 'error');
          return;
        }
        setEmailVerifyMessage('', '');
        sendEmailCodeBtn.disabled = true;
        sendEmailCodeBtn.textContent = 'Sending...';
        try {
          await profileApi.sendEmailCode(emailValue);
          clearEmailCodeEntry(true);
          setEmailVerifyMessage(`Verification code sent to ${emailValue}.`, '');
          codeRemaining = 60;
          sendEmailCodeBtn.textContent = `Resend in ${codeRemaining}s`;
          if (codeTimer) clearInterval(codeTimer);
          codeTimer = setInterval(() => {
            codeRemaining -= 1;
            if (codeRemaining <= 0) {
              clearInterval(codeTimer);
              codeTimer = null;
              sendEmailCodeBtn.disabled = false;
              sendEmailCodeBtn.textContent = 'Send code';
              return;
            }
            sendEmailCodeBtn.textContent = `Resend in ${codeRemaining}s`;
          }, 1000);
        } catch (err) {
          console.error(err);
          sendEmailCodeBtn.disabled = false;
          sendEmailCodeBtn.textContent = 'Send code';
          setEmailVerifyMessage((err && err.message) ? err.message : 'Failed to send verification code.', 'error');
        }
      });
      newEmailInput?.addEventListener('input', () => {
        setEmailVerifyMessage('', '');
        clearEmailCodeEntry(false);
      });
      emailCodeSlots.forEach((slot, index) => {
        slot.addEventListener('focus', () => {
          slot.select?.();
        });
        slot.addEventListener('input', () => {
          setEmailVerifyMessage('', '');
          const cleaned = String(slot.value || '').replace(/\D/g, '');
          if (!cleaned) {
            slot.value = '';
            updateEmailCodeFromSlots();
            return;
          }
          if (cleaned.length > 1) {
            applyEmailCodeFromIndex(cleaned, index);
            return;
          }
          slot.value = cleaned;
          const combined = updateEmailCodeFromSlots();
          if (combined.length === 6 && !emailVerifyInFlight) {
            submitEmailVerification({ auto: true });
          } else if (index < emailCodeSlots.length - 1) {
            focusEmailCodeSlot(index + 1);
          }
        });
        slot.addEventListener('keydown', (event) => {
          if (event.key === 'Backspace' && !slot.value && index > 0) {
            emailCodeSlots[index - 1].value = '';
            updateEmailCodeFromSlots();
            focusEmailCodeSlot(index - 1);
            event.preventDefault();
            return;
          }
          if (event.key === 'ArrowLeft' && index > 0) {
            focusEmailCodeSlot(index - 1);
            event.preventDefault();
            return;
          }
          if (event.key === 'ArrowRight' && index < emailCodeSlots.length - 1) {
            focusEmailCodeSlot(index + 1);
            event.preventDefault();
          }
        });
        slot.addEventListener('paste', (event) => {
          const pasted = event.clipboardData?.getData('text') || '';
          if (!pasted) return;
          event.preventDefault();
          setEmailVerifyMessage('', '');
          applyEmailCodeFromIndex(pasted, index);
        });
      });
      modalNameInput?.addEventListener('keydown', (event) => {
        if (modalNameInput.readOnly) {
          if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            setEditing(true);
            return;
          }
          if (event.key.length === 1 || event.key === 'Backspace' || event.key === 'Delete') {
            setEditing(true);
          }
        }
        if (event.key === 'Enter') {
          event.preventDefault();
          toggleConfirm(true);
        }
      });

      avatarInput?.addEventListener('change', async (event) => {
        const file = event.target.files && event.target.files[0];
        if (!file) return;
        const validationError = validateAvatarFile(file);
        if (validationError) {
          event.target.value = '';
          await showUserAlert(validationError, 'Avatar upload');
          return;
        }
        const previousAvatar = String(avatarImg?.getAttribute('src') || readStoredAvatar() || '').trim();
        const previewUrl = await fileToAvatarDataUrl(file);
        if (previewUrl) {
          applyAvatarPreview(previewUrl);
        }

        try {
          const uploaded = await profileApi.uploadAvatar(file);
          const serverAvatar = String(uploaded?.avatarUrl || uploaded?.avatar || '').trim();
          if (serverAvatar) {
            persistAvatar(serverAvatar);
            applyAvatarPreview(serverAvatar);
          }
        } catch (err) {
          console.error(err);
          if (previousAvatar) {
            applyAvatarPreview(previousAvatar);
          } else {
            applyAvatarPreview('');
          }
          await showUserAlert((err && err.message) ? err.message : 'Failed to upload avatar.', 'Avatar upload failed');
        } finally {
          event.target.value = '';
        }
      });

      updatePassBtn?.addEventListener('click', async () => {
        if (!passwordChangeAllowed) return;
        currentPassErr.style.display = 'none';
        newPassErr.style.display = 'none';
        confirmPassErr.style.display = 'none';

        const currentPass = currentPassInput.value;
        const newPass = newPassInput.value;
        const confirmPass = confirmPassInput.value;

        let valid = true;

        if (!currentPass) {
          currentPassErr.textContent = 'Current password is required.';
          currentPassErr.style.display = 'block';
          valid = false;
        }
        if (!newPass) {
          newPassErr.textContent = 'New password is required.';
          newPassErr.style.display = 'block';
          valid = false;
        }
        if (newPass && confirmPass && newPass !== confirmPass) {
          confirmPassErr.textContent = 'Passwords do not match.';
          confirmPassErr.style.display = 'block';
          valid = false;
        }

        if (!valid) return;

        const updateBtn = updatePassBtn;
        if (updateBtn) {
          updateBtn.disabled = true;
          updateBtn.textContent = 'Updating...';
        }
        try {
          const updated = await profileApi.updatePassword(currentPass, newPass);
          persistSessionToken(updated?.token);
          if (updateBtn) {
            updateBtn.textContent = 'Password updated';
          }
          setTimeout(() => {
            if (updateBtn) {
              updateBtn.disabled = false;
              updateBtn.textContent = 'Update Password';
            }
            resetSecurity();
          }, 900);
        } catch (err) {
          console.error(err);
          const message = (err && err.message) ? err.message : 'Failed to update password.';
          if (/current password/i.test(message)) {
            currentPassErr.textContent = message;
            currentPassErr.style.display = 'block';
          } else {
            newPassErr.textContent = message;
            newPassErr.style.display = 'block';
          }
          if (updateBtn) {
            updateBtn.disabled = false;
            updateBtn.textContent = 'Update Password';
          }
        }
      });

      rotateSessionBtn?.addEventListener('click', handleRotateSession);
      exportDataBtn?.addEventListener('click', handleExportData);
      requestDeletionBtn?.addEventListener('click', handleDeletionRequest);

      applyDataRightsConfig(window.ClubPortalInfoContent?.readPublicConfig?.() || {});
      window.ClubPortalInfoContent?.loadPublicConfig?.().then((config) => {
        applyDataRightsConfig(config);
      }).catch(() => {});
      window.addEventListener('clubportal:public-config-ready', (event) => {
        applyDataRightsConfig(event?.detail || {});
      });

      loadAndDisplay();
      loadBookings();
      switchPanel('bookings');
    });
