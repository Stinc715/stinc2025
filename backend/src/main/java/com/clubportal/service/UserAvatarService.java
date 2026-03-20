package com.clubportal.service;

import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserAvatarService {

    private static final long MAX_AVATAR_BYTES = 4L * 1024L * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private final UserRepository userRepo;
    private final Path avatarRootDir;

    public UserAvatarService(UserRepository userRepo,
                             @Value("${clubportal.user-avatar.dir:/home/ec2-user/app/uploads/user-avatars}") String avatarDir) {
        this.userRepo = userRepo;
        this.avatarRootDir = Paths.get(safe(avatarDir)).toAbsolutePath().normalize();
    }

    public AvatarUploadResult storeAvatar(User user, MultipartFile file) {
        if (user == null || user.getUserId() == null) {
            return AvatarUploadResult.failed("Missing user");
        }
        if (file == null || file.isEmpty()) {
            return AvatarUploadResult.failed("No avatar file provided");
        }

        String contentType = safe(file.getContentType()).toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(contentType)) {
            return AvatarUploadResult.failed("Only JPG, PNG, GIF, and WEBP avatars are supported");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            return AvatarUploadResult.failed("Avatar must be 4MB or smaller");
        }

        String fileName = generateFileName(user.getUserId(), extensionFor(contentType, file.getOriginalFilename()));
        Path target = resolveAvatarPath(fileName);
        if (target == null) {
            return AvatarUploadResult.failed("Invalid avatar path");
        }

        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            return AvatarUploadResult.failed("Failed to store avatar");
        }

        deleteAvatarFile(user.getAvatarFileName());
        user.setAvatarFileName(fileName);
        user.setAvatarMimeType(contentType);
        user.setAvatarUpdatedAt(LocalDateTime.now());
        User saved = userRepo.save(user);
        return AvatarUploadResult.ok(publicAvatarUrl(saved));
    }

    public AvatarContent loadPublicAvatar(Integer userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null || safe(user.getAvatarFileName()).isBlank()) {
            return null;
        }
        Path filePath = resolveAvatarPath(user.getAvatarFileName());
        if (filePath == null || !Files.exists(filePath)) {
            return null;
        }
        Resource resource = new FileSystemResource(filePath.toFile());
        MediaType mediaType = mediaTypeFor(user.getAvatarMimeType(), filePath);
        return new AvatarContent(resource, mediaType);
    }

    public String publicAvatarUrl(User user) {
        if (user == null || user.getUserId() == null || safe(user.getAvatarFileName()).isBlank()) {
            return "";
        }
        String version = user.getAvatarUpdatedAt() == null
                ? ""
                : String.valueOf(user.getAvatarUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        return version.isBlank()
                ? "/api/public/users/" + user.getUserId() + "/avatar"
                : "/api/public/users/" + user.getUserId() + "/avatar?v=" + version;
    }

    private void deleteAvatarFile(String fileName) {
        Path existing = resolveAvatarPath(fileName);
        if (existing == null) return;
        try {
            Files.deleteIfExists(existing);
        } catch (IOException ignored) {
        }
    }

    private Path resolveAvatarPath(String fileName) {
        String normalizedFileName = safe(fileName);
        if (normalizedFileName.isBlank()) return null;
        Path path = avatarRootDir.resolve(normalizedFileName).normalize();
        return path.startsWith(avatarRootDir) ? path : null;
    }

    private static String generateFileName(Integer userId, String extension) {
        return "user-" + userId + "-" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private static String extensionFor(String contentType, String originalFileName) {
        String lowerName = safe(originalFileName).toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".png")) return ".png";
        if (lowerName.endsWith(".gif")) return ".gif";
        if (lowerName.endsWith(".webp")) return ".webp";
        if ("image/png".equals(contentType)) return ".png";
        if ("image/gif".equals(contentType)) return ".gif";
        if ("image/webp".equals(contentType)) return ".webp";
        return ".jpg";
    }

    private static MediaType mediaTypeFor(String mimeType, Path path) {
        try {
            String detected = Files.probeContentType(path);
            if (detected != null && !detected.isBlank()) {
                return MediaType.parseMediaType(detected);
            }
        } catch (IOException ignored) {
        }
        String safeMimeType = safe(mimeType).toLowerCase(Locale.ROOT);
        if (!safeMimeType.isBlank()) {
            try {
                return MediaType.parseMediaType(safeMimeType);
            } catch (Exception ignored) {
            }
        }
        return MediaType.IMAGE_JPEG;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public record AvatarUploadResult(boolean success, String avatarUrl, String message) {
        public static AvatarUploadResult ok(String avatarUrl) {
            return new AvatarUploadResult(true, avatarUrl, null);
        }

        public static AvatarUploadResult failed(String message) {
            return new AvatarUploadResult(false, null, message);
        }
    }

    public record AvatarContent(Resource resource, MediaType mediaType) {
    }
}
