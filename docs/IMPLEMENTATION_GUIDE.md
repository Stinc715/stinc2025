# Club Portal Implementation Guide

## 1. Purpose

This document explains how the current Club Portal system is built, how each major feature works end to end, and how the features depend on each other.

It is written for future maintenance, debugging, and handoff. It focuses on:

- what each feature does
- which frontend pages implement it
- which backend controllers/services/models support it
- what data is stored
- how one feature affects another
- what is fully implemented vs what is still only partially wired

## 2. System Overview

The project is split into two runtime parts:

- `frontend/`: the deployed web UI
- `backend/`: the Spring Boot API and business logic

There is also:

- `deploy/`: release scripts and MySQL schema
- `dist/`: built frontend output from Vite
- `server/server.js`: a local static server with `/api` proxy support

## 3. Actual Runtime Architecture

### 3.1 Frontend

The live product is mostly a multi-page static HTML application, not a SPA.

Important detail:

- `package.json` includes Vue and `src/main.js`, but the currently deployed product behavior is primarily driven by the HTML files in `frontend/`
- each page contains its own inline JavaScript and directly calls `/api/...`
- Vite is used mainly as a build pipeline that copies/bundles assets into `dist/`

Main user-facing pages:

- `frontend/home.html`: public discovery page
- `frontend/login.html`: login/register/reset-password modal content page
- `frontend/club.html`: public club detail page, booking, memberships, chat
- `frontend/payment.html`: simulated payment confirmation page
- `frontend/user.html`: user account center
- `frontend/club home.html`: club-side workspace shell

Club workspace sub-pages loaded inside `club home.html` iframes:

- `frontend/club-info.html`
- `frontend/club-admin.html`
- `frontend/club bookings.html`
- `frontend/club updates.html`
- `frontend/club chat.html`

Onboarding flow:

- `frontend/onboarding.html`
- `frontend/onboarding-location.html`
- `frontend/onboarding-promo.html`

### 3.2 Backend

The backend is a Spring Boot 3.2.5 application using:

- Spring Web
- Spring Security
- Spring Data JPA
- MySQL
- JavaMail
- JWT auth

Core backend entry:

- `backend/src/main/java/com/clubportal/ClubPortalApplication.java`

## 4. Authentication and Authorization

### 4.1 Identity model

Users are stored in `backend/src/main/java/com/clubportal/model/User.java`.

The system distinguishes at least:

- `USER`
- `CLUB`
- `ADMIN`

JWT handling:

- `backend/src/main/java/com/clubportal/security/JwtUtil.java`
- `backend/src/main/java/com/clubportal/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/clubportal/service/CurrentUserService.java`

How it works:

1. login or Google auth returns a JWT
2. JWT carries:
   - email as subject
   - `role`
   - `sv` session version
3. `JwtAuthenticationFilter` resolves the token on each request
4. `CurrentUserService` maps the authenticated email back to the correct `User`

Why session version exists:

- every login bumps a per-user session version
- new tokens use the latest `sv`
- this lets the server invalidate older tokens logically

### 4.2 Security rules

Defined in:

- `backend/src/main/java/com/clubportal/config/SecurityConfig.java`

Important behavior:

- `/api/auth/**` is public
- `/api/login` and `/api/register` are public
- `/api/public/config` is public
- public `GET /api/clubs/**` is allowed
- club management routes require `ROLE_CLUB` or `ROLE_ADMIN`
- chat routes are split between user-side and club-side permissions

### 4.3 Login and registration UI

Implemented in:

- `frontend/auth-modal.js`
- `frontend/login.html`

Important behavior:

- login and register open in modal overlays, not page redirects
- the modal iframe loads `login.html#login` or `login.html#register`
- login and register are route-locked by hash, so each entry shows only its own form

### 4.4 Registration email verification

Backend:

- `backend/src/main/java/com/clubportal/controller/RegistrationVerificationController.java`
- `backend/src/main/java/com/clubportal/service/RegistrationEmailVerificationService.java`
- `backend/src/main/java/com/clubportal/service/VerificationEmailSenderService.java`

How it works:

1. frontend requests a verification code
2. backend stores the pending code in memory
3. email is sent through `MailService`
4. verification marks the email as temporarily approved for registration
5. `AuthController.register` refuses registration unless the email has been verified

