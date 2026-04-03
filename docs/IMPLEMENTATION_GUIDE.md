# Club Portal Implementation Guide

This guide describes the current implementation, not the original design intent. It is meant to answer the question: "What actually happens in the running system today?"

## 1. Runtime shape

The project currently runs as:

- `frontend/`: static multi-page HTML app built by Vite
- `backend/`: Spring Boot API
- `deploy/`: database schema, migrations, and deployment scripts
- `dist/`: build output for the frontend

Important implementation characteristic:
- the frontend is still page-based
- `frontend/club home.html` is still the club workspace shell and still loads sub-pages through `iframe`
- club workspace sub-pages remain:
  - `club-info.html`
  - `club-admin.html`
  - `club bookings.html`
  - `club updates.html`
  - `club chat.html`

## 2. Auth and session model

### Implemented

- JWT-based authentication exists in the backend
- browser flows also receive HttpOnly auth cookies
- login and registration responses still include `token` for compatibility with existing frontend code
- password policy is enforced server-side through:
  - [PasswordPolicyService.java](../backend/src/main/java/com/clubportal/service/PasswordPolicyService.java)
  - [PasswordPolicyProperties.java](../backend/src/main/java/com/clubportal/config/PasswordPolicyProperties.java)

### Default behavior

- minimum password length defaults to `8`
- passwords must include uppercase, lowercase, and a number
- password reset links only use `app.public.base-url`
- password reset request is non-enumerating

### Reserved / still improving

- the frontend still carries some transitional auth compatibility logic
- the project is not yet fully cookie-only everywhere

## 3. Registration and password flows

### Implemented

- registration requires prior email verification
- registration now performs backend password validation
- password reset request uses a generic success response for existent and non-existent emails
- password reset confirmation uses the same backend password policy as registration and profile password change
- internal exception details are no longer returned directly to the frontend

### Files

- [AuthController.java](../backend/src/main/java/com/clubportal/controller/AuthController.java)
- [PasswordResetController.java](../backend/src/main/java/com/clubportal/controller/PasswordResetController.java)
- [ProfileController.java](../backend/src/main/java/com/clubportal/controller/ProfileController.java)

## 4. Club discovery and public club page

### Implemented

- `home.html` lists public clubs
- `club.html` is the public club detail page
- the club page combines:
  - gallery
  - profile and address
  - booking schedule
  - membership area
  - right-side interaction rail for chat, Q&A, and member pricing

### Important current behavior

- the right-side booking rail can contain:
  - member pricing summary
  - chat launcher / unread widget
  - community Q&A entry
- on wide screens this rail is a dedicated collapsible side column beside the main content

## 5. Booking flow

## Implemented

Current booking flow is checkout-first. It is no longer direct booking.

Actual runtime flow:
1. user selects a slot on `club.html`
2. frontend calls `POST /api/payments/checkout-sessions`
3. backend creates a checkout session
4. frontend opens `payment.html?sessionId=...`
5. payment is confirmed
6. backend writes `booking_record`
7. backend writes a 6-digit `booking_verification_code`

### Important current behavior

- `POST /api/timeslots/{timeslotId}/bookings` does **not** create bookings directly anymore
- it intentionally returns `402` and tells the client to create a checkout session first
- the booking record now has:
  - `price_paid`
  - `user_membership_id`
  - `booking_verification_code`
- the booking checkout also creates a readable order number through `checkout_session.order_no`

### Club-side visibility

Club admins can see:
- booking status
- member info
- membership linkage
- booking verification code

### User-side visibility

Users can see:
- booking status
- order number
- verification code

## 6. Payment flow

This section has drifted the most historically, so the current state is spelled out explicitly.

### Implemented

There are now two backend payment paths in [CheckoutSessionService.java](../backend/src/main/java/com/clubportal/service/CheckoutSessionService.java):

- `VIRTUAL`
- `STRIPE`

The controller entry point is:
- [PaymentController.java](../backend/src/main/java/com/clubportal/controller/PaymentController.java)

The payment page is:
- [payment.html](../frontend/payment.html)

### Default mode

Current default mode is:

- `app.payments.mode = VIRTUAL`

Source:
- [application.yml](../backend/src/main/resources/application.yml)

This means:
- on a default environment, checkout provider is `VIRTUAL_CHECKOUT`
- users can complete booking and membership checkout without real card charging
- the backend still writes real business records and checkout session rows

### Stripe status

Stripe is **implemented but not default**.

What is implemented:
- Stripe checkout session creation
- Stripe webhook verification
- Stripe webhook fulfillment path

What is required before Stripe becomes active:
- `APP_PAYMENTS_MODE=STRIPE`
- valid Stripe secret key
- valid Stripe webhook secret
- valid `APP_PUBLIC_BASE_URL`

