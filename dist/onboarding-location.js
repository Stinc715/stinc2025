document.addEventListener('DOMContentLoaded', () => {
  const btnBack = document.getElementById('btnBack');
  const btnFinish = document.getElementById('btnFinish');
  const venueList = document.getElementById('venueList');
  const addVenueBtn = document.getElementById('addVenue');
  const locationInput = document.getElementById('clubLocationInput');

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

  const getSavedLocation = () => {
    const profile = loadClubProfile();
    const location = profile.location || {};
    return {
      city: location.city || '',
      venues: Array.isArray(location.venues) ? location.venues : []
    };
  };

  const collectVenues = () => {
    if (!venueList) return [];
    const cards = Array.from(venueList.querySelectorAll('.venue-card'));
    return cards.map((card) => {
      const name = card.querySelector('.venue-name')?.value?.trim() || '';
      const detail = card.querySelector('.venue-detail')?.value?.trim() || '';
      return { name, detail };
    }).filter((venue) => venue.name || venue.detail);
  };

  const saveLocation = () => {
    const profile = loadClubProfile();
    profile.location = {
      city: locationInput?.value?.trim() || '',
      venues: collectVenues()
    };
    saveClubProfile(profile);
  };

  const addVenueCard = (venue = {}, index = 1) => {
    if (!venueList) return;

    const card = document.createElement('div');
    card.className = 'sport-chip venue-card';

    const icon = document.createElement('div');
    icon.className = 'icon';
    icon.textContent = String(index);

    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    nameInput.className = 'venue-input venue-name';
    nameInput.placeholder = `Venue ${index} name`;
    nameInput.value = venue.name || '';

    const detailInput = document.createElement('input');
    detailInput.type = 'text';
    detailInput.className = 'venue-input venue-detail';
    detailInput.placeholder = 'Details: indoor/outdoor, surface, etc.';
    detailInput.value = venue.detail || '';

    nameInput.addEventListener('input', saveLocation);
    detailInput.addEventListener('input', saveLocation);

    card.appendChild(icon);
    card.appendChild(nameInput);
    card.appendChild(detailInput);
    venueList.appendChild(card);
  };

  const syncVenueList = (venues) => {
    if (!venueList) return;
    venueList.innerHTML = '';
    const list = Array.isArray(venues) && venues.length ? venues : [{}];
    list.forEach((venue, idx) => addVenueCard(venue, idx + 1));
  };

  if (locationInput) {
    const saved = getSavedLocation();
    locationInput.value = saved.city;
    locationInput.addEventListener('input', saveLocation);
    syncVenueList(saved.venues);
  }

  addVenueBtn?.addEventListener('click', () => {
    const nextIndex = venueList ? venueList.querySelectorAll('.venue-card').length + 1 : 1;
    addVenueCard({}, nextIndex);
    saveLocation();
  });

  btnBack?.addEventListener('click', () => {
    saveLocation();
    window.location.href = 'onboarding.html';
  });

  btnFinish?.addEventListener('click', () => {
    saveLocation();
    window.location.href = 'onboarding-promo.html';
  });
});
