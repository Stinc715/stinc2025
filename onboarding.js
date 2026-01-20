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

  if (!grid || !displayNameEl) {
    return;
  }

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
      const selected = btn.classList.toggle('selected');
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
      row.classList.toggle('selected');
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
    customValues.forEach((value) => addCustomRow(value, true));
    if (!customValues.length) {
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
        const baseSet = new Set(sports.map((item) => normalizeKey(item)));
        Array.from(grid.children).forEach((ch) => {
          const matched = baseSet.has(normalizeKey(ch.dataset.sport))
            && profile.sports.some((item) => normalizeKey(item) === normalizeKey(ch.dataset.sport));
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
    if (btnSave) btnSave.disabled = !nameOk && !hasSport;

    const parts = [];
    if (nameOk) parts.push(`Club name: ${displayNameEl.value.trim()}`);
    if (hasSport) parts.push(`Selected ${selected.length} sport(s)`);
    if (statusEl) statusEl.textContent = parts.length ? parts.join(' - ') : '';
  }

  function saveAndContinue(){
    const profile = {
      displayName: (displayNameEl.value || '').trim(),
      sports: selectedSports()
    };
    try {
      saveProfileToStorage(profile);
      if (statusEl) statusEl.textContent = 'Saved - continuing...';
      setTimeout(() => {
        window.location.href = 'onboarding-location.html';
      }, 700);
    } catch (e){
      if (statusEl) statusEl.textContent = 'Failed to save - please check browser settings.';
      console.error(e);
    }
  }

  function skipAndContinue(){
    try {
      localStorage.setItem('profile', JSON.stringify({displayName:'',sports:[]}));
    } catch (e) { /* ignore */ }
    window.location.href = 'home.html';
  }

  function loadProfile(){
    try {
      return JSON.parse(localStorage.getItem('profile') || 'null');
    } catch (e){
      return null;
    }
  }

  function saveProfileToStorage(profile){
    localStorage.setItem('profile', JSON.stringify(profile));
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

  buildGrid();
})();
