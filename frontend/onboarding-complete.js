document.addEventListener('DOMContentLoaded', () => {
  const clubNameEl = document.getElementById('completeClubName');
  const goBtn = document.getElementById('completeGoBtn');
  const DASHBOARD_GUIDE_PENDING_KEY = 'clubPortal.pendingDashboardGuide';

  let clubName = '';
  try {
    clubName = String(JSON.parse(localStorage.getItem('selectedClub') || 'null')?.name || '').trim();
  } catch {
    clubName = '';
  }

  if (clubName && clubNameEl) {
    clubNameEl.textContent = clubName;
    clubNameEl.hidden = false;
  }

  goBtn?.addEventListener('click', (event) => {
    event.preventDefault();
    try {
      sessionStorage.setItem(DASHBOARD_GUIDE_PENDING_KEY, '1');
    } catch {}
    window.location.replace('club home.html');
  });
});
