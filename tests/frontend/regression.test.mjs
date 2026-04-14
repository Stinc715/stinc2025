import test from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
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
  assert.doesNotMatch(paymentHtml, /Fetching the latest checkout state from the server\./);
  assert.doesNotMatch(paymentHtml, /Payment state is synchronised from the server\./);
});

test('desktop consistency layer is opt-in on key desktop pages', () => {
  const desktopCss = read('frontend/desktop-consistency.css');
  const clubHtml = read('frontend/club.html');
  const clubChatHtml = read('frontend/club chat.html');
  const clubHomeHtml = read('frontend/club home.html');
  const clubUpdatesHtml = read('frontend/club updates.html');
  const userHtml = read('frontend/user.html');
  const loginHtml = read('frontend/login.html');
  const clubRegisterHtml = read('frontend/club register.html');
  const clubBookingsHtml = read('frontend/club bookings.html');
  const clubAdminHtml = read('frontend/club-admin.html');
  const clubInfoHtml = read('frontend/club-info.html');
  const clubsHtml = read('frontend/clubs.html');
  const homeHtml = read('frontend/home.html');
  const indexHtml = read('frontend/index.html');
  const joinHtml = read('frontend/join.html');
  const onboardingCompleteHtml = read('frontend/onboarding-complete.html');
  const paymentHtml = read('frontend/payment.html');
  const onboardingHtml = read('frontend/onboarding.html');
  const onboardingLocationHtml = read('frontend/onboarding-location.html');
  const onboardingPromoHtml = read('frontend/onboarding-promo.html');
  const resetPasswordHtml = read('frontend/reset-password.html');
  const venueOverviewHtml = read('frontend/venue overview.html');

  assert.match(desktopCss, /--page-max-wide:\s*1600px/);
  assert.match(desktopCss, /--page-max-standard:\s*1180px/);
  assert.match(desktopCss, /--page-max-narrow:\s*920px/);
  assert.match(desktopCss, /--page-side-padding:\s*24px/);
  assert.match(desktopCss, /--card-gap:\s*24px/);
  assert.match(desktopCss, /--rail-width:\s*320px/);
  assert.match(desktopCss, /--club-primary-max:\s*1100px/);
  assert.match(desktopCss, /--club-rail-collapsed-width:\s*56px/);
  assert.match(desktopCss, /--club-rail-open-width:\s*320px/);
  assert.match(desktopCss, /body\.desktop-page--club \.page-main\.rail-open/);
  assert.match(desktopCss, /\.desktop-shell--wide/);
  assert.match(desktopCss, /\.desktop-shell--standard/);
  assert.match(desktopCss, /\.desktop-shell--narrow/);
  assert.match(desktopCss, /\.desktop-shell-header/);
  assert.match(desktopCss, /\.desktop-shell-main/);
  assert.match(desktopCss, /\.desktop-form-shell/);
  assert.match(clubHtml, /desktop-consistency\.css\?v=20260403a/);
  assert.match(clubHtml, /body class="page-with-footer desktop-consistency desktop-shell--wide desktop-page--club"/);
  assert.match(clubHtml, /class="wrap preview-topbar desktop-shell-header"/);
  assert.match(clubHtml, /class="back-link desktop-back-link"/);
  assert.match(clubHtml, /<div class="top-right-wrap">[\s\S]*id="bookingSideRailToggle"/);
  assert.match(clubHtml, /class="wrap page-main desktop-shell-main"/);
  assert.match(clubHtml, /class="club-primary-column desktop-card-gap"/);
  assert.match(clubHtml, /class="club-chat-badge desktop-badge"/);
  assert.match(clubHtml, /class="membership-copy-eyebrow desktop-badge"/);
  assert.match(clubHtml, /class="community-copy-eyebrow desktop-badge"/);
  assert.match(clubHtml, /class="site-footer__wrap desktop-shell-wrap"/);
  assert.match(desktopCss, /body\.desktop-consistency \.desktop-back-link,[\s\S]*body\.desktop-consistency \.back-link,[\s\S]*body\.desktop-consistency \.link-back \{[\s\S]*box-shadow:\s*0 8px 18px rgba\(29,\s*29,\s*31,\s*0\.08\);/);
  assert.match(desktopCss, /body\.desktop-consistency \.desktop-back-link::before,[\s\S]*body\.desktop-consistency \.link-back::before \{[\s\S]*content:\s*'<';/);
  assert.match(desktopCss, /body\.desktop-consistency \.desktop-shell-wrap \{[\s\S]*margin-inline:\s*auto;/);
  assert.match(desktopCss, /body\.desktop-consistency \.desktop-badge \{[\s\S]*border-radius:\s*999px;/);
  assert.match(desktopCss, /body\.desktop-consistency \.desktop-card-gap \{[\s\S]*gap:\s*var\(--card-gap\);/);
  assert.match(desktopCss, /body\.desktop-page--club \.wrap \{[\s\S]*max-width:\s*var\(--club-primary-max\);[\s\S]*padding:\s*16px;/);
  assert.match(clubChatHtml, /desktop-shell--standard desktop-page--club-chat/);
  assert.match(clubChatHtml, /class="back-link desktop-back-link"/);
  assert.match(clubChatHtml, /class="qa-count desktop-badge"/);
  assert.match(clubChatHtml, /class="footer-wrap desktop-shell-wrap"/);
  assert.match(clubChatHtml, /thread-count desktop-badge/);
  assert.match(desktopCss, /body\.desktop-page--club-chat\.page-with-footer \{[\s\S]*height:\s*100dvh;[\s\S]*padding-bottom:\s*0\s*!important;[\s\S]*overflow:\s*hidden;/);
  assert.match(desktopCss, /body\.desktop-page--club-chat \.wrap \{[\s\S]*padding:\s*14px 16px;/);
  assert.match(clubHomeHtml, /desktop-shell--wide desktop-page--club-home/);
  assert.match(clubHomeHtml, /class="workspace card desktop-card-gap"/);
  assert.match(clubHomeHtml, /class="site-footer__wrap desktop-shell-wrap"/);
  assert.match(clubHomeHtml, /const getAuthToken = \(\) => \{/);
  assert.match(clubHomeHtml, /let logoutInFlight = false;/);
  assert.match(clubHomeHtml, /if \(logoutInFlight\) return;/);
  assert.match(clubHomeHtml, /const liveToken = getAuthToken\(\);/);
  assert.match(clubHomeHtml, /const PUBLIC_HOME_TARGET = 'home\.html';/);
  assert.match(clubHomeHtml, /if \(!liveToken\) \{\s*(?:renderChatsUnreadBadge\(0\);\s*)?forceLogout\(PUBLIC_HOME_TARGET\);/);
  assert.match(clubHomeHtml, /window\.AuthSession\.logout\(PUBLIC_HOME_TARGET, \{ replace: true \}\);/);
  assert.match(desktopCss, /body\.desktop-page--club-home\.page-with-footer \{[\s\S]*padding-bottom:\s*0\s*!important;[\s\S]*overflow:\s*hidden;/);
  assert.match(desktopCss, /body\.desktop-page--club-home \.wrap \{[\s\S]*width:\s*min\(var\(--desktop-shell-max\), calc\(100vw - 24px\)\);[\s\S]*padding:\s*14px 10px;/);
  assert.match(desktopCss, /body\.desktop-page--club-home \.btn \{[\s\S]*border-radius:\s*999px;[\s\S]*font-size:\s*13px;/);
  assert.match(clubUpdatesHtml, /desktop-shell--standard desktop-page--club-updates/);
  assert.match(clubUpdatesHtml, /class="back-link desktop-back-link"/);
  assert.match(clubUpdatesHtml, /class="wrap page-shell desktop-shell-main desktop-form-shell desktop-card-gap"/);
  assert.match(clubUpdatesHtml, /class="button-count desktop-badge" id="planSavedCount"/);
  assert.match(clubUpdatesHtml, /class="button-count desktop-badge" id="kbSavedCount"/);
  assert.match(userHtml, /desktop-shell--wide desktop-page--user/);
  assert.match(userHtml, /class="link-back desktop-back-link"/);
  assert.match(userHtml, /class="profile-shell desktop-card-gap"/);
  assert.match(userHtml, /class="booking-shell desktop-card-gap"/);
  assert.match(userHtml, /class="profile-eyebrow desktop-badge"/);
  assert.match(userHtml, /class="security-eyebrow desktop-badge"/);
  assert.match(userHtml, /class="site-footer__wrap desktop-shell-wrap"/);
  assert.match(desktopCss, /body\.desktop-page--user \.wrap \{[\s\S]*width:\s*min\(var\(--desktop-shell-max\), calc\(100vw - 64px\)\);/);
  assert.match(desktopCss, /body\.desktop-page--user \.btn \{[\s\S]*box-shadow:\s*none;/);
  assert.match(loginHtml, /desktop-shell--narrow desktop-page--login/);
  assert.match(desktopCss, /body\.desktop-page--login \.btn \{[\s\S]*height:\s*44px;[\s\S]*background:\s*#111;/);
  assert.match(clubRegisterHtml, /desktop-consistency desktop-page--club-register/);
  assert.match(clubRegisterHtml, /class="link-back desktop-back-link page-back"/);
  assert.match(clubRegisterHtml, /class="site-footer__wrap desktop-shell-wrap"/);
  assert.match(paymentHtml, /desktop-shell--narrow desktop-page--payment/);
  assert.match(paymentHtml, /class="back-link desktop-back-link"/);
  assert.match(onboardingHtml, /desktop-consistency desktop-page--onboarding/);
  assert.match(onboardingHtml, /class="site-footer__wrap desktop-shell-wrap"/);
  assert.match(onboardingLocationHtml, /desktop-consistency desktop-page--onboarding/);
  assert.match(onboardingLocationHtml, /class="link-back desktop-back-link back-pill"/);
  assert.match(onboardingLocationHtml, /class="site-footer__wrap desktop-shell-wrap"/);
  assert.match(onboardingPromoHtml, /desktop-consistency desktop-page--onboarding/);
  assert.match(onboardingPromoHtml, /class="link-back desktop-back-link back-pill"/);
  assert.match(onboardingPromoHtml, /class="site-footer__wrap desktop-shell-wrap"/);
  assert.match(clubBookingsHtml, /desktop-consistency\.css\?v=20260403a/);
  assert.match(clubBookingsHtml, /body class="page-with-footer desktop-consistency desktop-shell--standard desktop-page--club-bookings"/);
  assert.match(clubBookingsHtml, /class="wrap header-wrap desktop-shell-header"/);
  assert.match(clubBookingsHtml, /class="back-link desktop-back-link"/);
  assert.match(clubBookingsHtml, /class="wrap desktop-shell-main"/);
  assert.match(clubBookingsHtml, /class="site-footer__wrap desktop-shell-wrap"/);
  assert.match(clubAdminHtml, /desktop-consistency\.css\?v=20260403a/);
  assert.match(clubAdminHtml, /body class="club-admin-page desktop-consistency desktop-shell--standard desktop-page--club-admin"/);
  assert.match(clubAdminHtml, /class="wrap header-wrap desktop-shell-header"/);
  assert.match(clubAdminHtml, /class="back-link desktop-back-link"/);
  assert.match(clubAdminHtml, /class="wrap admin-main desktop-shell-main desktop-form-shell"/);
  assert.match(clubInfoHtml, /desktop-consistency\.css\?v=20260403a/);
  assert.match(clubInfoHtml, /body class="desktop-consistency desktop-shell--standard desktop-page--club-info"/);
  assert.match(clubInfoHtml, /class="wrap header-wrap desktop-shell-header"/);
  assert.match(clubInfoHtml, /class="back-link desktop-back-link"/);
  assert.match(clubInfoHtml, /class="wrap desktop-shell-main desktop-form-shell"/);
  assert.match(clubsHtml, /desktop-consistency\.css\?v=20260403a/);
  assert.match(clubsHtml, /body class="desktop-consistency desktop-shell--standard desktop-page--clubs"/);
  assert.match(clubsHtml, /class="wrap header-wrap desktop-shell-header"/);
  assert.match(clubsHtml, /class="wrap desktop-shell-main"/);
  assert.match(homeHtml, /desktop-consistency\.css\?v=20260403a/);
  assert.match(homeHtml, /body class="page-with-footer desktop-consistency desktop-shell--standard desktop-page--home"/);
  assert.match(homeHtml, /class="wrap desktop-shell-header"/);
  assert.match(homeHtml, /class="wrap page-main desktop-shell-main"/);
  assert.match(homeHtml, /class="site-footer__wrap desktop-shell-wrap"/);
  assert.match(indexHtml, /desktop-consistency\.css\?v=20260403a/);
  assert.match(indexHtml, /body class="desktop-consistency desktop-shell--narrow desktop-page--redirect"/);
  assert.match(joinHtml, /desktop-consistency\.css\?v=20260403a/);
  assert.match(joinHtml, /body class="desktop-consistency desktop-shell--standard desktop-page--join"/);
  assert.match(joinHtml, /class="wrap header-row desktop-shell-header"/);
  assert.match(joinHtml, /class="wrap desktop-shell-main"/);
  assert.match(onboardingCompleteHtml, /desktop-consistency\.css\?v=20260403a/);
  assert.match(onboardingCompleteHtml, /body class="page-with-footer desktop-consistency desktop-shell--narrow desktop-page--onboarding-complete"/);
  assert.match(venueOverviewHtml, /desktop-consistency\.css\?v=20260403a/);
  assert.match(venueOverviewHtml, /body class="desktop-consistency desktop-shell--standard desktop-page--venue-overview"/);
  assert.match(venueOverviewHtml, /class="wrap header-row desktop-shell-header"/);
  assert.match(venueOverviewHtml, /class="wrap desktop-shell-main"/);
  assert.match(desktopCss, /body\.desktop-page--club-bookings \.wrap \{/);
  assert.match(desktopCss, /body\.desktop-page--club-admin \.wrap \{/);
  assert.match(desktopCss, /body\.desktop-page--club-info \.wrap \{/);
  assert.match(desktopCss, /body\.desktop-page--clubs \.wrap \{/);
  assert.match(desktopCss, /body\.desktop-page--clubs \.btn,[\s\S]*body\.desktop-page--join \.btn,[\s\S]*body\.desktop-page--venue-overview \.btn \{[\s\S]*display:\s*inline-flex;[\s\S]*font-weight:\s*700;/);
  assert.match(desktopCss, /body\.desktop-page--clubs \.btn \{[\s\S]*min-height:\s*38px;/);
  assert.match(desktopCss, /body\.desktop-page--join \.btn,[\s\S]*body\.desktop-page--venue-overview \.btn \{[\s\S]*min-height:\s*40px;/);
  assert.match(desktopCss, /body\.desktop-page--clubs \.btn\.ghost,[\s\S]*body\.desktop-page--join \.btn\.ghost,[\s\S]*body\.desktop-page--venue-overview \.btn\.ghost \{[\s\S]*background:\s*#fff;[\s\S]*color:\s*#111827;/);
  assert.match(desktopCss, /body\.desktop-page--home \.wrap \{/);
  assert.match(desktopCss, /body\.desktop-page--join \.wrap \{/);
  assert.match(desktopCss, /body\.desktop-page--onboarding-complete\.page-with-footer \{/);
  assert.match(desktopCss, /body\.desktop-page--venue-overview \.wrap \{/);
  assert.match(desktopCss, /body\.desktop-page--payment \.wrap \{[\s\S]*padding-block:\s*16px;/);
  assert.doesNotMatch(clubHtml, /header\s+\.wrap,\s*\.wrap\s*\{/);
  assert.doesNotMatch(clubHtml, /\.back-link\s*\{[\s\S]*box-shadow:\s*0 8px 18px rgba\(29,\s*29,\s*31,\s*0\.08\);/);
  assert.doesNotMatch(clubChatHtml, /body\.page-with-footer\s*\{[\s\S]*padding-bottom:\s*0\s*!important;[\s\S]*overflow:\s*hidden;/);
  assert.doesNotMatch(clubChatHtml, /\.wrap\s*\{[\s\S]*padding:\s*14px 16px;/);
  assert.doesNotMatch(clubChatHtml, /\.back-link\s*\{[\s\S]*box-shadow:\s*0 8px 18px rgba\(29,\s*29,\s*31,\s*0\.08\);/);
  assert.doesNotMatch(clubHomeHtml, /body\.page-with-footer\s*\{[\s\S]*padding-bottom:\s*0\s*!important;[\s\S]*overflow:\s*hidden;/);
  assert.doesNotMatch(clubHomeHtml, /\.wrap\s*\{[\s\S]*width:\s*min\(1680px,\s*calc\(100vw - 24px\)\);/);
  assert.doesNotMatch(clubHomeHtml, /\.btn\s*\{[\s\S]*padding:\s*8px 16px;[\s\S]*border-radius:\s*999px;/);
  assert.doesNotMatch(clubHomeHtml, /const newToken = String\(event\.newValue \|\| ''\)\.trim\(\);/);
  assert.doesNotMatch(clubBookingsHtml, /\.back-link\s*\{[\s\S]*box-shadow:\s*0 8px 18px rgba\(29,\s*29,\s*31,\s*0\.08\);/);
  assert.doesNotMatch(clubAdminHtml, /\.back-link\s*\{[\s\S]*box-shadow:\s*0 8px 18px rgba\(29,\s*29,\s*31,\s*0\.08\);/);
  assert.doesNotMatch(clubInfoHtml, /\.back-link\s*\{[\s\S]*box-shadow:\s*0 8px 18px rgba\(29,\s*29,\s*31,\s*0\.08\);/);
  assert.doesNotMatch(clubsHtml, /\.wrap\s*\{[\s\S]*max-width:\s*1120px;/);
  assert.doesNotMatch(clubsHtml, /\.btn\s*\{[\s\S]*border:\s*1px solid var\(--line\);[\s\S]*display:\s*inline-flex;/);
  assert.doesNotMatch(homeHtml, /header\s+\.wrap,\s*footer\s+\.wrap,\s*\.wrap\s*\{/);
  assert.doesNotMatch(joinHtml, /\.wrap\s*\{[\s\S]*max-width:\s*1140px;/);
  assert.doesNotMatch(joinHtml, /\.btn\s*\{[\s\S]*min-height:\s*40px;[\s\S]*font-weight:\s*700;/);
  assert.doesNotMatch(onboardingHtml, /\.back-link\s*\{[\s\S]*box-shadow:\s*0 8px 18px rgba\(29,\s*29,\s*31,\s*0\.08\);/);
  assert.doesNotMatch(paymentHtml, /\.wrap\s*\{[\s\S]*max-width:\s*920px;/);
  assert.doesNotMatch(paymentHtml, /\.back-link\s*\{[\s\S]*box-shadow:\s*0 8px 18px rgba\(29,\s*29,\s*31,\s*0\.08\);/);
  assert.doesNotMatch(resetPasswordHtml, /\.back-link\s*\{[\s\S]*box-shadow:\s*0 8px 18px rgba\(29,\s*29,\s*31,\s*0\.08\);/);
  assert.doesNotMatch(venueOverviewHtml, /\.wrap\s*\{[\s\S]*max-width:\s*1160px;/);
  assert.doesNotMatch(venueOverviewHtml, /\.btn\s*\{[\s\S]*min-height:\s*40px;[\s\S]*font-weight:\s*700;/);
  assert.doesNotMatch(userHtml, /\.wrap\s*\{[\s\S]*width:\s*min\(1600px,\s*calc\(100vw - 64px\)\);/);
  assert.doesNotMatch(userHtml, /\.link-back\s*\{[\s\S]*border:\s*1px solid rgba\(148,\s*163,\s*184,\s*0\.24\);/);
  assert.doesNotMatch(userHtml, /\.btn\s*\{[\s\S]*box-shadow:\s*none;/);
  assert.doesNotMatch(loginHtml, /\.btn\{\s*[\s\S]*height:\s*44px;[\s\S]*background:#111;/);
});

test('club-side logout and auth fallback return to the public home page instead of the standalone login page', () => {
  const clubHomeHtml = read('frontend/club home.html');
  const clubBookingsHtml = read('frontend/club bookings.html');
  const clubUpdatesHtml = read('frontend/club updates.html');
  const clubChatHtml = read('frontend/club chat.html');
  const clubAdminHtml = read('frontend/club-admin.html');
  const clubInfoHtml = read('frontend/club-info.html');
  const clubRegisterHtml = read('frontend/club register.html');
  const onboardingJs = read('frontend/onboarding.js');
  const onboardingPromoJs = read('frontend/onboarding-promo.js');

  assert.match(clubHomeHtml, /const PUBLIC_HOME_TARGET = 'home\.html';/);
  assert.match(clubHomeHtml, /window\.location\.replace\(PUBLIC_HOME_TARGET\);/);
  assert.match(clubHomeHtml, /forceLogout\(PUBLIC_HOME_TARGET\);/);
  assert.match(clubBookingsHtml, /window\.location\.replace\('home\.html'\);/);
  assert.match(clubUpdatesHtml, /window\.location\.replace\('home\.html'\);/);
  assert.match(clubChatHtml, /if \(redirectOnMissing\) window\.location\.replace\('home\.html'\);/);
  assert.match(clubAdminHtml, /window\.location\.replace\('home\.html'\);/);
  assert.match(clubInfoHtml, /window\.location\.replace\('home\.html'\);/);
  assert.match(clubRegisterHtml, /window\.location\.replace\('home\.html'\);/);
  assert.match(onboardingJs, /window\.location\.replace\('home\.html'\);/);
  assert.match(onboardingPromoJs, /window\.location\.replace\('home\.html'\);/);

  assert.doesNotMatch(clubHomeHtml, /login\.html#login/);
  assert.doesNotMatch(clubBookingsHtml, /login\.html#login/);
  assert.doesNotMatch(clubUpdatesHtml, /login\.html#login/);
  assert.doesNotMatch(clubChatHtml, /login\.html#login/);
  assert.doesNotMatch(clubAdminHtml, /login\.html#login/);
  assert.doesNotMatch(clubInfoHtml, /login\.html#login/);
  assert.doesNotMatch(clubRegisterHtml, /login\.html#login/);
  assert.doesNotMatch(onboardingJs, /login\.html#login/);
  assert.doesNotMatch(onboardingPromoJs, /login\.html#login/);
});

test('portable repo entrypoints do not depend on zip execute bits or cross-platform node_modules reuse', () => {
  const packageJson = JSON.parse(read('package.json'));
  const playwrightConfig = read('playwright.config.mjs');
  const runMavenScript = read('scripts/run-maven.mjs');
  const ensureRuntimeScript = read('scripts/ensure-runtime.mjs');

  assert.equal(packageJson.scripts.dev, 'node scripts/portable-vite.mjs dev');
  assert.equal(packageJson.scripts.build, 'node scripts/portable-vite.mjs build');
  assert.equal(packageJson.scripts.preview, 'node scripts/portable-vite.mjs preview');
  assert.equal(packageJson.scripts.start, 'node scripts/portable-vite.mjs preview');
  assert.equal(packageJson.scripts['test:frontend:visual'], 'node scripts/portable-playwright.mjs test');
  assert.match(playwrightConfig, /node scripts\/portable-vite\.mjs preview --host 127\.0\.0\.1 --port 4173 --strictPort/);
  assert.match(runMavenScript, /spawnSync\('sh', \[path\.join\(repoRoot, 'mvnw'\), \.\.\.args\], \{/);
  assert.match(ensureRuntimeScript, /missing platform dependency/);
  assert.match(ensureRuntimeScript, /running npm .* to rebuild dependencies/);
  assert.match(ensureRuntimeScript, /Playwright Chromium is missing; running playwright install chromium\./);
});

test('delivery scripts stay parameterized and the repo can stage a clean submission tree', () => {
  const packageJson = JSON.parse(read('package.json'));
  const prepareSubmissionScript = read('scripts/prepare-submission.mjs');
  const uploadScript = read('deploy/upload.ps1');
  const deployReadme = read('deploy/README.md');
  const nginxTemplate = read('deploy/nginx.conf');
  const apiContract = read('backend/API_CONTRACT.md');
  const appConfig = read('backend/src/main/resources/application.yml');

  assert.equal(packageJson.scripts['package:submission'], 'node scripts/prepare-submission.mjs');
  assert.equal(existsSync(path.join(rootDir, '.tmp_deploy_key.pem')), false);
  assert.match(prepareSubmissionScript, /club-portal-submission/);
  assert.match(prepareSubmissionScript, /node_modules/);
  assert.match(prepareSubmissionScript, /backend\/target/);
  assert.match(prepareSubmissionScript, /playwright\.config\.mjs/);
  assert.match(uploadScript, /\[switch\]\$SkipTests/);
  assert.match(uploadScript, /CLUB_PORTAL_DEPLOY_HOST/);
  assert.match(uploadScript, /CLUB_PORTAL_DEPLOY_USER/);
  assert.match(uploadScript, /CLUB_PORTAL_DEPLOY_SSH_KEY/);
  assert.match(uploadScript, /scripts\/run-maven\.mjs/);
  assert.doesNotMatch(uploadScript, /C:\\Users\\/);
  assert.doesNotMatch(uploadScript, /club-portal\.xyz/);
  assert.doesNotMatch(uploadScript, /ec2-user/);
  assert.match(nginxTemplate, /location ~ \^\/api\/\(clubs\/\[\^\/\]\+\/chat\/stream\|my\/clubs\/\[\^\/\]\+\/chat\/stream\)\$/);
  assert.match(nginxTemplate, /proxy_buffering off;/);
  assert.doesNotMatch(nginxTemplate, /proxy_set_header Connection 'upgrade';/);
  assert.doesNotMatch(nginxTemplate, /club-portal\.xyz/);
  assert.match(nginxTemplate, /example\.invalid/);
  assert.doesNotMatch(apiContract, /club-portal\.xyz/);
  assert.match(apiContract, /example\.invalid/);
  assert.doesNotMatch(appConfig, /no-reply@club-portal\.xyz/);
  assert.doesNotMatch(appConfig, /https:\/\/club-portal\.xyz/);
  assert.match(appConfig, /no-reply@example\.test/);
  assert.match(appConfig, /https:\/\/example\.invalid/);
  assert.match(deployReadme, /template-style script/);
  assert.match(deployReadme, /-SkipTests/);
});

test('readme documents the intentional auth-session build warning', () => {
  const readme = read('README.md');

  assert.match(readme, /auth-session\.js/);
  assert.match(readme, /non-blocking Vite warning/i);
  assert.match(readme, /classic external head script/i);
  assert.match(readme, /window\.AuthSession/);
  assert.match(readme, /Remaining complex-page[\s\S]*`\.btn`[\s\S]*retained/i);
  assert.match(readme, /single-page pilot/i);
});

test('final submission freeze assets stay wired into the delivery flow', () => {
  const packageJson = JSON.parse(read('package.json'));
  const readme = read('README.md');
  const prepareSubmissionScript = read('scripts/prepare-submission.mjs');
  const verifySubmissionScript = read('scripts/verify-submission.mjs');

  assert.equal(packageJson.scripts['verify:submission'], 'node scripts/verify-submission.mjs');
  assert.equal(existsSync(path.join(rootDir, 'docs', 'release-freeze-summary.md')), true);
  assert.equal(existsSync(path.join(rootDir, 'docs', 'final-delivery-guide.md')), true);
  assert.equal(existsSync(path.join(rootDir, 'docs', 'auth-session-loading-audit.md')), true);
  assert.equal(existsSync(path.join(rootDir, 'docs', 'button-system-audit.md')), true);
  assert.match(prepareSubmissionScript, /SUBMISSION_MANIFEST\.md/);
  assert.match(prepareSubmissionScript, /verification-report\.md/);
  assert.match(verifySubmissionScript, /verification-report\.json/);
  assert.match(verifySubmissionScript, /docs\/final-delivery-guide\.md/);
  assert.match(verifySubmissionScript, /verification-report\.md/);
  assert.match(readme, /verify:submission/);
  assert.match(readme, /final-delivery-guide\.md/);
  assert.match(readme, /release-freeze-summary\.md/);
  assert.match(readme, /standalone handoff/i);
});

test('auth-session stays head-loaded as a classic script on representative pages', () => {
  const representativePages = [
    {
      path: 'frontend/login.html',
      afterMarker: 'https://accounts.google.com/gsi/client',
    },
    {
      path: 'frontend/club home.html',
      afterMarker: 'const getAuthToken = () => {',
    },
    {
      path: 'frontend/onboarding.html',
      afterMarker: 'type="module" src="onboarding.js',
    },
    {
      path: 'frontend/payment.html',
      afterMarker: 'const authFetch = (url, options = {}) => {',
    },
    {
      path: 'frontend/reset-password.html',
      afterMarker: 'const API_BASE = "/api";',
    },
  ];

  representativePages.forEach(({ path: relativePath, afterMarker }) => {
    const html = read(relativePath);
    const authScriptIndex = html.indexOf('auth-session.js?v=');
    const bodyIndex = html.indexOf('<body');
    const afterIndex = html.indexOf(afterMarker);

    assert.notEqual(authScriptIndex, -1, `${relativePath} should include auth-session.js`);
    assert.notEqual(bodyIndex, -1, `${relativePath} should include a body tag`);
    assert(authScriptIndex < bodyIndex, `${relativePath} should load auth-session.js before <body>`);
    assert.doesNotMatch(html, /<script[^>]*type=["']module["'][^>]*auth-session\.js/i);
    assert.notEqual(afterIndex, -1, `${relativePath} should include its later page script marker`);
    assert(authScriptIndex < afterIndex, `${relativePath} should load auth-session.js before later page logic`);
  });
});

test('site typography keeps numerals aligned with surrounding text styles', () => {
  const themeCss = read('frontend/theme.css');
  const onboardingCss = read('frontend/onboarding.css');
  const localFontsCss = read('frontend/local-fonts.css');
  const clubInfoHtml = read('frontend/club-info.html');
  const loginHtml = read('frontend/login.html');
  const resetPasswordHtml = read('frontend/reset-password.html');
  const clubBookingsHtml = read('frontend/club bookings.html');
  const userHtml = read('frontend/user.html');

  assert.match(themeCss, /@import url\('\.\/local-fonts\.css'\);/);
  assert.match(onboardingCss, /@import url\('\.\/local-fonts\.css'\);/);
  assert.doesNotMatch(themeCss, /fonts\.googleapis\.com/);
  assert.doesNotMatch(onboardingCss, /fonts\.googleapis\.com/);
  assert.match(localFontsCss, /font-family:\s*'Sora'/);
  assert.match(localFontsCss, /font-family:\s*'Playfair Display'/);
  assert.match(localFontsCss, /font-family:\s*'Space Grotesk'/);
  assert.match(localFontsCss, /font-family:\s*'Fraunces'/);
  assert.match(themeCss, /font-variant-numeric:\s*lining-nums proportional-nums;/);
  assert.match(themeCss, /font-feature-settings:\s*"lnum"\s*1,\s*"pnum"\s*1;/);
  assert.match(onboardingCss, /font-variant-numeric:\s*lining-nums proportional-nums;/);
  assert.match(onboardingCss, /font-feature-settings:\s*"lnum"\s*1,\s*"pnum"\s*1;/);
  assert.match(clubInfoHtml, /font-variant-numeric:\s*lining-nums proportional-nums;/);
  assert.match(loginHtml, /font-variant-numeric:\s*lining-nums proportional-nums;/);
  assert.match(resetPasswordHtml, /font-variant-numeric:\s*lining-nums proportional-nums;/);
  assert.match(clubBookingsHtml, /\.member-pill\.code\s*\{[\s\S]*font-family:\s*inherit;[\s\S]*font-variant-numeric:\s*tabular-nums lining-nums;/);
  assert.match(userHtml, /\.booking-code-value\s*\{[\s\S]*font-family:\s*inherit;[\s\S]*font-variant-numeric:\s*tabular-nums lining-nums;/);
  assert.doesNotMatch(clubBookingsHtml, /font-family:\s*ui-monospace/);
  assert.doesNotMatch(userHtml, /font-family:\s*ui-monospace/);
});

test('join and venue overview are standalone pages instead of redirect shells', () => {
  const joinHtml = read('frontend/join.html');
  const venueOverviewHtml = read('frontend/venue overview.html');

  assert.match(joinHtml, /Membership Options/);
  assert.match(venueOverviewHtml, /Venue List/);
  assert.match(venueOverviewHtml, /No venues yet\./);
  assert.doesNotMatch(joinHtml, /window\.location\.replace\s*\(/);
  assert.doesNotMatch(venueOverviewHtml, /window\.location\.replace\s*\(/);
  assert.doesNotMatch(joinHtml, /http-equiv\s*=\s*["']refresh["']/i);
  assert.doesNotMatch(venueOverviewHtml, /http-equiv\s*=\s*["']refresh["']/i);
  assert.doesNotMatch(venueOverviewHtml, /This club has not published any venues yet\./);
  assert.doesNotMatch(venueOverviewHtml, /Independent venue overview page for public slot visibility and club-side handoff\./);
  assert.doesNotMatch(venueOverviewHtml, /Venue inventory, published capacities, and the next visible slots for this club\./);
  assert.doesNotMatch(venueOverviewHtml, /This page is public-facing\. Club accounts get a direct handoff into the management view, while users can continue into booking from here\./);
  assert.doesNotMatch(venueOverviewHtml, /Each card shows venue metadata plus the next few scheduled slots in the next seven days\./);
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
  assert.match(joinHtml, /No plans yet\./);
  assert.doesNotMatch(joinHtml, /This club has no public membership plans yet\./);
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

test('playwright visual baselines use hermetic local snapshots without platform suffixes', () => {
  const playwrightConfig = read('playwright.config.mjs');

  assert.match(playwrightConfig, /snapshotPathTemplate:\s*'\{testDir\}\/\{testFilePath\}-snapshots\/\{arg\}\{ext\}'/);
  assert.match(playwrightConfig, /name:\s*'desktop-chromium'/);
  assert.doesNotMatch(playwrightConfig, /\{platform\}/);
});

test('auth session strips token-bearing user payloads from localStorage persistence', () => {
  const authSession = read('frontend/auth-session.js');
  const loginHtml = read('frontend/login.html');
  const clubRegisterHtml = read('frontend/club register.html');

  assert.match(authSession, /const USER_STORAGE_KEY = 'user';/);
  assert.match(authSession, /const USER_TOKEN_FIELDS = Object\.freeze\(\['token', 'accessToken', 'refreshToken', 'idToken', 'jwt'\]\);/);
  assert.match(authSession, /const migrateLegacyUser = \(\) => \{/);
  assert.match(authSession, /const embeddedUserToken = migrateLegacyUser\(\);/);
  assert.match(authSession, /setStoredUser: persistStoredUser,/);
  assert.match(loginHtml, /persistStoredUser\(data\);/);
  assert.match(loginHtml, /persistStoredUser\(user\);/);
  assert.match(loginHtml, /persistStoredUser\(profileToSave\);/);
  assert.match(clubRegisterHtml, /persistStoredUser\(authenticatedProfile\);/);
  assert.match(clubRegisterHtml, /let authenticatedToken = String\(profileToSave\?\.token \|\| ''\)\.trim\(\);/);
  assert.match(clubRegisterHtml, /persistAuthToken\(authenticatedToken\);/);
  assert.doesNotMatch(loginHtml, /localStorage\.setItem\('user', JSON\.stringify\(data\)\)/);
  assert.doesNotMatch(loginHtml, /localStorage\.setItem\('user', JSON\.stringify\(user\)\)/);
  assert.doesNotMatch(loginHtml, /localStorage\.setItem\('user', JSON\.stringify\(profileToSave\)\)/);
  assert.doesNotMatch(clubRegisterHtml, /localStorage\.setItem\('user', JSON\.stringify\(loginData\)\)/);
});

test('chat pages are wired for realtime EventSource updates', () => {
  const clubChatHtml = read('frontend/club chat.html');
  const clubHtml = read('frontend/club.html');
  const desktopCss = read('frontend/desktop-consistency.css');

  assert.match(clubChatHtml, /EventSource/);
  assert.match(clubChatHtml, /html,\s*[\r\n\s]*body\s*\{[\s\S]*height:\s*100%;[\s\S]*overflow:\s*hidden;/);
  assert.match(desktopCss, /body\.desktop-page--club-chat\.page-with-footer \{[\s\S]*padding-bottom:\s*0\s*!important;[\s\S]*overflow:\s*hidden;/);
  assert.match(desktopCss, /body\.desktop-page--club-chat\.page-with-footer > \.site-footer \{[\s\S]*position:\s*static;/);
  assert.match(clubChatHtml, /\.page-main\s*\{[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /\.chat-shell\s*\{[\s\S]*height:\s*100%;[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /\.thread-list-panel\s*\{[\s\S]*display:\s*grid;[\s\S]*grid-template-rows:\s*auto minmax\(0,\s*1fr\);[\s\S]*height:\s*100%;/);
  assert.match(clubChatHtml, /\.thread-list\s*\{[\s\S]*overflow:\s*auto;[\s\S]*height:\s*100%;/);
  assert.match(clubChatHtml, /\.thread-list\s*\{[\s\S]*overscroll-behavior:\s*contain;/);
  assert.match(clubChatHtml, /\.thread-list\s*\{[\s\S]*min-height:\s*0;/);
  assert.match(clubChatHtml, /\.chat-log\s*\{[\s\S]*min-height:\s*0;[\s\S]*overflow:\s*auto;/);
  assert.match(clubChatHtml, /\.chat-log\s*\{[\s\S]*overscroll-behavior:\s*contain;/);
  assert.match(clubChatHtml, /\.thread-view\s*\{[\s\S]*height:\s*100%;/);
  assert.match(clubChatHtml, /\.thread-view\s*\{[\s\S]*display:\s*grid;[\s\S]*grid-template-rows:\s*auto minmax\(0,\s*1fr\) auto;/);
  assert.match(clubChatHtml, /\.thread-view\s*\{[\s\S]*padding-bottom:\s*12px;/);
  assert.match(clubChatHtml, /id="clubQaDock"/);
  assert.match(clubChatHtml, /id="clubQaToggle"/);
  assert.match(clubChatHtml, /id="clubQaPanel"/);
  assert.match(clubChatHtml, /id="clubQaList"/);
  assert.doesNotMatch(clubChatHtml, /id="clubQaDetail"/);
  assert.match(clubChatHtml, /id="clubQaThreadView"/);
  assert.match(clubChatHtml, /id="clubQaThreadLog"/);
  assert.match(clubChatHtml, /id="clubQaReplyHost"/);
  assert.match(clubChatHtml, /<aside class="thread-list-panel">[\s\S]*id="clubQaDock"[\s\S]*id="threadList"/);
  assert.doesNotMatch(clubChatHtml, /<section class="thread-view">[\s\S]*id="clubQaDock"/);
  assert.match(clubChatHtml, /Community Q&amp;A/);
  assert.match(clubChatHtml, /\.qa-dock\s*\{[\s\S]*position:\s*sticky;[\s\S]*top:\s*0;/);
  assert.match(clubChatHtml, /\.qa-dock-panel\s*\{[\s\S]*max-height:\s*min\(44vh,\s*360px\);[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /\.qa-dock-list\s*\{[\s\S]*max-height:\s*min\(44vh,\s*360px\);[\s\S]*overflow:\s*auto;/);
  assert.match(clubChatHtml, /\.qa-thread-view\s*\{[\s\S]*grid-row:\s*2\s*\/\s*4;[\s\S]*grid-template-rows:\s*minmax\(0,\s*1fr\)\s+auto;/);
  assert.match(clubChatHtml, /const getActiveClubQaQuestion = \(\) => \{/);
  assert.match(clubChatHtml, /const renderClubQaThreadView = \(\{ scrollTop = false \} = \{\}\) => \{/);
  assert.match(clubChatHtml, /const syncThreadViewMode = \(\{ scrollQaTop = false, scrollChatBottom = false \} = \{\}\) => \{/);
  assert.match(clubChatHtml, /const normalizeClubQaBoard = \(raw\) => \{/);
  assert.match(clubChatHtml, /canEdit:\s*Boolean\(raw\.canEdit\)/);
  assert.match(clubChatHtml, /canDelete:\s*Boolean\(raw\.canDelete\)/);
  assert.match(clubChatHtml, /const loadClubQaBoard = async \(\{ preserveSelection = true, quiet = false \} = \{\}\) => \{/);
  assert.match(clubChatHtml, /const submitClubQaAnswer = async \(questionId, rawText\) => \{/);
  assert.match(clubChatHtml, /const submitClubQaQuestionUpdate = async \(questionId, rawText\) => \{/);
  assert.match(clubChatHtml, /const deleteClubQaQuestion = async \(questionId\) => \{/);
  assert.match(clubChatHtml, /const submitClubQaAnswerUpdate = async \(questionId, answerId, rawText\) => \{/);
  assert.match(clubChatHtml, /const deleteClubQaAnswer = async \(questionId, answerId\) => \{/);
  assert.match(clubChatHtml, /data-club-qa-question-edit=/);
  assert.match(clubChatHtml, /data-club-qa-question-delete=/);
  assert.match(clubChatHtml, /data-club-qa-answer-edit=/);
  assert.match(clubChatHtml, /data-club-qa-answer-delete=/);
  assert.match(clubChatHtml, /openClubQaDeleteConfirm/);
  assert.match(clubChatHtml, /\/api\/clubs\/\$\{encodeURIComponent\(selectedClubId\)\}\/community-questions/);
  assert.match(clubChatHtml, /method:\s*'PUT'/);
  assert.match(clubChatHtml, /method:\s*'DELETE'/);
  assert.match(clubChatHtml, /\.reply-form\s*\{[\s\S]*padding:\s*12px 14px 14px;[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /\.reply-input\s*\{[\s\S]*height:\s*74px;[\s\S]*resize:\s*none;[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /const resizeReplyInput = \(\) => \{/);
  assert.match(clubChatHtml, /const bindPanelWheelScroll = \(panelEl, scrollTarget\) => \{/);
  assert.match(clubChatHtml, /bindPanelWheelScroll\(threadViewEl,\s*\(\) => \(activeThreadPane === 'qa' \? clubQaThreadLogEl : chatLogEl\)\);/);
  assert.match(clubChatHtml, /activeThreadPane = 'qa';/);
  assert.match(clubChatHtml, /activeThreadPane = 'chat';/);
  assert.match(clubChatHtml, /const threadHasClubReplyAfterHandoff = \(thread\) => \{/);
  assert.match(clubChatHtml, /if \(thread\.lastSender === 'club'\) return false;/);
  assert.match(clubChatHtml, /if \(threadHasClubReplyAfterHandoff\(thread\)\) return false;/);
  assert.match(clubChatHtml, /let replySendInFlight = false;/);
  assert.match(clubChatHtml, /const createPendingClubReply = \(text\) => \(\{/);
  assert.match(clubChatHtml, /No replies yet\./);
  assert.match(clubChatHtml, /No questions yet\./);
  assert.match(clubChatHtml, /No conversations yet\./);
  assert.match(clubChatHtml, /No messages yet\./);
  assert.doesNotMatch(clubChatHtml, /No replies yet\. Reply as club staff to guide members here\./);
  assert.doesNotMatch(clubChatHtml, /No questions yet\. Members can ask from the public club page\./);
  assert.doesNotMatch(clubChatHtml, /No member conversations yet\. New chats will appear here automatically\./);
  assert.doesNotMatch(clubChatHtml, /Conversation is empty\. Start by sending a reply\./);
  assert.match(clubHtml, /let chatSendInFlight = false;/);
  assert.match(clubHtml, /const runUserChatSync = async \(\{ scrollBottom = false, markRead = false \} = \{\}\) => \{\s*if \(chatPollInFlight \|\| chatSendInFlight\) return;/);
  assert.match(clubHtml, /const resumeRealtimeSyncAfterSend = shouldMaintainChatSync\(\);\s*stopChatStream\(\);\s*stopChatPolling\(\);\s*const previousInput = text;/);
  assert.match(clubHtml, /\} finally \{\s*if \(resumeRealtimeSyncAfterSend && chatWidgetOpen\) \{\s*startChatPolling\(\);\s*\}\s*\}\s*\}\);/);
  assert.match(clubHtml, /const createPendingUserChatMessage = \(text\) => \(\{/);
  assert.match(clubChatHtml, /\.thread-view\s*\{[\s\S]*overflow:\s*hidden;/);
  assert.match(clubChatHtml, /\.page-main\s*\{[\s\S]*padding-bottom:\s*24px;/);
  assert.match(clubChatHtml, /const threadNeedsHumanBadge = \(thread\) => \{[\s\S]*if \(thread\.chatMode !== 'HANDOFF_REQUESTED'\) return false;[\s\S]*if \(thread\.lastSender === 'club'\) return false;[\s\S]*if \(threadHasClubReplyAfterHandoff\(thread\)\) return false;[\s\S]*return true;[\s\S]*\};/);
  assert.match(clubHtml, /EventSource/);
  assert.doesNotMatch(clubChatHtml, /access_token=/);
  assert.doesNotMatch(clubHtml, /access_token=/);
});

test('chat UIs label FAQ answers, robot answers, and human handoff prompts', () => {
  const clubChatHtml = read('frontend/club chat.html');
  const clubHtml = read('frontend/club.html');
  const clubHomeHtml = read('frontend/club home.html');

  assert.match(clubHtml, /Verified club reply/);
  assert.match(clubHtml, /Bot/);
  assert.match(clubHtml, /HUMAN_HANDOFF_KEYWORDS/);
  assert.match(clubHtml, /\/api\/chat-sessions\/\$\{encodeURIComponent\(String\(chatSessionId\)\)\}\/handoff/);
  assert.match(clubHtml, /openInlineHumanPrompt/);
  assert.match(clubHtml, /clubChatLauncher/);
  assert.match(clubHtml, /clubChatToast/);
  assert.match(clubHtml, /showChatToast/);
  assert.match(clubHtml, /\.booking-chat-rail\s*\{[\s\S]*position:\s*relative;/);
  assert.match(clubHtml, /\.booking-chat-rail\s*\{[\s\S]*margin-top:\s*0;/);
  assert.doesNotMatch(clubHtml, /\.club-page-grid\.chat-focus\s*\{/);
  assert.doesNotMatch(clubHtml, /\.booking-side-rail\.chat-focus\s*\{[\s\S]*width:\s*0;/);
  assert.match(clubHtml, /\.booking-side-rail\.chat-focus\s+\.club-chat-launcher,[\s\S]*\.booking-side-rail\.chat-focus\s+\.community-rail\s*\{[\s\S]*display:\s*none;/);
  assert.match(clubHtml, /\.booking-side-rail\.chat-focus\s+\.booking-chat-rail\s*\{[\s\S]*position:\s*sticky;[\s\S]*top:\s*96px;[\s\S]*display:\s*block;/);
  assert.match(clubHtml, /\.booking-side-rail\.chat-focus\s+\.club-chat-panel\s*\{[\s\S]*position:\s*relative;[\s\S]*height:\s*min\(760px,\s*calc\(100dvh - 168px\)\);[\s\S]*box-shadow:\s*none;/);
  assert.match(clubHtml, /\.club-chat-toast\s*\{[\s\S]*position:\s*absolute;[\s\S]*bottom:\s*calc\(100% \+ 14px\);/);
  assert.match(clubHtml, /\.club-chat-panel\s*\{[\s\S]*position:\s*fixed;[\s\S]*right:\s*34px;[\s\S]*bottom:\s*30px;/);
  assert.match(clubHtml, /<div class="booking-chat-rail">[\s\S]*id="clubChatLauncher"[\s\S]*id="clubChatPanel"/);
  assert.equal((clubHtml.match(/id="clubChatPanel"/g) || []).length, 1);
  assert.match(clubHtml, /id="closeClubChatBtn"/);
  assert.match(clubHtml, /const showUnreadBadge = chatSessionMode === 'HUMAN' && count > 0;/);
  assert.match(clubHtml, /\.club-chat-badge\[hidden\]\s*\{\s*display:\s*none !important;\s*\}/);
  assert.match(clubHtml, /badgeEl\.textContent = showUnreadBadge \? String\(count\) : '';/);
  assert.doesNotMatch(clubHtml, /id="clubChatLauncherSubtitle"/);
  assert.doesNotMatch(clubHtml, /Ask while you browse/);
  assert.doesNotMatch(clubHtml, /id="clubChatSubtitle"/);
  assert.doesNotMatch(clubHtml, /Keep chatting while you browse this club\./);
  assert.doesNotMatch(clubHtml, /Replies now come from club staff\./);
  assert.match(clubHtml, /data-chat-handoff-action/);
  assert.match(clubHtml, /Contact club staff/);
  assert.doesNotMatch(clubHtml, /id="openClubChatBtn"/);
  assert.doesNotMatch(clubHtml, /id="clubChatBadge"/);
  assert.doesNotMatch(clubHtml, /window\.confirm\('This looks like a request for human support\./);
  assert.doesNotMatch(clubHtml, /window\.confirm\('This question may need a club staff member\./);
  assert.doesNotMatch(clubHtml, /document\.body\.classList\.toggle\('club-chat-open'/);
  assert.match(clubHtml, /id="bookingSideRailToggleLabel" class="booking-side-rail-toggle-label" aria-hidden="true">Open club chat, Q&amp;A &amp; plans<\/span>/);
  assert.match(clubHtml, /const syncClubChatMode = \(\) => \{/);
  assert.match(clubHtml, /bookingSideRailEl\?\.classList\.toggle\('chat-focus', railOverlay\);/);
  assert.doesNotMatch(clubHtml, /clubPageGridEl\?\.classList\.toggle\('chat-focus', chatWidgetOpen\);/);
  assert.match(clubHtml, /id="clubChatPanel"[\s\S]*class="club-chat-head-copy"[\s\S]*id="clubChatTitle"/);
  const railChatIndex = clubHtml.indexOf('class="booking-chat-rail"');
  const railCommunityIndex = clubHtml.indexOf('id="clubCommunitySection"');
  const railMembershipIndex = clubHtml.indexOf('id="clubMembershipSection"');
  assert.ok(railChatIndex >= 0 && railCommunityIndex >= 0 && railMembershipIndex >= 0, 'right rail modules should exist');
  assert.ok(railChatIndex < railMembershipIndex && railMembershipIndex < railCommunityIndex, 'right rail should order chat, membership, community');
  assert.match(clubHtml, /--booking-side-rail-stack-width:\s*320px/);
  assert.doesNotMatch(clubHtml, /id="bookingSideRailBackBtn"/);
  assert.doesNotMatch(clubHtml, /\.booking-side-rail:not\(\.is-collapsed\)\s+\.booking-side-rail-toggle\s*\{/);
  assert.match(clubChatHtml, /Human support pending/);
  assert.match(clubChatHtml, /answerSource/);
  assert.match(clubChatHtml, /clubUnreadCount/);
  assert.match(clubChatHtml, /const conversationNeedsAttention = \(item\) => \{/);
  assert.match(clubChatHtml, /return Math\.max\(unreadCount, conversationNeedsAttention\(item\) \? 1 : 0\);/);
  assert.match(clubHomeHtml, /const conversationNeedsAttention = \(item\) => \{/);
  assert.match(clubHomeHtml, /return Math\.max\(unreadCount, conversationNeedsAttention\(item\) \? 1 : 0\);/);
  assert.match(clubChatHtml, /let hasExplicitActiveThreadSelection = false;/);
  assert.match(clubChatHtml, /let pendingExplicitReadUserId = null;/);
  assert.match(clubChatHtml, /if \(loadingThread && markRead && hasExplicitActiveThreadSelection && Number\.isFinite\(Number\(activeUserId\)\)\) \{\s*pendingExplicitReadUserId = Number\(activeUserId\);\s*\}/);
  assert.match(clubChatHtml, /const pendingReadMatchesActiveThread = Number\(pendingExplicitReadUserId\) === Number\(activeThread\.userId\);/);
  assert.match(clubChatHtml, /const shouldMarkRead = hasExplicitActiveThreadSelection && \(Boolean\(markRead\) \|\| pendingReadMatchesActiveThread\);/);
  assert.match(clubChatHtml, /const shouldMarkRead = hasExplicitActiveThreadSelection[\s\S]*Number\(payload\?\.userId\) === Number\(activeUserId\);/);
});

test('club community q&a is exposed on member and club views', () => {
  const clubHtml = read('frontend/club.html');
  const clubChatHtml = read('frontend/club chat.html');

  assert.match(clubHtml, /id="clubCommunitySection"/);
  assert.doesNotMatch(clubHtml, /id="clubCommunitySummaryPreview"/);
  assert.match(clubHtml, /id="clubCommunityOpenBtn"/);
  assert.match(clubHtml, /id="clubCommunityDialog"/);
  assert.match(clubHtml, /id="closeClubCommunityBtn"/);
  assert.match(clubHtml, /id="clubCommunityToast" class="community-dialog-toast"/);
  assert.match(clubHtml, /id="clubCommunityAskForm"/);
  assert.match(clubHtml, /id="clubCommunityList"/);
  assert.match(clubHtml, /role="dialog" aria-modal="false" aria-label="Community Q&amp;A"/);
  assert.match(clubHtml, /Community Q&amp;A/);
  assert.doesNotMatch(clubHtml, /Ask everyone/);
  assert.match(clubHtml, /Community Q&amp;A/);
  assert.doesNotMatch(clubHtml, /<h3>Member pricing<\/h3>/);
  assert.doesNotMatch(clubHtml, /id="clubMembershipIntro"/);
  assert.doesNotMatch(clubHtml, /Check member discounts while you review available slots\./);
  assert.doesNotMatch(clubHtml, /<h3>Community Q&amp;A<\/h3>/);
  assert.doesNotMatch(clubHtml, /id="clubCommunityDialogTitle"/);
  assert.doesNotMatch(clubHtml, /id="clubCommunityIntro"/);
  assert.doesNotMatch(clubHtml, /Ask the club and members who have booked here\./);
  assert.doesNotMatch(clubHtml, /Ask a question here or open the board to read other members&apos; questions and replies\./);
  assert.doesNotMatch(clubHtml, /Booked members and club staff can reply below\./);
  assert.doesNotMatch(clubHtml, /\.community-question-author::before\s*\{[\s\S]*content:\s*'\?';/);
  assert.match(clubHtml, /\.community-question-author::before\s*\{[\s\S]*background-image:\s*url\("data:image\/svg\+xml,/);
  assert.doesNotMatch(clubHtml, /id="clubCommunitySummaryMeta"/);
  assert.doesNotMatch(clubHtml, /id="clubCommunityMeta"/);
  assert.doesNotMatch(clubHtml, /community-summary-snippet/);
  assert.match(clubHtml, /\.booking-side-rail\.community-focus\s+\.community-dialog-backdrop\s*\{[\s\S]*display:\s*none;/);
  assert.match(clubHtml, /\.booking-side-rail\.community-focus\s+\.booking-chat-rail,[\s\S]*\.booking-side-rail\.community-focus\s+\.community-rail\s*\{[\s\S]*display:\s*none;/);
  assert.match(clubHtml, /\.booking-side-rail\.community-focus\s+\.community-dialog-panel\s*\{[\s\S]*position:\s*relative;[\s\S]*height:\s*min\(820px,\s*calc\(100dvh - 168px\)\);[\s\S]*box-shadow:\s*none;/);
  assert.match(clubHtml, /\.community-dialog-body\s*\{[\s\S]*overflow:\s*auto;[\s\S]*scrollbar-gutter:\s*stable;/);
  assert.match(clubHtml, /\.community-dialog-toast\s*\{[\s\S]*position:\s*sticky;[\s\S]*top:\s*0;/);
  assert.match(clubHtml, /const setCommunityDialogOpen = \(open\) => \{/);
  assert.match(clubHtml, /const syncCommunityDialogMode = \(\) => \{/);
  assert.match(clubHtml, /const showCommunityToast = \(message, type = 'info'\) => \{/);
  assert.match(clubHtml, /const hideCommunityToast = \(\) => \{/);
  assert.match(clubHtml, /bookingSideRailEl\?\.classList\.toggle\('community-focus', railOverlay\);/);
  assert.match(clubHtml, /setCommunityDialogOpen\(false\);/);
  assert.match(clubHtml, /clubCommunityOpenBtnEl\?\.addEventListener\('click', \(\) => \{/);
  assert.match(clubHtml, /closeClubCommunityBtnEl\?\.addEventListener\('click', \(\) => \{/);
  assert.match(clubHtml, /const loadCommunityQuestions = async \(\{ preserveExpanded = true \} = \{\}\) => \{/);
  assert.match(clubHtml, /const activeQuestionId = communityBoard\.questions\.some\(\(question\) => question\.questionId === expandedCommunityQuestionId\)\s*\?[\s\S]*:\s*null;/);
  assert.match(clubHtml, /cache:\s*'no-store'/);
  assert.match(clubHtml, /const syncAuthenticatedProfile = async \(\) => \{/);
  assert.match(clubHtml, /const isCommunityQuestionAllowedForUser = \(\) => Boolean\(communityBoard\.canAsk\);/);
  assert.match(clubHtml, /const canAskQuestion = isCommunityQuestionAllowedForUser\(\);/);
  assert.match(clubHtml, /\.community-ask-actions\s*\{[\s\S]*justify-content:\s*flex-end;/);
  assert.doesNotMatch(clubHtml, /You can ask your own question here\./);
  assert.doesNotMatch(clubHtml, /Open Q&A to browse questions from other members and read the replies\./);
  assert.doesNotMatch(clubHtml, /const latestQuestion = communityBoard\.questions\[0\];/);
  assert.match(clubHtml, /clubCommunityAskFormEl\.style\.display = canAskQuestion \? '' : 'none';/);
  assert.match(clubHtml, /await syncAuthenticatedProfile\(\)\.catch\(\(\) => null\);\s*[\r\n]+\s*await Promise\.all\(\[/);
  assert.match(clubHtml, /const ensureCommunityMemberAuth = async \(\{ redirectOnMissing = false, notify = false \} = \{\}\) => \{/);
  assert.match(clubHtml, /const loggedUser = await syncAuthenticatedProfile\(\);/);
  assert.match(clubHtml, /const submitCommunityQuestion = async \(\) => \{/);
  assert.match(clubHtml, /canEdit:\s*Boolean\(raw\.canEdit\)/);
  assert.match(clubHtml, /canDelete:\s*Boolean\(raw\.canDelete\)/);
  assert.match(clubHtml, /const submitCommunityQuestionUpdate = async \(questionId, rawText\) => \{/);
  assert.match(clubHtml, /const deleteCommunityQuestion = async \(questionId\) => \{/);
  assert.match(clubHtml, /const submitCommunityAnswerUpdate = async \(questionId, answerId, rawText\) => \{/);
  assert.match(clubHtml, /const deleteCommunityAnswer = async \(questionId, answerId\) => \{/);
  assert.match(clubHtml, /const ensureCommunityActorAuth = async \(\{ redirectOnMissing = false, notify = false \} = \{\}\) => \{/);
  assert.match(clubHtml, /openCommunityDeleteConfirm/);
  assert.match(clubHtml, /data-community-question-edit=/);
  assert.match(clubHtml, /data-community-question-delete=/);
  assert.match(clubHtml, /data-community-answer-edit=/);
  assert.match(clubHtml, /data-community-answer-delete=/);
  assert.match(clubHtml, /Questions must be at least 5 characters long\./);
  assert.match(clubHtml, /showCommunityToast\('Question posted\.', 'success'\);/);
  assert.match(clubHtml, /showCommunityToast\('Question updated\.', 'success'\);/);
  assert.match(clubHtml, /showCommunityToast\('Question deleted\.', 'success'\);/);
  assert.match(clubHtml, /showCommunityToast\('Failed to post question\.', 'error'\);/);
  assert.match(clubHtml, /const createdQuestion = normalizeCommunityQuestion\(await res\.json\(\)\.catch\(\(\) => null\)\);/);
  assert.match(clubHtml, /questions:\s*\[createdQuestion,\s*\.\.\.communityBoard\.questions\.filter\(\(question\) => question\.questionId !== createdQuestion\.questionId\)\]/);
  assert.match(clubHtml, /const submitCommunityAnswer = async \(questionId, text\) => \{/);
  assert.match(clubHtml, /showCommunityToast\('Answer posted\.', 'success'\);/);
  assert.match(clubHtml, /showCommunityToast\('Answer updated\.', 'success'\);/);
  assert.match(clubHtml, /showCommunityToast\('Answer deleted\.', 'success'\);/);
  assert.match(clubHtml, /showCommunityToast\('Failed to post answer\.', 'error'\);/);
  assert.match(clubHtml, /\/api\/clubs\/\$\{encodeURIComponent\(clubId\)\}\/community-questions/);
  assert.match(clubHtml, /\/api\/clubs\/\$\{encodeURIComponent\(clubId\)\}\/community-questions\/\$\{encodeURIComponent\(String\(questionId\)\)\}/);
  assert.match(clubHtml, /\/api\/clubs\/\$\{encodeURIComponent\(clubId\)\}\/community-questions\/\$\{encodeURIComponent\(String\(questionId\)\)\}\/answers\/\$\{encodeURIComponent\(String\(answerId\)\)\}/);
  assert.match(clubHtml, /authFetch\('\/api\/profile', \{ cache: 'no-store' \}\)/);
  assert.doesNotMatch(clubHtml, /communityBoard = \{\s*\.\.\.communityBoard,\s*canAsk:\s*true\s*\}/);
  assert.doesNotMatch(clubHtml, /showPageToast\('Question posted\.', 'success'\);/);
  assert.doesNotMatch(clubHtml, /showPageToast\('Answer posted\.', 'success'\);/);
  assert.match(clubChatHtml, /Reply as club staff to this question/);
  assert.match(clubChatHtml, /Visible to everyone viewing this club page\./);
});

test('membership plans open in a floating submenu from the right rail', () => {
  const clubHtml = read('frontend/club.html');

  assert.match(clubHtml, /id="clubMembershipSection"/);
  assert.match(clubHtml, /<span class="membership-copy-eyebrow desktop-badge">Member pricing<\/span>/);
  assert.doesNotMatch(clubHtml, /Booking extras/);
  assert.doesNotMatch(clubHtml, />Plans</);
  assert.match(clubHtml, /id="clubMembershipToggle"/);
  assert.match(clubHtml, /aria-controls="membershipPlansPopover"/);
  assert.match(clubHtml, /id="membershipPlansPopover"/);
  assert.match(clubHtml, /id="membershipPlansTitle"/);
  assert.match(clubHtml, /id="membershipPlansMeta"/);
  assert.match(clubHtml, /id="closeMembershipPlansBtn"/);
  assert.match(clubHtml, /\.membership-popover\s*\{/);
  assert.match(clubHtml, /\.membership-popover-shell\s*\{/);
  assert.match(clubHtml, /\.membership-popover\s+\.membership-grid\s*\{[\s\S]*grid-template-columns:\s*1fr;/);
  assert.match(clubHtml, /\.booking-side-rail\.membership-focus\s+\.booking-chat-rail,[\s\S]*\.booking-side-rail\.membership-focus\s+\.membership-rail > \.membership-section\s*\{[\s\S]*display:\s*none;/);
  assert.match(clubHtml, /\.booking-side-rail\.membership-focus\s+\.membership-popover\s*\{[\s\S]*position:\s*static;[\s\S]*width:\s*100%;/);
  assert.match(clubHtml, /\.booking-side-rail\.membership-focus\s+\.membership-popover-shell\s*\{[\s\S]*max-height:\s*calc\(100dvh - 168px\);[\s\S]*overflow:\s*hidden;[\s\S]*display:\s*flex;[\s\S]*flex-direction:\s*column;[\s\S]*box-shadow:\s*none;/);
  assert.doesNotMatch(clubHtml, /\.booking-side-rail\.membership-focus\s+\.membership-popover-shell\s*\{[\s\S]*min-height:\s*min\(760px,\s*calc\(100dvh - 168px\)\);/);
  assert.match(clubHtml, /let pendingMembershipAutoOpen = \['membershipPlansPopover', 'membershipPlansWrap'\]/);
  assert.match(clubHtml, /const setMembershipPlansOpen = \(open\) => \{/);
  assert.match(clubHtml, /membershipPlansPopoverEl\.hidden = !next;/);
  assert.match(clubHtml, /const syncMembershipPlansMode = \(\) => \{/);
  assert.match(clubHtml, /bookingSideRailEl\?\.classList\.toggle\('membership-focus', railOverlay\);/);
  assert.match(clubHtml, /clubMembershipToggleEl\?\.addEventListener\('click', \(\) => \{/);
  assert.match(clubHtml, /closeMembershipPlansBtnEl\?\.addEventListener\('click', \(\) => \{/);
  assert.match(clubHtml, /updateBookingSideRail\(\);/);
  assert.match(clubHtml, /window\.setTimeout\(\(\) => closeMembershipPlansBtnEl\?\.focus\(\), 60\);/);
  assert.match(clubHtml, /if \(evt\.target\.closest\('#clubMembershipSection'\)\) return;/);
  assert.match(clubHtml, /buildClubPageHref\('membershipPlansPopover'\)/);
  assert.match(clubHtml, /membership-status-card/);
  assert.match(clubHtml, /Member pricing is already applied to eligible bookings\./);
  assert.match(clubHtml, /membership-status-current/);
  assert.doesNotMatch(clubHtml, /Active pass:/);
  assert.doesNotMatch(clubHtml, /clubMembershipToggleEl\.textContent = clubMembershipExpanded \? 'Hide plans' : 'View plans';/);
});

test('club chat composer keeps the send button visible and supports enter-to-send', () => {
  const clubHtml = read('frontend/club.html');

  assert.equal((clubHtml.match(/id="clubChatPanel"/g) || []).length, 1);
  assert.match(clubHtml, /closeClubChatBtnEl\?\.addEventListener\('click', \(\) => \{/);
  assert.match(clubHtml, /bookingSideRailCollapsed = false;[\s\S]*updateBookingSideRail\(\);/);
  assert.match(clubHtml, /\.club-chat-form\s*\{[\s\S]*grid-template-columns:\s*minmax\(0,\s*1fr\)\s+auto;[\s\S]*align-items:\s*end;/);
  assert.match(clubHtml, /\.club-chat-input\s*\{[\s\S]*min-height:\s*52px;[\s\S]*resize:\s*none;[\s\S]*max-height:\s*120px;[\s\S]*overflow-y:\s*hidden;/);
  assert.match(clubHtml, /\.club-chat-send\s*\{[\s\S]*display:\s*inline-flex;[\s\S]*align-self:\s*end;[\s\S]*min-height:\s*44px;/);
  assert.match(clubHtml, /const resizeClubChatInput = \(\) => \{/);
  assert.match(clubHtml, /clubChatInputEl\?\.addEventListener\('input', resizeClubChatInput\);/);
  assert.match(clubHtml, /clubChatInputEl\?\.addEventListener\('keydown', \(evt\) => \{/);
  assert.match(clubHtml, /if \(evt\.key !== 'Enter' \|\| evt\.shiftKey \|\| evt\.isComposing\) return;/);
  assert.match(clubHtml, /clubChatFormEl\?\.requestSubmit\?\.\(\);/);
});

test('stale auth and data-source TODO placeholders were removed from user-facing pages', () => {
  const loginHtml = read('frontend/login.html');
  const homeHtml = read('frontend/home.html');

  assert.doesNotMatch(loginHtml, /TODO: Replace with real backend endpoints/i);
  assert.doesNotMatch(homeHtml, /TODO: Replace with real backend endpoint/i);
  assert.doesNotMatch(homeHtml, /TODO: Replace with backend data fetch/i);
  assert.match(homeHtml, /No clubs found\./);
  assert.match(homeHtml, /No clubs yet\./);
  assert.doesNotMatch(homeHtml, /No clubs match your keyword\./);
  assert.doesNotMatch(homeHtml, /No clubs available yet\./);
});

test('club registration keeps password fields editable while still enforcing email verification on submit', () => {
  const clubRegisterHtml = read('frontend/club register.html');
  const codeSlotMatches = clubRegisterHtml.match(/class="code-slot"/g) || [];

  assert.doesNotMatch(clubRegisterHtml, /Create a club profile to manage schedules, accept bookings, and share updates with members\./);
  assert.doesNotMatch(clubRegisterHtml, /Set availability and manage court schedules in minutes\./);
  assert.doesNotMatch(clubRegisterHtml, /Highlight your club types so members find you faster\./);
  assert.doesNotMatch(clubRegisterHtml, /Keep bookings organized with one shared dashboard\./);
  assert.doesNotMatch(clubRegisterHtml, /Fill in the details below to create your club account\./);
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
  assert.match(clubRegisterHtml, /let authenticatedToken = String\(profileToSave\?\.token \|\| ''\)\.trim\(\);/);
  assert.match(clubRegisterHtml, /if \(!authenticatedToken\) \{/);
  assert.match(clubRegisterHtml, /showStatus\(e\?\.message \|\| 'Club account created, but automatic sign-in failed\. Please log in and continue setup\.', true\);/);
  assert.doesNotMatch(clubRegisterHtml, /clubPass\.disabled\s*=/);
  assert.doesNotMatch(clubRegisterHtml, /clubPass2\.disabled\s*=/);
  assert.doesNotMatch(clubRegisterHtml, /clubRegisterBtn\.disabled\s*=\s*!unlock/);
  assert.doesNotMatch(clubRegisterHtml, /register endpoint does not return token/i);
});

test('user registration uses six verification slots with automatic submit just like club registration', () => {
  const loginHtml = read('frontend/login.html');
  const codeSlotMatches = loginHtml.match(/class="code-slot"/g) || [];

  assert.match(loginHtml, /verification succeeds/i);
  assert.equal(codeSlotMatches.length, 6);
  assert.match(loginHtml, /id="reg-email-code" type="hidden"/);
  assert.doesNotMatch(loginHtml, /placeholder="6-digit code"/i);
  assert.doesNotMatch(loginHtml, /id="reg-verify-code-btn"/);
  assert.match(loginHtml, /const regCodeSlots = Array\.from\(document\.querySelectorAll\('#panel-register \.code-slot'\)\);/);
  assert.match(loginHtml, /function syncRegCodeSlots\(value\)/);
  assert.match(loginHtml, /function clearRegCodeEntry\(focusFirst = false\)/);
  assert.match(loginHtml, /function applyRegCodeFromIndex\(rawValue, startIndex = 0\)/);
  assert.match(loginHtml, /combined\.length === 6 && !regVerifyInFlight/);
  assert.match(loginHtml, /handleRegisterVerifyCode\(\{ auto: true \}\)/);
  assert.match(loginHtml, /inputChangedSinceRequest/);
  assert.match(loginHtml, /shouldRetryLatestCode/);
  assert.match(loginHtml, /clearRegCodeEntry\(true\);/);
});

test('club onboarding entry points replace the previous page in browser history', () => {
  const clubHomeHtml = read('frontend/club home.html');

  assert.match(clubHomeHtml, /window\.location\.replace\('onboarding\.html'\)/);
});

test('onboarding page hides default back UI and blocks browser back in setup mode', () => {
  const onboardingHtml = read('frontend/onboarding.html');
  const onboardingJs = read('frontend/onboarding.js');

  assert.match(onboardingHtml, /onboarding\.css\?v=20260402a/);
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
  assert.match(onboardingJs, /backBtn\.className = 'back-link desktop-back-link';/);
  assert.doesNotMatch(onboardingJs, /window\.history\.go\(1\);/);
  assert.match(onboardingJs, /clearBackButton\(\);/);
  assert.match(onboardingJs, /installOnboardingBackGuard\(\);/);
});

test('club onboarding draft is session-scoped instead of localStorage-backed', () => {
  const onboardingHtml = read('frontend/onboarding.html');
  const onboardingJs = read('frontend/onboarding.js');
  const onboardingLocationHtml = read('frontend/onboarding-location.html');
  const onboardingLocationJs = read('frontend/onboarding-location.js');
  const onboardingCss = read('frontend/onboarding.css');
  const onboardingPromoHtml = read('frontend/onboarding-promo.html');
  const onboardingPromoJs = read('frontend/onboarding-promo.js');

  assert.doesNotMatch(onboardingHtml, /Add your club name and choose the sports your club offers so members can find you\./);
  assert.doesNotMatch(onboardingHtml, /Finish in under a minute\./);
  assert.doesNotMatch(onboardingHtml, /This appears on your public club page\./);
  assert.doesNotMatch(onboardingHtml, /Pick any sports your club offers\./);
  assert.match(onboardingJs, /const ONBOARDING_DRAFT_STORAGE_KEY = 'clubPortal\.onboardingDraft'/);
  assert.match(onboardingJs, /sessionStorage\.getItem\(ONBOARDING_DRAFT_STORAGE_KEY\)/);
  assert.match(onboardingJs, /sessionStorage\.setItem\(ONBOARDING_DRAFT_STORAGE_KEY/);
  assert.doesNotMatch(onboardingJs, /localStorage\.getItem\('clubProfile'\)/);
  assert.doesNotMatch(onboardingJs, /localStorage\.setItem\('clubProfile'/);

  assert.match(onboardingLocationHtml, /onboarding\.css\?v=20260402a/);
  assert.match(onboardingLocationHtml, /onboarding-location\.js\?v=20260327f/);
  assert.match(onboardingLocationHtml, /Step 2 of 3/);
  assert.match(onboardingLocationHtml, /Search on Google Maps or enter full address/);
  assert.doesNotMatch(onboardingLocationHtml, /Choose where your club operates and how many venues you manage\./);
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

  assert.match(onboardingPromoHtml, /onboarding\.css\?v=20260402a/);
  assert.match(onboardingPromoHtml, /Step 3 of 3/);
  assert.match(onboardingPromoHtml, /onboarding-promo\.js\?v=20260412b/);
  assert.doesNotMatch(onboardingPromoHtml, /promoImageUrl/);
  assert.doesNotMatch(onboardingPromoHtml, /Paste image URLs/);
  assert.doesNotMatch(onboardingPromoHtml, /Add a cover photo and a short description to showcase your club\./);
  assert.doesNotMatch(onboardingPromoHtml, /Share a photo and a short story\./);
  assert.doesNotMatch(onboardingPromoHtml, /JPG, PNG, or WEBP; multiple files supported\./);
  assert.doesNotMatch(onboardingPromoHtml, /Add multiple images for your club gallery\./);
  assert.doesNotMatch(onboardingPromoHtml, /Tip: Drag cards to reorder\. The first image is the cover\./);
  assert.doesNotMatch(onboardingPromoHtml, /Keep it short \(1-2 sentences\)\./);
  assert.doesNotMatch(onboardingPromoHtml, /锟\?|ï¿½|�\?/);
  assert.match(onboardingPromoJs, /sessionStorage\.getItem\(ONBOARDING_DRAFT_STORAGE_KEY\)/);
  assert.match(onboardingPromoJs, /sessionStorage\.setItem\(ONBOARDING_DRAFT_STORAGE_KEY/);
  assert.match(onboardingPromoJs, /sessionStorage\.removeItem\(ONBOARDING_DRAFT_STORAGE_KEY\)/);
  assert.match(onboardingPromoJs, /const listClubImages = async \(clubId\) => \{/);
  assert.match(onboardingPromoJs, /const uploadSingleDraftImageToClub = async \(clubId, item, index, knownImageIds\) => \{/);
  assert.match(onboardingPromoJs, /throw new Error\('Failed to confirm club photo upload\.'\);/);
  assert.match(onboardingPromoJs, /const getAuthToken = \(\) => \{/);
  assert.match(onboardingPromoJs, /const authFetchImpl = window\.AuthSession\?\.authFetch;/);
  assert.match(onboardingPromoJs, /if \(!club\) return;/);
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
  assert.match(onboardingCompleteHtml, /onboarding\.css\?v=20260402a/);
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

  assert.doesNotMatch(clubHomeHtml, /workspace-tab-desc/);
  assert.doesNotMatch(clubHomeHtml, /id="sectionDesc"/);
  assert.doesNotMatch(clubHomeHtml, /Manage your club account profile and public content\./);
  assert.doesNotMatch(clubHomeHtml, /Review bookings, member statuses, and attendance changes\./);
  assert.doesNotMatch(clubHomeHtml, /Set membership plans, review active members, and manage club chat replies\./);
  assert.doesNotMatch(clubHomeHtml, /Profile, email, images/);
  assert.doesNotMatch(clubHomeHtml, /Venue and schedule management/);
  assert.doesNotMatch(clubHomeHtml, /Review and update statuses/);
  assert.doesNotMatch(clubHomeHtml, /Manage plans, members, and chat replies/);
  assert.doesNotMatch(clubHomeHtml, /View conversation messages/);
  assert.match(clubHomeHtml, /const DASHBOARD_GUIDE_PENDING_KEY = 'clubPortal\.pendingDashboardGuide'/);
  assert.match(clubHomeHtml, /const DASHBOARD_GUIDE_SEEN_PREFIX = 'clubPortal\.dashboardGuideSeen\.'/);
  assert.match(clubHomeHtml, /id="dashboardGuideOverlay"/);
  assert.match(clubHomeHtml, /First-time guide/);
  assert.match(clubHomeHtml, /Set time slots/);
  assert.match(clubHomeHtml, /Set membership cards/);
  assert.match(clubHomeHtml, /Skip for now/);
  assert.doesNotMatch(clubHomeHtml, /Add at least one time slot and decide whether to offer membership cards\./);
  assert.doesNotMatch(clubHomeHtml, /Open your venue setup page and publish the first bookable time slot for members\./);
  assert.doesNotMatch(clubHomeHtml, /Review the default membership cards and manually turn on the plans you want to sell\./);
  assert.match(clubHomeHtml, /maybeOpenDashboardGuide\(clubs\);/);
  assert.match(clubHomeHtml, /sessionStorage\.getItem\(DASHBOARD_GUIDE_PENDING_KEY\) === '1'/);
  assert.match(clubHomeHtml, /localStorage\.setItem\(getDashboardGuideSeenKey\(clubId\), '1'\)/);
  assert.match(clubHomeHtml, /jumpFromGuideToSection\('venues'\)/);
  assert.match(clubHomeHtml, /jumpFromGuideToSection\('updates'\)/);
});

test('club info page no longer exposes Maps API key override controls', () => {
  const clubInfoHtml = read('frontend/club-info.html');

  assert.match(clubInfoHtml, /id="clubLocationInput" type="text" placeholder="Search on Google Maps or enter full address"/);
  assert.doesNotMatch(clubInfoHtml, /id="clubMapSearchInput"/);
  assert.doesNotMatch(clubInfoHtml, /clubMapSetKeyBtn/);
  assert.doesNotMatch(clubInfoHtml, /Set Maps API key/);
  assert.doesNotMatch(clubInfoHtml, /Override Maps API key/);
  assert.match(clubInfoHtml, /@import url\('https:\/\/fonts\.googleapis\.com\/css2\?family=Sora:wght@400;500;600;700&display=swap'\);/);
  assert.match(clubInfoHtml, /font:\s*14px\/1\.5 'Sora', system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif;/);
  assert.match(clubInfoHtml, /font-family:\s*'Sora', system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif;/);
  assert.match(clubInfoHtml, /input\[type="time"\]::\-webkit-datetime-edit,\s*input\[type="time"\]::\-webkit-date-and-time-value/);
  assert.doesNotMatch(clubInfoHtml, /Google Maps API key was not updated\./);
  assert.doesNotMatch(clubInfoHtml, /promptGoogleMapsApiKey/);
  assert.match(clubInfoHtml, /id="clubMapStatus" class="status" aria-live="polite" hidden/);
  assert.match(clubInfoHtml, /Google Maps is temporarily unavailable\. Please enter the address manually\./);
  assert.doesNotMatch(clubInfoHtml, /Google Maps ready\. Search or click map to select address\./);
  assert.match(clubInfoHtml, /clubMapStatusEl\.hidden = !normalized;/);
  assert.match(clubInfoHtml, /mapAutocomplete = new maps\.places\.Autocomplete\(clubLocationInput,/);
  assert.doesNotMatch(clubInfoHtml, /new maps\.places\.Autocomplete\(clubMapSearchInput,/);
  assert.match(clubInfoHtml, /\.map-panel\s*\{[\s\S]*display:\s*grid;[\s\S]*gap:\s*8px;/);
  assert.match(clubInfoHtml, /\.image-grid\s*\{[\s\S]*grid-auto-rows:\s*1fr;/);
  assert.match(clubInfoHtml, /\.image-card\s*\{[\s\S]*grid-template-rows:\s*220px minmax\(0,\s*1fr\);/);
  assert.match(clubInfoHtml, /\.image-media\s*\{[\s\S]*height:\s*220px;/);
  assert.match(clubInfoHtml, /\.image-meta\s*\{[\s\S]*grid-template-rows:\s*auto minmax\(0,\s*1fr\) auto;/);
  assert.match(clubInfoHtml, /\.image-actions\s*\{[\s\S]*margin-top:\s*auto;/);
  assert.match(clubInfoHtml, /actions\.className = 'image-actions';/);
  assert.match(clubInfoHtml, /No images yet\./);
  assert.match(clubInfoHtml, /No club yet\./);
  assert.doesNotMatch(clubInfoHtml, /No images uploaded yet\./);
  assert.doesNotMatch(clubInfoHtml, /No club found yet\. You can create one here\./);
  assert.doesNotMatch(clubInfoHtml, /\.map-toolbar\s*\{/);
});

test('club admin added time slots list shows each slot under its real date', () => {
  const clubAdminHtml = read('frontend/club-admin.html');

  assert.match(clubAdminHtml, /const formatSlotDateLabel = \(dateISO\) =>/);
  assert.match(clubAdminHtml, /\.slot-date-group\s*\{/);
  assert.match(clubAdminHtml, /const list = weekSlots\s*\.filter\(s => String\(s\.venueId\) === String\(selectedVenueId\)\)\s*\.sort/);
  assert.match(clubAdminHtml, /const slotDateISO = parseDateISO\(s\.startTime\) \|\| 'Unknown date';/);
  assert.match(clubAdminHtml, /slotEmpty\.textContent = "No time slots yet for this venue in the next 7 days\."/);
  assert.match(clubAdminHtml, /message: `\$\{slotDateISO\} \$\{startHHMM\}-\$\{endHHMM\}`/);
  assert.doesNotMatch(clubAdminHtml, /\.filter\(s => String\(s\.venueId\) === String\(selectedVenueId\) && parseDateISO\(s\.startTime\) === selectedDayISO\)/);
  assert.doesNotMatch(clubAdminHtml, /<strong>Date:<\/strong> \$\{escapeHtml\(String\(selectedDayISO\)\)\}/);
});

test('membership plans support custom cards and booking-pack benefits across admin and public flows', () => {
  const clubUpdatesHtml = read('frontend/club updates.html');
  const clubHtml = read('frontend/club.html');
  const joinHtml = read('frontend/join.html');
  const paymentHtml = read('frontend/payment.html');
  const userProfileJs = read('frontend/user-profile.js');

  assert.match(clubUpdatesHtml, /id="addCustomPlanBtn"/);
  assert.match(clubUpdatesHtml, /Add membership plan/);
  assert.match(clubUpdatesHtml, /const planOriginalPayloadByKey = new Map\(\);/);
  assert.match(clubUpdatesHtml, /const persistPlanCardState = \(cardOrKey\) => \{/);
  assert.match(clubUpdatesHtml, /persistPlanCardState\(btn\.closest\('\[data-plan-key\]'\)\);/);
  assert.match(clubUpdatesHtml, /id="showSavedPlansBtn"/);
  assert.match(clubUpdatesHtml, /Added plans/);
  assert.match(clubUpdatesHtml, /id="planSavedCount"/);
  assert.match(clubUpdatesHtml, /id="planSavedInlineCount"/);
  assert.match(clubUpdatesHtml, /id="planDraftSection"/);
  assert.match(clubUpdatesHtml, /id="planDraftEmpty"/);
  assert.match(clubUpdatesHtml, /id="planDraftList"/);
  assert.match(clubUpdatesHtml, /id="planSavedSection"/);
  assert.match(clubUpdatesHtml, /id="planSavedList"/);
  assert.match(clubUpdatesHtml, /data-field="benefitType"/);
  assert.match(clubUpdatesHtml, /Booking pack/);
  assert.match(clubUpdatesHtml, /data-field="includedBookings"/);
  assert.match(clubUpdatesHtml, /class="plan-fields"/);
  assert.match(clubUpdatesHtml, /data-toggle-plan-note=/);
  assert.match(clubUpdatesHtml, /Optional note/);
  assert.match(clubUpdatesHtml, /Create plan/);
  assert.match(clubUpdatesHtml, /const setPlanViewMode = \(mode\) => \{/);
  assert.match(clubUpdatesHtml, /planDraftSectionEl\) planDraftSectionEl\.hidden = planViewMode !== 'draft';/);
  assert.match(clubUpdatesHtml, /planSavedSectionEl\) planSavedSectionEl\.hidden = planViewMode !== 'saved';/);
  assert.match(clubUpdatesHtml, /showSavedPlansBtnEl\?\.addEventListener\('click'/);
  assert.match(clubUpdatesHtml, /setPlanViewMode\('saved'\);/);
  assert.match(clubUpdatesHtml, /membershipPlanDrafts = \[draft, \.\.\.membershipPlanDrafts\];/);
  assert.match(clubUpdatesHtml, /showStatus\(`\$\{payload\.planName \|\| 'Membership plan'\} created\. Manage it from Added plans\.`/);
  assert.match(clubUpdatesHtml, /Enabled for members/);
  assert.match(clubUpdatesHtml, /No members yet\./);
  assert.doesNotMatch(clubUpdatesHtml, /Offer this plan to members/);
  assert.doesNotMatch(clubUpdatesHtml, /No member has purchased a pass for this club yet\./);
  assert.match(clubUpdatesHtml, /\$\{plan\.draft \? '' : `\s*<div class="plan-toggle-row">/);
  assert.match(clubUpdatesHtml, /\/api\/my\/clubs\/\$\{encodeURIComponent\(String\(activeClubId\)\)\}\/membership-plans\$\{isDraft \? '' : ''\}/);
  assert.match(clubUpdatesHtml, /method:\s*isDraft \? 'POST' : 'PUT'/);
  assert.match(clubUpdatesHtml, /\/membership-plans\/\$\{encodeURIComponent\(String\(planId\)\)\}/);

  assert.match(clubHtml, /Included with pass/);
  assert.match(clubHtml, /membershipBenefitType/);
  assert.match(clubHtml, /membershipRemainingBookings/);
  assert.match(joinHtml, /Pack booking/);
  assert.match(joinHtml, /bookings included/);
  assert.match(paymentHtml, /prepaid bookings/);
  assert.match(userProfileJs, /credits left/);
});

test('public club page shows the booking schedule before optional membership cards', () => {
  const clubHtml = read('frontend/club.html');
  const desktopCss = read('frontend/desktop-consistency.css');
  const scheduleIndex = clubHtml.indexOf('id="schedule"');
  const membershipIndex = clubHtml.indexOf('id="clubMembershipSection"');

  assert.ok(scheduleIndex >= 0, 'schedule container should exist');
  assert.ok(membershipIndex >= 0, 'membership section should exist');
  assert.ok(scheduleIndex < membershipIndex, 'schedule should appear before membership cards');
  assert.match(clubHtml, /class="booking-detail-layout"/);
  assert.match(clubHtml, /<div id="weekTabs"[\s\S]*?<div class="booking-detail-layout">/);
  assert.match(clubHtml, /\.club-page-grid\s*\{[\s\S]*?grid-template-columns:\s*minmax\(0,\s*1fr\)\s+minmax\(0,\s*320px\);[\s\S]*?gap:\s*18px;/);
  assert.match(clubHtml, /\.booking-side-rail\s*\{[\s\S]*?position:\s*relative;[\s\S]*?width:\s*100%;[\s\S]*?justify-self:\s*stretch;/);
  assert.doesNotMatch(clubHtml, /\.booking-side-rail\s*\{[\s\S]*?grid-template-rows:\s*auto minmax\(0,\s*1fr\) auto;/);
  assert.match(clubHtml, /\.booking-side-rail-stack\s*\{[\s\S]*position:\s*relative;[\s\S]*width:\s*100%;[\s\S]*max-height:\s*none;[\s\S]*overflow:\s*visible;/);
  assert.match(clubHtml, /\.club-page-grid\.rail-collapsed\s*\{[\s\S]*grid-template-columns:\s*minmax\(0,\s*1fr\)\s+56px;/);
  assert.match(clubHtml, /\.booking-side-rail\.is-collapsed\s+\.booking-side-rail-stack\s*\{[\s\S]*max-height:\s*0;/);
  assert.match(clubHtml, /\.club-page-grid\s*\{[\s\S]*transition:\s*grid-template-columns \.24s ease, gap \.24s ease;/);
  assert.match(clubHtml, /id="bookingSideRailToggle"/);
  assert.match(clubHtml, /id="bookingSideRailToggleIcon" class="booking-side-rail-toggle-icon" aria-hidden="true">&rarr;<\/span>[\s\S]*id="bookingSideRailToggleLabel" class="booking-side-rail-toggle-label" aria-hidden="true">Open club chat, Q&amp;A &amp; plans<\/span>/);
  assert.doesNotMatch(clubHtml, /<div id="bookingSideRail" class="booking-side-rail is-collapsed">[\s\S]*id="bookingSideRailToggle"/);
  assert.match(clubHtml, /\.booking-side-rail-toggle\s*\{[\s\S]*border-radius:\s*999px;[\s\S]*display:\s*inline-flex;[\s\S]*align-items:\s*center;/);
  assert.match(clubHtml, /\.booking-side-rail-toggle-label\s*\{[\s\S]*font-size:\s*13px;[\s\S]*white-space:\s*nowrap;/);
  assert.match(clubHtml, /bookingSideRailToggleIconEl\.textContent = collapsed \? '→' : '←';/);
  assert.match(clubHtml, /bookingSideRailToggleLabelEl\.textContent = collapsed \? 'Open club chat, Q&A & plans' : 'Back to booking';/);
  assert.match(clubHtml, /const overlayPanelOpen = collapsible[\s\S]*chatWidgetOpen \|\| clubMembershipExpanded \|\| communityDialogOpen\);/);
  assert.match(clubHtml, /bookingSideRailToggleEl\?\.toggleAttribute\('hidden', !collapsible \|\| overlayPanelOpen\);/);
  assert.match(clubHtml, /const pageMainEl = document\.querySelector\('\.page-main'\);/);
  assert.match(clubHtml, /let bookingSideRailCollapsed = true;/);
  assert.match(clubHtml, /updateBookingSideRail\(\);/);
  assert.match(clubHtml, /pageMainEl\?\.classList\.toggle\('rail-open', !collapsed\);/);
  assert.doesNotMatch(clubHtml, /pageMainEl\?\.classList\.toggle\('chat-focus', chatWidgetOpen\);/);
  assert.match(clubHtml, /\.page-main\s*\{[\s\S]*padding:\s*20px 0 28px;/);
  assert.match(desktopCss, /body\.desktop-page--club\.page-with-footer \{[\s\S]*grid-template-rows:\s*auto minmax\(0,\s*1fr\) auto;[\s\S]*padding-bottom:\s*0;/);
  assert.match(desktopCss, /body\.desktop-page--club\.page-with-footer > \.site-footer \{[\s\S]*position:\s*static;/);
  assert.match(clubHtml, /window\.AuthSession\?\.authFetch/);
  assert.match(clubHtml, /window\.AuthSession\?\.getToken\?\.\(\)/);
  assert.match(clubHtml, /localStorage\.getItem\('loggedUser'\)\s*\|\|\s*localStorage\.getItem\('user'\)/);
  assert.match(clubHtml, /const isCommunityQuestionAllowedForUser = \(\) => Boolean\(communityBoard\.canAsk\);/);
  assert.match(clubHtml, /const isCommunityAnswerAllowedForUser = \(\) => Boolean\(communityBoard\.canAnswer && !communityBoard\.canReplyAsClub\);/);
  assert.match(clubHtml, /const loggedUser = await syncAuthenticatedProfile\(\);/);
  assert.match(clubHtml, /No questions yet\./);
  assert.doesNotMatch(clubHtml, /No questions yet\. Ask the first one and help future members\./);
  assert.doesNotMatch(clubHtml, /@media \(min-width: 1400px\)[\s\S]*?position:\s*fixed;/);
});

test('club updates page manages club-level chat knowledge base entries', () => {
  const clubUpdatesHtml = read('frontend/club updates.html');
  const clubHomeHtml = read('frontend/club home.html');

  assert.match(clubUpdatesHtml, /Memberships &amp; chat replies/);
  assert.match(clubUpdatesHtml, /data-section-card="chat-kb"/);
  assert.match(clubUpdatesHtml, /Chat knowledge base/);
  assert.match(clubUpdatesHtml, /Add entry/);
  assert.match(clubUpdatesHtml, /id="showKbSavedBtn"/);
  assert.match(clubUpdatesHtml, /Added entries/);
  assert.match(clubUpdatesHtml, /id="kbSavedCount"/);
  assert.match(clubUpdatesHtml, /id="kbSavedInlineCount"/);
  assert.match(clubUpdatesHtml, /id="kbDraftSection"/);
  assert.match(clubUpdatesHtml, /id="kbDraftEmpty"/);
  assert.match(clubUpdatesHtml, /id="kbDraftList"/);
  assert.match(clubUpdatesHtml, /id="kbSavedSection"/);
  assert.match(clubUpdatesHtml, /No draft plan yet\./);
  assert.match(clubUpdatesHtml, /No added plans yet\./);
  assert.match(clubUpdatesHtml, /No draft entry yet\./);
  assert.match(clubUpdatesHtml, /No saved entries yet\./);
  assert.match(clubUpdatesHtml, /\.header-wrap h2,\s*\.page-shell h1,\s*\.page-shell h2,\s*\.page-shell h3\s*\{[\s\S]*font-family:\s*"Sora",\s*sans-serif;/);
  assert.match(clubUpdatesHtml, /\.section-head h2\s*\{[\s\S]*font-size:\s*24px;/);
  assert.match(clubUpdatesHtml, /\.plan-grid\s*\{[\s\S]*grid-template-columns:\s*repeat\(auto-fit,\s*minmax\(190px,\s*1fr\)\)/);
  assert.match(clubUpdatesHtml, /\.plan-card\.expanded\s*\{[\s\S]*grid-column:\s*1 \/ -1;/);
  assert.match(clubUpdatesHtml, /id="kbStatusBox"/);
  assert.match(clubUpdatesHtml, /id="kbList"/);
  assert.match(clubUpdatesHtml, /const kbListEl = document\.getElementById\('kbList'\)/);
  assert.match(clubUpdatesHtml, /let expandedPlanKey = null;/);
  assert.match(clubUpdatesHtml, /const formatPlanMetricBenefit = \(plan\) => \{/);
  assert.match(clubUpdatesHtml, /data-toggle-plan-editor=/);
  assert.match(clubUpdatesHtml, /expandedPlanKey = draft\.key;/);
  assert.match(clubUpdatesHtml, /const showKbSavedBtnEl = document\.getElementById\('showKbSavedBtn'\)/);
  assert.match(clubUpdatesHtml, /const kbDraftSectionEl = document\.getElementById\('kbDraftSection'\)/);
  assert.match(clubUpdatesHtml, /const kbDraftEmptyEl = document\.getElementById\('kbDraftEmpty'\)/);
  assert.match(clubUpdatesHtml, /const kbDraftListEl = document\.getElementById\('kbDraftList'\)/);
  assert.match(clubUpdatesHtml, /const showSavedPlansBtnEl = document\.getElementById\('showSavedPlansBtn'\)/);
  assert.match(clubUpdatesHtml, /const planDraftSectionEl = document\.getElementById\('planDraftSection'\)/);
  assert.match(clubUpdatesHtml, /const planDraftEmptyEl = document\.getElementById\('planDraftEmpty'\)/);
  assert.match(clubUpdatesHtml, /const planDraftListEl = document\.getElementById\('planDraftList'\)/);
  assert.match(clubUpdatesHtml, /const planSavedSectionEl = document\.getElementById\('planSavedSection'\)/);
  assert.match(clubUpdatesHtml, /const planSavedListEl = document\.getElementById\('planSavedList'\)/);
  assert.match(clubUpdatesHtml, /const syncPlanSavedSummary = \(count\) => \{/);
  assert.match(clubUpdatesHtml, /const syncPlanDraftListVisibility = \(\) => \{/);
  assert.doesNotMatch(clubUpdatesHtml, /id="planIntro"/);
  assert.doesNotMatch(clubUpdatesHtml, /\.section-tip\s*\{/);
  assert.doesNotMatch(clubUpdatesHtml, /\.plan-duration\s*\{/);
  assert.doesNotMatch(clubUpdatesHtml, /\.plan-availability\s*\{/);
  assert.doesNotMatch(clubUpdatesHtml, /\.plan-note\s*\{/);
  assert.doesNotMatch(clubUpdatesHtml, /See active pass holders\./);
  assert.doesNotMatch(clubUpdatesHtml, /Add standard chatbot replies\./);
  assert.doesNotMatch(clubUpdatesHtml, /Use this for simple questions only\./);
  assert.doesNotMatch(clubUpdatesHtml, /Click <strong>Add entry<\/strong> to create a new question and standard reply for this club chatbot\./);
  assert.doesNotMatch(clubUpdatesHtml, /Visible on the club page/);
  assert.doesNotMatch(clubUpdatesHtml, /Hidden from new purchases/);
  assert.doesNotMatch(clubUpdatesHtml, /Members get .* prepaid booking credit/);
  assert.doesNotMatch(clubUpdatesHtml, /Members with this pass can book eligible slots for free while the pass is active\./);
  assert.doesNotMatch(clubUpdatesHtml, /Members with this pass pay the normal slot price minus this discount\./);
  assert.match(clubUpdatesHtml, /const kbSavedSectionEl = document\.getElementById\('kbSavedSection'\)/);
  assert.match(clubUpdatesHtml, /const syncKbSavedSummary = \(count\) => \{/);
  assert.match(clubUpdatesHtml, /const syncKbDraftListVisibility = \(\) => \{/);
  assert.match(clubUpdatesHtml, /const setKbViewMode = \(mode\) => \{/);
  assert.match(clubUpdatesHtml, /kbDraftSectionEl\) kbDraftSectionEl\.hidden = kbViewMode !== 'draft';/);
  assert.match(clubUpdatesHtml, /kbSavedSectionEl\) kbSavedSectionEl\.hidden = kbViewMode !== 'saved';/);
  assert.match(clubUpdatesHtml, /const loadChatKb = async \(\) => \{/);
  assert.match(clubUpdatesHtml, /\/api\/my\/clubs\/\$\{encodeURIComponent\(String\(activeClubId\)\)\}\/chat-kb/);
  assert.match(clubUpdatesHtml, /<label>Question<\/label>/);
  assert.match(clubUpdatesHtml, /answerText/);
  assert.doesNotMatch(clubUpdatesHtml, /The chatbot handles close phrasings, similarity, and language matching automatically for this club reply\./i);
  assert.doesNotMatch(clubUpdatesHtml, /Write the exact reply members should receive when this club-specific question is matched\./i);
  assert.doesNotMatch(clubUpdatesHtml, /Low-risk FAQ only\. High-risk requests still stay on the existing safe chat path\./i);
  assert.doesNotMatch(clubUpdatesHtml, /<label>Question title<\/label>/);
  assert.doesNotMatch(clubUpdatesHtml, /<label>Language<\/label>/);
  assert.doesNotMatch(clubUpdatesHtml, /<label>Trigger keywords<\/label>/);
  assert.doesNotMatch(clubUpdatesHtml, /<label>Example questions<\/label>/);
  assert.doesNotMatch(clubUpdatesHtml, /<label>Priority<\/label>/);
  assert.doesNotMatch(clubUpdatesHtml, /Enable this reply/);
  assert.match(clubUpdatesHtml, /setKbViewMode\('draft'\);/);
  assert.match(clubUpdatesHtml, /showKbSavedBtnEl\?\.addEventListener\('click', \(\) => \{/);
  assert.match(clubUpdatesHtml, /setKbViewMode\('saved'\);/);
  assert.match(clubUpdatesHtml, /kbSavedSectionEl\?\.scrollIntoView\(\{ behavior: 'smooth', block: 'start' \}\)/);
  assert.match(clubUpdatesHtml, /Promise\.all\(\[loadPlans\(\), loadMembers\(\), loadChatKb\(\)\]\)/);

  assert.match(clubHomeHtml, /Memberships &amp; FAQ/);
  assert.match(clubHomeHtml, /club updates\.html\?v=20260328-chat-kb-b/);
  assert.doesNotMatch(clubHomeHtml, /manage club chat replies/i);
});

test('user profile layout uses wider desktop sizing and a low-emphasis back link', () => {
  const userHtml = read('frontend/user.html');
  const desktopCss = read('frontend/desktop-consistency.css');
  const userProfileJs = read('frontend/user-profile.js');

  assert.match(desktopCss, /body\.desktop-page--user \.wrap \{[\s\S]*width:\s*min\(var\(--desktop-shell-max\), calc\(100vw - 64px\)\);/);
  assert.match(userHtml, /\.link-back\s*\{\s*justify-self:\s*start;\s*\}/);
  assert.match(userHtml, /\.header-wrap\s*\{[\s\S]*grid-template-columns:\s*minmax\(0,\s*1fr\)\s+auto\s+minmax\(0,\s*1fr\)/);
  assert.match(userHtml, /background:\s*rgba\(255,\s*255,\s*255,\s*0\.9\)/);
  assert.match(desktopCss, /body\.desktop-consistency \.desktop-back-link::before,[\s\S]*body\.desktop-consistency \.link-back::before \{[\s\S]*content:\s*'<';/);
  assert.doesNotMatch(userHtml, /\.link-back:hover\s*\{[^}]*transform:/);
  assert.doesNotMatch(userHtml, /\.link-back\s*\{[^}]*position:\s*absolute/);
  assert.match(userHtml, /grid-template-columns:\s*280px minmax\(0,\s*1fr\)/);
  assert.match(userHtml, /backdrop-filter:\s*blur\(14px\)/);
  assert.match(userProfileJs, /booking-pill desktop-badge/);
});

test('user profile email update uses six verification slots with automatic submit', () => {
  const userHtml = read('frontend/user.html');
  const userProfileJs = read('frontend/user-profile.js');
  const codeSlotMatches = userHtml.match(/class="code-slot"/g) || [];

  assert.equal(codeSlotMatches.length, 6);
  assert.match(userHtml, /id="emailCodeInput" type="hidden"/);
  assert.match(userHtml, /Verification will start automatically/i);
  assert.match(userHtml, /<script type="module" src="user-profile\.js\?v=20\d{6}[a-z]"><\/script>/);
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
  assert.match(clubBookingsHtml, /<span class="stat-label">Booked<\/span>/);
  assert.match(clubBookingsHtml, /<span class="stat-label">No-show<\/span>/);
  assert.match(clubBookingsHtml, /<span class="stat-label">Closed<\/span>/);
  assert.match(clubBookingsHtml, /const attendanceKey = \(member, slot\) => \{/);
  assert.match(clubBookingsHtml, /label: 'Check in'/);
  assert.match(clubBookingsHtml, /label: 'Close'/);
  assert.match(clubBookingsHtml, /attendance === 'booked' \|\| attendance === 'checked'/);
  assert.doesNotMatch(clubBookingsHtml, /label: 'Approve'/);
  assert.doesNotMatch(clubBookingsHtml, /label: 'Revert'/);
  assert.doesNotMatch(clubBookingsHtml, /label: 'Reopen'/);
  assert.match(clubBookingsHtml, /const payload = await res\.json\(\)\.catch\(\(\) => \(\{\}\)\);/);
  assert.match(clubBookingsHtml, /slotBookings = slotBookings\.map\(\(slot\) => \(\{/);
  assert.match(clubBookingsHtml, /renderStats\(slotBookings\);/);
  assert.match(clubBookingsHtml, /renderGroupedView\(slotBookings\);/);
  assert.match(clubBookingsHtml, /No members yet\./);
  assert.match(userProfileJs, /No upcoming bookings yet\./);
  assert.match(userProfileJs, /No memberships yet\./);
  assert.doesNotMatch(clubBookingsHtml, /No members booked yet\./);
  assert.doesNotMatch(userProfileJs, /No upcoming bookings yet\. Go to Home and pick a club slot\./);
  assert.doesNotMatch(userProfileJs, /You have not purchased any club memberships yet\./);
});

test('user security form keeps current password on its own row and splits new passwords into two columns', () => {
  const userHtml = read('frontend/user.html');
  const userProfileJs = read('frontend/user-profile.js');

  assert.match(userHtml, /\.security-field\.current\s*\{\s*grid-column:\s*1\s*\/\s*-1;/);
  assert.match(userHtml, /id="securityForm" class="security-form"/);
  assert.match(userHtml, /id="passwordProviderCard" class="security-provider-card" hidden/);
  assert.match(userHtml, /Password managed by Google/);
  assert.match(userHtml, /<div class="security-field current">\s*<label for="currentPassInput">Current password<\/label>/);
  assert.match(userHtml, /<div class="security-field">\s*<label for="newPassInput">New password<\/label>/);
  assert.match(userHtml, /<div class="security-field confirm">\s*<label for="confirmPassInput">Confirm new password<\/label>/);
  assert.match(userProfileJs, /const applyPasswordChangeAvailability = \(\.\.\.sources\) => \{/);
  assert.match(userProfileJs, /source\.authProvider/);
  assert.match(userProfileJs, /source\.canChangePassword/);
  assert.match(userProfileJs, /Password changes are managed in your Google account\./);
  assert.match(userProfileJs, /securityGridEl\?\.toggleAttribute\('hidden', !passwordChangeAllowed\);/);
  assert.match(userProfileJs, /passwordProviderCardEl\?\.toggleAttribute\('hidden', passwordChangeAllowed\);/);
});
