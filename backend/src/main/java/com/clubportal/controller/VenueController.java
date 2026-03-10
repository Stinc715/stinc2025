package com.clubportal.controller;

import com.clubportal.dto.VenueResponse;
import com.clubportal.dto.VenueUpsertRequest;
import com.clubportal.model.User;
import com.clubportal.model.Venue;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.VenueRepository;
import com.clubportal.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs/{clubId}/venues")
public class VenueController {

    private final ClubRepository clubRepo;
    private final VenueRepository venueRepo;
    private final ClubAdminRepository clubAdminRepo;
    private final CurrentUserService currentUserService;

    public VenueController(ClubRepository clubRepo,
                           VenueRepository venueRepo,
                           ClubAdminRepository clubAdminRepo,
                           CurrentUserService currentUserService) {
        this.clubRepo = clubRepo;
        this.venueRepo = venueRepo;
        this.clubAdminRepo = clubAdminRepo;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<?> listVenues(@PathVariable Integer clubId) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }
        List<VenueResponse> out = venueRepo.findByClubId(clubId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(out);
    }

    @PostMapping
    public ResponseEntity<?> createVenue(@PathVariable Integer clubId, @RequestBody VenueUpsertRequest req) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        String name = safe(req.getName());
        if (name.isBlank()) {
            return ResponseEntity.badRequest().body("Missing venue name");
        }

        Venue v = new Venue();
        v.setClubId(clubId);
        v.setVenueName(name);
        v.setLocation(safe(req.getLocation()));
        v.setCapacity(req.getCapacity());

        Venue saved = venueRepo.save(v);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/{venueId}")
    public ResponseEntity<?> updateVenue(@PathVariable Integer clubId,
                                         @PathVariable Integer venueId,
                                         @RequestBody VenueUpsertRequest req) {
        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        Venue v = venueRepo.findById(venueId).orElse(null);
        if (v == null || !clubId.equals(v.getClubId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venue not found");
        }

        String name = safe(req.getName());
        if (!name.isBlank()) {
            v.setVenueName(name);
        }
        if (req.getLocation() != null) {
            v.setLocation(safe(req.getLocation()));
        }
        if (req.getCapacity() != null) {
            v.setCapacity(req.getCapacity());
        }

        Venue saved = venueRepo.save(v);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{venueId}")
    public ResponseEntity<?> deleteVenue(@PathVariable Integer clubId, @PathVariable Integer venueId) {
        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        Venue v = venueRepo.findById(venueId).orElse(null);
        if (v == null || !clubId.equals(v.getClubId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venue not found");
        }

        venueRepo.delete(v);
        return ResponseEntity.ok(java.util.Map.of("deleted", true));
    }

    private ResponseEntity<?> requireClubAdmin(User me, Integer clubId) {
        if (me.getRole() == null || me.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only club accounts can manage venues");
        }

        boolean isAdmin = me.getRole() == User.Role.ADMIN;
        boolean isClubAdmin = clubAdminRepo.existsByUserIdAndClubId(me.getUserId(), clubId);
        if (!isAdmin && !isClubAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only manage your own club");
        }
        return null;
    }

    private VenueResponse toResponse(Venue v) {
        return new VenueResponse(
                v.getVenueId(),
                v.getClubId(),
                v.getVenueName(),
                safe(v.getLocation()),
                v.getCapacity()
        );
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
