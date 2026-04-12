package com.clubportal.controller;

import com.clubportal.dto.ClubSummaryResponse;
import com.clubportal.model.Club;
import com.clubportal.model.ClubAdmin;
import com.clubportal.model.User;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubImageRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.service.CurrentUserService;
import com.clubportal.util.ClubTagCodec;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my")
public class MyClubController {

    private final CurrentUserService currentUserService;
    private final ClubAdminRepository clubAdminRepo;
    private final ClubRepository clubRepo;
    private final ClubImageRepository clubImageRepo;

    public MyClubController(CurrentUserService currentUserService,
                            ClubAdminRepository clubAdminRepo,
                            ClubRepository clubRepo,
                            ClubImageRepository clubImageRepo) {
        this.currentUserService = currentUserService;
        this.clubAdminRepo = clubAdminRepo;
        this.clubRepo = clubRepo;
        this.clubImageRepo = clubImageRepo;
    }

    @GetMapping("/clubs")
    public ResponseEntity<?> myClubs() {
        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only club accounts can access this resource");
        }

        List<Club> clubs;
        if (me.getRole() == User.Role.ADMIN) {
            clubs = clubRepo.findAll();
        } else {
            List<ClubAdmin> rows = clubAdminRepo.findByUserId(me.getUserId());
            List<Integer> ids = rows.stream().map(ClubAdmin::getClubId).distinct().toList();
            if (ids.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            clubs = clubRepo.findAllById(ids);
        }

        List<ClubSummaryResponse> out = clubs.stream().map(this::toSummary).toList();
        return ResponseEntity.ok(out);
    }

    private ClubSummaryResponse toSummary(Club c) {
        Integer id = c.getClubId();
        List<String> tags = ClubTagCodec.decode(c.getCategoryTags(), c.getCategory());
        String category = ClubTagCodec.primary(tags, c.getCategory());
        String coverImageUrl = clubImageRepo.findByClubIdOrderBySortOrderAscImageIdAsc(id).stream()
                .sorted((left, right) -> Boolean.compare(Boolean.TRUE.equals(right.getPrimaryImage()), Boolean.TRUE.equals(left.getPrimaryImage())))
                .findFirst()
                .map(img -> "/api/clubs/" + id + "/images/" + img.getImageId() + "/content")
                .orElse("");
        return new ClubSummaryResponse(
                id,
                id,
                c.getClubName(),
                c.getDescription(),
                category.isBlank() ? null : category,
                tags,
                coverImageUrl.isBlank() ? null : coverImageUrl
        );
    }
}