Important limitation:

- registration verification state is in-memory, not persisted to MySQL
- service restart clears pending verification state

### 4.5 Password reset

Backend:

- `backend/src/main/java/com/clubportal/controller/PasswordResetController.java`
- `backend/src/main/java/com/clubportal/service/PasswordResetService.java`
- `backend/src/main/java/com/clubportal/service/VerificationEmailSenderService.java`

Frontend:

- `frontend/login.html`
- `frontend/reset-password.html`

How it works:

1. user requests reset link from login modal
2. `PasswordResetService` generates an in-memory token
3. email is sent with a reset URL
4. reset page validates token
5. confirm endpoint consumes token and updates the password

Important limitation:

- like registration codes, reset tokens are in-memory
- restart clears pending reset tokens

### 4.6 Google login

Backend:

- `backend/src/main/java/com/clubportal/controller/GoogleAuthController.java`
- `backend/src/main/java/com/clubportal/service/GoogleAuthService.java`

Frontend:

- `frontend/login.html`

Config source:

- `backend/src/main/java/com/clubportal/controller/PublicConfigController.java`

The frontend asks `/api/public/config` for the Google OAuth client ID, then renders the Google button.

## 5. Club Discovery and Public Club Page

### 5.1 Public club list

Backend:

- `backend/src/main/java/com/clubportal/controller/ClubController.java`

Frontend:

- `frontend/home.html`

How it works:

- `GET /api/clubs` returns public club summaries
- placeholder records are filtered out in `ClubController.listClubs()`
- category tags are returned through `ClubTagCodec`

### 5.2 Club detail page

Frontend:

- `frontend/club.html`

Backend data sources:

- `GET /api/clubs/{clubId}`
- `GET /api/clubs/{clubId}/images`
- `GET /api/clubs/{clubId}/venues`
- `GET /api/clubs/{clubId}/timeslots`
- `GET /api/clubs/{clubId}/membership-plans`
- user chat endpoints

This page combines several modules:

- club profile and images
- address / Google Maps jump link
- 7-day schedule and slot booking
- memberships
- club chat

This page is one of the strongest examples of feature composition in the system.

## 6. Club Profile and Onboarding

### 6.1 Club creation

Backend:

- `backend/src/main/java/com/clubportal/controller/ClubController.java`

Important behavior:

- `POST /api/clubs` creates a club
- creator is automatically inserted into `club_admin`
- placeholder clubs are no longer intentionally published from onboarding drafts

### 6.2 Onboarding flow

Frontend:

- `frontend/onboarding.html`
- `frontend/onboarding.js`
- `frontend/onboarding-location.html`
- `frontend/onboarding-location.js`
- `frontend/onboarding-promo.html`
- `frontend/onboarding-promo.js`

How it works now:

1. profile step collects club name and category tags
2. location step collects real address and optional Google place metadata
3. promo step handles media / final submission
4. final submission creates or updates the real club record

Important improvement already made:

- earlier onboarding created public clubs too early
- now the early steps behave more like draft capture and the real club write happens at the end

### 6.3 Club account information page

Frontend:

- `frontend/club-info.html`

What it manages:

- club name
- categories
- email
- phone
- description
- opening hours
- display address
- Google Maps place metadata
- images

Special behavior:

- club name and categories are read-only on this page
- editing them redirects to onboarding-style secondary flows
- categories are multi-select and persist to both `category` and `category_tags`

Backend support:

- `ClubController`
- `ClubTagCodec`
- image upload endpoints in `ClubController`

### 6.4 Google Maps integration

Frontend:

- `frontend/club-info.html`
- `frontend/onboarding-location.js`

Backend:

- `backend/src/main/java/com/clubportal/controller/PublicConfigController.java`
- club model fields in `backend/src/main/java/com/clubportal/model/Club.java`

How it works:

1. frontend requests `/api/public/config`
2. if `googleMapsEnabled=true`, the page loads Google Maps JS dynamically
3. selected address writes:
   - `display_location`
   - `google_place_id`
   - `location_lat`
   - `location_lng`
4. public club page uses those fields to build a more precise Google Maps jump link

## 7. Venue and Time Slot Management

### 7.1 Venue model

Backend model:

