document.addEventListener('DOMContentLoaded', () => {
  const btnBack = document.getElementById('btnBack');
  const btnFinish = document.getElementById('btnFinish');
  const venueList = document.getElementById('venueList');
  const addVenueBtn = document.getElementById('addVenue');
  let venueCount = 0;

  const addVenueCard = () => {
    if (!venueList) return;
    venueCount += 1;

    const card = document.createElement('div');
    card.className = 'sport-chip venue-card';

    const icon = document.createElement('div');
    icon.className = 'icon';
    icon.textContent = String(venueCount);

    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    nameInput.className = 'venue-input venue-name';
    nameInput.placeholder = `Venue ${venueCount} name (placeholder)`;

    const detailInput = document.createElement('input');
    detailInput.type = 'text';
    detailInput.className = 'venue-input venue-detail';
    detailInput.placeholder = 'Details: indoor/outdoor, surface, etc.';

    card.appendChild(icon);
    card.appendChild(nameInput);
    card.appendChild(detailInput);
    venueList.appendChild(card);
  };

  if (venueList) {
    addVenueCard();
  }

  addVenueBtn?.addEventListener('click', () => {
    addVenueCard();
  });

  btnBack?.addEventListener('click', () => {
    window.location.href = 'onboarding.html';
  });

  btnFinish?.addEventListener('click', () => {
    window.location.href = 'club home.html';
  });
});
