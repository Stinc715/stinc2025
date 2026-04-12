package com.clubportal.controller;

import com.clubportal.dto.ClubDetailResponse;
import com.clubportal.dto.ClubImageResponse;
import com.clubportal.dto.ClubSummaryResponse;
import com.clubportal.dto.ClubUpsertRequest;
import com.clubportal.model.Club;
import com.clubportal.model.ClubImage;
import com.clubportal.model.ClubAdmin;
import com.clubportal.model.User;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubImageRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.service.CurrentUserService;
import com.clubportal.util.ClubTagCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {
    private static final Logger log = LoggerFactory.getLogger(ClubController.class);
    private static final Pattern HH_MM = Pattern.compile("^([01]\\d|2[0-3]):([0-5]\\d)$");
    private static final Pattern AUTO_CLUB_PLACEHOLDER = Pattern.compile("^auto\\s+club(?:\\s+\\d+)?$", Pattern.CASE_INSENSITIVE);
    private static final long MAX_IMAGE_BYTES = 8L * 1024L * 1024L;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );
    private static final Set<String> PLACEHOLDER_VALUES = Set.of(
            "tmp",
            "test",
            "testing",
            "placeholder",
            "tbd",
            "demo",
            "sample"
    );

    private final ClubRepository clubRepo;
    private final ClubAdminRepository clubAdminRepo;
    private final ClubImageRepository clubImageRepo;
    private final CurrentUserService currentUserService;
    private final Path clubImageRootDir;

    public ClubController(ClubRepository clubRepo,
                          ClubAdminRepository clubAdminRepo,
                          ClubImageRepository clubImageRepo,
                          CurrentUserService currentUserService,
                          @Value("${clubportal.images.dir:/home/ec2-user/app/uploads/club-images}") String clubImageDir) {
        this.clubRepo = clubRepo;
        this.clubAdminRepo = clubAdminRepo;
        this.clubImageRepo = clubImageRepo;
        this.currentUserService = currentUserService;
        this.clubImageRootDir = Paths.get(safe(clubImageDir)).toAbsolutePath().normalize();
    }

    @GetMapping
    public List<ClubSummaryResponse> listClubs() {
        return clubRepo.findAll().stream()
                // Hide obvious placeholder rows from the public discovery feed.
                .filter(this::shouldExposeInPublicList)
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/{clubId}")
    public ResponseEntity<?> getClub(@PathVariable Integer clubId) {
        return clubRepo.findById(clubId)
                .<ResponseEntity<?>>map(c -> ResponseEntity.ok(toDetail(c)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found"));
    }

    @GetMapping("/{clubId}/images")
    public ResponseEntity<?> listClubImages(@PathVariable Integer clubId) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }
        List<ClubImageResponse> out = sortClubImages(clubImageRepo.findByClubId(clubId)).stream()
                .map(img -> toImageResponse(clubId, img))
                .toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{clubId}/images/{imageId}/content")
    public ResponseEntity<?> getClubImageContent(@PathVariable Integer clubId, @PathVariable Integer imageId) {
        ClubImage image = clubImageRepo.findByImageIdAndClubId(imageId, clubId).orElse(null);
        if (image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
        }

        Path filePath = resolveClubImagePath(clubId, image.getFileName());
        if (filePath == null || !Files.exists(filePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image file missing");
        }

        MediaType mediaType = mediaTypeForImage(filePath, image.getMimeType());
        Resource body = new FileSystemResource(filePath.toFile());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .contentType(mediaType)
                .body(body);
    }

    @PostMapping(value = "/{clubId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> uploadClubImages(@PathVariable Integer clubId,
                                              @RequestParam("files") List<MultipartFile> files) {
        User me = currentUserService.requireUser();
        if (!canManageClub(me, clubId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only manage your own club");
        }
        if (clubRepo.findByIdForUpdate(clubId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }
        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body("No image files provided");
        }

        Integer maxOrder = clubImageRepo.findMaxSortOrderByClubId(clubId);
        int nextOrder = maxOrder == null ? 0 : (maxOrder + 1);
        boolean hasPrimary = clubImageRepo.findByClubId(clubId).stream()
                .anyMatch(img -> Boolean.TRUE.equals(img.getPrimaryImage()));
        List<ClubImageResponse> uploaded = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            String contentType = safe(file.getContentType()).toLowerCase(Locale.ROOT);
            if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                return ResponseEntity.badRequest().body("Only JPG/PNG/GIF/WEBP images are supported");
            }
            if (file.getSize() > MAX_IMAGE_BYTES) {
                return ResponseEntity.badRequest().body("Each image must be 8MB or smaller");
            }

            String originalName = safe(file.getOriginalFilename());
            String ext = extensionForImage(originalName, contentType);
            String generatedName = generateImageFileName(clubId, ext);
            Path target = resolveClubImagePath(clubId, generatedName);
            if (target == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid image path");
            }

            try {
                Files.createDirectories(target.getParent());
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to store image");
            }

            ClubImage row = new ClubImage();
            row.setClubId(clubId);
            row.setFileName(generatedName);
            row.setOriginalName(originalName.isBlank() ? generatedName : originalName);
            row.setMimeType(contentType);
            row.setSizeBytes(file.getSize());
            row.setSortOrder(nextOrder++);
            if (!hasPrimary) {
                row.setPrimaryImage(true);
                hasPrimary = true;
            } else {
                row.setPrimaryImage(false);
            }

            ClubImage saved = clubImageRepo.save(row);
            uploaded.add(toImageResponse(clubId, saved));
        }

        if (uploaded.isEmpty()) {
            return ResponseEntity.badRequest().body("No image files uploaded");
        }
        return ResponseEntity.ok(uploaded);
    }

    @PutMapping("/{clubId}/images/{imageId}/primary")
    @Transactional
    public ResponseEntity<?> setPrimaryClubImage(@PathVariable Integer clubId, @PathVariable Integer imageId) {
        User me = currentUserService.requireUser();
        if (!canManageClub(me, clubId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only manage your own club");
        }
        if (clubRepo.findByIdForUpdate(clubId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        List<ClubImage> images = clubImageRepo.findByClubId(clubId);
        if (images.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No images found");
        }
        ClubImage selected = images.stream()
                .filter(image -> image.getImageId() != null && image.getImageId().equals(imageId))
                .findFirst()
                .orElse(null);
        if (selected == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
        }
        for (ClubImage image : images) {
            boolean isPrimary = image.getImageId() != null && image.getImageId().equals(imageId);
            image.setPrimaryImage(isPrimary);
        }
        clubImageRepo.saveAll(images);

        return ResponseEntity.ok(toImageResponse(clubId, selected));
    }

    @DeleteMapping("/{clubId}/images/{imageId}")
    @Transactional
    public ResponseEntity<?> deleteClubImage(@PathVariable Integer clubId, @PathVariable Integer imageId) {
        User me = currentUserService.requireUser();
        if (!canManageClub(me, clubId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only manage your own club");
        }
        if (clubRepo.findByIdForUpdate(clubId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        ClubImage image = clubImageRepo.findByImageIdAndClubId(imageId, clubId).orElse(null);
        if (image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
        }

        Path filePath = resolveClubImagePath(clubId, image.getFileName());
        boolean deletedWasPrimary = Boolean.TRUE.equals(image.getPrimaryImage());
        clubImageRepo.delete(image);
        if (filePath != null) {
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {
            }
        }

        if (deletedWasPrimary) {
            List<ClubImage> remaining = sortClubImages(clubImageRepo.findByClubId(clubId));
            if (!remaining.isEmpty()) {
                ClubImage first = remaining.get(0);
                first.setPrimaryImage(true);
                clubImageRepo.save(first);
            }
        }

        return ResponseEntity.ok(java.util.Map.of("deleted", true, "imageId", imageId));
    }

    @PostMapping
    public ResponseEntity<?> createClub(@RequestBody ClubUpsertRequest req) {
        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only club accounts can create clubs");
        }
        ResponseEntity<?> validationErr = validateUpsert(req);
        if (validationErr != null) return validationErr;

        String name = safe(req.getName());
        if (name.isBlank()) {
            return ResponseEntity.badRequest().body("Missing club name");
        }

        Club club = new Club();
        club.setClubName(name);

        applyUpsert(club, req, true);

        Club saved = clubRepo.save(club);

        // Creator becomes the first club admin.
        ClubAdmin admin = new ClubAdmin();
        admin.setUserId(me.getUserId());
        admin.setClubId(saved.getClubId());
        clubAdminRepo.save(admin);

        log.info("Created club id={} by userId={} email={} name='{}' category='{}' description='{}'",
                saved.getClubId(),
                me.getUserId(),
                safe(me.getEmail()),
                safe(saved.getClubName()),
                safe(saved.getCategory()),
                safe(saved.getDescription()));

        return ResponseEntity.ok(toDetail(saved));
    }

    @PutMapping("/{clubId}")
    public ResponseEntity<?> updateClub(@PathVariable Integer clubId, @RequestBody ClubUpsertRequest req) {
        User me = currentUserService.requireUser();
        ResponseEntity<?> validationErr = validateUpsert(req);
        if (validationErr != null) return validationErr;

        Club club = clubRepo.findById(clubId).orElse(null);
        if (club == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        if (!canManageClub(me, clubId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own club");
        }

        applyUpsert(club, req, false);

        Club saved = clubRepo.save(club);
        log.info("Updated club id={} by userId={} email={} name='{}' category='{}' description='{}'",
                saved.getClubId(),
                me.getUserId(),
                safe(me.getEmail()),
                safe(saved.getClubName()),
                safe(saved.getCategory()),
                safe(saved.getDescription()));
        return ResponseEntity.ok(toDetail(saved));
    }

    private ClubSummaryResponse toSummary(Club c) {
        Integer id = c.getClubId();
        List<String> tags = ClubTagCodec.decode(c.getCategoryTags(), c.getCategory());
        String category = ClubTagCodec.primary(tags, c.getCategory());
        String coverImageUrl = resolvePrimaryClubImageUrl(id);
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

    private ClubDetailResponse toDetail(Club c) {
        Integer id = c.getClubId();
        List<String> tags = ClubTagCodec.decode(c.getCategoryTags(), c.getCategory());
        String category = ClubTagCodec.primary(tags, c.getCategory());
        String email = safe(c.getEmail());
        String phone = safe(c.getPhone());
        String location = safe(c.getDisplayLocation());
        String placeId = safe(c.getGooglePlaceId());
        Double locationLat = c.getLocationLat();
        Double locationLng = c.getLocationLng();
        String openingStart = safe(c.getOpeningStart());
        String openingEnd = safe(c.getOpeningEnd());
        String coverImageUrl = resolvePrimaryClubImageUrl(id);
        return new ClubDetailResponse(
                id,
                id,
                c.getClubName(),
                c.getDescription(),
                category.isBlank() ? null : category,
                email.isBlank() ? null : email,
                phone.isBlank() ? null : phone,
                location.isBlank() ? null : location,
                placeId.isBlank() ? null : placeId,
                locationLat,
                locationLng,
                openingStart.isBlank() ? null : openingStart,
                openingEnd.isBlank() ? null : openingEnd,
                c.getDisplayCourts(),
                tags,
                coverImageUrl.isBlank() ? null : coverImageUrl
        );
    }

    private void applyUpsert(Club club, ClubUpsertRequest req, boolean isCreate) {
        if (!isCreate) {
            String name = safe(req.getName());
            if (!name.isBlank()) {
                club.setClubName(name);
            }
        }

        String category = safe(req.getCategory());
        List<String> nextTags = req.getTags() == null ? null : ClubTagCodec.normalize(req.getTags());
        if (nextTags != null) {
            if (nextTags.isEmpty() && !category.isBlank()) {
                nextTags = List.of(category);
            }
            if (!nextTags.isEmpty()) {
                club.setCategory(nextTags.get(0));
                club.setCategoryTags(ClubTagCodec.encode(nextTags, nextTags.get(0)));
            }
        } else if (!category.isBlank()) {
            List<String> existingTags = new ArrayList<>(ClubTagCodec.decode(club.getCategoryTags(), club.getCategory()));
            if (existingTags.isEmpty()) {
                existingTags.add(category);
            } else {
                existingTags.set(0, category);
            }
            club.setCategory(category);
            club.setCategoryTags(ClubTagCodec.encode(existingTags, category));
        }

        if (req.getDescription() != null) {
            club.setDescription(safe(req.getDescription()));
        }

        if (req.getEmail() != null) {
            club.setEmail(safe(req.getEmail()));
        }

        if (req.getPhone() != null) {
            club.setPhone(safe(req.getPhone()));
        }

        if (req.getLocation() != null) {
            String location = safe(req.getLocation());
            club.setDisplayLocation(location.isBlank() ? null : location);
            if (location.isBlank()) {
                club.setGooglePlaceId(null);
                club.setLocationLat(null);
                club.setLocationLng(null);
            }
        }

        if (req.getPlaceId() != null) {
            String placeId = safe(req.getPlaceId());
            club.setGooglePlaceId(placeId.isBlank() ? null : placeId);
            if (placeId.isBlank()) {
                club.setLocationLat(null);
                club.setLocationLng(null);
            }
        }

        if (req.getLocationLat() != null || req.getLocationLng() != null) {
            club.setLocationLat(req.getLocationLat());
            club.setLocationLng(req.getLocationLng());
        }

        if (req.getOpeningStart() != null) {
            club.setOpeningStart(normalizeHHmm(req.getOpeningStart()));
        }

        if (req.getOpeningEnd() != null) {
            club.setOpeningEnd(normalizeHHmm(req.getOpeningEnd()));
        }

        if (req.getCourtsCount() != null) {
            club.setDisplayCourts(req.getCourtsCount());
        }
    }

    private ResponseEntity<?> validateUpsert(ClubUpsertRequest req) {
        if (req.getCourtsCount() != null && req.getCourtsCount() < 0) {
            return ResponseEntity.badRequest().body("courtsCount must be >= 0");
        }

        String openingStart = req.getOpeningStart();
        if (openingStart != null && normalizeHHmm(openingStart) == null && !safe(openingStart).isBlank()) {
            return ResponseEntity.badRequest().body("openingStart must match HH:mm");
        }

        String openingEnd = req.getOpeningEnd();
        if (openingEnd != null && normalizeHHmm(openingEnd) == null && !safe(openingEnd).isBlank()) {
            return ResponseEntity.badRequest().body("openingEnd must match HH:mm");
        }

        String startNorm = openingStart == null ? null : normalizeHHmm(openingStart);
        String endNorm = openingEnd == null ? null : normalizeHHmm(openingEnd);
        boolean hasStart = startNorm != null && !startNorm.isBlank();
        boolean hasEnd = endNorm != null && !endNorm.isBlank();
        if (hasStart && hasEnd && startNorm.compareTo(endNorm) >= 0) {
            return ResponseEntity.badRequest().body("openingStart must be earlier than openingEnd");
        }

        boolean hasLat = req.getLocationLat() != null;
        boolean hasLng = req.getLocationLng() != null;
        if (hasLat != hasLng) {
            return ResponseEntity.badRequest().body("locationLat and locationLng must be provided together");
        }
        if (hasLat && (req.getLocationLat() < -90.0 || req.getLocationLat() > 90.0)) {
            return ResponseEntity.badRequest().body("locationLat must be between -90 and 90");
        }
        if (hasLng && (req.getLocationLng() < -180.0 || req.getLocationLng() > 180.0)) {
            return ResponseEntity.badRequest().body("locationLng must be between -180 and 180");
        }

        return null;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String normalizeHHmm(String s) {
        String v = safe(s);
        if (v.isBlank()) return null;
        if (!HH_MM.matcher(v).matches()) return null;
        return v;
    }

    private boolean shouldExposeInPublicList(Club club) {
        return !(looksLikePlaceholder(safe(club.getClubName())) || looksLikePlaceholder(safe(club.getDescription())));
    }

    private static boolean looksLikePlaceholder(String value) {
        String normalized = safe(value).toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) return false;
        if (PLACEHOLDER_VALUES.contains(normalized)) return true;
        return AUTO_CLUB_PLACEHOLDER.matcher(normalized).matches();
    }

    private boolean canManageClub(User me, Integer clubId) {
        if (me.getRole() == null || me.getRole() == User.Role.USER) return false;
        if (me.getRole() == User.Role.ADMIN) return true;
        return clubAdminRepo.existsByUserIdAndClubId(me.getUserId(), clubId);
    }

    private ClubImageResponse toImageResponse(Integer clubId, ClubImage img) {
        return new ClubImageResponse(
                img.getImageId(),
                "/api/clubs/" + clubId + "/images/" + img.getImageId() + "/content",
                safe(img.getOriginalName()),
                img.getSortOrder(),
                Boolean.TRUE.equals(img.getPrimaryImage())
        );
    }

    private String resolvePrimaryClubImageUrl(Integer clubId) {
        if (clubId == null) return "";
        return sortClubImages(clubImageRepo.findByClubId(clubId)).stream()
                .findFirst()
                .map(img -> "/api/clubs/" + clubId + "/images/" + img.getImageId() + "/content")
                .orElse("");
    }

    private List<ClubImage> sortClubImages(List<ClubImage> images) {
        if (images == null || images.isEmpty()) return List.of();
        return images.stream()
                .sorted(Comparator
                        .comparing((ClubImage img) -> Boolean.TRUE.equals(img.getPrimaryImage()) ? 0 : 1)
                        .thenComparing(img -> img.getSortOrder() == null ? Integer.MAX_VALUE : img.getSortOrder())
                        .thenComparing(img -> img.getImageId() == null ? Integer.MAX_VALUE : img.getImageId()))
                .toList();
    }

    private Path resolveClubImagePath(Integer clubId, String fileName) {
        String safeName = safe(fileName);
        if (safeName.isBlank() || safeName.contains("/") || safeName.contains("\\")) return null;
        Path clubDir = clubImageRootDir.resolve(String.valueOf(clubId)).normalize();
        Path target = clubDir.resolve(safeName).normalize();
        if (!target.startsWith(clubDir)) return null;
        return target;
    }

    private static String generateImageFileName(Integer clubId, String ext) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "club-" + clubId + "-" + System.currentTimeMillis() + "-" + suffix + "." + ext;
    }

    private static String extensionForImage(String originalName, String contentType) {
        String lower = safe(originalName).toLowerCase(Locale.ROOT);
        int idx = lower.lastIndexOf('.');
        if (idx > -1 && idx < lower.length() - 1) {
            String ext = lower.substring(idx + 1);
            if ("jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || "gif".equals(ext) || "webp".equals(ext)) {
                return ext;
            }
        }
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private static MediaType mediaTypeForImage(Path filePath, String storedType) {
        String st = safe(storedType);
        if (!st.isBlank()) {
            try {
                return MediaType.parseMediaType(st);
            } catch (Exception ignored) {
            }
        }
        try {
            String guessed = Files.probeContentType(filePath);
            if (guessed != null && !guessed.isBlank()) {
                return MediaType.parseMediaType(guessed);
            }
        } catch (Exception ignored) {
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
