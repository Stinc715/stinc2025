## API Contract (Baseline)

Base URL: `/api`

Auth uses `Authorization: Bearer <jwt>` (JWT contains `role` claim: `user` or `club`).

### Auth

#### POST `/api/register`

Request body:

```json
{ "fullName": "Alice", "email": "alice@example.com", "password": "...", "role": "user|club" }
```

Response (200):

```json
{ "id": 1, "fullName": "Alice", "email": "alice@example.com", "role": "user|club" }
```

Notes:
- If the email already exists: `409`.
- One email can only be used for one account type; registering again with the same email is not allowed.

#### POST `/api/login`

Request body:

```json
{ "email": "alice@example.com", "password": "..." }
```

Response (200):

```json
{ "token": "...", "id": 1, "fullName": "Alice", "email": "alice@example.com", "role": "user|club" }
```

#### POST `/api/auth/google`

Request body:

```json
{ "credential": "google_id_token" }
```

Response (200): same shape as `/api/login`.

### Clubs

#### GET `/api/clubs`

Response (200):

```json
[
  { "id": 1, "clubId": 1, "name": "Basketball Club", "description": "...", "category": "basketball", "tags": ["basketball"] }
]
```

#### GET `/api/clubs/{clubId}`

Response (200):

```json
{
  "id": 1,
  "clubId": 1,
  "name": "Basketball Club",
  "description": "...",
  "category": "basketball",
  "email": "club@example.com",
  "phone": "1234567890",
  "location": "Hall A",
  "openingStart": "08:00",
  "openingEnd": "17:00",
  "courtsCount": 2,
  "tags": ["basketball"]
}
```

#### POST `/api/clubs`

Requires: `role=club`.

Request body (aliases supported):

```json
{
  "name": "Basketball Club",
  "description": "...",
  "category": "basketball",
  "email": "club@example.com",
  "phone": "1234567890",
  "location": "Hall A",
  "openingStart": "08:00",
  "openingEnd": "17:00",
  "courtsCount": 2,
  "tags": ["basketball"]
}
```

Response (200): same as `GET /api/clubs/{clubId}`.

Behavior:
- The creator automatically becomes a `club_admin` for the created club.

#### PUT `/api/clubs/{clubId}`

Requires: `role=club` and user must be a `club_admin` of this club (or system `ADMIN`).

Request body: same as POST (partial updates supported for fields present).

Response (200): same as `GET /api/clubs/{clubId}`.

### Venues (Courts)

#### GET `/api/clubs/{clubId}/venues`

Response (200):

```json
[
  { "venueId": 1, "clubId": 1, "name": "Court A", "location": "Hall A", "capacity": 20 }
]
```

#### POST `/api/clubs/{clubId}/venues`

Requires: `role=club` and user must be a `club_admin` of this club (or system `ADMIN`).

Request body (aliases supported):

```json
{ "name": "Court A", "location": "Hall A", "capacity": 20 }
```

Response (200): same shape as `GET /api/clubs/{clubId}/venues`.

Constraints:
- One club can only have one venue. Creating a second venue returns `409`.

### Time Slots

#### GET `/api/clubs/{clubId}/timeslots?from=YYYY-MM-DD&to=YYYY-MM-DD`

Response (200):

```json
[
  {
    "timeslotId": 10,
    "venueId": 1,
    "clubId": 1,
    "venueName": "Court A",
    "startTime": "2026-02-14T10:00:00",
    "endTime": "2026-02-14T11:00:00",
    "maxCapacity": 4,
    "bookedCount": 1,
    "remaining": 3
  }
]
```

#### POST `/api/clubs/{clubId}/venues/{venueId}/timeslots`

Requires: `role=club` and user must be a `club_admin` of this club (or system `ADMIN`).

Request body:

```json
{ "startTime": "2026-02-14T10:00:00", "endTime": "2026-02-14T11:00:00", "maxCapacity": 4 }
```

Response (200): same as `GET /api/clubs/{clubId}/timeslots` item shape.

### Bookings

#### POST `/api/timeslots/{timeslotId}/bookings`

Requires: logged-in `role=user`.

Response (200):

```json
{ "bookingId": 99, "timeslotId": 10, "status": "PENDING" }
```

Validation:
- Duplicate booking for the same user+timeslot is rejected with `409`.
- If `bookedCount >= maxCapacity`, booking is rejected with `409`.

### My (Club Admin)

#### GET `/api/my/clubs`

Requires: logged-in `role=club` (or system `ADMIN`).

Response (200): same shape as `GET /api/clubs` list items.

### Chat

#### User-side chat

#### GET `/api/clubs/{clubId}/chat/messages`

Requires: logged-in `role=user`.

Response (200):

```json
{
  "clubId": 1,
  "clubName": "Basketball Club",
  "userId": 12,
  "userName": "Alice",
  "unreadCount": 2,
  "messages": [
    {
      "messageId": 101,
      "clubId": 1,
      "userId": 12,
      "sender": "user",
      "text": "Hi, is this slot beginner-friendly?",
      "authorName": "Alice",
      "readByClub": true,
      "readByUser": true,
      "createdAt": "2026-02-22T17:35:00"
    }
  ]
}
```

#### POST `/api/clubs/{clubId}/chat/messages`

Requires: logged-in `role=user`.

Request body:

```json
{ "text": "Can I bring my own ball?" }
```

Response (200): one `ChatMessageResponse` item.

#### POST `/api/clubs/{clubId}/chat/read`

Requires: logged-in `role=user`.

Response (200):

```json
{ "updated": 3 }
```

#### Club-side chat

#### GET `/api/my/clubs/{clubId}/chat/conversations`

Requires: logged-in `role=club` and current user must be admin of the club (or system `ADMIN`).

Response (200):

```json
[
  {
    "clubId": 1,
    "userId": 12,
    "userName": "Alice",
    "userEmail": "alice@example.com",
    "lastMessageId": 102,
    "lastSender": "user",
    "lastMessageText": "Can I bring my own ball?",
    "lastMessageAt": "2026-02-22T17:36:00",
    "unreadCount": 1,
    "totalMessages": 4
  }
]
```

#### GET `/api/my/clubs/{clubId}/chat/conversations/{userId}/messages`

Requires: same as above.

Response (200): same shape as user-side `GET /api/clubs/{clubId}/chat/messages`.

#### POST `/api/my/clubs/{clubId}/chat/conversations/{userId}/messages`

Requires: same as above.

Request body:

```json
{ "text": "Yes, absolutely. All levels are welcome." }
```

Response (200): one `ChatMessageResponse` item.

#### POST `/api/my/clubs/{clubId}/chat/conversations/{userId}/read`

Requires: same as above.

Response (200):

```json
{ "updated": 1 }
```