So the accurate implementation statement is:
- **Virtual checkout is implemented and enabled by default**
- **Stripe checkout is implemented but disabled by default unless explicitly configured**

### Not accurate anymore

It is no longer correct to describe `payment.html` as only a "simulated/mock payment page".

Accurate wording:
- `payment.html` is the checkout status and confirmation page
- in virtual mode it confirms payment locally through the backend
- in Stripe mode it continues to Stripe and waits for webhook confirmation

## 7. Memberships

### Implemented

- public membership plans are exposed from `/api/clubs/{clubId}/membership-plans`
- club admins manage plans from `club updates.html`
- purchases now also use checkout sessions instead of immediate completion
- memberships receive readable order numbers through `checkout_session.order_no`

### Important current behavior

- `POST /api/membership-plans/{planId}/purchase` no longer directly completes purchase
- it intentionally returns `402` and points the client to `/api/payments/checkout-sessions`

## 8. Chat system

### Implemented

The club chat system currently supports:
- AI mode
- handoff requested mode
- human mode
- closed mode

Core backend chain:
- [ChatMessageService.java](../backend/src/main/java/com/clubportal/service/ChatMessageService.java)
- [ClubChatAiService.java](../backend/src/main/java/com/clubportal/service/ClubChatAiService.java)
- [ChatIntentRouter.java](../backend/src/main/java/com/clubportal/service/ChatIntentRouter.java)
- [ChatResponseBuilder.java](../backend/src/main/java/com/clubportal/service/ChatResponseBuilder.java)

### Club FAQ layer

Implemented:
- club-level FAQ CRUD
- FAQ question embedding generation
- FAQ semantic matching
- FAQ backfill
- third-layer guard before FAQ direct answer

Current runtime order:
1. safe-path / sensitive routing
2. FAQ semantic match
3. FAQ guard
4. normal bot reply / fallback
5. human handoff when applicable

### Default / active behavior

- FAQ direct answer is active
- booking/refund/payment/account-change still remain protected and do not execute in chat
- FAQ answers can be labeled as verified club replies in the UI
- bot answers and human handoff prompts are visibly distinguished in the UI

### Reserved / extensible

- the structure allows stronger semantic scoring later
- no vector database is used now
- matching is still in-process and per-club

## 9. Club FAQ knowledge base

### Implemented

Backend:
- FAQ entries stored in `club_chat_kb_entry`
- question embedding stored in:
  - `question_embedding`
  - `embedding_model`
  - `embedding_dim`
- enabled flag controls runtime eligibility

Matching:
- current production matching path is [ClubChatKbMatcherService.java](../backend/src/main/java/com/clubportal/service/ClubChatKbMatcherService.java)
- FAQ answers are isolated by `clubId`

Backfill:
- historical FAQ embedding rebuild is implemented
- club-level and admin-level backfill endpoints exist

## 10. User account center

### Implemented

`user.html` now supports:
- bookings
- memberships
- profile information
- email change flow with 6-digit verification
- password change with backend-enforced password policy

## 11. Deployment model

### Current actual release behavior

Current intended release model is:
- build frontend into `dist/`
- deploy built frontend output
- package backend jar
- apply schema and migrations on server

Supported release path today:
- [deploy/upload.ps1](../deploy/upload.ps1) builds the frontend first
- the frontend release artifact is packaged from `dist/`
- remote release scripts then publish the packaged build output and backend JAR

Legacy manual scripts still exist in `deploy/`, but they are not the primary handoff path for this repo.

## 12. Implemented vs default-off vs reserved summary

### Implemented and active by default

- email verification for registration
- password reset with trusted base URL
- server-side password policy
- virtual checkout
- booking verification code
- club FAQ semantic matching
- chat handoff flow
- order numbers for checkout-backed flows

### Implemented but disabled by default

- Stripe checkout path
  - code path exists
  - not active unless payment mode and Stripe secrets are configured

### Reserved / future extension points

- stronger semantic FAQ scoring beyond the current in-process matcher
- full replacement of iframe-based club workspace shell
- tighter cookie-only auth model across all pages
- stricter CSP after more inline scripts are removed

## 13. What teachers should be told if they ask

Use these statements, because they are aligned with the current code:

- Registration returns both browser auth cookies and a token field for compatibility.
- Direct booking endpoints no longer create bookings. Checkout sessions are mandatory first.
- Direct membership purchase endpoints no longer complete purchase. Checkout sessions are mandatory first.
- Default payment mode is virtual checkout.
- Stripe is implemented, but not enabled by default.
- FAQ semantic matching is implemented per club and guarded by low-risk checks before direct answer.

Those statements match the current implementation and avoid the older documentation drift.
