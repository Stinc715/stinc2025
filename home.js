// home.js

document.addEventListener('DOMContentLoaded', () => {
  // 1) DOM 引用
  const cardsEl = document.getElementById('cards');
  const emptyEl = document.getElementById('empty');
  const kwEl = document.getElementById('kw');
  const kwOverlayEl = document.getElementById('kwOverlay');
  const userArea = document.getElementById('userArea');
  const yEl = document.getElementById('y');
  const searchOverlay = document.getElementById('searchOverlay');
  const globalSearchBtn = document.getElementById('globalSearchBtn');
  const closeSearchBtn = document.getElementById('closeSearchBtn');
  const searchFormOverlay = document.getElementById('searchFormOverlay');
  const bodyEl = document.body;
  if (yEl) yEl.textContent = new Date().getFullYear();

  // 2) 登录区域（与首页右上角的两个按钮配对）
  let logged = null;
  try {
    logged = JSON.parse(localStorage.getItem('loggedUser') || 'null');
  } catch (e) {
    logged = null;
    localStorage.removeItem('loggedUser');
  }

  if (logged) {
    const isClub = (logged && logged.type) === 'club';
    const targetHref = isClub ? 'club.html' : 'user.html';
    const myLabel = isClub ? 'Club' : 'My';
    userArea.innerHTML = `
      <div class="user-menu">
        <button id="btnMy" class="btn blue" type="button" aria-haspopup="true" aria-expanded="false">${myLabel}</button>
        <div class="menu" id="myMenu" role="menu">
          <button type="button" id="btnDetails" role="menuitem">Details</button>
          <button type="button" id="btnLogout" role="menuitem">Logout</button>
        </div>
      </div>
    `;
    const myBtn = document.getElementById('btnMy');
    const menuEl = document.getElementById('myMenu');
    const detailsBtn = document.getElementById('btnDetails');
    const logoutBtn = document.getElementById('btnLogout');

    if (detailsBtn) {
      detailsBtn.addEventListener('click', () => {
        window.location.href = targetHref;
      });
    }

    if (logoutBtn) {
      logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('loggedUser');
        location.reload();
      });
    }

    if (myBtn && menuEl) {
      let closeTimer = null;
      const wrapper = userArea ? userArea.querySelector('.user-menu') : null;
      const CLOSE_DELAY = 400;

      const clearCloseTimer = () => {
        if (closeTimer) {
          clearTimeout(closeTimer);
          closeTimer = null;
        }
      };

      const openMenu = () => {
        clearCloseTimer();
        menuEl.classList.add('open');
        myBtn.setAttribute('aria-expanded', 'true');
      };

      const closeMenu = () => {
        clearCloseTimer();
        menuEl.classList.remove('open');
        myBtn.setAttribute('aria-expanded', 'false');
      };

      const scheduleClose = (delay = CLOSE_DELAY) => {
        clearCloseTimer();
        closeTimer = window.setTimeout(() => {
          menuEl.classList.remove('open');
          myBtn.setAttribute('aria-expanded', 'false');
          closeTimer = null;
        }, delay);
      };

      myBtn.addEventListener('click', (evt) => {
        evt.preventDefault();
        if (menuEl.classList.contains('open')) {
          closeMenu();
        } else {
          openMenu();
        }
      });

      myBtn.addEventListener('focus', openMenu);
      myBtn.addEventListener('mouseenter', openMenu);
      myBtn.addEventListener('keydown', (evt) => {
        if (evt.key === 'Escape') {
          closeMenu();
          myBtn.blur();
        }
      });

      menuEl.addEventListener('focusin', openMenu);
      menuEl.addEventListener('mouseenter', openMenu);
      menuEl.addEventListener('mouseleave', () => scheduleClose());
      menuEl.addEventListener('keydown', (evt) => {
        if (evt.key === 'Escape') {
          closeMenu();
          myBtn.focus();
        }
      });

      if (wrapper) {
        wrapper.addEventListener('mouseenter', openMenu);
        wrapper.addEventListener('mouseleave', () => scheduleClose());
      }

      document.addEventListener('click', (evt) => {
        if (wrapper && wrapper.contains(evt.target)) {
          return;
        }
        closeMenu();
      });
    }
  } else {
    const btnLogin = document.getElementById('btnLogin');
    const btnRegister = document.getElementById('btnRegister');
    if (btnLogin) btnLogin.addEventListener('click', () => window.location.href = 'login.html#login');
    if (btnRegister) btnRegister.addEventListener('click', () => window.location.href = 'login.html#register');
  }

  // 3) 数据来源
  let CLUBS = [];
  let currentQuery = '';

  // 3.1 本地备用数据
  const FALLBACK_CLUBS = [
    { id:'badminton', name:'Badminton Club', tags:['badminton'], desc:'Suitable for all levels, regular evening training and friendlies.' },
    { id:'basketball', name:'Basketball Club', tags:['basketball'], desc:'Varsity training + amateur matches, new members welcome.' },
    { id:'football', name:'Football Club', tags:['football'], desc:'Weekly matches and local tournaments, join our squad!' },
    { id:'yoga', name:'Yoga Club', tags:['yoga'], desc:'Relax and strengthen your body and mind, small group sessions.' },
    { id:'tabletennis', name:'Table Tennis Club', tags:['table tennis'], desc:'Weekend open training, tables and equipment provided.' },
    { id:'swimming', name:'Swimming Club', tags:['swimming'], desc:'Morning and evening sessions available, professional coach guidance.' },
    { id:'running', name:'Running Club', tags:['running'], desc:'Morning jogs, half marathons, and social running events.' },
    { id:'volleyball', name:'Volleyball Club', tags:['volleyball'], desc:'Indoor and beach volleyball activities for all skill levels.' },
    { id:'cycling', name:'Cycling Club', tags:['cycling'], desc:'Weekend city rides and countryside cycling challenges.' },
    { id:'tennis', name:'Tennis Club', tags:['tennis'], desc:'Book your court and join tournaments or casual games.' },
    // 这里可以继续添加更多俱乐部数据
  ];

  // 3.2 尝试从后端获取数据
  async function loadClubs() {
    try {
      const res = await fetch('/api/clubs');
      if (!res.ok) throw new Error('network');
      const data = await res.json();

      // 确保返回的每条数据至少包含name字段
      CLUBS = (data || []).map((c, idx) => {
        return {
          id: c.id || `club-${idx+1}`,
          name: c.name || `Club ${idx+1}`,
          desc: c.desc || 'No description.',
          tags: Array.isArray(c.tags) && c.tags.length ? c.tags : ['sport']
        };
      });
    } catch (err) {
      // 接口不可用或报错时使用本地数据
      CLUBS = [...FALLBACK_CLUBS];
    }

    render(CLUBS);
  }

  // 4) 工具函数
  function normalize(s) {
    return (s || '').toLowerCase().trim();
  }

  function slug(s) {
    return (s || '').toLowerCase().replace(/[^a-z0-9]+/g, '');
  }

  function getSportType(c) {
    // 优先使用tags
    if (c.tags && c.tags.length) return c.tags[0];
    // 从名称推断运动类型
    const n = (c.name || '').toLowerCase();
    if (n.includes('badminton')) return 'badminton';
    if (n.includes('basketball')) return 'basketball';
    if (n.includes('football')) return 'football';
    if (n.includes('yoga')) return 'yoga';
    if (n.includes('tennis')) return 'tennis';
    return 'sport';
  }

  function clubCard(c, idx){
    const sport = getSportType(c);
    return `
      <div class="card club" data-id="${c.id}">
        <div class="thumb">Cover</div>
        <h3>${c.name || `Club ${idx+1}`}</h3>
        <p class="muted">${c.desc || ''}</p >
        <div class="chips"><span class="chip"># ${sport}</span></div>
        <div class="toolbar">
          <button class="btn">Join Now</button>
          <button class="btn ghost">View Details</button>
        </div>
      </div>
    `;
  }

  function render(list){
    if (!cardsEl) return;
    if (!list.length) {
      cardsEl.innerHTML = '';
      if (emptyEl) emptyEl.style.display = 'block';
    } else {
      if (emptyEl) emptyEl.style.display = 'none';
      cardsEl.innerHTML = list.map((c,i) => clubCard(c,i)).join('');
    }
  }

  function synchroniseInputs(value) {
    if (kwEl && typeof kwEl.value === 'string' && kwEl.value !== value) {
      kwEl.value = value;
    }
    if (kwOverlayEl && typeof kwOverlayEl.value === 'string' && kwOverlayEl.value !== value) {
      kwOverlayEl.value = value;
    }
  }

  // 搜索功能
  function filterResults(rawInput) {
    const hasRawInput = typeof rawInput === 'string';
    const qRaw = hasRawInput ? rawInput : (
      (kwEl && typeof kwEl.value === 'string') ? kwEl.value :
      (kwOverlayEl && typeof kwOverlayEl.value === 'string') ? kwOverlayEl.value : ''
    );
    currentQuery = qRaw;

    const q = normalize(qRaw);
    const qSlug = slug(qRaw);
    if (!q) { 
      currentQuery = '';
      synchroniseInputs('');
      render(CLUBS); 
      return; 
    }

    synchroniseInputs(currentQuery);

    // 支持纯数字查询，如 "1" -> Club 1
    const numberOnlyMatch = qSlug.match(/^[0-9]+$/);
    const numberOnly = numberOnlyMatch ? parseInt(numberOnlyMatch[0],10) : null;

    const list = CLUBS.filter((c, idx) => {
      const nameMatch = normalize(c.name).includes(q);
      const descMatch = normalize(c.desc).includes(q);
      const tagMatch  = (c.tags||[]).some(t => normalize(t).includes(q));

      // 运动类型匹配（从名称提取）
      const sport = getSportType(c);
      const sportMatch = normalize(sport).includes(q);

      // 支持多种格式：club1 / club 1 / club-1
      const displaySlug = `club${idx+1}`;
      const numberMatchBySlug   = (qSlug === displaySlug);
      const numberMatchByDigits = (numberOnly !== null) && (idx + 1 === numberOnly);

      return nameMatch || descMatch || tagMatch || sportMatch || numberMatchBySlug || numberMatchByDigits;
    });

    render(list);
  }

  // 6) 事件绑定
  const btnSearchMain = document.getElementById('btnSearch');
  const btnClearMain = document.getElementById('btnClear');
  const btnSearchOverlay = document.getElementById('btnSearchOverlay');
  const btnClearOverlay = document.getElementById('btnClearOverlay');
  let lastFocused = null;

  function openSearchOverlay(options = {}) {
    if (!searchOverlay) return;

    const { focus = true, preset, selectAll } = options;
    const overlayAlreadyOpen = searchOverlay.classList.contains('open');
    const shouldSelectAll = typeof selectAll === 'boolean' ? selectAll : (typeof preset === 'undefined');

    if (typeof preset === 'string') {
      currentQuery = preset;
      synchroniseInputs(currentQuery);
    } else if (!overlayAlreadyOpen) {
      const baseValue = (kwEl && typeof kwEl.value === 'string') ? kwEl.value : currentQuery || '';
      currentQuery = baseValue;
      synchroniseInputs(currentQuery || '');
    } else if (overlayAlreadyOpen) {
      synchroniseInputs(currentQuery || '');
    }

    const focusInput = () => {
      if (!focus || !kwOverlayEl) return;
      kwOverlayEl.focus();
      if (shouldSelectAll && typeof kwOverlayEl.select === 'function') {
        kwOverlayEl.select();
      } else if (typeof kwOverlayEl.setSelectionRange === 'function') {
        const len = kwOverlayEl.value.length;
        kwOverlayEl.setSelectionRange(len, len);
      }
    };

    if (overlayAlreadyOpen) {
      window.requestAnimationFrame(focusInput);
      return;
    }

    lastFocused = document.activeElement;
    searchOverlay.classList.add('open');
    searchOverlay.setAttribute('aria-hidden', 'false');
    if (bodyEl) bodyEl.classList.add('no-scroll');

    window.requestAnimationFrame(focusInput);
  }

  function closeSearchOverlay({ restoreFocus = true } = {}) {
    if (!searchOverlay || !searchOverlay.classList.contains('open')) return;

    searchOverlay.classList.remove('open');
    searchOverlay.setAttribute('aria-hidden', 'true');
    if (bodyEl) bodyEl.classList.remove('no-scroll');

    if (restoreFocus && lastFocused && typeof lastFocused.focus === 'function') {
      lastFocused.focus();
    }
    lastFocused = null;
  }

  const isEditableElement = (el) => {
    if (!el) return false;
    if (el.isContentEditable) return true;
    const tag = el.tagName;
    if (!tag) return false;
    return tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT';
  };

  if (globalSearchBtn) {
    globalSearchBtn.addEventListener('click', () => openSearchOverlay());
  }

  if (closeSearchBtn) {
    closeSearchBtn.addEventListener('click', () => closeSearchOverlay());
  }

  if (searchOverlay) {
    searchOverlay.addEventListener('click', (evt) => {
      if (evt.target === searchOverlay) {
        closeSearchOverlay();
      }
    });
  }

  if (searchFormOverlay) {
    searchFormOverlay.addEventListener('submit', (evt) => {
      evt.preventDefault();
      const value = kwOverlayEl ? kwOverlayEl.value : '';
      filterResults(value);
      closeSearchOverlay();
    });
  }

  if (btnSearchOverlay && !searchFormOverlay) {
    btnSearchOverlay.addEventListener('click', (evt) => {
      evt.preventDefault();
      const value = kwOverlayEl ? kwOverlayEl.value : '';
      filterResults(value);
      closeSearchOverlay();
    });
  }

  if (btnSearchMain) {
    btnSearchMain.addEventListener('click', (evt) => {
      evt.preventDefault();
      const value = kwEl ? kwEl.value : '';
      filterResults(value);
    });
  }

  if (btnClearMain) {
    btnClearMain.addEventListener('click', () => { 
      currentQuery = '';
      synchroniseInputs(''); 
      render(CLUBS); 
      if (kwEl) kwEl.focus(); 
    });
  }

  if (btnClearOverlay) {
    btnClearOverlay.addEventListener('click', () => {
      currentQuery = '';
      synchroniseInputs('');
      render(CLUBS);
      if (kwOverlayEl) kwOverlayEl.focus();
    });
  }

  if (kwEl) {
    kwEl.addEventListener('keydown', (evt) => {
      if (evt.key === 'Enter') {
        evt.preventDefault();
        filterResults(kwEl.value);
      } else if (evt.key === 'Escape') {
        evt.preventDefault();
        kwEl.blur();
      }
    });
  }

  if (kwOverlayEl) {
    kwOverlayEl.addEventListener('keydown', (evt) => {
      if (evt.key === 'Escape') {
        evt.preventDefault();
        closeSearchOverlay();
      }
    });
  }

  document.addEventListener('keydown', (evt) => {
    const overlayOpen = searchOverlay && searchOverlay.classList.contains('open');

    if (evt.key === 'Escape' && overlayOpen) {
      evt.preventDefault();
      closeSearchOverlay();
      return;
    }

    if (isEditableElement(evt.target)) return;

    if ((evt.key === 'k' || evt.key === 'K') && (evt.ctrlKey || evt.metaKey)) {
      evt.preventDefault();
      openSearchOverlay();
      return;
    }

    if (evt.key === '/' && !evt.ctrlKey && !evt.metaKey && !evt.altKey) {
      evt.preventDefault();
      openSearchOverlay();
      return;
    }

    const isPrintable = evt.key.length === 1 && !evt.ctrlKey && !evt.metaKey && !evt.altKey && !evt.repeat && /\S/.test(evt.key);
    if (!overlayOpen && isPrintable) {
      evt.preventDefault();
      openSearchOverlay({ preset: evt.key, selectAll: false });
    }
  });

  // 7) 初始化
  loadClubs();
});