- `backend/src/main/java/com/clubportal/model/Venue.java`

Backend API:

- `backend/src/main/java/com/clubportal/controller/VenueController.java`

Frontend management page:

- `frontend/club-admin.html`

What is implemented:

- create venue
- edit venue
- delete venue
- modal-based venue management from the venue list

### 7.2 Time slot model

Backend model:

- `backend/src/main/java/com/clubportal/model/TimeSlot.java`

Backend controller:

- `backend/src/main/java/com/clubportal/controller/TimeSlotController.java`

What a time slot stores:

- venue
- start/end time
- max capacity
- base price

### 7.3 Time slot admin workflow

Frontend:

- `frontend/club-admin.html`

What club admins can do:

- add one slot for the selected day
- apply weekly recurrence
- apply daily recurrence across the visible week range
- edit slot
- delete slot
- inspect per-day counts in week tabs

Important validation:

- end time must be later than start time
- max capacity cannot be below existing bookings on update

### 7.4 Time slot user-facing workflow

Frontend:

- `frontend/club.html`

Backend:

- `TimeSlotController.listClubTimeslots()`

How it works:

- user selects a day from the next 7 days
- backend returns slots plus computed booking counts
- if the viewer has an active membership, backend also returns:
  - discounted `price`
  - original `basePrice`
  - membership plan name
  - membership discount percent
  - `membershipApplied=true`

This means the pricing logic is not only cosmetic. It is computed server-side.

## 8. Booking System

### 8.1 Booking creation

Backend:

- `backend/src/main/java/com/clubportal/controller/BookingController.java`

Frontend:

- `frontend/club.html`
- `frontend/payment.html`

How booking works:

1. user chooses a slot
2. `club.html` creates a `pendingPayment` object in `localStorage`
3. user goes to `payment.html`
4. payment page simulates payment confirmation
5. `POST /api/timeslots/{timeslotId}/bookings` creates the booking

Important business rules:

- only `USER` accounts can book
- duplicate active booking for same user + timeslot is blocked
- full slots are blocked
- if the user has an active membership, discounted price is stored in `booking_record.price_paid`
- related membership is stored in `booking_record.user_membership_id`

### 8.2 Booking cancellation

Backend:

- `DELETE /api/timeslots/{timeslotId}/bookings/me`

Used by:

- `frontend/club.html`
- `frontend/user.html`

### 8.3 Booking views

User-side:

- `GET /api/my/bookings`
- shown in `frontend/user.html`

Club-side:

- `GET /api/clubs/{clubId}/timeslot-bookings`
- shown in `frontend/club bookings.html`

Club admins can see:

- booked members
- booking status
- paid amount
- membership plan name
- membership status

## 9. Membership System

This is now a first-class feature and touches many modules.

### 9.1 Membership data model

Backend models:

- `backend/src/main/java/com/clubportal/model/MembershipPlan.java`
- `backend/src/main/java/com/clubportal/model/UserMembership.java`
- `backend/src/main/java/com/clubportal/model/TransactionRecord.java`

Plan types currently standardized in `MembershipService`:

- `MONTHLY`
- `QUARTERLY`
- `HALF_YEAR`
- `YEARLY`

### 9.2 Membership plan defaults and normalization

Backend service:

- `backend/src/main/java/com/clubportal/service/MembershipService.java`

Responsibilities:

- creates standard plans for a club if missing
- normalizes price and discount values
- normalizes plan codes
- provides default names, durations, descriptions, prices, and discounts
- determines active membership
- calculates discounted booking price

This service is the shared business layer that links:

- public membership display
- club membership admin
- booking pricing
- user membership display

### 9.3 Club-side membership management

Frontend:

- `frontend/club updates.html`
- loaded inside `frontend/club home.html`

Backend:

- `GET /api/my/clubs/{clubId}/membership-plans`
- `PUT /api/my/clubs/{clubId}/membership-plans`
- `GET /api/my/clubs/{clubId}/memberships`

Current UX:

- plans section can be collapsed
- members section can be collapsed
- each plan is edited and saved separately
- each plan has an enable/disable switch
- changes are not live until that individual plan card is saved
- cards now show `Saved` vs `Unsaved changes`

Important business rule:

- disabling a plan hides it from new purchases
- existing purchased memberships stay valid

