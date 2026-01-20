$ErrorActionPreference = "Stop"

$API = "http://localhost:8080"
$ADMIN_TOKEN = "change-me-123"

$adminHeaders = @{
    "X-ADMIN-TOKEN" = $ADMIN_TOKEN
}

Write-Host "1) Create club"
$club = Invoke-RestMethod -Method Post -Uri "$API/api/admin/clubs" `
    -Headers $adminHeaders -ContentType "application/json" `
    -Body (@{ clubName = "Basketball Club"; sportType = "Basketball"; status = 0 } | ConvertTo-Json -Compress)
$clubId = $club.clubId
Write-Host "clubId=$clubId"

Write-Host "2) Create venue"
$venue = Invoke-RestMethod -Method Post -Uri "$API/api/admin/venues" `
    -Headers $adminHeaders -ContentType "application/json" `
    -Body (@{ clubId = $clubId; venueName = "Main Court"; capacity = 20 } | ConvertTo-Json -Compress)
$venueId = $venue.venueId
Write-Host "venueId=$venueId"

Write-Host "3) Create membership plan"
$plan = Invoke-RestMethod -Method Post -Uri "$API/api/admin/membership-plans" `
    -Headers $adminHeaders -ContentType "application/json" `
    -Body (@{ clubId = $clubId; planName = "Monthly Pass"; durationDays = 30; price = 10.00; maxBookingsPerDay = 2; maxParallelBookings = 2; status = 0 } | ConvertTo-Json -Compress)
$planId = $plan.planId
Write-Host "planId=$planId"

Write-Host "4) Purchase membership for userId=1"
$membership = Invoke-RestMethod -Method Post -Uri "$API/api/user-memberships/purchase" `
    -ContentType "application/json" `
    -Body (@{ userId = 1; planId = $planId } | ConvertTo-Json -Compress)
$userMembershipId = $membership.userMembershipId
Write-Host "userMembershipId=$userMembershipId"

Write-Host "5) Create timeslot (non-members)"
Invoke-RestMethod -Method Post -Uri "$API/api/admin/timeslots" `
    -Headers $adminHeaders -ContentType "application/json" `
    -Body (@{ clubId = $clubId; venueId = $venueId; startTime = "2026-01-16T10:00:00"; endTime = "2026-01-16T11:00:00"; capacity = 10; allowBooking = $true; membersOnly = $false; price = 5.00; membersPrice = 3.00 } | ConvertTo-Json -Compress) `
    | Out-Null
Write-Host "timeslot (non-members) created"

Write-Host "6) Create timeslot (members-only)"
Invoke-RestMethod -Method Post -Uri "$API/api/admin/timeslots" `
    -Headers $adminHeaders -ContentType "application/json" `
    -Body (@{ clubId = $clubId; venueId = $venueId; startTime = "2026-01-16T12:00:00"; endTime = "2026-01-16T13:00:00"; capacity = 10; allowBooking = $true; membersOnly = $true; price = 5.00; membersPrice = 3.00 } | ConvertTo-Json -Compress) `
    | Out-Null
Write-Host "timeslot (members-only) created"

Write-Host "Done. Use frontend with:"
Write-Host "userId=1"
Write-Host "userMembershipId=$userMembershipId"
