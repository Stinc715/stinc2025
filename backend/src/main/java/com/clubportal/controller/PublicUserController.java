package com.clubportal.controller;

import com.clubportal.service.UserAvatarService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public/users")
public class PublicUserController {

    private final UserAvatarService userAvatarService;

    public PublicUserController(UserAvatarService userAvatarService) {
        this.userAvatarService = userAvatarService;
    }

    @GetMapping("/{userId}/avatar")
    public ResponseEntity<?> getUserAvatar(@PathVariable Integer userId) {
        UserAvatarService.AvatarContent avatar = userAvatarService.loadPublicAvatar(userId);
        if (avatar == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .contentType(avatar.mediaType())
                .body(avatar.resource());
    }
}
