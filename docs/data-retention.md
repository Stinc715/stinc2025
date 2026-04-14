# Club Portal Data Retention Note

This document records the current retention behavior in code today.

Important: this is not a target policy. It is a runtime-truth note. If a deployment privacy notice claims shorter retention than the rows below, that notice would not match the current implementation.

## 1. Current position

The repository currently has a mix of:

- short-lived technical records with explicit TTLs and cleanup logic
- core operational records with no automatic deletion job

For the second category, the real retention period today is:

**retained until manually deleted, removed by ad hoc database work, or changed by future code**

That means the current effective duration for several important data classes is **indefinite / long-lived**.

## 2. Core business records

### Chat sessions and chat messages

- Data classes:
  - [ChatSession.java](../backend/src/main/java/com/clubportal/model/ChatSession.java)
  - [ChatMessage.java](../backend/src/main/java/com/clubportal/model/ChatMessage.java)
- Current user-facing statement in the chat UI:
  - "Your messages are stored in your account." in [frontend/club.html](../frontend/club.html)
- Current behavior:
  - persisted in the database
  - no scheduled cleanup or TTL-based delete job exists for chat sessions/messages
  - human handoff mode resets to `AI` after `60` minutes of inactivity by default, but that mode reset does not delete stored chat records
- Current effective retention:
  - **indefinite / until manual deletion**

### Booking records

- Data class:
  - [BookingRecord.java](../backend/src/main/java/com/clubportal/model/BookingRecord.java)
- Current behavior:
  - user booking history is loaded from stored booking records in [BookingController.java](../backend/src/main/java/com/clubportal/controller/BookingController.java)
  - club-side booking/member views also read historical booking records
  - no automatic retention cleanup exists for booking records
- Current effective retention:
  - **indefinite / until manual deletion**

### Membership records

- Data class:
  - [UserMembership.java](../backend/src/main/java/com/clubportal/model/UserMembership.java)
- Current behavior:
  - user memberships and club membership rosters are loaded from persisted membership records in [MembershipController.java](../backend/src/main/java/com/clubportal/controller/MembershipController.java)
  - no automatic retention cleanup exists for membership records
- Current effective retention:
  - **indefinite / until manual deletion**

### Payment records

There are two related record types:

- checkout/payment workflow records in [CheckoutSession.java](../backend/src/main/java/com/clubportal/model/CheckoutSession.java)
- transaction records in [TransactionRecord.java](../backend/src/main/java/com/clubportal/model/TransactionRecord.java)

#### Checkout sessions

- Current behavior:
  - active checkout sessions are cleaned up when expired by [CheckoutSessionService.java](../backend/src/main/java/com/clubportal/service/CheckoutSessionService.java)
  - expired active sessions are marked `EXPIRED`
  - paid, cancelled, failed, and historical checkout session rows are not automatically deleted
- Current effective retention:
  - active unfinished sessions: **expire operationally**
  - historical checkout session rows: **indefinite / until manual deletion**

#### Transaction records

- Current behavior:
  - completed membership purchase transactions are persisted
  - no automatic retention cleanup exists for transaction records
- Current effective retention:
  - **indefinite / until manual deletion**

### Deletion request records

- Data class:
  - [ProfileDeletionRequest.java](../backend/src/main/java/com/clubportal/model/ProfileDeletionRequest.java)
- Current behavior:
  - deletion requests submitted through the profile API are persisted for later manual review
  - no automatic cleanup or automatic erase-execution job exists for these records
- Current effective retention:
  - **indefinite / until manual deletion**

### Security event records

- Data class:
  - [SecurityEventLog.java](../backend/src/main/java/com/clubportal/model/SecurityEventLog.java)
- Current behavior:
  - security-relevant events such as login failures, blocked logins, token rejection, access denial, export, deletion requests, and session rotation are persisted for audit and incident follow-up
  - no automatic cleanup job currently deletes these records
- Current effective retention:
  - **indefinite / until manual deletion**

## 3. Temporary technical records with explicit TTLs

These records do have concrete expiry behavior today.

### Registration email verification

- Service:
  - [RegistrationEmailVerificationService.java](../backend/src/main/java/com/clubportal/service/RegistrationEmailVerificationService.java)
- Defaults from [application.yml](../backend/src/main/resources/application.yml):
  - verification code TTL: `600` seconds
  - verified registration window: `1800` seconds
- Current effective retention:
  - code rows persist only while the code or verified window is still active, then are deleted by cleanup

### Profile email change verification

- Service:
  - [ProfileEmailVerificationService.java](../backend/src/main/java/com/clubportal/service/ProfileEmailVerificationService.java)
- Default from [application.yml](../backend/src/main/resources/application.yml):
  - verification code TTL: `600` seconds
- Current effective retention:
  - pending verification rows are deleted after expiry or successful verification

### Password reset tokens

- Service:
  - [PasswordResetService.java](../backend/src/main/java/com/clubportal/service/PasswordResetService.java)
- Default from [application.yml](../backend/src/main/resources/application.yml):
  - reset token TTL: `3600` seconds
- Current effective retention:
  - token rows are deleted after expiry or successful consumption

### Booking holds / active checkout holds

- Service/config:
  - [CheckoutSessionService.java](../backend/src/main/java/com/clubportal/service/CheckoutSessionService.java)
  - [application.yml](../backend/src/main/resources/application.yml)
- Default from config:
  - booking hold TTL: `600` seconds
- Current effective retention:
  - active holds expire operationally and are released

## 4. Browser-side retention

The frontend also keeps some user-linked data in browser storage:

- auth token in `sessionStorage`, see [auth-session.js](../frontend/auth-session.js)
- sanitized user/profile payloads and selected-club state in `localStorage`, see [auth-session.js](../frontend/auth-session.js) and related frontend pages
- auth cookies issued by [StreamAuthCookieService.java](../backend/src/main/java/com/clubportal/security/StreamAuthCookieService.java)

Current effective durations:

- `club_portal_auth_token` cookie: `7` days
- `club_portal_stream_token` cookie: `1` day
- browser `sessionStorage`: until the browser session/tab is cleared
- browser `localStorage`: until the user clears it or frontend code removes it

## 5. Practical rule for current deployment documentation

If documentation needs to match the code today, the following statements are accurate:

- chat history is stored and currently has no automatic deletion policy
- booking records are currently retained long-term unless manually removed
- membership records are currently retained long-term unless manually removed
- completed payment and transaction history is currently retained long-term unless manually removed
- deletion request records are currently retained long-term unless manually removed
- only verification codes, password reset tokens, booking holds, and unfinished checkout sessions have short automatic lifetimes

## 6. Recommended follow-up

Before formal public deployment, convert this runtime-truth note into an explicit policy with approved retention targets for at least:

- chat sessions and messages
- booking records
- membership records
- checkout sessions
- transaction records
- deletion request records
- browser-side compatibility storage
