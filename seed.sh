#!/usr/bin/env bash
set -euo pipefail

API="http://localhost:8080"
ADMIN_TOKEN="change-me-123"

echo "1) Create club"
CLUB_ID=$(curl -sS -X POST "$API/api/admin/clubs" \
  -H "Content-Type: application/json" \
  -H "X-ADMIN-TOKEN: $ADMIN_TOKEN" \
  -d '{"clubName":"Basketball Club","sportType":"Basketball","status":0}' \
  | sed -n 's/.*"clubId":\([0-9]\+\).*/\1/p')
echo "clubId=$CLUB_ID"

echo "2) Create venue"
VENUE_ID=$(curl -sS -X POST "$API/api/admin/venues" \
  -H "Content-Type: application/json" \
  -H "X-ADMIN-TOKEN: $ADMIN_TOKEN" \
  -d "{\"clubId\":$CLUB_ID,\"venueName\":\"Main Court\",\"capacity\":20}" \
  | sed -n 's/.*"venueId":\([0-9]\+\).*/\1/p')
echo "venueId=$VENUE_ID"

echo "3) Create membership plan"
PLAN_ID=$(curl -sS -X POST "$API/api/admin/membership-plans" \
  -H "Content-Type: application/json" \
  -H "X-ADMIN-TOKEN: $ADMIN_TOKEN" \
  -d "{\"clubId\":$CLUB_ID,\"planName\":\"Monthly Pass\",\"durationDays\":30,\"price\":10.00,\"maxBookingsPerDay\":2,\"maxParallelBookings\":2,\"status\":0}" \
  | sed -n 's/.*"planId":\([0-9]\+\).*/\1/p')
echo "planId=$PLAN_ID"

echo "4) Purchase membership for userId=1"
USER_MEMBERSHIP_ID=$(curl -sS -X POST "$API/api/user-memberships/purchase" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"planId\":$PLAN_ID}" \
  | sed -n 's/.*"userMembershipId":\([0-9]\+\).*/\1/p')
echo "userMembershipId=$USER_MEMBERSHIP_ID"

echo "5) Create timeslot (non-members)"
curl -sS -X POST "$API/api/admin/timeslots" \
  -H "Content-Type: application/json" \
  -H "X-ADMIN-TOKEN: $ADMIN_TOKEN" \
  -d "{\"clubId\":$CLUB_ID,\"venueId\":$VENUE_ID,\"startTime\":\"2026-01-16T10:00:00\",\"endTime\":\"2026-01-16T11:00:00\",\"capacity\":10,\"allowBooking\":true,\"membersOnly\":false,\"price\":5.00,\"membersPrice\":3.00}" \
  >/dev/null
echo "timeslot (non-members) created"

echo "6) Create timeslot (members-only)"
curl -sS -X POST "$API/api/admin/timeslots" \
  -H "Content-Type: application/json" \
  -H "X-ADMIN-TOKEN: $ADMIN_TOKEN" \
  -d "{\"clubId\":$CLUB_ID,\"venueId\":$VENUE_ID,\"startTime\":\"2026-01-16T12:00:00\",\"endTime\":\"2026-01-16T13:00:00\",\"capacity\":10,\"allowBooking\":true,\"membersOnly\":true,\"price\":5.00,\"membersPrice\":3.00}" \
  >/dev/null
echo "timeslot (members-only) created"

echo "Done. Use frontend with:"
echo "userId=1"
echo "userMembershipId=$USER_MEMBERSHIP_ID"
