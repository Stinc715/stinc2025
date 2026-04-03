# API Contract

Base URL: `/api`

This document describes the current backend contract as implemented in code. It is intentionally biased toward runtime truth, not historical behavior.

## Authentication

Runtime behavior:
- The backend writes authenticated cookies for browser flows.
- Login and registration responses still include a `token` field for backward compatibility with existing frontend code.
- Protected endpoints accept the existing JWT bearer token model and browser cookie model.

### POST `/api/register`

Request body:

```json
{
  "fullName": "Alice Example",
  "email": "alice@example.com",
  "password": "StrongPass1",
  "role": "user"
}
```

Response `200`:

```json
{
  "token": "jwt-token",
  "id": 1,
  "fullName": "Alice Example",
  "email": "alice@example.com",
  "role": "user"
}
```

Notes:
- Registration requires prior email verification.
- Password policy is enforced server-side.
- Duplicate email returns `409`.
- Public registration never allows self-assignment to `ADMIN`.

### POST `/api/login`

Request body:

```json
{
  "email": "alice@example.com",
  "password": "StrongPass1"
}
```

Response `200`:

```json
{
  "token": "jwt-token",
  "id": 1,
  "fullName": "Alice Example",
  "email": "alice@example.com",
  "role": "user"
}
```

Failure response:
- `401 Invalid email or password`

### POST `/api/auth/logout`

Response `200`:

```json
{ "logout": true }
```

### POST `/api/auth/password-reset/request`

Request body:

```json
{ "email": "alice@example.com" }
```

Response `200` for both existent and non-existent accounts:

```json
{
  "success": true,
  "message": "If an account exists for this email, a password reset link will be sent shortly."
}
```

Notes:
- The reset link base URL comes only from `app.public.base-url`.
- The endpoint is intentionally non-enumerating.

### POST `/api/auth/password-reset/confirm`

Request body:

```json
{
  "token": "reset-token",
  "password": "StrongPass1"
}
```

Response `200`:

```json
{ "reset": true }
```

## Clubs

### GET `/api/clubs`

Response `200`:

```json
[
  {
    "id": 1,
    "clubId": 1,
    "name": "Basketball Club",
    "description": "Public club summary",
    "category": "basketball",
    "tags": ["basketball"]
  }
]
```

### GET `/api/clubs/{clubId}`

Response `200` includes club profile, location, hours, and public display metadata.

### POST `/api/clubs`

Requires:
- authenticated club or admin user

Behavior:
- creates a club
- automatically links the creator as `club_admin`

### PUT `/api/clubs/{clubId}`

Requires:
- authenticated club or admin user
- current user must manage that club

## Venues and Time Slots

### GET `/api/clubs/{clubId}/venues`

Response `200`:

```json
[
  {
    "venueId": 1,
    "clubId": 1,
    "name": "Court A",
    "location": "Hall A",
    "capacity": 20
  }
]
```

### POST `/api/clubs/{clubId}/venues`

Requires club admin.

### GET `/api/clubs/{clubId}/timeslots?from=YYYY-MM-DD&to=YYYY-MM-DD`

Response `200` item shape:

```json
{
  "timeslotId": 10,
  "venueId": 1,
  "clubId": 1,
  "venueName": "Court A",
  "startTime": "2026-04-01T15:00:00",
  "endTime": "2026-04-01T16:00:00",
  "maxCapacity": 50,
  "bookedCount": 1,
  "remaining": 49,
  "price": 0.8,
  "basePrice": 1.0,
  "membershipApplied": true,
  "membershipPlanName": "Monthly Pass"
}
```

### POST `/api/clubs/{clubId}/venues/{venueId}/timeslots`

Requires club admin.

## Booking

### POST `/api/timeslots/{timeslotId}/bookings`

Current runtime behavior:
- this endpoint no longer creates a booking directly
- it intentionally returns `402 Payment Required`

Response `402`:

```json
"Create a checkout session first via /api/payments/checkout-sessions"
```

Canonical booking flow:
1. client calls `POST /api/payments/checkout-sessions`
2. backend creates a checkout session
3. client opens `payment.html?sessionId=...`
4. payment is completed through virtual confirmation or Stripe
5. backend writes `booking_record`

### DELETE `/api/timeslots/{timeslotId}/bookings/me`

Cancels the caller's active booking for that timeslot when status is cancelable.

### GET `/api/my/bookings`

Response items include:

```json
{
  "bookingId": 24,
  "orderNo": "BK-20260331150315-ABC123",
  "timeslotId": 10,
  "status": "PENDING",
  "clubId": 2,
  "clubName": "manba basketball",
  "venueId": 1,
  "venueName": "A",
  "startTime": "2026-03-31T15:00:00",
  "endTime": "2026-03-31T16:00:00",
  "pricePaid": 0.8,
  "basePrice": 1.0,
  "membershipPlanName": "Monthly Pass",
  "membershipApplied": true,
  "bookingVerificationCode": "565767"
}
```

