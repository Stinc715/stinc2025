document.addEventListener('DOMContentLoaded', () => {
  const btnBack = document.getElementById('btnBack');
  const btnFinish = document.getElementById('btnFinish');
  const imageUrlInput = document.getElementById('promoImageUrl');
  const imageFileInput = document.getElementById('promoImageFile');
  const promoText = document.getElementById('promoText');
  const gallery = document.getElementById('promoGallery');

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

  btnFinish?.addEventListener('click', () => {
    savePromo();
    window.location.href = 'club home.html';
  });

  hydrate();
  setupDragAndDrop();
});
