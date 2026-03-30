// onboarding.js
(function(){
  const sports = [
    'Badminton',
    'Running',
    'Volleyball',
    'Yoga',
    'Skating',
    'Basketball',
    'Tennis',
    'Swimming',
    'Martial Arts',
    'Skiing',
    'Football',
    'Table Tennis',
    'Cycling',
    'Climbing',
    'Gym'
  ];

  const emojiMap = {
    'Badminton': '\u{1F3F8}',
    'Basketball': '\u{1F3C0}',
    'Football': '\u26BD',
    'Running': '\u{1F3C3}',
    'Tennis': '\u{1F3BE}',
    'Table Tennis': '\u{1F3D3}',
    'Volleyball': '\u{1F3D0}',
    'Swimming': '\u{1F3CA}',
    'Cycling': '\u{1F6B4}',
    'Yoga': '\u{1F9D8}',
    'Martial Arts': '\u{1F94B}',
    'Climbing': '\u{1F9D7}',
    'Skating': '\u26F8',
    'Skiing': '\u{1F3BF}',
    'Gym': '\u{1F3CB}',
    'Dancing': '\u{1F483}',
    'Cricket': '\u{1F3CF}',
    'Rugby': '\u{1F3C9}',
    'Hiking': '\u{1F97E}',
    'Golf': '\u26F3',
    'Boxing': '\u{1F94A}',
    'Archery': '\u{1F3F9}',
    'Rowing': '\u{1F6A3}',
    'Horse Riding': '\u{1F40E}',
    'Surfing': '\u{1F3C4}'
  };

  const normalizeKey = (value) => (value || '').toLowerCase().replace(/[\s_-]+/g, ' ').trim();
  const emojiByName = {};
  Object.keys(emojiMap).forEach((name) => {
    emojiByName[normalizeKey(name)] = emojiMap[name];
  });

  const emojiAliases = [
    { emoji: emojiMap['Badminton'], keys: ['badminton', '\u7fbd\u6bdb\u7403'] },
    { emoji: emojiMap['Basketball'], keys: ['basketball', '\u7bee\u7403'] },
    { emoji: emojiMap['Football'], keys: ['football', 'soccer', '\u8db3\u7403'] },
    { emoji: emojiMap['Running'], keys: ['running', 'run', '\u8dd1\u6b65'] },
    { emoji: emojiMap['Tennis'], keys: ['tennis', '\u7f51\u7403'] },
    { emoji: emojiMap['Table Tennis'], keys: ['table tennis', 'ping pong', 'pingpong', '\u4e52\u4e53\u7403'] },
    { emoji: emojiMap['Volleyball'], keys: ['volleyball', '\u6392\u7403'] },
    { emoji: emojiMap['Swimming'], keys: ['swimming', 'swim', '\u6e38\u6cf3'] },
    { emoji: emojiMap['Cycling'], keys: ['cycling', 'bike', 'biking', '\u9a91\u884c', '\u9a91\u8f66'] },
    { emoji: emojiMap['Yoga'], keys: ['yoga', '\u745c\u4f3d'] },
    { emoji: emojiMap['Martial Arts'], keys: ['martial', 'martial arts', '\u6b66\u672f', '\u64c2\u51fb'] },
    { emoji: emojiMap['Climbing'], keys: ['climbing', 'climb', '\u6500\u5ca9', '\u767b\u5c71'] },
    { emoji: emojiMap['Skating'], keys: ['skating', 'skate', '\u6ed1\u51b0', '\u6ed1\u677f'] },
    { emoji: emojiMap['Skiing'], keys: ['ski', 'skiing', '\u6ed1\u96ea'] },
    { emoji: emojiMap['Gym'], keys: ['gym', 'fitness', '\u5065\u8eab'] },
    { emoji: emojiMap['Dancing'], keys: ['dancing', 'dance', '\u821e\u8e48', '\u8df3\u821e'] },
    { emoji: emojiMap['Cricket'], keys: ['cricket', '\u677f\u7403'] },
    { emoji: emojiMap['Rugby'], keys: ['rugby', '\u6a44\u6984\u7403'] },
    { emoji: emojiMap['Hiking'], keys: ['hiking', 'hike', '\u5f92\u6b65'] },
    { emoji: emojiMap['Golf'], keys: ['golf', '\u9ad8\u5c14\u592b'] },
    { emoji: emojiMap['Boxing'], keys: ['boxing', 'box', '\u62f3\u51fb'] },
    { emoji: emojiMap['Archery'], keys: ['archery', '\u5c04\u7bad'] },
    { emoji: emojiMap['Rowing'], keys: ['rowing', '\u5212\u8239'] },
    { emoji: emojiMap['Horse Riding'], keys: ['horse', 'riding', 'equestrian', '\u9a6c\u672f'] },
    { emoji: emojiMap['Surfing'], keys: ['surf', 'surfing', '\u51b2\u6d6a'] }
  ].map((entry) => ({
    emoji: entry.emoji,
    keys: entry.keys.map(normalizeKey)
  }));

  const grid = document.getElementById('sportsGrid');
  const displayNameEl = document.getElementById('displayName');
  const customList = document.getElementById('customTypeList');
  const addCustomBtn = document.getElementById('addCustomType');
  const btnSave = document.getElementById('btnSave');
  const statusEl = document.getElementById('status');
  const headerSideEl = document.querySelector('.header-side');
  const stickyTitleEl = document.querySelector('header h2');
  const heroTitleEl = document.querySelector('.hero h1');
  const heroLeadEl = document.querySelector('.hero .lead');
  const panelTitleEl = document.querySelector('.panel-header h2');
  const panelSubtitleEl = document.querySelector('.panel-header p');
  const panelBadgeEl = document.querySelector('.panel-badge');
  const formBlocks = Array.from(document.querySelectorAll('.form-block'));
  const nameBlockEl = formBlocks[0] || null;
  const sportsBlockEl = formBlocks[1] || null;
  const sportsLabelEl = sportsBlockEl?.querySelector('.label') || null;
  const sportsHintEl = sportsBlockEl?.querySelector('.field-hint') || null;
  const query = new URLSearchParams(window.location.search);
  const editMode = String(query.get('mode') || '').trim().toLowerCase();
  const isCategoryEditMode = editMode === 'category-edit';
  const isNameEditMode = editMode === 'name-edit';
  const isProfileEditMode = isCategoryEditMode || isNameEditMode;
  const returnTarget = sanitizeReturnTarget(query.get('return'));
  const ONBOARDING_DRAFT_STORAGE_KEY = 'clubPortal.onboardingDraft';
  let backBtn = headerSideEl?.querySelector('.back-link') || null;
  let onboardingBackGuardInstalled = false;

  if (!grid || !displayNameEl) {
    return;
  }

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

  function sanitizeReturnTarget(value) {
    const allowed = new Set(['club-info.html', 'club home.html', 'onboarding-location.html']);
    const raw = String(value || '').trim();
    return allowed.has(raw) ? raw : 'club-info.html';
  }

  function canUseHistoryBack() {
    if (window.history.length <= 1) return false;
    const ref = String(document.referrer || '').trim();
    if (!ref) return false;
    try {
      return new URL(ref, window.location.href).origin === window.location.origin;
    } catch {
      return false;
    }
  }

  function navigateBackOrFallback(target) {
    if (canUseHistoryBack()) {
      window.history.back();
      return;
    }
    window.location.href = target;
  }

  function clearBackButton() {
    if (backBtn) {
      backBtn.remove();
      backBtn = null;
    }
    if (headerSideEl) {
      headerSideEl.setAttribute('aria-hidden', 'true');
    }
  }

  function ensureBackButton() {
    if (!headerSideEl) return null;
    headerSideEl.removeAttribute('aria-hidden');
    if (backBtn) return backBtn;

    backBtn = document.createElement('button');
    backBtn.type = 'button';
    backBtn.className = 'back-link';
    backBtn.setAttribute('aria-label', 'Back');
    backBtn.textContent = '\u2190 Back';
    headerSideEl.appendChild(backBtn);
    return backBtn;
  }

  function setBackTarget(target) {
    const button = ensureBackButton();
    if (!button) return;
    button.onclick = (event) => {
      if (event) event.preventDefault();
      navigateBackOrFallback(target);
    };
  }

  function pushOnboardingLockState() {
    const currentState = (window.history.state && typeof window.history.state === 'object')
      ? window.history.state
      : {};
    window.history.pushState({ ...currentState, onboardingLock: true }, '', window.location.href);
  }

  function installOnboardingBackGuard() {
    if (isProfileEditMode || onboardingBackGuardInstalled) return;
    onboardingBackGuardInstalled = true;
    if (window.__clubOnboardingBackGuardInstalled) return;
    window.__clubOnboardingBackGuardInstalled = true;

    const currentState = (window.history.state && typeof window.history.state === 'object')
      ? window.history.state
      : {};
    window.history.replaceState({ ...currentState, onboardingEntry: true }, '', window.location.href);
    pushOnboardingLockState();
    window.addEventListener('popstate', () => {
      pushOnboardingLockState();
    });
    window.addEventListener('pageshow', (event) => {
      if (!event.persisted) return;
      const state = (window.history.state && typeof window.history.state === 'object')
        ? window.history.state
        : {};
      if (!state.onboardingLock) {
        pushOnboardingLockState();
      }
    });
  }

  function configurePageMode() {
    if (isNameEditMode) {
      setBackTarget(returnTarget);
      if (stickyTitleEl) stickyTitleEl.textContent = 'Edit club name';
      if (heroTitleEl) heroTitleEl.textContent = 'Update your club name';
      if (heroLeadEl) heroLeadEl.textContent = 'Change the club name shown on your club profile.';
      if (panelTitleEl) panelTitleEl.textContent = 'Club name';
      if (panelSubtitleEl) panelSubtitleEl.textContent = 'Save to update your club profile and database.';
      if (panelBadgeEl) panelBadgeEl.textContent = 'Name edit';
      if (nameBlockEl) nameBlockEl.hidden = false;
      if (sportsBlockEl) sportsBlockEl.hidden = true;
      if (displayNameEl) displayNameEl.readOnly = false;
      if (btnSave) btnSave.textContent = 'Save name';
      return;
    }
    if (isCategoryEditMode) {
      setBackTarget(returnTarget);
      if (stickyTitleEl) stickyTitleEl.textContent = 'Edit club categories';
      if (heroTitleEl) heroTitleEl.textContent = 'Update your club categories';
      if (heroLeadEl) heroLeadEl.textContent = 'Choose all sports or categories shown on your club profile.';
      if (panelTitleEl) panelTitleEl.textContent = 'Category selections';
      if (panelSubtitleEl) panelSubtitleEl.textContent = 'Save to update your club profile and database.';
      if (panelBadgeEl) panelBadgeEl.textContent = 'Categories edit';
      if (sportsLabelEl) sportsLabelEl.textContent = 'Club categories';
      if (sportsHintEl) sportsHintEl.textContent = 'Select all categories to show on your club profile.';
      if (nameBlockEl) nameBlockEl.hidden = true;
      if (sportsBlockEl) sportsBlockEl.hidden = false;
      if (displayNameEl) displayNameEl.readOnly = true;
      if (btnSave) btnSave.textContent = 'Save categories';
      return;
    }
    if (stickyTitleEl) stickyTitleEl.textContent = 'Club profile';
    clearBackButton();
    installOnboardingBackGuard();
  }

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

  const saveSelectedClub = (club) => {
    const id = club?.id ?? club?.clubId ?? null;
    if (!id) return;
    const name = club?.name ?? club?.clubName ?? club?.title ?? `Club #${id}`;
    const tags = Array.isArray(club?.tags) ? club.tags : [];
    try { localStorage.setItem('selectedClub', JSON.stringify({ id: String(id), name, tags })); } catch {}
  };

  const pickClubFromMyClubs = (clubs) => {
    const list = Array.isArray(clubs) ? clubs : [];
    if (!list.length) return null;
    const storedId = loadSelectedClubId();
    if (storedId) {
      const hit = list.find((c) => String(c.id ?? c.clubId) === String(storedId));
      if (hit) return hit;
    }
    return list[0];
  };

  const saveCategorySelection = async (profile) => {
    if (!requireClubLogin()) {
      window.location.href = 'login.html#login';
      return null;
    }

    const primaryCategory = String(profile?.sports?.[0] || '').trim();
    if (!primaryCategory) {
      throw new Error('Please choose at least one category.');
    }

    const clubsRes = await authFetch('/api/my/clubs');
    if (!clubsRes.ok) {
      throw new Error('Failed to load your club before updating category.');
    }
    const clubs = await clubsRes.json();
    const pick = pickClubFromMyClubs(clubs);
    const clubId = pick?.id ?? pick?.clubId ?? null;
    if (!clubId) {
      throw new Error('No club found to update.');
    }

    const res = await authFetch(`/api/clubs/${encodeURIComponent(clubId)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        category: primaryCategory,
        tags: profile.sports
      })
    });

    if (!res.ok) {
      const text = await res.text().catch(() => '');
      throw new Error(text || 'Failed to update club categories.');
    }

    const updatedClub = await res.json();
    saveSelectedClub(updatedClub);
    return updatedClub;
  };

  const saveNameSelection = async (profile) => {
    if (!requireClubLogin()) {
      window.location.href = 'login.html#login';
      return null;
    }

    const clubName = String(profile?.displayName || '').trim();
    if (!clubName) {
      throw new Error('Please enter a club name.');
    }

    const clubsRes = await authFetch('/api/my/clubs');
    if (!clubsRes.ok) {
      throw new Error('Failed to load your club before updating name.');
    }
    const clubs = await clubsRes.json();
    const pick = pickClubFromMyClubs(clubs);
    const clubId = pick?.id ?? pick?.clubId ?? null;
    if (!clubId) {
      throw new Error('No club found to update.');
    }

    const res = await authFetch(`/api/clubs/${encodeURIComponent(clubId)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: clubName
      })
    });

    if (!res.ok) {
      const text = await res.text().catch(() => '');
      throw new Error(text || 'Failed to update club name.');
    }

    const updatedClub = await res.json();
    saveSelectedClub(updatedClub);
    return updatedClub;
  };

  const hydrateFromBackend = async () => {
    if (!requireClubLogin()) {
      if (isProfileEditMode) setBackTarget('home.html');
      return;
    }

    if (statusEl) statusEl.textContent = 'Loading saved club profile...';
    try {
      const res = await authFetch('/api/my/clubs');
      if (!res.ok) return;
      const clubs = await res.json();
      const pick = pickClubFromMyClubs(clubs);
      if (!pick) {
        if (isNameEditMode) setBackTarget('home.html');
        return;
      }

      const clubId = pick.id ?? pick.clubId ?? null;
      const clubName = pick.name || pick.clubName || `Club #${clubId}`;
      saveSelectedClub({ id: clubId, name: clubName });
      if (!clubId) return;

      const detailRes = await authFetch(`/api/clubs/${encodeURIComponent(clubId)}`);
      if (!detailRes.ok) return;
      const detail = await detailRes.json();
      saveSelectedClub(detail);

      const name = String(detail?.name ?? detail?.clubName ?? '').trim();
      const tags = Array.isArray(detail?.tags) ? detail.tags : [];
      const category = String(detail?.category ?? (tags[0] || '') ?? '').trim();

      const current = loadProfile() || { displayName: '', sports: [] };
      const next = {
        displayName: name || current.displayName || '',
        sports: Array.isArray(current.sports) ? current.sports.slice() : []
      };
      if (tags.length) {
        next.sports = uniqueList(tags.concat(next.sports || []));
      } else if (category) {
        next.sports = uniqueList([category].concat(next.sports || []));
      }
      saveProfileToStorage(next);
      buildGrid();
    } catch (e) {
      console.error(e);
    } finally {
      if (statusEl) statusEl.textContent = '';
    }
  };

  function matchEmoji(value) {
    const normalized = normalizeKey(value);
    if (!normalized) return '';
    if (emojiByName[normalized]) return emojiByName[normalized];
    for (const entry of emojiAliases) {
      for (const key of entry.keys) {
        if (key && normalized.includes(key)) {
          return entry.emoji;
        }
      }
    }
    return '';
  }

  function makeChip(name, idx){
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'sport-chip';
    btn.setAttribute('role','listitem');
    btn.setAttribute('aria-pressed','false');
    btn.dataset.sport = name;
    btn.style.setProperty('--chip-index', idx);

    const icon = document.createElement('div');
    icon.className = 'icon';
    icon.textContent = emojiMap[name] || '';
    icon.setAttribute('aria-hidden', 'true');

    const span = document.createElement('div');
    span.className = 'label';
    span.textContent = name;

    btn.setAttribute('aria-label', name);
    btn.appendChild(icon);
    btn.appendChild(span);

    btn.addEventListener('click', () => {
      const selected = !btn.classList.contains('selected');
      btn.classList.toggle('selected', selected);
      btn.setAttribute('aria-pressed', selected ? 'true' : 'false');
      updateSaveState();
    });

    return btn;
  }

  function syncRemoveButtons() {
    if (!customList) return;
    const rows = Array.from(customList.querySelectorAll('.custom-type-row'));
    const disable = rows.length <= 1;
    rows.forEach((row) => {
      const btn = row.querySelector('.custom-type-remove');
      if (btn) btn.disabled = disable;
    });
  }

  function addCustomRow(value = '', selected = false) {
    if (!customList) return;
    const row = document.createElement('div');
    row.className = 'sport-chip custom-type-row';
    if (selected && value.trim()) {
      row.classList.add('selected');
    }
    row.style.setProperty('--chip-index', customList.children.length);

    const removeBtn = document.createElement('button');
    removeBtn.type = 'button';
    removeBtn.className = 'custom-type-remove';
    removeBtn.setAttribute('aria-label', 'Remove custom type');
    removeBtn.textContent = 'x';

    const icon = document.createElement('span');
    icon.className = 'custom-type-icon';
    icon.textContent = matchEmoji(value);

    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'custom-type-input';
    input.setAttribute('aria-label', 'Custom club type');
    input.value = value;

    input.addEventListener('input', () => {
      const trimmed = input.value.trim();
      icon.textContent = matchEmoji(trimmed);
      if (!trimmed) {
        row.classList.remove('selected');
      }
      updateSaveState();
    });

    input.addEventListener('blur', () => {
      if (input.value.trim() === '') {
        row.remove();
        updateSaveState();
        syncRemoveButtons();
      }
    });

    row.addEventListener('click', (evt) => {
      if (evt.target === removeBtn) return;
      if (evt.target === input) return;
      const trimmed = input.value.trim();
      if (!trimmed) {
        input.focus();
        input.select();
        return;
      }
      const selected = !row.classList.contains('selected');
      row.classList.toggle('selected', selected);
      updateSaveState();
    });

    removeBtn.addEventListener('click', () => {
      const total = customList ? customList.querySelectorAll('.custom-type-row').length : 0;
      if (total <= 1) {
        syncRemoveButtons();
        return;
      }
      row.remove();
      updateSaveState();
      syncRemoveButtons();
    });

    row.appendChild(removeBtn);
    row.appendChild(icon);
    row.appendChild(input);
    customList.appendChild(row);
    syncRemoveButtons();
  }

  function getCustomValues() {
    if (!customList) return [];
    return Array.from(customList.querySelectorAll('.custom-type-row'))
      .filter((row) => row.classList.contains('selected'))
      .map((row) => row.querySelector('.custom-type-input').value.trim())
      .filter(Boolean);
  }

  function uniqueList(values) {
    const seen = new Set();
    const out = [];
    values.forEach((value) => {
      const key = normalizeKey(value);
      if (!key || seen.has(key)) return;
      seen.add(key);
      out.push(value.trim());
    });
    return out;
  }

  function syncCustomInputs(profile) {
    if (!customList) return;
    customList.innerHTML = '';
    const baseSet = new Set(sports.map((item) => normalizeKey(item)));
    const saved = profile && Array.isArray(profile.sports) ? profile.sports : [];
    const customValues = saved.filter((item) => !baseSet.has(normalizeKey(item)));
    const valuesToRender = customValues;
    valuesToRender.forEach((value) => {
      addCustomRow(value, true);
    });
    if (!valuesToRender.length) {
      addCustomRow('');
    }
  }

  function buildGrid(){
    grid.innerHTML = '';
    sports.forEach((s, i) => grid.appendChild(makeChip(s, i)));

    const profile = loadProfile();
    if (profile) {
      if (profile.displayName) displayNameEl.value = profile.displayName;
      if (Array.isArray(profile.sports)) {
        Array.from(grid.children).forEach((ch) => {
          const chipKey = normalizeKey(ch.dataset.sport);
          const matched = profile.sports.some((item) => normalizeKey(item) === chipKey);
          if (matched) {
            ch.classList.add('selected');
            ch.setAttribute('aria-pressed','true');
          }
        });
      }
    }

    syncCustomInputs(profile);
    updateSaveState();
  }

  function selectedSports(){
    const base = Array.from(grid.children)
      .filter((ch) => ch.classList.contains('selected'))
      .map((ch) => ch.dataset.sport);
    const custom = getCustomValues();
    return uniqueList(base.concat(custom));
  }

  function updateSaveState(){
    const nameOk = (displayNameEl.value || '').trim().length > 0;
    const selected = selectedSports();
    const hasSport = selected.length > 0;
    if (btnSave) {
      btnSave.disabled = isCategoryEditMode ? !hasSport : isNameEditMode ? !nameOk : !(nameOk && hasSport);
    }

    const parts = [];
    if (!isCategoryEditMode && nameOk) parts.push(`Club name: ${displayNameEl.value.trim()}`);
    if (!isNameEditMode && hasSport) parts.push(`Selected ${selected.length} ${isCategoryEditMode ? 'categories' : 'sport(s)'}`);
    if (statusEl) statusEl.textContent = parts.length ? parts.join(' - ') : '';
  }

  async function saveAndContinue(){
    const currentProfile = loadProfile() || { displayName: '', sports: [] };
    const profile = {
      displayName: (displayNameEl.value || '').trim(),
      sports: isNameEditMode
        ? (Array.isArray(currentProfile.sports) ? currentProfile.sports.slice() : [])
        : selectedSports()
    };

    if ((isNameEditMode || !isCategoryEditMode) && !profile.displayName) {
      if (statusEl) statusEl.textContent = 'Please enter a club name.';
      return;
    }
    if (!profile.sports.length) {
      if (statusEl) statusEl.textContent = isCategoryEditMode
        ? 'Please choose at least one category.'
        : 'Please choose at least one club type.';
      return;
    }

    if (btnSave) btnSave.disabled = true;
    try {
      saveProfileToStorage(profile);
      if (isNameEditMode) {
        if (statusEl) statusEl.textContent = 'Saving club name...';
        await saveNameSelection(profile);
        if (statusEl) statusEl.textContent = 'Club name saved - returning...';
        window.location.href = returnTarget;
        return;
      }
      if (isCategoryEditMode) {
        if (statusEl) statusEl.textContent = 'Saving category...';
        await saveCategorySelection(profile);
        if (statusEl) statusEl.textContent = 'Category saved - returning...';
        window.location.href = returnTarget;
        return;
      }

      // Keep onboarding data as a local draft. The public club record is finalized on the last step.
      if (statusEl) statusEl.textContent = 'Draft saved - continuing...';
      window.location.href = 'onboarding-location.html';
    } catch (e){
      if (statusEl) statusEl.textContent = e?.message || 'Failed to save. Please try again.';
      console.error(e);
    } finally {
      updateSaveState();
    }
  }

  function loadProfile(){
    try {
      const saved = readOnboardingDraft();
      if (saved && saved.profile) return saved.profile;
      return null;
    } catch (e){
      return null;
    }
  }

  function saveProfileToStorage(profile){
    try {
      const saved = readOnboardingDraft();
      saved.profile = profile;
      writeOnboardingDraft(saved);
    } catch (e) { /* ignore */ }
    try{
      const loggedUser = JSON.parse(localStorage.getItem('loggedUser') || 'null') || {};
      if (profile.displayName) loggedUser.name = profile.displayName;
      localStorage.setItem('loggedUser', JSON.stringify(loggedUser));
      const userObj = JSON.parse(localStorage.getItem('user') || 'null') || {};
      if (profile.displayName) userObj.fullName = profile.displayName;
      localStorage.setItem('user', JSON.stringify(userObj));
    }catch(e){ /* ignore */ }
  }

  displayNameEl.addEventListener('input', updateSaveState);
  if (btnSave) btnSave.addEventListener('click', saveAndContinue);
  if (addCustomBtn) {
    addCustomBtn.addEventListener('click', () => {
      addCustomRow('');
      const inputs = customList ? customList.querySelectorAll('.custom-type-input') : [];
      const last = inputs.length ? inputs[inputs.length - 1] : null;
      if (last) {
        last.focus();
        last.select();
      }
    });
  }

  configurePageMode();
  buildGrid();
  hydrateFromBackend();
})();