### GET `/api/clubs/{clubId}/timeslot-bookings`

Requires club admin.

Club-side booking members include:
- member name and email
- booking status
- paid amount
- membership info
- `bookingVerificationCode`

## Memberships

### GET `/api/clubs/{clubId}/membership-plans`

Returns enabled public plans only.

### POST `/api/membership-plans/{planId}/purchase`

Current runtime behavior:
- this endpoint no longer completes a membership purchase directly
- it intentionally returns `402 Payment Required`

Response `402`:

```json
"Create a checkout session first via /api/payments/checkout-sessions"
```

Canonical membership purchase flow:
1. client calls `POST /api/payments/checkout-sessions`
2. request type is `MEMBERSHIP`
3. payment is completed
4. backend writes `user_membership` and `transaction`

### GET `/api/my/memberships`

Response items include:

```json
{
  "userMembershipId": 11,
  "orderNo": "MB-20260331151042-QWE456",
  "clubId": 2,
  "clubName": "manba basketball",
  "planId": 5,
  "planCode": "MONTHLY",
  "planName": "Monthly Pass",
  "price": 49.0,
  "discountPercent": 20.0,
  "status": "ACTIVE"
}
```

## Payments and Checkout

### POST `/api/payments/checkout-sessions`

Creates a checkout session for either booking or membership purchase.

Request body examples:

Booking:

```json
{
  "type": "BOOKING",
  "timeslotId": 10
}
```

Membership:

```json
{
  "type": "MEMBERSHIP",
  "planId": 5
}
```

Response `200`:

```json
{
  "sessionId": "chk_xxx",
  "orderNo": "BK-20260331150315-ABC123",
  "status": "CREATED",
  "provider": "VIRTUAL_CHECKOUT",
  "checkoutUrl": "",
  "paymentPageUrl": "https://example.invalid/payment.html?sessionId=chk_xxx",
  "expiresAt": "2026-03-31T15:13:15Z"
}
```

### GET `/api/payments/checkout-sessions/{sessionId}`

Response `200`:

```json
{
  "sessionId": "chk_xxx",
  "orderNo": "BK-20260331150315-ABC123",
  "type": "BOOKING",
  "status": "CREATED",
  "provider": "VIRTUAL_CHECKOUT",
  "amount": 0.8,
  "currency": "GBP",
  "checkoutUrl": "",
  "canContinueCheckout": false,
  "canCancel": true,
  "clubId": 2,
  "clubName": "manba basketball",
  "timeslotId": 10,
  "venueName": "A",
  "title": "Booking at A",
  "subtitle": "31 Mar 2026, 15:00 - 16:00"
}
```

### POST `/api/payments/checkout-sessions/{sessionId}/confirm-virtual`

Only valid when provider is `VIRTUAL_CHECKOUT`.

Result:
- confirms payment
- writes booking or membership business records
- marks checkout session as `PAID`

### POST `/api/payments/checkout-sessions/{sessionId}/cancel`

Cancels an active checkout session.

### POST `/api/payments/webhook/stripe`

Stripe webhook endpoint.

## Chat

### User-side chat

### GET `/api/clubs/{clubId}/chat/messages`

Returns the current user's thread with that club, including session metadata.

### POST `/api/clubs/{clubId}/chat/messages`

Request body:

```json
{ "text": "营业时间是什么时候？" }
```

Behavior:
- respects chat session mode
- may return club FAQ direct answer
- may return bot reply
- may suggest human handoff

Message records can include:
- `answerSource`
- `matchedFaqId`
- `handoffSuggested`

### POST `/api/chat-sessions/{sessionId}/handoff`

User requests club staff takeover.

### Club-side chat

### GET `/api/my/clubs/{clubId}/chat/conversations`

Requires club admin.

Conversation items may include:
- `chatMode`
- `clubUnreadCount`
- `memberUnreadCount`
- `handoffReason`

### GET `/api/my/clubs/{clubId}/chat/conversations/{userId}/messages`

Requires club admin.

### POST `/api/my/clubs/{clubId}/chat/conversations/{userId}/messages`

Requires club admin.

Sends a human club reply into the conversation.

## Public Config

### GET `/api/public/config`

Response includes:

```json
{
  "googleMapsEnabled": true,
  "googleOauthEnabled": true,
  "paymentsEnabled": true,
  "paymentProvider": "VIRTUAL_CHECKOUT",
  "paymentCurrency": "GBP"
}
```

Notes:
- `paymentProvider` is runtime-derived from backend payment mode and Stripe readiness
- this response is the frontend-safe source of payment capability flags