### 9.4 Public membership purchase

Frontend:

- `frontend/club.html`
- `frontend/payment.html`

Backend:

- `GET /api/clubs/{clubId}/membership-plans`
- `POST /api/membership-plans/{planId}/purchase`

Current purchase flow:

1. club page loads enabled plans only
2. user chooses a plan
3. plan purchase is routed through `payment.html`
4. payment page is still a mock payment page
5. backend creates:
   - `user_membership`
   - `transaction` with `MOCK_CARD`

Important limitation:

- no real payment gateway is connected yet

### 9.5 User-side membership visibility

Frontend:

- `frontend/user.html`

Backend:

- `GET /api/my/memberships`
- `GET /api/my/clubs/{clubId}/membership`

The user center shows:

- which club the user is a member of
- which pass was purchased
- validity dates
- price
- discount
- status (`ACTIVE`, `SCHEDULED`, `EXPIRED`)

### 9.6 Membership and booking coupling

This is the most important functional dependency in the system.

When listing slots:

- `TimeSlotController` asks `MembershipService` for the viewer's active membership
- returned slot price is the effective member price

When creating a booking:

- `BookingController` again checks the active membership
- it calculates the real discounted price server-side
- it stores the actual paid amount and the membership reference in `booking_record`

This means:

- club page price display
- booking confirmation
- my bookings history
- club admin booking reports

all stay consistent because they all derive from the same membership relationship.

## 10. Chat System

### 10.1 Data model

- `backend/src/main/java/com/clubportal/model/ChatMessage.java`

Each message stores:

- club id
- user id
- sender (`USER` or `CLUB`)
- text
- `read_by_club`
- `read_by_user`

### 10.2 User-side chat

Frontend:

- embedded on `frontend/club.html`

Backend:

- `GET /api/clubs/{clubId}/chat/messages`
- `POST /api/clubs/{clubId}/chat/messages`
- `POST /api/clubs/{clubId}/chat/read`

### 10.3 Club-side chat

Frontend:

- `frontend/club chat.html`
- loaded in `frontend/club home.html`

Backend:

- `GET /api/my/clubs/{clubId}/chat/conversations`
- `GET /api/my/clubs/{clubId}/chat/conversations/{userId}/messages`
- `POST /api/my/clubs/{clubId}/chat/conversations/{userId}/messages`
- `POST /api/my/clubs/{clubId}/chat/conversations/{userId}/read`

How it works:

- left list groups messages into conversations per user
- unread counts are computed from read flags
- opening a conversation marks club-side unread messages as read
- each message now shows its own read/unread state in the UI

## 11. User Account Center

Frontend:

- `frontend/user.html`

Current implemented backed sections:

- `My Bookings`
- `Memberships`
- `Information`

Backed endpoints that definitely exist:

- `GET /api/my/bookings`
- `GET /api/my/memberships`
- `GET /api/profile`
- `PATCH /api/profile/email`

Important honesty note:

- `frontend/user.html` still contains client code for `/api/profile/avatar` and `/api/profile/email/code`
- those endpoints are not present in the current backend controllers
- so avatar upload and email-code verification inside the user center are frontend-prepared but not backend-complete

This should be treated as a known gap, not as a finished feature.

## 12. Club Workspace Shell

Frontend:

- `frontend/club home.html`

This page is the club-side container, not the business logic itself.

What it does:

- verifies club login
- loads the current club selection
- renders a left navigation
- loads section pages into iframes
- manages active section switching and reloads

Current iframe sections:

- `club-info.html`
- `club-admin.html`
- `club bookings.html`
- `club updates.html`
- `club chat.html`

This structure keeps each club feature page isolated, which reduces cross-page JS coupling.

## 13. Payment Flow

Frontend:

- `frontend/payment.html`

What it currently does:

- reads `pendingPayment` from `localStorage`
- shows either booking payment or membership purchase confirmation
- simulates payment, then calls backend write endpoint

Booking endpoint:

- `/api/timeslots/{timeslotId}/bookings`

Membership endpoint:

- `/api/membership-plans/{planId}/purchase`

Important limitation:

- this is a mock payment layer, not Stripe/PayPal/Apple Pay/Google Pay

## 14. Images and Media

Backend:

