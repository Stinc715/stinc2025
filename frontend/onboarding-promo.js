document.addEventListener('DOMContentLoaded', () => {
  const btnBack = document.getElementById('btnBack');
  const btnFinish = document.getElementById('btnFinish');
  const imageFileInput = document.getElementById('promoImageFile');
  const promoText = document.getElementById('promoText');
  const gallery = document.getElementById('promoGallery');
  const actionsEl = document.querySelector('.actions');
  const ONBOARDING_DRAFT_STORAGE_KEY = 'clubPortal.onboardingDraft';
  const MAX_PROMO_IMAGE_BYTES = 8 * 1024 * 1024;
  const ALLOWED_PROMO_IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/gif', 'image/webp']);

  const getAuthToken = () => {
    try {
      return String(window.AuthSession?.getToken?.() || localStorage.getItem('token') || '').trim();
    } catch {
      return '';
    }
  };

  const requireClubLogin = () => {
    const token = getAuthToken();
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
    const headers = { ...(options.headers || {}) };
    const authFetchImpl = window.AuthSession?.authFetch;
    if (typeof authFetchImpl === 'function') {
      return authFetchImpl(url, {
        ...options,
        credentials: options.credentials ?? 'include',
        headers
      });
    }
    const token = getAuthToken();
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

  const safe = (value) => String(value ?? '').trim();

  const normalizeImageId = (value) => {
    const num = Number(value);
    return Number.isFinite(num) && num > 0 ? num : null;
  };

  const extractImageIdFromUrl = (url) => {
    const match = String(url || '').match(/\/images\/(\d+)\/content(?:[?#]|$)/);
    return match ? normalizeImageId(match[1]) : null;
  };

  const extensionForMimeType = (mimeType) => {
    const type = safe(mimeType).toLowerCase();
    if (type === 'image/png') return 'png';
    if (type === 'image/gif') return 'gif';
    if (type === 'image/webp') return 'webp';
    return 'jpg';
  };

  const sanitizeFileName = (value, fallback = 'club-photo.jpg') => {
    const normalized = safe(value).replace(/[^\w.\-]+/g, '-').replace(/-+/g, '-').replace(/^-|-$/g, '');
    return normalized || fallback;
  };

  const normalizePromoImageItem = (item, index = 0) => {
    if (!item) return null;
    if (typeof item === 'string') {
      const src = safe(item);
      if (!src) return null;
      const origin = src.startsWith('data:') ? 'draft' : 'uploaded';
      const mimeType = origin === 'draft'
        ? safe(src.slice(5, src.indexOf(';')))
        : '';
      return {
        src,
        url: origin === 'uploaded' ? src : '',
        origin,
        imageId: origin === 'uploaded' ? extractImageIdFromUrl(src) : null,
        fileName: `club-photo-${index + 1}.${extensionForMimeType(mimeType)}`,
        mimeType
      };
    }

    const src = safe(item.src || item.url);
    if (!src) return null;
    const origin = safe(item.origin) || (src.startsWith('data:') ? 'draft' : 'uploaded');
    const mimeType = safe(item.mimeType || item.type || (src.startsWith('data:') ? src.slice(5, src.indexOf(';')) : ''));
    return {
      src,
      url: safe(item.url || (origin === 'uploaded' ? src : '')),
      origin,
      imageId: normalizeImageId(item.imageId ?? item.id) ?? (origin === 'uploaded' ? extractImageIdFromUrl(src) : null),
      fileName: sanitizeFileName(
        item.fileName || item.originalName || item.name || `club-photo-${index + 1}.${extensionForMimeType(mimeType)}`
      ),
      mimeType
    };
  };

  const normalizePromoImageList = (list) => (
    Array.isArray(list) ? list.map((item, index) => normalizePromoImageItem(item, index)).filter(Boolean) : []
  );

  const readOnboardingDraft = () => {
    try {
      return JSON.parse(sessionStorage.getItem(ONBOARDING_DRAFT_STORAGE_KEY) || 'null') || {};
    } catch {
      return {};
    }
  };

  const writeOnboardingDraft = (draft) => {
    try {
      sessionStorage.setItem(ONBOARDING_DRAFT_STORAGE_KEY, JSON.stringify(draft || {}));
    } catch {
      // Ignore sessionStorage errors.
    }
    try {
      localStorage.removeItem('clubProfile');
    } catch {
      // Ignore localStorage errors.
    }
  };

  const clearOnboardingDraft = () => {
    try {
      sessionStorage.removeItem(ONBOARDING_DRAFT_STORAGE_KEY);
    } catch {
      // Ignore sessionStorage errors.
    }
    try {
      localStorage.removeItem('clubProfile');
    } catch {
      // Ignore localStorage errors.
    }
  };

  const loadClubProfile = () => {
    try {
      return readOnboardingDraft();
    } catch (e) {
      return {};
    }
  };

  const saveClubProfile = (profile) => {
    try {
      writeOnboardingDraft(profile);
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

  const updateFinishState = () => {
    const hasImages = state.images.length > 0;
    const hasPromoText = String(promoText?.value || '').trim().length > 0;
    if (btnFinish) btnFinish.disabled = !(hasImages && hasPromoText);
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
      window.location.replace('home.html');
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

  const validatePromoFiles = (files) => {
    const valid = [];
    for (const file of files) {
      const contentType = safe(file?.type).toLowerCase();
      if (!ALLOWED_PROMO_IMAGE_TYPES.has(contentType)) {
        showInlineError('Only JPG, PNG, GIF, and WEBP images are supported.');
        continue;
      }
      if ((Number(file?.size) || 0) > MAX_PROMO_IMAGE_BYTES) {
        showInlineError('Each club photo must be 8MB or smaller.');
        continue;
      }
      valid.push(file);
    }
    return valid;
  };

  const readFileAsDataUrl = (file) => new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = () => reject(new Error(`Failed to read ${safe(file?.name) || 'image file'}.`));
    reader.readAsDataURL(file);
  });

  const normalizeUploadedClubImage = (item, index = 0) => {
    const url = safe(item?.url || item?.imageUrl || item?.src);
    const imageId = normalizeImageId(item?.imageId ?? item?.id) ?? extractImageIdFromUrl(url);
    if (!url || !imageId) return null;
    return {
      src: url,
      url,
      origin: 'uploaded',
      imageId,
      fileName: sanitizeFileName(item?.originalName || item?.name || `club-photo-${index + 1}.jpg`),
      mimeType: safe(item?.mimeType)
    };
  };

  const dataUrlToBlob = async (dataUrl) => {
    const raw = String(dataUrl || '').trim();
    const commaIndex = raw.indexOf(',');
    if (raw.startsWith('data:') && commaIndex > 5) {
      const header = raw.slice(5, commaIndex);
      const body = raw.slice(commaIndex + 1);
      const isBase64 = /;base64/i.test(header);
      const mimeType = safe(header.replace(/;base64/i, '').split(';')[0]).toLowerCase() || 'application/octet-stream';
      if (isBase64) {
        const binary = atob(body.replace(/\s+/g, ''));
        const bytes = new Uint8Array(binary.length);
        for (let index = 0; index < binary.length; index += 1) {
          bytes[index] = binary.charCodeAt(index);
        }
        return new Blob([bytes], { type: mimeType });
      }
      return new Blob([decodeURIComponent(body)], { type: mimeType });
    }

    const res = await fetch(raw);
    if (!res.ok) {
      throw new Error('Failed to prepare image upload.');
    }
    return await res.blob();
  };

  const listClubImages = async (clubId) => {
    const normalizedClubId = safe(clubId);
    if (!normalizedClubId) return [];
    try {
      const res = await authFetch(`/api/clubs/${encodeURIComponent(normalizedClubId)}/images`, {
        cache: 'no-store'
      });
      if (!res.ok) return [];
      const data = await apiJson(res);
      return Array.isArray(data) ? data.map(normalizeUploadedClubImage).filter(Boolean) : [];
    } catch {
      return [];
    }
  };

  const pickFreshUploadedImage = (images, knownImageIds, fileName) => {
    const normalizedFileName = sanitizeFileName(fileName, '');
    return images.find((item) => (
        item?.imageId
        && !knownImageIds.has(String(item.imageId))
        && sanitizeFileName(item.fileName, '') === normalizedFileName
      ))
      || images.find((item) => item?.imageId && !knownImageIds.has(String(item.imageId)))
      || null;
  };

  const setPrimaryClubImage = async (clubId, imageId) => {
    const normalizedClubId = safe(clubId);
    const normalizedImageId = normalizeImageId(imageId);
    if (!normalizedClubId || !normalizedImageId) return;
    const res = await authFetch(`/api/clubs/${encodeURIComponent(normalizedClubId)}/images/${encodeURIComponent(String(normalizedImageId))}/primary`, {
      method: 'PUT'
    });
    if (!res.ok) {
      const body = await apiJson(res);
      throw new Error(body?.message || 'Failed to set the main club photo.');
    }
  };

  const uploadSingleDraftImageToClub = async (clubId, item, index, knownImageIds) => {
    const blob = await dataUrlToBlob(item?.src);
    const mimeType = safe(item?.mimeType || blob.type).toLowerCase();
    if (!ALLOWED_PROMO_IMAGE_TYPES.has(mimeType)) {
      throw new Error('Only JPG, PNG, GIF, and WEBP images are supported.');
    }
    if ((Number(blob.size) || 0) > MAX_PROMO_IMAGE_BYTES) {
      throw new Error('Each club photo must be 8MB or smaller.');
    }

    const ext = extensionForMimeType(mimeType);
    const fileName = sanitizeFileName(item?.fileName, `club-photo-${index + 1}.${ext}`);
    const formData = new FormData();
    formData.append('files', blob, fileName);

    const res = await authFetch(`/api/clubs/${encodeURIComponent(String(clubId))}/images`, {
      method: 'POST',
      body: formData
    });
    if (!res.ok) {
      const body = await apiJson(res);
      throw new Error(body?.message || 'Failed to upload club photos.');
    }

    const data = await apiJson(res);
    const uploaded = Array.isArray(data) ? data.map(normalizeUploadedClubImage).filter(Boolean) : [];
    if (uploaded.length) {
      return uploaded[0];
    }

    const refreshedImages = await listClubImages(clubId);
    return pickFreshUploadedImage(refreshedImages, knownImageIds, fileName);
  };

  const uploadDraftImagesToClub = async (clubId) => {
    const uploadableImages = state.images.filter((item) => item?.src && String(item.src).startsWith('data:'));
    if (!uploadableImages.length) return [];

    const knownImageIds = new Set(
      (await listClubImages(clubId))
        .map((item) => item?.imageId)
        .filter((imageId) => imageId)
        .map((imageId) => String(imageId))
    );
    const uploadedImages = [];
    for (let index = 0; index < uploadableImages.length; index += 1) {
      const uploaded = await uploadSingleDraftImageToClub(clubId, uploadableImages[index], index, knownImageIds);
      if (!uploaded?.imageId) {
        throw new Error('Failed to confirm club photo upload.');
      }
      knownImageIds.add(String(uploaded.imageId));
      uploadedImages.push(uploaded);
    }
    return uploadedImages;
  };

  const syncPromoImagesToClub = async (club) => {
    const clubId = safe(club?.id ?? club?.clubId);
    if (!clubId) {
      throw new Error('Club setup finished, but the club id is missing.');
    }

    const uploadedImages = await uploadDraftImagesToClub(clubId);
    const serverImages = await listClubImages(clubId);
    const serverImageById = new Map(
      serverImages
        .filter((item) => item?.imageId)
        .map((item) => [String(item.imageId), item])
    );
    let uploadCursor = 0;
    const mergedImages = state.images.map((item, index) => {
      const normalized = normalizePromoImageItem(item, index);
      if (!normalized) return null;
      if (String(normalized.src).startsWith('data:')) {
        const uploaded = uploadedImages[uploadCursor] || null;
        uploadCursor += 1;
        return uploaded || null;
      }
      const normalizedUploaded = normalizeUploadedClubImage(normalized, index) || normalized;
      if (normalizedUploaded?.imageId && serverImageById.has(String(normalizedUploaded.imageId))) {
        return serverImageById.get(String(normalizedUploaded.imageId));
      }
      return normalizedUploaded;
    }).filter(Boolean);

    if (mergedImages.length) {
      state.images = dedupeImages(mergedImages);
      savePromo({ images: state.images });
    }

    const primaryTarget = state.images[0] || uploadedImages[0] || null;
    if (primaryTarget?.imageId) {
      await setPrimaryClubImage(clubId, primaryTarget.imageId);
    }

    saveSelectedClub({
      ...club,
      coverUrl: primaryTarget?.url || safe(club?.coverUrl || club?.coverImageUrl || '')
    });

    return {
      images: mergedImages,
      primaryImageUrl: primaryTarget?.url || ''
    };
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

  const dedupeImages = (list) => {
    const seen = new Set();
    return normalizePromoImageList(list).filter((item) => {
      const key = item.imageId ? `id:${item.imageId}` : item.src;
      if (!key || seen.has(key)) return false;
      seen.add(key);
      return true;
    });
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
      state.images = normalizePromoImageList(promo.images);
    }
    if (Array.isArray(overrides.images)) {
      state.images = normalizePromoImageList(overrides.images);
    }
    const nextText = Object.prototype.hasOwnProperty.call(overrides, 'text')
      ? safe(overrides.text)
      : safe(promoText?.value);
    const updated = {
      ...promo,
      ...overrides,
      images: dedupeImages(state.images),
      text: nextText
    };
    state.images = updated.images;
    profile.promo = updated;
    saveClubProfile(profile);
    clearInlineError();
    updateGallery(state.images);
    updateFinishState();
  };

  const hydrate = () => {
    const profile = loadClubProfile();
    const promo = profile.promo || {};
    if (promoText) promoText.value = promo.text || '';
    state.images = normalizePromoImageList(promo.images);
    updateGallery(state.images);
    updateFinishState();
  };

  imageFileInput?.addEventListener('change', (event) => {
    clearInlineError();
    const files = validatePromoFiles(Array.from(event.target.files || []));
    if (!files.length) {
      if (imageFileInput) imageFileInput.value = '';
      return;
    }
    const readers = files.map((file) => readFileAsDataUrl(file).then((src) => ({
      src: String(src || ''),
      origin: 'draft',
      fileName: sanitizeFileName(file.name, `club-photo.${extensionForMimeType(file.type)}`),
      mimeType: safe(file.type).toLowerCase()
    })));
    Promise.all(readers).then((results) => {
      const existingItems = state.images.slice();
      state.images = dedupeImages(existingItems.concat(results));
      savePromo({ images: state.images });
      if (imageFileInput) imageFileInput.value = '';
    }).catch((error) => {
      showInlineError(error?.message || 'Failed to read selected images.');
      if (imageFileInput) imageFileInput.value = '';
    });
  });

  promoText?.addEventListener('input', () => {
    clearInlineError();
    savePromo();
  });

  btnBack?.addEventListener('click', () => {
    savePromo();
    window.location.href = 'onboarding-location.html';
  });

  btnFinish?.addEventListener('click', async () => {
    savePromo();
    clearInlineError();
    const hasImages = state.images.length > 0;
    const hasPromoText = String(promoText?.value || '').trim().length > 0;
    if (!hasImages || !hasPromoText) {
      showInlineError(!hasImages
        ? 'Please upload at least one club photo before finishing setup.'
        : 'Please enter promo text before finishing setup.');
      updateFinishState();
      return;
    }
    try {
      if (btnFinish) btnFinish.disabled = true;
      const club = await upsertClubFromDraft();
      if (!club) return;
      await syncPromoImagesToClub(club);
      clearOnboardingDraft();
      window.location.replace('onboarding-complete.html');
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
