import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';

const rootDir = process.cwd();

function read(relativePath) {
  return readFileSync(path.join(rootDir, relativePath), 'utf8');
}

test('payment flow no longer uses localStorage payment stubs or placeholder copy', () => {
  const paymentHtml = read('frontend/payment.html');
  const clubHtml = read('frontend/club.html');

  assert.match(paymentHtml, /Payment status/i);
  assert.doesNotMatch(paymentHtml, /placeholder payment page/i);
  assert.doesNotMatch(paymentHtml, /pendingPayment/);
  assert.doesNotMatch(clubHtml, /pendingPayment/);
  assert.match(paymentHtml, /Confirm payment/i);
  assert.match(paymentHtml, /confirm-virtual/);
});

test('join and venue overview are standalone pages instead of redirect shells', () => {
  const joinHtml = read('frontend/join.html');
  const venueOverviewHtml = read('frontend/venue overview.html');

  assert.match(joinHtml, /Membership Options/);
  assert.match(venueOverviewHtml, /Venue List/);
  assert.doesNotMatch(joinHtml, /window\.location\.replace\s*\(/);
  assert.doesNotMatch(venueOverviewHtml, /window\.location\.replace\s*\(/);
  assert.doesNotMatch(joinHtml, /http-equiv\s*=\s*["']refresh["']/i);
  assert.doesNotMatch(venueOverviewHtml, /http-equiv\s*=\s*["']refresh["']/i);
});

test('join page hides membership card UI when a club has no public plans', () => {
  const joinHtml = read('frontend/join.html');

  assert.match(joinHtml, /<title>Club Booking Options<\/title>/);
  assert.match(joinHtml, /id="membershipSection"/);
  assert.match(joinHtml, /id="planCountCard"/);
  assert.match(joinHtml, /id="membershipStateCard"/);
  assert.match(joinHtml, /const membershipSectionEl = document\.getElementById\('membershipSection'\)/);
  assert.match(joinHtml, /const syncMembershipLayout = \(plans, membership\) => \{/);
  assert.match(joinHtml, /membershipSectionEl\.style\.display = hasPublicPlans \? '' : 'none';/);
  assert.match(joinHtml, /planCountCardEl\.style\.display = hasPublicPlans \? 'grid' : 'none';/);
  assert.match(joinHtml, /membershipStateCardEl\.style\.display = hasPublicPlans \|\| hasActiveMembership \? 'grid' : 'none';/);
  assert.match(joinHtml, /renderMembershipBanner\(membership,\s*membershipLayout\.hasPublicPlans\);/);
});

test('login handoff uses explicit returnTo routing', () => {
  const authModal = read('frontend/auth-modal.js');
  const paymentHtml = read('frontend/payment.html');
  const clubHtml = read('frontend/club.html');
  const loginHtml = read('frontend/login.html');

  assert.match(authModal, /returnTo=/);
  assert.match(paymentHtml, /returnTo=/);
  assert.match(clubHtml, /returnTo=/);
  assert.doesNotMatch(loginHtml, /postLoginRedirect/);
});

test('chat pages are wired for realtime EventSource updates', () => {
  const clubChatHtml = read('frontend/club chat.html');
  const clubHtml = read('frontend/club.html');

  assert.match(clubChatHtml, /EventSource/);
  assert.match(clubChatHtml, /html,\s*[\r\n\s]*body\s*\{[\s\S]*height:\s*100%;[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /body\.page-with-footer\s*\{[\s\S]*padding-bottom:\s*0\s*!important;[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /body\.page-with-footer\s*>\s*\.site-footer\s*\{[\s\S]*position:\s*static;/);
  assert.match(clubChatHtml, /\.page-main\s*\{[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /\.chat-shell\s*\{[\s\S]*height:\s*100%;[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /\.thread-list-panel\s*\{[\s\S]*display:\s*grid;[\s\S]*grid-template-rows:\s*minmax\(0,\s*1fr\);[\s\S]*height:\s*100%;/);
  assert.match(clubChatHtml, /\.thread-list\s*\{[\s\S]*overflow:\s*auto;[\s\S]*height:\s*100%;/);
  assert.match(clubChatHtml, /\.thread-list\s*\{[\s\S]*overscroll-behavior:\s*contain;/);
  assert.match(clubChatHtml, /\.thread-list\s*\{[\s\S]*min-height:\s*0;/);
  assert.match(clubChatHtml, /\.chat-log\s*\{[\s\S]*min-height:\s*0;[\s\S]*overflow:\s*auto;/);
  assert.match(clubChatHtml, /\.chat-log\s*\{[\s\S]*overscroll-behavior:\s*contain;/);
  assert.match(clubChatHtml, /\.thread-view\s*\{[\s\S]*height:\s*100%;/);
  assert.match(clubChatHtml, /\.thread-view\s*\{[\s\S]*display:\s*grid;[\s\S]*grid-template-rows:\s*auto minmax\(0,\s*1fr\) auto;/);
  assert.match(clubChatHtml, /\.reply-form\s*\{[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /\.reply-input\s*\{[\s\S]*height:\s*74px;[\s\S]*resize:\s*none;[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /const resizeReplyInput = \(\) => \{/);
  assert.match(clubChatHtml, /const bindPanelWheelScroll = \(panelEl, scrollEl\) => \{/);
  assert.match(clubChatHtml, /bindPanelWheelScroll\(threadViewEl, chatLogEl\);/);
  assert.match(clubChatHtml, /const threadHasClubReplyAfterHandoff = \(thread\) => \{/);
  assert.match(clubChatHtml, /if \(thread\.lastSender === 'club'\) return false;/);
  assert.match(clubChatHtml, /if \(threadHasClubReplyAfterHandoff\(thread\)\) return false;/);
  assert.match(clubChatHtml, /let replySendInFlight = false;/);
  assert.match(clubChatHtml, /const createPendingClubReply = \(text\) => \(\{/);
  assert.match(clubHtml, /let chatSendInFlight = false;/);
  assert.match(clubHtml, /const createPendingUserChatMessage = \(text\) => \(\{/);
  assert.match(clubChatHtml, /\.thread-view\s*\{[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /const threadNeedsHumanBadge = \(thread\) => \{[\s\S]*if \(thread\.chatMode !== 'HANDOFF_REQUESTED'\) return false;[\s\S]*if \(thread\.lastSender === 'club'\) return false;[\s\S]*if \(threadHasClubReplyAfterHandoff\(thread\)\) return false;[\s\S]*return true;[\s\S]*\};/);
  assert.match(clubHtml, /EventSource/);
  assert.doesNotMatch(clubChatHtml, /access_token=/);
  assert.doesNotMatch(clubHtml, /access_token=/);
});

test('chat UIs label FAQ answers, robot answers, and human handoff prompts', () => {
  const clubChatHtml = read('frontend/club chat.html');
  const clubHtml = read('frontend/club.html');

  assert.match(clubHtml, /Verified club reply/);
  assert.match(clubHtml, /Bot/);
  assert.match(clubHtml, /HUMAN_HANDOFF_KEYWORDS/);
  assert.match(clubHtml, /\/api\/chat-sessions\/\$\{encodeURIComponent\(String\(chatSessionId\)\)\}\/handoff/);
  assert.match(clubHtml, /openInlineHumanPrompt/);
  assert.match(clubHtml, /clubChatLauncher/);
  assert.match(clubHtml, /clubChatToast/);
  assert.match(clubHtml, /showChatToast/);
  assert.match(clubHtml, /Keep chatting while you browse this club\./);
  assert.match(clubHtml, /data-chat-handoff-action/);
  assert.match(clubHtml, /Contact club staff/);
  assert.doesNotMatch(clubHtml, /window\.confirm\('This looks like a request for human support\./);
  assert.doesNotMatch(clubHtml, /window\.confirm\('This question may need a club staff member\./);
  assert.doesNotMatch(clubHtml, /document\.body\.classList\.toggle\('club-chat-open'/);
  assert.match(clubChatHtml, /人工待接入/);
  assert.match(clubChatHtml, /answerSource/);
  assert.match(clubChatHtml, /clubUnreadCount/);
});

test('stale auth and data-source TODO placeholders were removed from user-facing pages', () => {
  const loginHtml = read('frontend/login.html');
  const homeHtml = read('frontend/home.html');

  assert.doesNotMatch(loginHtml, /TODO: Replace with real backend endpoints/i);
  assert.doesNotMatch(homeHtml, /TODO: Replace with real backend endpoint/i);
  assert.doesNotMatch(homeHtml, /TODO: Replace with backend data fetch/i);
});

test('club registration keeps password fields editable while still enforcing email verification on submit', () => {
  const clubRegisterHtml = read('frontend/club register.html');
  const codeSlotMatches = clubRegisterHtml.match(/class="code-slot"/g) || [];

  assert.doesNotMatch(clubRegisterHtml, /<input id="clubPass"[^>]*disabled/i);
  assert.doesNotMatch(clubRegisterHtml, /<input id="clubPass2"[^>]*disabled/i);
  assert.doesNotMatch(clubRegisterHtml, /<button id="clubRegisterBtn"[^>]*disabled/i);
  assert.match(clubRegisterHtml, /verification will start automatically/i);
  assert.equal(codeSlotMatches.length, 6);
  assert.match(clubRegisterHtml, /id="clubEmailCodeInput" type="hidden"/);
  assert.match(clubRegisterHtml, /Please verify this email before registering\./);
  assert.match(clubRegisterHtml, /Please verify your email code before continuing\./);
  assert.match(clubRegisterHtml, /Password must include uppercase, lowercase, and a number\./);
  assert.match(clubRegisterHtml, /Passwords do not match\./);
  assert.match(clubRegisterHtml, /const syncCodeSlots =/);
  assert.match(clubRegisterHtml, /const clearCodeEntry =/);
  assert.match(clubRegisterHtml, /const applyCodeFromIndex =/);
  assert.match(clubRegisterHtml, /combined\.length === 6 && !clubVerifyInFlight/);
  assert.match(clubRegisterHtml, /handleVerifyCode\(\{ auto: true \}\)/);
  assert.match(clubRegisterHtml, /inputChangedSinceRequest/);
  assert.match(clubRegisterHtml, /shouldRetryLatestCode/);
  assert.match(clubRegisterHtml, /clearCodeEntry\(true\);/);
  assert.match(clubRegisterHtml, /sessionStorage\.removeItem\('clubPortal\.onboardingDraft'\)/);
  assert.match(clubRegisterHtml, /window\.location\.replace\('onboarding\.html'\)/);
  assert.doesNotMatch(clubRegisterHtml, /clubPass\.disabled\s*=/);
  assert.doesNotMatch(clubRegisterHtml, /clubPass2\.disabled\s*=/);
  assert.doesNotMatch(clubRegisterHtml, /clubRegisterBtn\.disabled\s*=\s*!unlock/);
});

test('club onboarding entry points replace the previous page in browser history', () => {
  const clubHomeHtml = read('frontend/club home.html');

  assert.match(clubHomeHtml, /window\.location\.replace\('onboarding\.html'\)/);
});

test('onboarding page hides default back UI and blocks browser back in setup mode', () => {
  const onboardingHtml = read('frontend/onboarding.html');
  const onboardingJs = read('frontend/onboarding.js');

  assert.match(onboardingHtml, /onboarding\.css\?v=20260327f/);
  assert.match(onboardingHtml, /width:\s*min\(1760px,\s*calc\(100vw - 56px\)\)/);
  assert.match(onboardingHtml, /onboarding\.js\?v=20260327f/);
  assert.match(onboardingHtml, /class="header-side"/);
  assert.match(onboardingHtml, /__clubOnboardingBackGuardInstalled/);
  assert.match(onboardingHtml, /window\.addEventListener\('popstate'/);
  assert.doesNotMatch(onboardingHtml, /&larr; Back/);
  assert.doesNotMatch(onboardingHtml, /window\.history\.back\(\)/);
  assert.doesNotMatch(onboardingHtml, /闁跨喐鏋婚幏绌﹂敓鏂ゆ嫹/);
  assert.match(onboardingJs, /function clearBackButton\(\)/);
  assert.match(onboardingJs, /function ensureBackButton\(\)/);
  assert.match(onboardingJs, /function pushOnboardingLockState\(\)/);
  assert.match(onboardingJs, /function installOnboardingBackGuard\(\)/);
  assert.match(onboardingJs, /window\.__clubOnboardingBackGuardInstalled/);
  assert.match(onboardingJs, /window\.history\.pushState/);
  assert.match(onboardingJs, /window\.addEventListener\('pageshow'/);
  assert.doesNotMatch(onboardingJs, /window\.history\.go\(1\);/);
  assert.match(onboardingJs, /clearBackButton\(\);/);
  assert.match(onboardingJs, /installOnboardingBackGuard\(\);/);
});

test('club onboarding draft is session-scoped instead of localStorage-backed', () => {
  const onboardingJs = read('frontend/onboarding.js');
  const onboardingLocationHtml = read('frontend/onboarding-location.html');
  const onboardingLocationJs = read('frontend/onboarding-location.js');
  const onboardingCss = read('frontend/onboarding.css');
  const onboardingPromoHtml = read('frontend/onboarding-promo.html');
  const onboardingPromoJs = read('frontend/onboarding-promo.js');

  assert.match(onboardingJs, /const ONBOARDING_DRAFT_STORAGE_KEY = 'clubPortal\.onboardingDraft'/);
  assert.match(onboardingJs, /sessionStorage\.getItem\(ONBOARDING_DRAFT_STORAGE_KEY\)/);
  assert.match(onboardingJs, /sessionStorage\.setItem\(ONBOARDING_DRAFT_STORAGE_KEY/);
  assert.doesNotMatch(onboardingJs, /localStorage\.getItem\('clubProfile'\)/);
  assert.doesNotMatch(onboardingJs, /localStorage\.setItem\('clubProfile'/);

  assert.match(onboardingLocationHtml, /onboarding\.css\?v=20260327f/);
  assert.match(onboardingLocationHtml, /onboarding-location\.js\?v=20260327f/);
  assert.match(onboardingLocationHtml, /Step 2 of 3/);
  assert.match(onboardingLocationHtml, /Search on Google Maps or enter full address/);
  assert.doesNotMatch(onboardingLocationHtml, /clubMapSetKeyBtn/);
  assert.doesNotMatch(onboardingLocationHtml, /clubMapSearchInput/);
  assert.match(onboardingLocationHtml, /id="clubMapStatus" class="status" aria-live="polite" hidden/);
  assert.doesNotMatch(onboardingLocationHtml, /This will be shown on your public club page\./);
  assert.doesNotMatch(onboardingLocationHtml, /Pick a real address with Google Maps or type one manually\./);
  assert.match(onboardingLocationJs, /sessionStorage\.getItem\(ONBOARDING_DRAFT_STORAGE_KEY\)/);
  assert.match(onboardingLocationJs, /sessionStorage\.setItem\(ONBOARDING_DRAFT_STORAGE_KEY/);
  assert.match(onboardingLocationJs, /new maps\.places\.Autocomplete\(locationInput/);
  assert.doesNotMatch(onboardingLocationJs, /mapSetKeyBtn/);
  assert.doesNotMatch(onboardingLocationJs, /const mapSearchInput =/);
  assert.doesNotMatch(onboardingLocationJs, /Override Maps API key/);
  assert.doesNotMatch(onboardingLocationJs, /Set Maps API key/);
  assert.doesNotMatch(onboardingLocationJs, /Google Maps ready\. Search or click map to select address\./);
  assert.match(onboardingLocationJs, /mapStatusEl\.hidden = !normalized;/);
  assert.match(onboardingLocationJs, /const updateNavigationState = \(\) => \{/);
  assert.match(onboardingLocationJs, /btnFinish\) btnFinish\.disabled = !hasAddress;/);
  assert.match(onboardingLocationJs, /Please enter your club location before continuing\./);
  assert.doesNotMatch(onboardingLocationJs, /localStorage\.getItem\('clubProfile'\)/);
  assert.doesNotMatch(onboardingLocationJs, /localStorage\.setItem\('clubProfile'/);
  assert.match(onboardingCss, /\.wrap\s*\{[\s\S]*width:\s*min\(1760px,\s*calc\(100vw - 56px\)\);/);
  assert.match(onboardingCss, /\.wrap\s*\{[\s\S]*max-width:\s*none;/);
  assert.match(onboardingCss, /@media \(min-width:\s*1280px\)\s*\{[\s\S]*width:\s*min\(1880px,\s*calc\(100vw - 64px\)\);/);
  assert.match(onboardingCss, /\.map-canvas\s*\{[\s\S]*min-height:\s*360px;/);
  assert.match(onboardingCss, /@media \(min-width:\s*1280px\)\s*\{[\s\S]*min-height:\s*420px;/);
  assert.match(onboardingCss, /\.map-toolbar\s*\{[\s\S]*grid-template-columns:\s*minmax\(0,\s*1fr\);/);
  assert.match(onboardingCss, /\.promo-gallery\s*\{[\s\S]*grid-template-columns:\s*repeat\(auto-fill,\s*minmax\(240px,\s*280px\)\);/);
  assert.match(onboardingCss, /\.promo-gallery\s*\{[\s\S]*justify-content:\s*start;/);
  assert.match(onboardingCss, /\.promo-thumb\s*\{[\s\S]*aspect-ratio:\s*4 \/ 3;/);
  assert.match(onboardingCss, /\.promo-thumb\s*\{[\s\S]*height:\s*auto;/);
  assert.match(onboardingCss, /\.promo-card\s*\{[\s\S]*overflow:\s*hidden;/);
  assert.match(onboardingCss, /\.promo-cover\s*\{[\s\S]*color:\s*#ffffff;/);
  assert.match(onboardingCss, /\.promo-cover\s*\{[\s\S]*background:\s*rgba\(15,\s*23,\s*42,\s*0\.82\);/);
  assert.match(onboardingCss, /\.promo-cover\s*\{[\s\S]*backdrop-filter:\s*blur\(10px\);/);
  assert.match(onboardingCss, /\.promo-cover\s*\{[\s\S]*text-shadow:\s*0 1px 2px rgba\(15,\s*23,\s*42,\s*0\.35\);/);

  assert.match(onboardingPromoHtml, /onboarding\.css\?v=20260327f/);
  assert.match(onboardingPromoHtml, /Step 3 of 3/);
  assert.match(onboardingPromoHtml, /onboarding-promo\.js\?v=20260327d/);
  assert.doesNotMatch(onboardingPromoHtml, /promoImageUrl/);
  assert.doesNotMatch(onboardingPromoHtml, /Paste image URLs/);
  assert.match(onboardingPromoHtml, /JPG, PNG, or WEBP; multiple files supported\./);
  assert.match(onboardingPromoHtml, /Keep it short \(1-2 sentences\)\./);
  assert.doesNotMatch(onboardingPromoHtml, /锟\?|ï¿½|�\?/);
  assert.match(onboardingPromoJs, /sessionStorage\.getItem\(ONBOARDING_DRAFT_STORAGE_KEY\)/);
  assert.match(onboardingPromoJs, /sessionStorage\.setItem\(ONBOARDING_DRAFT_STORAGE_KEY/);
  assert.match(onboardingPromoJs, /sessionStorage\.removeItem\(ONBOARDING_DRAFT_STORAGE_KEY\)/);
  assert.match(onboardingPromoJs, /const updateFinishState = \(\) => \{/);
  assert.match(onboardingPromoJs, /btnFinish\) btnFinish\.disabled = !\(hasImages && hasPromoText\);/);
  assert.match(onboardingPromoJs, /Please upload at least one club photo before finishing setup\./);
  assert.match(onboardingPromoJs, /Please enter promo text before finishing setup\./);
  assert.doesNotMatch(onboardingPromoJs, /imageUrlInput/);
  assert.doesNotMatch(onboardingPromoJs, /parseUrlList/);
  assert.doesNotMatch(onboardingPromoJs, /syncUrlInput/);
  assert.doesNotMatch(onboardingPromoJs, /localStorage\.getItem\('clubProfile'\)/);
  assert.doesNotMatch(onboardingPromoJs, /localStorage\.setItem\('clubProfile'/);
});

test('club onboarding finish now routes through a success handoff page', () => {
  const onboardingHtml = read('frontend/onboarding.html');
  const onboardingPromoJs = read('frontend/onboarding-promo.js');
  const onboardingCompleteHtml = read('frontend/onboarding-complete.html');
  const onboardingCompleteJs = read('frontend/onboarding-complete.js');

  assert.match(onboardingHtml, /Step 1 of 3/);
  assert.match(onboardingPromoJs, /window\.location\.replace\('onboarding-complete\.html'\)/);
  assert.doesNotMatch(onboardingPromoJs, /window\.location\.href = 'club home\.html'/);

  assert.match(onboardingCompleteHtml, /Registration successful/);
  assert.match(onboardingCompleteHtml, /Your club account is ready\./);
  assert.match(onboardingCompleteHtml, /club management page/);
  assert.match(onboardingCompleteHtml, /log in from the homepage/);
  assert.match(onboardingCompleteHtml, /Enter club management page/);
  assert.match(onboardingCompleteHtml, /onboarding-complete\.js\?v=20260328a/);
  assert.match(onboardingCompleteHtml, /onboarding\.css\?v=20260327f/);
  assert.match(onboardingCompleteHtml, /body\.page-with-footer\s*\{\s*background:\s*#ffffff !important;/);
  assert.match(onboardingCompleteHtml, /\.success-cta\s*\{[\s\S]*text-decoration:\s*none;/);
  assert.doesNotMatch(onboardingCompleteHtml, /You do not need to repeat registration after this\./);
  assert.match(onboardingCompleteHtml, /id="completeClubName"/);

  assert.match(onboardingCompleteJs, /localStorage\.getItem\('selectedClub'\)/);
  assert.match(onboardingCompleteJs, /const DASHBOARD_GUIDE_PENDING_KEY = 'clubPortal\.pendingDashboardGuide'/);
  assert.match(onboardingCompleteJs, /sessionStorage\.setItem\(DASHBOARD_GUIDE_PENDING_KEY,\s*'1'\)/);
  assert.match(onboardingCompleteJs, /const goBtn = document\.getElementById\('completeGoBtn'\)/);
  assert.match(onboardingCompleteJs, /window\.location\.replace\('club home\.html'\)/);
});

test('club home shows a first-run guide for time slots and memberships after onboarding handoff', () => {
  const clubHomeHtml = read('frontend/club home.html');

  assert.match(clubHomeHtml, /const DASHBOARD_GUIDE_PENDING_KEY = 'clubPortal\.pendingDashboardGuide'/);
  assert.match(clubHomeHtml, /const DASHBOARD_GUIDE_SEEN_PREFIX = 'clubPortal\.dashboardGuideSeen\.'/);
  assert.match(clubHomeHtml, /id="dashboardGuideOverlay"/);
  assert.match(clubHomeHtml, /First-time guide/);
  assert.match(clubHomeHtml, /Set time slots/);
  assert.match(clubHomeHtml, /Set membership cards/);
  assert.match(clubHomeHtml, /Skip for now/);
  assert.match(clubHomeHtml, /Open your venue setup page and publish the first bookable time slot for members\./);
  assert.match(clubHomeHtml, /Review the default membership cards and manually turn on the plans you want to sell\./);
  assert.match(clubHomeHtml, /maybeOpenDashboardGuide\(clubs\);/);
  assert.match(clubHomeHtml, /sessionStorage\.getItem\(DASHBOARD_GUIDE_PENDING_KEY\) === '1'/);
  assert.match(clubHomeHtml, /localStorage\.setItem\(getDashboardGuideSeenKey\(clubId\), '1'\)/);
  assert.match(clubHomeHtml, /jumpFromGuideToSection\('venues'\)/);
  assert.match(clubHomeHtml, /jumpFromGuideToSection\('updates'\)/);
});

test('club info page no longer exposes Maps API key override controls', () => {
  const clubInfoHtml = read('frontend/club-info.html');

  assert.doesNotMatch(clubInfoHtml, /clubMapSetKeyBtn/);
  assert.doesNotMatch(clubInfoHtml, /Set Maps API key/);
  assert.doesNotMatch(clubInfoHtml, /Override Maps API key/);
  assert.doesNotMatch(clubInfoHtml, /Google Maps API key was not updated\./);
  assert.doesNotMatch(clubInfoHtml, /promptGoogleMapsApiKey/);
  assert.match(clubInfoHtml, /Google Maps is temporarily unavailable\. Please enter the address manually\./);
  assert.match(clubInfoHtml, /grid-template-columns:\s*minmax\(0,\s*1fr\);/);
});

test('public club page shows the booking schedule before optional membership cards', () => {
  const clubHtml = read('frontend/club.html');
  const scheduleIndex = clubHtml.indexOf('id="schedule"');
  const membershipIndex = clubHtml.indexOf('id="clubMembershipSection"');

  assert.ok(scheduleIndex >= 0, 'schedule container should exist');
  assert.ok(membershipIndex >= 0, 'membership section should exist');
  assert.ok(scheduleIndex < membershipIndex, 'schedule should appear before membership cards');
  assert.match(clubHtml, /class="booking-detail-layout"/);
  assert.match(clubHtml, /<div id="weekTabs"[\s\S]*?<div class="booking-detail-layout">/);
  assert.match(clubHtml, /@media \(min-width: 1500px\)[\s\S]*?left: calc\(100% \+ 56px\);/);
});

test('club updates page manages club-level chat knowledge base entries', () => {
  const clubUpdatesHtml = read('frontend/club updates.html');
  const clubHomeHtml = read('frontend/club home.html');

  assert.match(clubUpdatesHtml, /Memberships &amp; chat replies/);
  assert.match(clubUpdatesHtml, /data-section-card="chat-kb"/);
  assert.match(clubUpdatesHtml, /Chat knowledge base/);
  assert.match(clubUpdatesHtml, /Add entry/);
  assert.match(clubUpdatesHtml, /Add the question and the standard reply\./);
  assert.match(clubUpdatesHtml, /Similarity, phrasing, and language matching are handled automatically/i);
  assert.match(clubUpdatesHtml, /low-risk questions/i);
  assert.match(clubUpdatesHtml, /id="kbStatusBox"/);
  assert.match(clubUpdatesHtml, /id="kbList"/);
  assert.match(clubUpdatesHtml, /const kbListEl = document\.getElementById\('kbList'\)/);
  assert.match(clubUpdatesHtml, /const loadChatKb = async \(\) => \{/);
  assert.match(clubUpdatesHtml, /\/api\/my\/clubs\/\$\{encodeURIComponent\(String\(activeClubId\)\)\}\/chat-kb/);
  assert.match(clubUpdatesHtml, /<label>Question<\/label>/);
  assert.match(clubUpdatesHtml, /answerText/);
  assert.match(clubUpdatesHtml, /Write the exact reply members should receive/i);
  assert.doesNotMatch(clubUpdatesHtml, /<label>Question title<\/label>/);
  assert.doesNotMatch(clubUpdatesHtml, /<label>Language<\/label>/);
  assert.doesNotMatch(clubUpdatesHtml, /<label>Trigger keywords<\/label>/);
  assert.doesNotMatch(clubUpdatesHtml, /<label>Example questions<\/label>/);
  assert.doesNotMatch(clubUpdatesHtml, /<label>Priority<\/label>/);
  assert.doesNotMatch(clubUpdatesHtml, /Enable this reply/);
  assert.match(clubUpdatesHtml, /Promise\.all\(\[loadPlans\(\), loadMembers\(\), loadChatKb\(\)\]\)/);

  assert.match(clubHomeHtml, /Memberships &amp; FAQ/);
  assert.match(clubHomeHtml, /club updates\.html\?v=20260328-chat-kb-b/);
  assert.match(clubHomeHtml, /manage club chat replies/i);
});

test('user profile layout uses wider desktop sizing and a low-emphasis back link', () => {
  const userHtml = read('frontend/user.html');

  assert.match(userHtml, /width:\s*min\(1600px,\s*calc\(100vw - 64px\)\)/);
  assert.match(userHtml, /\.link-back\s*\{/);
  assert.match(userHtml, /background:\s*rgba\(255,\s*255,\s*255,\s*0\.72\)/);
  assert.match(userHtml, /grid-template-columns:\s*280px minmax\(0,\s*1fr\)/);
  assert.match(userHtml, /backdrop-filter:\s*blur\(14px\)/);
});

test('user profile email update uses six verification slots with automatic submit', () => {
  const userHtml = read('frontend/user.html');
  const userProfileJs = read('frontend/user-profile.js');
  const codeSlotMatches = userHtml.match(/class="code-slot"/g) || [];

  assert.equal(codeSlotMatches.length, 6);
  assert.match(userHtml, /id="emailCodeInput" type="hidden"/);
  assert.match(userHtml, /Verification will start automatically/i);
  assert.match(userHtml, /<script type="module" src="user-profile\.js\?v=20260329b"><\/script>/);
  assert.match(userProfileJs, /const emailCodeSlots = Array\.from\(document\.querySelectorAll\('#emailOverlay \.code-slot'\)\)/);
  assert.match(userProfileJs, /const syncEmailCodeSlots =/);
  assert.match(userProfileJs, /const clearEmailCodeEntry =/);
  assert.match(userProfileJs, /const applyEmailCodeFromIndex =/);
  assert.match(userProfileJs, /submitEmailVerification\(\{ auto: true \}\)/);
  assert.match(userProfileJs, /clearEmailCodeEntry\(true\);/);
  assert.doesNotMatch(userHtml, /id="confirmEmailBtn"/);
});

test('booking pages expose the six-digit verification code to both members and club staff', () => {
  const userHtml = read('frontend/user.html');
  const userProfileJs = read('frontend/user-profile.js');
  const clubBookingsHtml = read('frontend/club bookings.html');

  assert.match(userHtml, /\.booking-code-row\s*\{/);
  assert.match(userHtml, /\.booking-code-value\s*\{/);
  assert.match(userProfileJs, /const verificationCode = String\(item\?\.bookingVerificationCode \|\| ''\)\.trim\(\);/);
  assert.match(userProfileJs, /Check-in code/);
  assert.match(userProfileJs, /booking-code-value/);

  assert.match(clubBookingsHtml, /\.member-pill\.code\s*\{/);
  assert.match(clubBookingsHtml, /const verificationCode = String\(m\?\.bookingVerificationCode \|\| ''\)\.trim\(\);/);
  assert.match(clubBookingsHtml, /Code \$\{escapeHtml\(verificationCode\)\}/);
  assert.match(clubBookingsHtml, /Member check-in verification code/);
});

test('user security form keeps current password on its own row and splits new passwords into two columns', () => {
  const userHtml = read('frontend/user.html');

  assert.match(userHtml, /\.security-field\.current\s*\{\s*grid-column:\s*1\s*\/\s*-1;/);
  assert.match(userHtml, /<div class="security-field current">\s*<label for="currentPassInput">Current password<\/label>/);
  assert.match(userHtml, /<div class="security-field">\s*<label for="newPassInput">New password<\/label>/);
  assert.match(userHtml, /<div class="security-field confirm">\s*<label for="confirmPassInput">Confirm new password<\/label>/);
});