- image upload/list/primary/delete in `ClubController`

Frontend:

- `frontend/club-info.html`
- `frontend/onboarding-promo.html`
- `frontend/club.html`

Stored data:

- metadata in `club_image` table
- actual files in the configured image directory on disk

Important behavior:

- one image can be primary
- public image content is served through `/api/clubs/{clubId}/images/{imageId}/content`

## 15. Public Configuration and Environment-driven Features

Backend:

- `backend/src/main/java/com/clubportal/controller/PublicConfigController.java`

This endpoint exposes frontend-safe config only:

- Google Maps API key
- Google OAuth client ID

Current environment variables commonly used:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `GOOGLE_OAUTH_CLIENT_ID`
- `GOOGLE_MAPS_JS_API_KEY`
- `APP_PUBLIC_BASE_URL`
- `SPRING_MAIL_*`
- `APP_MAIL_FROM`

## 16. Data Relationships

Core business relationships:

- `user` -> can own bookings, memberships, chat messages
- `club` -> owns venues, images, membership plans
- `club_admin` -> links a club account user to manageable clubs
- `venue` -> belongs to a club
- `timeslot` -> belongs to a venue
- `booking_record` -> links user to timeslot
- `membership_plan` -> belongs to a club
- `user_membership` -> links user to membership plan
- `transaction` -> records membership purchase payment
- `chat_message` -> links club and user conversation threads

Feature dependency map:

- club profile feeds public discovery and club page
- venues feed time slots
- time slots feed bookings
- memberships feed slot pricing and booking payment amount
- bookings feed both user history and club booking reports
- chat links public club page and club workspace

## 17. Deployment and Release Flow

Relevant files:

- `deploy/remote_release_frontend_only.sh`
- `deploy/remote_release_full.sh`
- `deploy/remote_apply_schema_from_env.sh`
- `deploy/mysql_schema.sql`

How releases work:

1. frontend is built into `dist/`
2. a tar archive is created
3. archive is copied to the server
4. release script extracts it into a timestamped release folder
5. files are `rsync`'d into `/var/www/club-portal`

Database updates:

- `deploy/mysql_schema.sql` is written to be rerunnable
- many schema changes are guarded with `IF NOT EXISTS` or information_schema checks
- `remote_apply_schema_from_env.sh` reads DB credentials from `/etc/club-portal.env`

## 18. Known Design Characteristics

These are important to understand before making large changes:

### 18.1 Frontend state is heavily localStorage-based

Examples:

- `selectedClub`
- `loggedUser`
- `token`
- `postLoginRedirect`
- `pendingPayment`

This keeps the pages loosely coupled, but it also means:

- cross-page flows depend on client-side state
- stale localStorage can affect behavior

### 18.2 The system mixes public pages and role-specific pages

Public pages:

- home
- club
- login
- payment

User-only pages:

- user center

Club-only pages:

- club home and its sub-pages

### 18.3 The deployed app is page-based, not component-driven

Even though Vue is present in the repo, the current production feature set is maintained mostly through standalone HTML files with inline scripts.

## 19. Known Gaps and Technical Debt

The most important current gaps are:

1. `payment.html` is still a simulated payment flow.
2. registration verification and password reset tokens are stored in memory, not in the database.
3. `frontend/user.html` references avatar upload and email-code endpoints that do not exist in the current backend.
4. the codebase still contains traces of an older Vue app structure that is not the main production path.
5. some deployment and generated artifacts are mixed into the working tree, so operational and source concerns are not fully separated.

## 20. Suggested Reading Order for New Developers

If someone new joins this project, the fastest useful reading order is:

1. `frontend/club home.html`
2. `frontend/club.html`
3. `frontend/club-admin.html`
4. `frontend/club-info.html`
5. `frontend/club updates.html`
6. `backend/src/main/java/com/clubportal/controller/ClubController.java`
7. `backend/src/main/java/com/clubportal/controller/TimeSlotController.java`
8. `backend/src/main/java/com/clubportal/controller/BookingController.java`
9. `backend/src/main/java/com/clubportal/controller/MembershipController.java`
10. `backend/src/main/java/com/clubportal/service/MembershipService.java`
11. `deploy/mysql_schema.sql`

That sequence gives the clearest view of the real product behavior.
