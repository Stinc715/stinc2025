document.addEventListener('DOMContentLoaded', () => {
  const btnBack = document.getElementById('btnBack');
  const btnFinish = document.getElementById('btnFinish');
  const locationInput = document.getElementById('clubLocationInput');
  const mapCanvasEl = document.getElementById('clubMapCanvas');
  const mapStatusEl = document.getElementById('clubMapStatus');

  const ONBOARDING_DRAFT_STORAGE_KEY = 'clubPortal.onboardingDraft';
  const GOOGLE_MAPS_API_KEY_STORAGE_KEY = 'clubPortal.googleMapsApiKey';
  const DEFAULT_MAP_CENTER = Object.freeze({ lat: 51.5074, lng: -0.1278 });

  let mapApiLoadPromise = null;
  let mapInstance = null;
  let mapMarker = null;
  let mapGeocoder = null;
  let mapAutocomplete = null;
  let serverGoogleMapsApiKey = '';
  let serverGoogleMapsApiKeyLoaded = false;
  let suppressLocationFieldSync = false;
  let savedLocation = {
    address: '',
    placeId: '',
    lat: null,
    lng: null
  };

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

  const loadClubProfile = () => {
    try {
      return readOnboardingDraft();
    } catch {
      return {};
    }
  };

  const saveClubProfile = (profile) => {
    try {
      writeOnboardingDraft(profile);
    } catch {
      // Ignore storage errors.
    }
  };

  const normalizeCoordinate = (value) => {
    const num = Number(value);
    return Number.isFinite(num) ? num : null;
  };

  const setMapStatus = (text, kind = '') => {
    if (!mapStatusEl) return;
    const normalized = String(text || '').trim();
    mapStatusEl.textContent = normalized;
    mapStatusEl.className = `status ${kind}`.trim();
    mapStatusEl.hidden = !normalized;
  };

  const updateNavigationState = () => {
    const hasAddress = String(locationInput?.value || savedLocation.address || '').trim().length > 0;
    if (btnFinish) btnFinish.disabled = !hasAddress;
  };

  const readSavedLocation = () => {
    const profile = loadClubProfile();
    const location = profile.location || {};
    return {
      address: String(location.formattedAddress || location.address || location.city || '').trim(),
      placeId: String(location.placeId || '').trim(),
      lat: normalizeCoordinate(location.lat),
      lng: normalizeCoordinate(location.lng)
    };
  };

  const setSavedLocation = ({ address = '', placeId = '', lat = null, lng = null } = {}) => {
    savedLocation = {
      address: String(address || '').trim(),
      placeId: String(placeId || '').trim(),
      lat: normalizeCoordinate(lat),
      lng: normalizeCoordinate(lng)
    };
  };

  const clearSavedLocationMetadata = (address = '') => {
    setSavedLocation({ address, placeId: '', lat: null, lng: null });
  };

  const persistLocation = () => {
    const profile = loadClubProfile();
    const address = String(locationInput?.value || '').trim();
    const nextAddress = address || savedLocation.address;
    profile.location = {
      ...(profile.location || {}),
      city: nextAddress,
      address: nextAddress,
      formattedAddress: nextAddress,
      placeId: savedLocation.placeId || '',
      lat: savedLocation.lat,
      lng: savedLocation.lng
    };
    saveClubProfile(profile);
  };

  const setLocationInputs = (address) => {
    const normalized = String(address || '').trim();
    suppressLocationFieldSync = true;
    if (locationInput) locationInput.value = normalized;
    suppressLocationFieldSync = false;
  };

  const getStoredGoogleMapsApiKey = () => {
    const query = new URLSearchParams(window.location.search);
    const fromQuery = String(query.get('gmapsKey') || '').trim();
    if (fromQuery) {
      try {
        localStorage.setItem(GOOGLE_MAPS_API_KEY_STORAGE_KEY, fromQuery);
      } catch {}
    }
    const fromGlobal = String(window.GOOGLE_MAPS_API_KEY || window.__GOOGLE_MAPS_API_KEY__ || '').trim();
    if (fromGlobal) return fromGlobal;
    if (String(serverGoogleMapsApiKey || '').trim()) return String(serverGoogleMapsApiKey || '').trim();
    try {
      return String(localStorage.getItem(GOOGLE_MAPS_API_KEY_STORAGE_KEY) || '').trim();
    } catch {
      return '';
    }
  };

  const loadServerGoogleMapsApiKey = async () => {
    if (serverGoogleMapsApiKeyLoaded) return String(serverGoogleMapsApiKey || '').trim();
    serverGoogleMapsApiKeyLoaded = true;
    try {
      const res = await fetch('/api/public/config', { credentials: 'same-origin' });
      if (!res.ok) {
        return '';
      }
      const data = await res.json();
      const key = String(data?.googleMapsApiKey || '').trim();
      serverGoogleMapsApiKey = key;
      if (key) {
        try {
          localStorage.setItem(GOOGLE_MAPS_API_KEY_STORAGE_KEY, key);
        } catch {}
      }
      return key;
    } catch (err) {
      console.warn('[onboarding-location] public config load failed', err);
      return '';
    }
  };

  const loadGoogleMapsApi = async () => {
    if (window.google?.maps?.Map) {
      return window.google.maps;
    }
    if (mapApiLoadPromise) {
      return mapApiLoadPromise;
    }

    const apiKey = getStoredGoogleMapsApiKey();
    if (!apiKey) {
      throw new Error('Missing Google Maps API key');
    }

    mapApiLoadPromise = new Promise((resolve, reject) => {
      const callbackName = '__onboardingLocationGoogleMapsInit';
      const existing = document.querySelector('script[data-onboarding-maps-api="1"]');
      if (existing) existing.remove();

      let settled = false;
      const doneResolve = () => {
        if (settled) return;
        settled = true;
        window.clearTimeout(timerId);
        try { delete window[callbackName]; } catch {}
        resolve(window.google.maps);
      };
      const doneReject = (err) => {
        if (settled) return;
        settled = true;
        window.clearTimeout(timerId);
        try { delete window[callbackName]; } catch {}
        mapApiLoadPromise = null;
        reject(err);
      };

      window[callbackName] = doneResolve;

      const script = document.createElement('script');
      script.dataset.onboardingMapsApi = '1';
      script.async = true;
      script.defer = true;
      script.src = `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(apiKey)}&libraries=places&callback=${callbackName}`;
      script.onerror = () => doneReject(new Error('Failed to load Google Maps script'));
      document.head.appendChild(script);

      const timerId = window.setTimeout(() => {
        doneReject(new Error('Google Maps load timed out'));
      }, 12000);
    });

    return mapApiLoadPromise;
  };

  const setMapMarkerPosition = (latLng, zoom = 15) => {
    if (!mapInstance || !window.google?.maps || !latLng) return;
    const maps = window.google.maps;
    if (!mapMarker) {
      mapMarker = new maps.Marker({
        map: mapInstance,
        position: latLng
      });
    } else {
      mapMarker.setPosition(latLng);
      mapMarker.setMap(mapInstance);
    }
    mapInstance.panTo(latLng);
    mapInstance.setZoom(zoom);
  };

  const applySelectedLocation = ({ address = '', placeId = '', lat = null, lng = null, statusText = 'Address selected on map.' } = {}) => {
    const normalizedAddress = String(address || '').trim();
    const locationLat = normalizeCoordinate(lat);
    const locationLng = normalizeCoordinate(lng);
    setSavedLocation({
      address: normalizedAddress,
      placeId,
      lat: locationLat,
      lng: locationLng
    });
    setLocationInputs(normalizedAddress);
    if (locationLat !== null && locationLng !== null) {
      setMapMarkerPosition({ lat: locationLat, lng: locationLng }, 15);
    }
    persistLocation();
    updateNavigationState();
    if (statusText) setMapStatus(statusText, 'ok');
  };

  const geocodeAddressToMap = async (address, { showStatus = false } = {}) => {
    const normalized = String(address || '').trim();
    if (!normalized || !mapGeocoder) return false;

    return new Promise((resolve) => {
      mapGeocoder.geocode({ address: normalized }, (results, status) => {
        if (status !== 'OK' || !Array.isArray(results) || !results.length || !results[0]?.geometry?.location) {
          if (showStatus) setMapStatus('Could not locate that address on map.', 'err');
          resolve(false);
          return;
        }
        const top = results[0];
        const lat = typeof top.geometry.location?.lat === 'function' ? top.geometry.location.lat() : top.geometry.location?.lat;
        const lng = typeof top.geometry.location?.lng === 'function' ? top.geometry.location.lng() : top.geometry.location?.lng;
        applySelectedLocation({
          address: String(top.formatted_address || normalized).trim(),
          placeId: top.place_id || '',
          lat,
          lng,
          statusText: showStatus ? 'Address selected on map.' : ''
        });
        resolve(true);
      });
    });
  };

  const reverseGeocodeFromMap = (latLng) => {
    if (!mapGeocoder || !latLng) return;
    mapGeocoder.geocode({ location: latLng }, (results, status) => {
      if (status !== 'OK' || !Array.isArray(results) || !results.length) {
        setMapStatus('Map point selected, but reverse geocoding failed.', 'err');
        return;
      }
      const address = String(results[0]?.formatted_address || '').trim();
      if (!address) {
        setMapStatus('Map point selected, but no address returned.', 'err');
        return;
      }
      const lat = typeof latLng?.lat === 'function' ? latLng.lat() : latLng?.lat;
      const lng = typeof latLng?.lng === 'function' ? latLng.lng() : latLng?.lng;
      applySelectedLocation({
        address,
        placeId: results[0]?.place_id || '',
        lat,
        lng,
        statusText: 'Address selected on map.'
      });
    });
  };

  const initMapPicker = async () => {
    if (!mapCanvasEl) return;
    await loadServerGoogleMapsApiKey();
    const apiKey = getStoredGoogleMapsApiKey();
    if (!apiKey) {
      setMapStatus('Set Google Maps API key to enable map picker.', 'err');
      return;
    }

    try {
      const maps = await loadGoogleMapsApi();
      if (!mapInstance) {
        mapInstance = new maps.Map(mapCanvasEl, {
          center: DEFAULT_MAP_CENTER,
          zoom: 11,
          mapTypeControl: false,
          streetViewControl: false,
          fullscreenControl: false
        });
        mapGeocoder = new maps.Geocoder();

        mapInstance.addListener('click', (event) => {
          const location = event?.latLng;
          if (!location) return;
          setMapMarkerPosition(location, 15);
          reverseGeocodeFromMap(location);
        });

        if (locationInput) {
          mapAutocomplete = new maps.places.Autocomplete(locationInput, {
            fields: ['formatted_address', 'geometry', 'name', 'place_id']
          });
          mapAutocomplete.addListener('place_changed', () => {
            const place = mapAutocomplete.getPlace();
            const loc = place?.geometry?.location;
            const address = String(place?.formatted_address || place?.name || locationInput.value || '').trim();
            if (!loc || !address) {
              setMapStatus('Please choose a valid search result.', 'err');
              return;
            }
            applySelectedLocation({
              address,
              placeId: String(place?.place_id || '').trim(),
              lat: typeof loc.lat === 'function' ? loc.lat() : loc.lat,
              lng: typeof loc.lng === 'function' ? loc.lng() : loc.lng,
              statusText: 'Address selected on map.'
            });
          });
        }
      }

      const hasCoordinates = Number.isFinite(Number(savedLocation.lat)) && Number.isFinite(Number(savedLocation.lng));
      const currentAddress = String(savedLocation.address || locationInput?.value || '').trim();
      if (hasCoordinates) {
        setMapMarkerPosition({
          lat: Number(savedLocation.lat),
          lng: Number(savedLocation.lng)
        }, 15);
      } else if (currentAddress) {
        await geocodeAddressToMap(currentAddress, { showStatus: false });
      }
      setMapStatus('', '');
    } catch (err) {
      console.error(err);
      setMapStatus('Failed to initialize Google Maps picker.', 'err');
    }
  };

  setSavedLocation(readSavedLocation());
  if (savedLocation.address) {
    setLocationInputs(savedLocation.address);
  }
  updateNavigationState();

  locationInput?.addEventListener('input', () => {
    if (suppressLocationFieldSync) return;
    clearSavedLocationMetadata(String(locationInput.value || '').trim());
    persistLocation();
    setMapStatus('', '');
    updateNavigationState();
  });

  locationInput?.addEventListener('blur', async () => {
    const address = String(locationInput.value || '').trim();
    if (!address || !mapInstance) return;
    await geocodeAddressToMap(address, { showStatus: false });
  });

  locationInput?.addEventListener('keydown', async (event) => {
    if (event.key !== 'Enter') return;
    event.preventDefault();
    const address = String(locationInput.value || '').trim();
    if (!address) return;
    if (!mapInstance) await initMapPicker();
    if (!mapInstance) return;
    await geocodeAddressToMap(address, { showStatus: true });
  });

  btnBack?.addEventListener('click', () => {
    persistLocation();
    window.location.href = 'onboarding.html';
  });

  btnFinish?.addEventListener('click', () => {
    const address = String(locationInput?.value || savedLocation.address || '').trim();
    if (!address) {
      setMapStatus('Please enter your club location before continuing.', 'err');
      locationInput?.focus();
      updateNavigationState();
      return;
    }
    persistLocation();
    window.location.href = 'onboarding-promo.html';
  });

  initMapPicker();
});
