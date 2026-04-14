# Club Portal DPIA Working Note

This is an internal working document for the current repository state. It describes what the system actually does today and highlights the main data-protection risks and controls that exist in code now.

It is not legal advice, and it is not a substitute for a formal deployment-specific DPIA signed off by the real deployment owner.

## 1. Why this document exists

Before real public deployment, Club Portal should be treated as processing personal data in several linked flows:

- account creation and authentication
- profile management
- bookings and membership management
- payment session and transaction handling
- club-to-member chat
- optional third-party integrations such as Google sign-in, Google Maps, Stripe, and AI-assisted chat

The codebase already stores enough user-linked operational data that a deployment owner should document risks, controls, and unresolved gaps before launch.

## 2. Current processing scope in this repository

### User and profile data

The backend persists account and profile fields including name, email, password hash, role, optional phone, and avatar metadata in [User.java](../backend/src/main/java/com/clubportal/model/User.java).

Authenticated users can now:

- export current account-linked data through `GET /api/profile/export`
- submit a manual-review deletion request through `POST /api/profile/deletion-request`

Deletion-request records are persisted in [ProfileDeletionRequest.java](../backend/src/main/java/com/clubportal/model/ProfileDeletionRequest.java).

The frontend also stores some user-linked state in browser storage:

- session token in `sessionStorage`, see [auth-session.js](../frontend/auth-session.js)
- sanitized user/profile payloads in `localStorage`, see [auth-session.js](../frontend/auth-session.js)
- auth cookies via [StreamAuthCookieService.java](../backend/src/main/java/com/clubportal/security/StreamAuthCookieService.java)

### Membership data

Membership status, dates, and remaining booking entitlements are persisted in [UserMembership.java](../backend/src/main/java/com/clubportal/model/UserMembership.java).

### Booking data

Booking records are persisted in [BookingRecord.java](../backend/src/main/java/com/clubportal/model/BookingRecord.java). Club-side views also expose member booking information to club operators through [BookingController.java](../backend/src/main/java/com/clubportal/controller/BookingController.java).

### Payment data

The repository stores:

- payment workflow/session data in [CheckoutSession.java](../backend/src/main/java/com/clubportal/model/CheckoutSession.java)
- transaction records for completed membership purchases in [TransactionRecord.java](../backend/src/main/java/com/clubportal/model/TransactionRecord.java)

Payments can run in virtual mode or Stripe mode through [PaymentController.java](../backend/src/main/java/com/clubportal/controller/PaymentController.java) and [CheckoutSessionService.java](../backend/src/main/java/com/clubportal/service/CheckoutSessionService.java).

### Chat data

The repository stores:

- chat session metadata in [ChatSession.java](../backend/src/main/java/com/clubportal/model/ChatSession.java)
- chat message content in [ChatMessage.java](../backend/src/main/java/com/clubportal/model/ChatMessage.java)

Current runtime behavior:

- user messages start in `AI` mode by default, see [ChatMessageService.java](../backend/src/main/java/com/clubportal/service/ChatMessageService.java)
- the backend persists both the user message and the assistant reply
- club staff cannot reply while the session is still in `AI` mode, see [ChatMessageService.java](../backend/src/main/java/com/clubportal/service/ChatMessageService.java)
- the user can request human support, and the session moves to `HANDOFF_REQUESTED` / `HUMAN`, see [ChatSessionController.java](../backend/src/main/java/com/clubportal/controller/ChatSessionController.java) and [ChatSessionService.java](../backend/src/main/java/com/clubportal/service/ChatSessionService.java)
- if a human-managed chat session is idle for `60` minutes by default, the mode resets back to `AI`, but the records are not deleted, see [application.yml](../backend/src/main/resources/application.yml) and [ChatSessionService.java](../backend/src/main/java/com/clubportal/service/ChatSessionService.java)

The current user-facing chat UI already tells users:

> "Start a conversation with the club. Your messages are stored in your account."

This string is rendered in [frontend/club.html](../frontend/club.html).

