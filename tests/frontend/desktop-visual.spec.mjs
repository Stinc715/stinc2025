import { test, expect } from '@playwright/test';

const FIXED_NOW = '2026-04-02T09:30:00.000Z';
const CLUB_ID = '101';

const clubImageOne = createSvgDataUri({
  width: 1280,
  height: 640,
  background: '#f3f4f6',
  accent: '#111827',
  label: 'Court A'
});

const clubImageTwo = createSvgDataUri({
  width: 1280,
  height: 640,
  background: '#e2e8f0',
  accent: '#1d4ed8',
  label: 'Court B'
});

const avatarSvg = createSvgDataUri({
  width: 128,
  height: 128,
  background: '#eef2ff',
  accent: '#4f46e5',
  label: 'SM'
});

function createSvgDataUri({ width, height, background, accent, label }) {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
      <rect width="${width}" height="${height}" rx="24" fill="${background}"/>
      <rect x="${Math.round(width * 0.08)}" y="${Math.round(height * 0.12)}" width="${Math.round(width * 0.84)}" height="${Math.round(height * 0.76)}" rx="28" fill="${accent}" opacity="0.12"/>
      <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="${accent}" font-family="Segoe UI, Arial, sans-serif" font-size="${Math.round(width * 0.09)}" font-weight="700">${label}</text>
    </svg>
  `.trim();
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
}

function jsonResponse(route, data, status = 200) {
  return route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify(data)
  });
}

function clubPayload() {
  return {
    id: Number(CLUB_ID),
    name: 'manba basketball',
    description: 'Indoor courts for casual sessions, league runs, and coach-led training.',
    category: 'Basketball',
    tags: ['Volleyball', 'Skating'],
    location: '600 Cape May St, Harrison, NJ 07029 United States',
    openingStart: '10:00',
    openingEnd: '20:00',
    courtsCount: 5,
    email: 'hello@manbabasketball.example',
    phone: '+44 20 7000 0000'
  };
}

function membershipPlanPayload() {
  return [
    {
      planId: 301,
      planName: 'Monthly Pass',
      durationDays: 30,
      discountPercent: 20,
      price: 29.99,
      description: 'Save on published member prices for all eligible bookings.'
    }
  ];
}

function membershipStatusPayload() {
  return {
    clubId: Number(CLUB_ID),
    planId: 301,
    planName: 'Monthly Pass',
    status: 'ACTIVE',
    endDate: '2026-05-07'
  };
}

function adminMembershipPlansPayload() {
  return [
    {
      planId: 301,
      planCode: 'MONTHLY',
      planName: 'Monthly Pass',
      benefitType: 'DISCOUNT',
      durationDays: 30,
      discountPercent: 20,
      includedBookings: 0,
      price: 49,
      enabled: true,
      standardPlan: false,
      description: 'Save 20% on member bookings this month.'
    },
    {
      planId: 302,
      planCode: '',
      planName: '10 booking pack',
      benefitType: 'BOOKING_PACK',
      durationDays: 45,
      discountPercent: 0,
      includedBookings: 10,
      price: 45,
      enabled: true,
      standardPlan: false,
      description: 'Prepaid credits for regular members.'
    }
  ];
}

function clubMembershipMembersPayload() {
  return [
    {
      userId: 501,
      memberName: 'Sonia Member',
      email: 'sonia@example.com',
      planName: 'Monthly Pass',
      planPrice: 49,
      discountPercent: 20,
      benefitType: 'DISCOUNT',
      includedBookings: 0,
      remainingBookings: 0,
      startDate: '2026-04-01',
      endDate: '2026-05-01',
      status: 'ACTIVE'
    },
    {
      userId: 502,
      memberName: 'Chris Booker',
      email: 'chris@example.com',
      planName: '10 booking pack',
      planPrice: 45,
      discountPercent: 0,
      benefitType: 'BOOKING_PACK',
      includedBookings: 10,
      remainingBookings: 7,
      startDate: '2026-04-01',
      endDate: '2026-05-16',
      status: 'ACTIVE'
    }
  ];
}

function clubChatKbPayload() {
  return [
    {
      id: 41,
      questionTitle: 'When are you open?',
      answerText: 'Weekdays 10:00-22:00 and weekends 12:00-20:00.'
    },
    {
      id: 42,
      questionTitle: 'How do I contact the club?',
      answerText: 'Use the chat for simple questions or email hello@manbabasketball.example.'
    }
  ];
}

function communityBoardPayload() {
  return {
    canAsk: true,
    canAnswer: true,
    canReplyAsClub: false,
    questionCount: 1,
    questions: [
      {
        questionId: 901,
        authorName: 'Sonia Member',
        questionText: 'Do you provide basketballs on site?',
        clubAnswered: true,
        answerCount: 2,
        createdAt: '2026-04-01T10:00:00Z',
        updatedAt: '2026-04-01T10:10:00Z',
        answers: [
          {
            answerId: 7001,
            authorName: 'manba basketball',
            responderType: 'CLUB',
            answerText: 'Yes. Members can borrow practice basketballs from the front desk.',
            createdAt: '2026-04-01T10:04:00Z'
          },
          {
            answerId: 7002,
            authorName: 'Chris Booker',
            responderType: 'USER',
            answerText: 'I used the desk balls last week and they were ready to go.',
            createdAt: '2026-04-01T10:10:00Z'
          }
        ]
      }
    ]
  };
}

function myBookingsPayload() {
  return [
    {
      bookingId: 25,
      clubId: Number(CLUB_ID),
      clubName: 'manba basketball',
      venueName: 'Court A',
      timeslotId: 501,
      startTime: '2026-04-03T15:00:00Z',
      endTime: '2026-04-03T16:00:00Z',
      price: 0.8,
      status: 'PENDING',
      bookingVerificationCode: '565767',
      orderNo: 'BK-20260402093000-001257'
    }
  ];
}

function myMembershipsPayload() {
  return [
    {
      clubId: Number(CLUB_ID),
      clubName: 'manba basketball',
      planName: 'Monthly Pass',
      status: 'ACTIVE',
      startDate: '2026-04-01',
      endDate: '2026-05-01',
      planPrice: 29.99,
      discountPercent: 20,
      orderNo: 'MB-20260401090000-008311'
    }
  ];
}

function clubConversationsPayload() {
  return [
    {
      userId: 501,
      userName: 'Sonia Member',
      unreadCount: 0,
      clubUnreadCount: 0,
      chatMode: 'HUMAN',
      handoffRequestedAt: '2026-04-01T08:50:00Z',
      handoffReason: 'Human support requested',
      lastSender: 'club',
      lastMessageAt: '2026-04-01T09:06:00Z',
      lastMessagePreview: 'We can hold Court A for ten minutes while you confirm.',
      messages: []
    },
    {
      userId: 502,
      userName: 'Chris Booker',
      unreadCount: 2,
      clubUnreadCount: 2,
      chatMode: 'AI',
      handoffRequestedAt: '',
      handoffReason: '',
      lastSender: 'member',
      lastMessageAt: '2026-04-01T08:15:00Z',
      lastMessagePreview: 'Is member pricing already applied at checkout?',
      messages: []
    }
  ];
}

function clubConversationDetailPayload() {
  return {
    userId: 501,
    userName: 'Sonia Member',
    unreadCount: 0,
    clubUnreadCount: 0,
    chatMode: 'HUMAN',
    handoffRequestedAt: '2026-04-01T08:50:00Z',
    handoffReason: 'Human support requested',
    messages: [
      {
        messageId: 8001,
        sender: 'member',
        text: 'Can you help me confirm whether Court A still has room this Friday?',
        createdAt: '2026-04-01T08:50:00Z',
        readByClub: true,
        readByUser: true
      },
      {
        messageId: 8002,
        sender: 'system',
        text: 'Member requested human support.',
        createdAt: '2026-04-01T08:51:00Z',
        readByClub: true,
        readByUser: true
      },
      {
        messageId: 8003,
        sender: 'club',
        text: 'We can hold Court A for ten minutes while you confirm.',
        createdAt: '2026-04-01T09:06:00Z',
        readByClub: true,
        readByUser: false
      }
    ]
  };
}

function clubChatMessagesPayload() {
  return {
    sessionId: 71,
    chatMode: 'AI',
    clubUnreadCount: 0,
    messages: [
      {
        messageId: 6101,
        sender: 'assistant',
        text: 'Hi. Ask about hours, pricing, memberships, or published slots.',
        createdAt: '2026-04-01T08:00:00Z',
        answerSource: 'CLUB_CHAT_FALLBACK',
        matchedFaqId: null,
        handoffSuggested: false,
        readByUser: true
      }
    ]
  };
}

function checkoutDetailPayload() {
  return {
    sessionId: 'chk_demo_checkout',
    orderNo: 'BK-20260402093000-001257',
    type: 'BOOKING',
    title: 'Booking payment',
    subtitle: 'Confirm the booking for Court A.',
    status: 'CREATED',
    provider: 'VIRTUAL_CHECKOUT',
    canContinueCheckout: false,
    canCancel: true,
    amount: 0.8,
    currency: 'GBP',
    clubName: 'manba basketball',
    venueName: 'Court A',
    slotStartTime: '2026-04-03T15:00:00Z',
    slotEndTime: '2026-04-03T16:00:00Z',
    expiresAt: '2026-04-02T10:00:00Z',
    checkoutUrl: '',
    returnUrl: `club.html?club=${CLUB_ID}`
  };
}

function timeslotsPayload() {
  return [
    {
      timeslotId: 501,
      venueId: 1,
      venueName: 'Court A',
      startTime: '2026-04-02T15:00:00Z',
      endTime: '2026-04-02T16:00:00Z',
      price: 1,
      memberPrice: 0.8,
      membershipLabel: 'Monthly Pass',
      remaining: 50,
      maxCapacity: 50,
      status: 'AVAILABLE'
    },
    {
      timeslotId: 502,
      venueId: 2,
      venueName: 'Court B',
      startTime: '2026-04-02T15:00:00Z',
      endTime: '2026-04-02T16:00:00Z',
      price: 0,
      memberPrice: 0,
      membershipLabel: 'Monthly Pass',
      remaining: 1,
      maxCapacity: 1,
      status: 'AVAILABLE'
    }
  ];
}

async function installFixedClock(page) {
  await page.addInitScript(({ now }) => {
    const fixed = new Date(now).valueOf();
    const RealDate = Date;
    class MockDate extends RealDate {
      constructor(...args) {
        if (args.length === 0) {
          super(fixed);
          return;
        }
        super(...args);
      }
      static now() {
        return fixed;
      }
    }
    MockDate.UTC = RealDate.UTC;
    MockDate.parse = RealDate.parse;
    window.Date = MockDate;
  }, { now: FIXED_NOW });
}

async function seedStorage(page, values) {
  await page.addInitScript((storage) => {
    Object.entries(storage.localStorage || {}).forEach(([key, value]) => {
      window.localStorage.setItem(key, value);
    });
    Object.entries(storage.sessionStorage || {}).forEach(([key, value]) => {
      window.sessionStorage.setItem(key, value);
    });
  }, values);
}

async function mockExternalAssets(page) {
  await page.route('https://accounts.google.com/gsi/client*', (route) => route.fulfill({
    status: 200,
    contentType: 'application/javascript',
    body: `
      window.google = {
        accounts: {
          id: {
            initialize: function () {},
            renderButton: function (node) {
              if (!node) return;
              node.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;width:100%;height:100%;border-radius:999px;border:1px solid #dbe2ea;font:600 14px Segoe UI,sans-serif;background:#fff;color:#111827;">Continue with Google</div>';
            },
            prompt: function () {}
          }
        }
      };
    `
  }));
}

async function mockApi(page, mode) {
  await page.route('**/api/**', async (route) => {
    const url = new URL(route.request().url());
    const { pathname } = url;

    if (pathname === '/api/public/config') {
      return jsonResponse(route, {
        googleMapsApiKey: '',
        paymentsEnabled: true,
        paymentProvider: 'VIRTUAL_CHECKOUT'
      });
    }

    if (pathname === '/api/profile') {
      return jsonResponse(route, mode === 'club'
        ? { id: 201, role: 'club', type: 'club', displayName: 'manba basketball', email: 'club@example.com' }
        : { id: 501, role: 'user', type: 'user', displayName: 'Sonia Member', email: 'sonia@example.com', avatarUrl: avatarSvg });
    }

    if (pathname === '/api/my/bookings') {
      return jsonResponse(route, myBookingsPayload());
    }

    if (pathname === '/api/my/memberships') {
      return jsonResponse(route, myMembershipsPayload());
    }

    if (pathname === `/api/clubs/${CLUB_ID}`) {
      return jsonResponse(route, clubPayload());
    }

    if (pathname === `/api/clubs/${CLUB_ID}/images`) {
      return jsonResponse(route, [
        { imageId: 11, imageUrl: clubImageOne, sortOrder: 1, originalName: 'court-a.png', primary: true },
        { imageId: 12, imageUrl: clubImageTwo, sortOrder: 2, originalName: 'court-b.png', primary: false }
      ]);
    }

    if (pathname === `/api/clubs/${CLUB_ID}/venues`) {
      return jsonResponse(route, [
        { venueId: 1, venueName: 'Court A' },
        { venueId: 2, venueName: 'Court B' }
      ]);
    }

    if (pathname === `/api/clubs/${CLUB_ID}/timeslots`) {
      return jsonResponse(route, timeslotsPayload());
    }

    if (pathname.startsWith('/api/timeslots/') && pathname.endsWith('/bookings/me')) {
      return jsonResponse(route, { bookingId: null }, 200);
    }

    if (pathname === `/api/clubs/${CLUB_ID}/community-questions`) {
      return jsonResponse(route, communityBoardPayload());
    }

    if (pathname.startsWith(`/api/clubs/${CLUB_ID}/community-questions/`) && pathname.endsWith('/answers')) {
      return jsonResponse(route, { ok: true });
    }

    if (pathname === `/api/clubs/${CLUB_ID}/membership-plans`) {
      return jsonResponse(route, membershipPlanPayload());
    }

    if (pathname === `/api/my/clubs/${CLUB_ID}/membership`) {
      return jsonResponse(route, membershipStatusPayload());
    }

    if (pathname === `/api/my/clubs/${CLUB_ID}/membership-plans`) {
      return jsonResponse(route, adminMembershipPlansPayload());
    }

    if (pathname === `/api/my/clubs/${CLUB_ID}/memberships`) {
      return jsonResponse(route, clubMembershipMembersPayload());
    }

    if (pathname === `/api/my/clubs/${CLUB_ID}/chat-kb`) {
      return jsonResponse(route, clubChatKbPayload());
    }

    if (pathname === `/api/clubs/${CLUB_ID}/chat/messages`) {
      return jsonResponse(route, clubChatMessagesPayload());
    }

    if (pathname === `/api/clubs/${CLUB_ID}/chat/read`) {
      return route.fulfill({ status: 204, body: '' });
    }

    if (pathname === `/api/clubs/${CLUB_ID}/chat/stream`) {
      return route.fulfill({
        status: 200,
        headers: {
          'Content-Type': 'text/event-stream',
          'Cache-Control': 'no-cache'
        },
        body: 'retry: 5000\n\n'
      });
    }

    if (pathname === '/api/my/clubs') {
      return jsonResponse(route, [
        { clubId: CLUB_ID, id: Number(CLUB_ID), clubName: 'manba basketball', name: 'manba basketball' }
      ]);
    }

    if (pathname === `/api/my/clubs/${CLUB_ID}/chat/conversations`) {
      return jsonResponse(route, clubConversationsPayload());
    }

    if (pathname === `/api/my/clubs/${CLUB_ID}/chat/conversations/501/messages`) {
      return jsonResponse(route, clubConversationDetailPayload());
    }

    if (pathname === `/api/my/clubs/${CLUB_ID}/chat/conversations/501/read`) {
      return route.fulfill({ status: 204, body: '' });
    }

    if (pathname === `/api/my/clubs/${CLUB_ID}/chat/stream`) {
      return route.fulfill({
        status: 200,
        headers: {
          'Content-Type': 'text/event-stream',
          'Cache-Control': 'no-cache'
        },
        body: 'retry: 5000\n\n'
      });
    }

    if (pathname === '/api/payments/checkout-sessions/chk_demo_checkout') {
      return jsonResponse(route, checkoutDetailPayload());
    }

    return route.fulfill({
      status: 404,
      contentType: 'application/json',
      body: JSON.stringify({ message: `Unhandled mocked API route: ${pathname}` })
    });
  });
}

function storageForUser() {
  const user = {
    id: 501,
    userId: 501,
    type: 'user',
    role: 'user',
    name: 'Sonia Member',
    email: 'sonia@example.com'
  };
  return {
    localStorage: {
      loggedUser: JSON.stringify(user),
      user: JSON.stringify(user),
      selectedClub: JSON.stringify({ id: CLUB_ID, name: 'manba basketball' }),
      token: 'desktop-visual-token'
    },
    sessionStorage: {
      'clubPortal.authToken': 'desktop-visual-token'
    }
  };
}

function storageForClubAdmin() {
  const user = {
    id: 201,
    userId: 201,
    type: 'club',
    role: 'club',
    name: 'manba basketball',
    email: 'club@example.com'
  };
  return {
    localStorage: {
      loggedUser: JSON.stringify(user),
      user: JSON.stringify(user),
      selectedClub: JSON.stringify({ id: CLUB_ID, name: 'manba basketball' }),
      token: 'desktop-visual-token'
    },
    sessionStorage: {
      'clubPortal.authToken': 'desktop-visual-token'
    }
  };
}

async function preparePage(page, { mode = 'user' } = {}) {
  await installFixedClock(page);
  await seedStorage(page, mode === 'club' ? storageForClubAdmin() : storageForUser());
  await mockExternalAssets(page);
  await mockApi(page, mode);
}

async function waitForFrameByPath(page, pathnamePattern) {
  await expect.poll(() => page.frames().some((frame) => pathnamePattern.test(new URL(frame.url()).pathname))).toBe(true);
  const frame = page.frames().find((item) => pathnamePattern.test(new URL(item.url()).pathname));
  expect(frame).toBeTruthy();
  return frame;
}

async function expectEmbeddedSectionReady(page, { section, framePath, title, selector }) {
  await page.click(`.workspace-tab[data-section="${section}"]`);
  await expect(page.locator('#sectionTitle')).toHaveText(title);
  const frameEl = page.locator(`iframe[data-section="${section}"]`);
  await expect(frameEl).toHaveClass(/active/);
  const frame = await waitForFrameByPath(page, framePath);
  await expect.poll(() => frame.evaluate(() => Boolean(document.getElementById('club-home-embed-style')))).toBe(true);
  await expect.poll(() => frame.evaluate(() => {
    const header = document.querySelector('header');
    const footer = document.querySelector('.site-footer');
    return Boolean((!header || getComputedStyle(header).display === 'none') && (!footer || getComputedStyle(footer).display === 'none'));
  })).toBe(true);
  await expect(page.frameLocator(`iframe[data-section="${section}"]`).locator(selector).first()).toBeVisible();
}

test.describe('desktop consistency baselines', () => {
  test('club page desktop baseline at 1366x768', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await preparePage(page);
    await page.goto(`/club.html?club=${CLUB_ID}`);
    await expect(page.locator('#schedule')).toBeVisible();
    await expect(page).toHaveScreenshot('club-1366x768.png');
  });

  test('club page desktop baseline at 1920x1080 with expanded rail', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await preparePage(page);
    await page.goto(`/club.html?club=${CLUB_ID}`);
    await expect(page.locator('#schedule')).toBeVisible();
    await page.click('#bookingSideRailToggle');
    await expect(page.locator('#clubCommunitySection')).toBeVisible();
    await expect(page).toHaveScreenshot('club-1920x1080.png');
  });

  test('club page chat rail opens populated panel', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await preparePage(page);
    await page.goto(`/club.html?club=${CLUB_ID}`);
    await expect(page.locator('#schedule')).toBeVisible();
    await page.click('#bookingSideRailToggle');
    await page.click('#clubChatLauncher');
    await expect(page.locator('#clubChatPanel')).toHaveClass(/open/);
    await expect(page.locator('#closeClubChatBtn')).toBeVisible();
    await expect(page.locator('#clubChatTitle')).toHaveText('Talk with manba basketball');
    await expect(page.locator('#clubChatThread')).toContainText('Hi. Ask about hours, pricing, memberships, or published slots.');
    await expect(page.locator('#clubChatForm')).toBeVisible();
    await expect(page.locator('#clubChatInput')).toBeVisible();
    await page.click('#closeClubChatBtn');
    await expect(page.locator('#clubChatPanel')).not.toHaveClass(/open/);
    await expect(page.locator('#clubChatLauncher')).toBeVisible();
    await expect(page.locator('#clubMembershipSection .membership-section')).toBeVisible();
    await expect(page.locator('#clubCommunitySection')).toBeVisible();
  });

  test('club page membership plans stay inside the rail column', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await preparePage(page);
    await page.goto(`/club.html?club=${CLUB_ID}`);
    await expect(page.locator('#schedule')).toBeVisible();
    await page.click('#bookingSideRailToggle');
    await page.click('#clubMembershipToggle');

    await expect(page.locator('#membershipPlansPopover')).toBeVisible();
    await expect(page.locator('#closeMembershipPlansBtn')).toBeVisible();
    await expect(page.locator('#clubChatLauncher')).toBeHidden();
    await expect(page.locator('#clubCommunitySection')).toBeHidden();
    await expect(page.locator('#clubMembershipSection .membership-section')).toBeHidden();

    const railBox = await page.locator('#bookingSideRail').boundingBox();
    const plansBox = await page.locator('#membershipPlansPopover').boundingBox();
    const shellBox = await page.locator('.membership-popover-shell').boundingBox();
    expect(plansBox).not.toBeNull();
    expect(shellBox).not.toBeNull();
    expect(shellBox.height).toBeLessThan(700);
    await page.click('#closeMembershipPlansBtn');
    await expect(page.locator('#membershipPlansPopover')).toBeHidden();
    await expect(page.locator('#clubChatLauncher')).toBeVisible();
    await expect(page.locator('#clubCommunitySection')).toBeVisible();
    await expect(page.locator('#clubMembershipSection .membership-section')).toBeVisible();
  });

  test('club page community q&a uses the shared top-level back control only', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await preparePage(page);
    await page.goto(`/club.html?club=${CLUB_ID}`);
    await expect(page.locator('#schedule')).toBeVisible();
    await page.click('#bookingSideRailToggle');
    await page.click('#clubCommunityOpenBtn');

    await expect(page.locator('#clubCommunityDialog')).toBeVisible();
    await expect(page.locator('#closeClubCommunityBtn')).toBeVisible();
    await expect(page.locator('#clubChatLauncher')).toBeHidden();
    await expect(page.locator('#clubMembershipSection .membership-section')).toBeHidden();
    await expect(page.locator('#bookingSideRailToggleLabel')).toHaveText('Back to booking');
    await expect(page.locator('.community-answer')).toHaveCount(0);
    await expect(page.locator('.community-answer-form')).toHaveCount(0);
    await page.click('[data-community-question-toggle]');
    await expect(page.locator('.community-answer')).toHaveCount(2);
    await expect(page.locator('.community-answer-form')).toHaveCount(1);
    await page.click('#closeClubCommunityBtn');
    await expect(page.locator('#clubCommunityDialog')).toBeHidden();
    await expect(page.locator('#clubChatLauncher')).toBeVisible();
    await expect(page.locator('#clubMembershipSection .membership-section')).toBeVisible();
    await expect(page.locator('#clubCommunitySection')).toBeVisible();
  });

  test('club chat desktop baseline at 1440x900', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await preparePage(page, { mode: 'club' });
    await page.goto('/club%20chat.html');
    await expect(page.locator('#threadList .thread-card').first()).toBeVisible();
    await expect(page).toHaveScreenshot('club-chat-1440x900.png');
  });

  test('club chat q&a question opens in the right thread pane', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await preparePage(page, { mode: 'club' });
    await page.goto('/club%20chat.html');

    await page.click('#clubQaToggle');
    await page.click('[data-club-qa-question="901"]');

    await expect(page.locator('#clubQaThreadView')).toBeVisible();
    await expect(page.locator('#chatLog')).toBeHidden();
    await expect(page.locator('#activeTitle')).toHaveText('Community Q&A');
    await expect(page.locator('#clubQaThreadLog')).toContainText('Do you provide basketballs on site?');
    await expect(page.locator('#clubQaThreadLog')).toContainText('Yes. Members can borrow practice basketballs from the front desk.');
    await expect(page.locator('#clubQaPanel')).not.toContainText('Yes. Members can borrow practice basketballs from the front desk.');

    await page.click('#threadList .thread-card');
    await expect(page.locator('#clubQaThreadView')).toBeHidden();
    await expect(page.locator('#chatLog')).toBeVisible();
    await expect(page.locator('#activeTitle')).toHaveText(/Sonia Member/);
  });

  test('user page desktop baseline at 1440x900', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await preparePage(page);
    await page.goto('/user.html');
    await expect(page.locator('#bookingList .booking-card').first()).toBeVisible();
    await expect(page).toHaveScreenshot('user-1440x900.png');
  });

  test('login page desktop baseline at 1366x768', async ({ page }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await installFixedClock(page);
    await mockExternalAssets(page);
    await mockApi(page, 'user');
    await page.goto('/login.html');
    await expect(page.locator('.card')).toBeVisible();
    await expect(page).toHaveScreenshot('login-1366x768.png');
  });

  test('club home workspace baseline at 1920x1080 with real embedded info page', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await preparePage(page, { mode: 'club' });
    await page.goto('/club%20home.html');
    await expect(page.locator('.workspace')).toBeVisible();
    await expectEmbeddedSectionReady(page, {
      section: 'info',
      framePath: /\/club-info\.html$/,
      title: 'Club information',
      selector: '#clubProfileForm'
    });
    await expect(page).toHaveScreenshot('club-home-1920x1080.png');
  });

  test('club home workspace updates iframe baseline at 1920x1080', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await preparePage(page, { mode: 'club' });
    await page.goto('/club%20home.html');
    await expect(page.locator('.workspace')).toBeVisible();
    await expectEmbeddedSectionReady(page, {
      section: 'updates',
      framePath: /\/club%20updates\.html$/,
      title: 'Memberships & FAQ',
      selector: '#showSavedPlansBtn'
    });
    await expect(page).toHaveScreenshot('club-home-updates-1920x1080.png');
  });

  test('club home workspace loads real embedded business pages across tabs', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await preparePage(page, { mode: 'club' });
    await page.goto('/club%20home.html');
    await expect(page.locator('.workspace')).toBeVisible();

    await expectEmbeddedSectionReady(page, {
      section: 'info',
      framePath: /\/club-info\.html$/,
      title: 'Club information',
      selector: '#clubProfileForm'
    });
    await expectEmbeddedSectionReady(page, {
      section: 'venues',
      framePath: /\/club-admin\.html$/,
      title: 'Venues and time slots',
      selector: '#venueList .venue-item'
    });
    await expectEmbeddedSectionReady(page, {
      section: 'bookings',
      framePath: /\/club%20bookings\.html$/,
      title: 'Member bookings',
      selector: '#stats [data-stat="booked"]'
    });
    await expectEmbeddedSectionReady(page, {
      section: 'updates',
      framePath: /\/club%20updates\.html$/,
      title: 'Memberships & FAQ',
      selector: '#showSavedPlansBtn'
    });
    await expectEmbeddedSectionReady(page, {
      section: 'chats',
      framePath: /\/club%20chat\.html$/,
      title: 'Member chats',
      selector: '#threadList .thread-card'
    });
  });
});
