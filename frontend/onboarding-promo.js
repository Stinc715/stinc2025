document.addEventListener('DOMContentLoaded', () => {
  const btnBack = document.getElementById('btnBack');
  const btnFinish = document.getElementById('btnFinish');
  const imageUrlInput = document.getElementById('promoImageUrl');
  const imageFileInput = document.getElementById('promoImageFile');
  const promoText = document.getElementById('promoText');
  const gallery = document.getElementById('promoGallery');
  const actionsEl = document.querySelector('.actions');

  const requireClubLogin = () => {
    const token = localStorage.getItem('token');
    if (!token) return false;
    try {
      const u = JSON.parse(localStorage.getItem('loggedUser') || 'null');
      if (!u || u.type !== 'club') return false;
    } catch {
      return false;
    }
    return true;
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

  const loadSelectedClubId = () => {
    try {
      const stored = JSON.parse(localStorage.getItem('selectedClub') || 'null');
      return stored?.id ?? null;
    } catch {
      return null;
    }
  };

  const state = {
    images: []
  };

  const loadClubProfile = () => {
    try {
      return JSON.parse(localStorage.getItem('clubProfile') || 'null') || {};
    } catch (e) {
      return {};
    }
  };

  const saveClubProfile = (profile) => {
    try {
      localStorage.setItem('clubProfile', JSON.stringify(profile));
    } catch (e) { /* ignore */ }
  };

  const showInlineError = (message) => {
    const text = String(message || '').trim();
    if (!text) return;
    let box = document.getElementById('promo-inline-status');
    if (!box) {
      box = document.createElement('div');
      box.id = 'promo-inline-status';
      box.style.marginTop = '12px';
      box.style.padding = '10px 12px';
      box.style.borderRadius = '12px';
      box.style.border = '1px solid rgba(185, 28, 28, 0.2)';
      box.style.background = 'rgba(254, 242, 242, 0.98)';
      box.style.color = '#991b1b';
      box.style.fontSize = '13px';
      box.style.lineHeight = '1.45';
      if (actionsEl && actionsEl.parentNode) {
        actionsEl.parentNode.insertBefore(box, actionsEl.nextSibling);
      } else {
        document.body.appendChild(box);
      }
    }
    box.textContent = text;
  };

  const clearInlineError = () => {
    const box = document.getElementById('promo-inline-status');
    if (box) box.remove();
  };

  const apiJson = async (res) => {
    const ct = String(res.headers.get('content-type') || '');
    if (ct.includes('application/json')) return await res.json();
    const text = await res.text();
    return text ? { message: text } : null;
  };

  const saveSelectedClub = (club) => {
    const id = club?.id ?? club?.clubId ?? null;
    if (!id) return;
    const name = club?.name ?? club?.clubName ?? club?.title ?? `Club #${id}`;
    const tags = Array.isArray(club?.tags) ? club.tags : [];
    try {
      localStorage.setItem('selectedClub', JSON.stringify({ id: String(id), name, tags }));
    } catch (e) {
      // Ignore localStorage errors.
    }
  };

  const pickClubFromMyClubs = (clubs) => {
    const list = Array.isArray(clubs) ? clubs : [];
    if (!list.length) return null;
    const storedId = loadSelectedClubId();
    if (storedId) {
      const hit = list.find((club) => String(club?.id ?? club?.clubId ?? '') === String(storedId));
      if (hit) return hit;
    }
    return list[0];
  };

  const getLoggedUserEmail = () => {
    try {
      return String(JSON.parse(localStorage.getItem('loggedUser') || 'null')?.email || '').trim();
    } catch {
      return '';
    }
  };

  const loadDraftClubPayload = () => {
    const draft = loadClubProfile();
    const profile = draft?.profile || {};
    const location = draft?.location || {};
    const promo = draft?.promo || {};
    const sports = Array.isArray(profile?.sports) ? profile.sports : [];
    const name = String(profile?.displayName || profile?.name || '').trim();
    const category = String(sports[0] || '').trim();
    const description = String((promoText?.value ?? promo?.text) || '').trim();
    const address = String(location?.formattedAddress || location?.address || location?.city || '').trim();
    const placeId = String(location?.placeId || '').trim();
    const locationLat = Number(location?.lat);
    const locationLng = Number(location?.lng);

    const payload = { name };
    if (category) payload.category = category;
    if (sports.length) payload.tags = sports;
    if (description) payload.description = description;
    if (address) {
      payload.location = address;
      payload.placeId = placeId;
    }
    if (Number.isFinite(locationLat) && Number.isFinite(locationLng)) {
      payload.locationLat = locationLat;
      payload.locationLng = locationLng;
    }

    const email = getLoggedUserEmail();
    if (email) payload.email = email;
    return payload;
  };

  const upsertClubFromDraft = async () => {
    if (!requireClubLogin()) {
      window.location.href = 'login.html#login';
      return null;
    }

    const payload = loadDraftClubPayload();
    if (!payload.name) {
      throw new Error('Club name is required before finishing setup.');
    }

    let clubs = [];
    try {
      const res = await authFetch('/api/my/clubs');
      if (res.ok) clubs = await res.json();
    } catch (e) {
      clubs = [];
    }

    const selected = pickClubFromMyClubs(clubs);
    const existingId = selected?.id ?? selected?.clubId ?? null;
    const endpoint = existingId ? `/api/clubs/${encodeURIComponent(existingId)}` : '/api/clubs';
    const method = existingId ? 'PUT' : 'POST';

    // Publish the accumulated draft only when the club owner finishes onboarding.
    const res = await authFetch(endpoint, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      const body = await apiJson(res);
      throw new Error(body?.message || (existingId ? 'Failed to update club profile.' : 'Failed to create club.'));
    }

    const club = await res.json();
    saveSelectedClub(club);
    return club;
  };

  const updateGallery = (images) => {
    if (!gallery) return;
    gallery.innerHTML = '';
    if (!Array.isArray(images) || images.length === 0) return;
    images.forEach((item, index) => {
      const card = document.createElement('div');
      card.className = 'promo-card';
      card.draggable = true;
      card.dataset.index = String(index);

      const img = document.createElement('img');
      img.className = 'promo-thumb';
      img.alt = 'Club photo';
      img.src = item.src;

      const badges = document.createElement('div');
      badges.className = 'promo-badges';

      const badge = document.createElement('span');
      badge.className = 'promo-cover';
      badge.textContent = index === 0 ? 'Cover' : 'Gallery';

      const delBtn = document.createElement('button');
      delBtn.className = 'promo-delete';
      delBtn.type = 'button';
      delBtn.setAttribute('aria-label', 'Remove image');
      delBtn.textContent = '×';

      delBtn.addEventListener('click', (event) => {
        event.preventDefault();
        event.stopPropagation();
        const next = state.images.slice();
        next.splice(index, 1);
        state.images = next;
        savePromo({ images: state.images });
      });

      badges.appendChild(badge);
      badges.appendChild(delBtn);

      card.appendChild(img);
      card.appendChild(badges);
      gallery.appendChild(card);
    });
  };

  const parseUrlList = (value) => {
    return (value || '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  };

  const dedupeImages = (list) => {
    const seen = new Set();
    return list.filter((item) => {
      if (!item || !item.src) return false;
      if (seen.has(item.src)) return false;
      seen.add(item.src);
      return true;
    });
  };

  const syncUrlInput = () => {
    if (!imageUrlInput) return;
    const urls = state.images.filter((item) => item.origin === 'url').map((item) => item.src);
    imageUrlInput.value = urls.join(', ');
  };

  const moveImage = (from, to) => {
    if (from === to) return;
    if (to < 0 || to >= state.images.length) return;
    const next = state.images.slice();
    const [moved] = next.splice(from, 1);
    next.splice(to, 0, moved);
    state.images = next;
    savePromo({ images: state.images });
  };

  const setupDragAndDrop = () => {
    if (!gallery) return;
    let draggingIndex = null;

    gallery.addEventListener('dragstart', (event) => {
      const card = event.target.closest('.promo-card');
      if (!card) return;
      draggingIndex = Number(card.dataset.index);
      card.classList.add('dragging');
      event.dataTransfer.effectAllowed = 'move';
    });

    gallery.addEventListener('dragend', (event) => {
      const card = event.target.closest('.promo-card');
      if (card) card.classList.remove('dragging');
      draggingIndex = null;
    });

    gallery.addEventListener('dragover', (event) => {
      event.preventDefault();
      event.dataTransfer.dropEffect = 'move';
    });

    gallery.addEventListener('drop', (event) => {
      event.preventDefault();
      const card = event.target.closest('.promo-card');
      if (!card) return;
      const targetIndex = Number(card.dataset.index);
      if (Number.isNaN(draggingIndex) || Number.isNaN(targetIndex)) return;
      moveImage(draggingIndex, targetIndex);
    });
  };

  const savePromo = (overrides = {}) => {
    const profile = loadClubProfile();
    const promo = profile.promo || {};
    if (!state.images.length && Array.isArray(promo.images)) {
      state.images = promo.images;
    }
    if (Array.isArray(overrides.images)) {
      state.images = overrides.images;
    }
    const updated = {
      images: dedupeImages(state.images),
      text: promoText?.value?.trim() || '',
      ...overrides
    };
    state.images = updated.images;
    profile.promo = updated;
    saveClubProfile(profile);
    updateGallery(state.images);
    syncUrlInput();
  };

  const hydrate = () => {
    const profile = loadClubProfile();
    const promo = profile.promo || {};
    if (promoText) promoText.value = promo.text || '';
    state.images = Array.isArray(promo.images) ? promo.images : [];
    updateGallery(state.images);
    syncUrlInput();
  };

  imageUrlInput?.addEventListener('input', () => {
    const urlItems = parseUrlList(imageUrlInput.value).map((src) => ({ src, origin: 'url' }));
    const dataItems = state.images.filter((item) => item.origin === 'data');
    state.images = dedupeImages(urlItems.concat(dataItems));
    savePromo({ images: state.images });
  });

  imageFileInput?.addEventListener('change', (event) => {
    const files = Array.from(event.target.files || []);
    if (!files.length) {
      const remaining = state.images.filter((item) => item.origin !== 'data');
      state.images = remaining;
      savePromo({ images: state.images });
      return;
    }
    const readers = files.map((file) => new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.readAsDataURL(file);
    }));
    Promise.all(readers).then((results) => {
      const urlItems = state.images.filter((item) => item.origin === 'url');
      const dataItems = results.map((src) => ({ src, origin: 'data' }));
      state.images = dedupeImages(urlItems.concat(dataItems));
      savePromo({ images: state.images });
    });
  });

  promoText?.addEventListener('input', () => {
    savePromo();
  });

  btnBack?.addEventListener('click', () => {
    savePromo();
    window.location.href = 'onboarding-location.html';
  });

  btnFinish?.addEventListener('click', async () => {
    savePromo();
    clearInlineError();
    try {
      if (btnFinish) btnFinish.disabled = true;
      await upsertClubFromDraft();
      window.location.href = 'club home.html';
    } catch (e) {
      console.warn('Failed to finalize club setup', e);
      if (window.AppPrompt && typeof window.AppPrompt.toast === 'function') {
        window.AppPrompt.toast(e?.message || 'Failed to finish setup. Please try again.', { type: 'error', duration: 3200 });
      }
      showInlineError(e?.message || 'Failed to finish setup. Please try again.');
    } finally {
      if (btnFinish) btnFinish.disabled = false;
    }
  });

  hydrate();
  setupDragAndDrop();
});