## 3. Third-party processors and optional integrations

Depending on deployment configuration, the system can involve third-party services:

- Google sign-in via [GoogleAuthService.java](../backend/src/main/java/com/clubportal/service/GoogleAuthService.java)
- Google Maps front-end integration via public config
- Stripe checkout and webhook processing via [StripeCheckoutGateway.java](../backend/src/main/java/com/clubportal/service/StripeCheckoutGateway.java)
- AI-assisted chat / embeddings via [OpenAiConfig.java](../backend/src/main/java/com/clubportal/config/OpenAiConfig.java) and the club chat services

These integrations are configuration-dependent. The repository exposes some of this state publicly via [PublicConfigController.java](../backend/src/main/java/com/clubportal/controller/PublicConfigController.java).

## 4. Current access model

### End users

Users can access their own profile, bookings, memberships, and their own club chat threads.

### Club staff

Club-side operators can access:

- member booking details, including member name and email, via [BookingController.java](../backend/src/main/java/com/clubportal/controller/BookingController.java)
- membership holder details, including member name and email, via [MembershipController.java](../backend/src/main/java/com/clubportal/controller/MembershipController.java)
- club conversation lists and chat threads, including user-linked chat history, via [ChatController.java](../backend/src/main/java/com/clubportal/controller/ChatController.java)

### Platform administrators

This repository includes admin-level capabilities and deployment-level access paths, but the codebase does not yet publish a deployment-specific operational access policy to end users.

## 5. Main data-protection risks in the current implementation

### Risk 1: long-lived user-linked operational history

Chat, booking, membership, and completed payment records are user-linked and currently have no formal automatic deletion policy in code.

### Risk 2: optional third-party processing

Depending on deployment, the service may send data to third-party processors for sign-in, mapping, payment, and AI-assisted chat.

### Risk 3: role-based internal access

Club-side operators can access member identity and operational records needed to run club workflows. This is expected for the product, but it should be explicitly documented in a deployment privacy notice and internal access policy.

### Risk 4: browser-side persistence

The frontend still stores some user-linked data in browser storage for compatibility. That expands the local exposure surface on shared devices.

### Risk 5: partial user-rights tooling

The repository now supports authenticated JSON export and deletion-request submission, but it still does not implement a full self-service erase workflow or an operator-facing resolution workflow in the product UI.

## 6. Controls that already exist in code

- authenticated API access and role checks across user and club flows
- password hashing and password policy enforcement
- login throttling
- structured security-event audit logging for registration, login, token rejection, access denial, export, deletion requests, and session rotation
- optional high-severity security alert email dispatch when an operational mailbox is configured
- email verification and password reset TTLs
- expiring checkout sessions and booking holds
- reset of inactive human handoff mode back to AI after idle timeout
- authenticated profile export and deletion-request submission endpoints
- configurable auth-cookie and stream-cookie lifetimes plus explicit session-rotation support
- CSP and cookie security settings in the frontend/backend configuration
- HTTPS redirect and HSTS in the deployment nginx configuration

## 7. Gaps before formal production deployment

The following are still open from a DPIA perspective:

- no formal deployment-specific lawful-basis statement in code or docs
- no formal retention schedule for core business data
- no automated deletion execution flow for user data
- no operator-facing workflow in the product for resolving deletion requests
- no deployment-specific list of controllers, processors, contacts, or support channels
- no documented review sign-off for AI-assisted chat risk, including prompt/data boundaries and operator oversight

## 8. Practical conclusion

For the repository in its current state, a deployment owner should treat the following as the minimum pre-launch DPIA actions:

1. confirm the real deployment contact, controller/processor roles, and enabled third-party services
2. publish a deployment-specific privacy notice and terms
3. adopt an explicit retention schedule for chat, booking, membership, and payment records
4. decide whether AI-assisted chat is enabled and document the operational safeguards
5. add an internal process for correction, export, deletion, and deletion-request resolution

Until those steps are completed, this repository should be understood as technically capable of production-style personal-data processing, but not yet fully governed as a finished public service.
