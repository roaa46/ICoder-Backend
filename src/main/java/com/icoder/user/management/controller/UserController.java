package com.icoder.user.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.user.management.dto.auth.UpdateEmailRequest;
import com.icoder.user.management.dto.user.UpdateUserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileResponse;
import com.icoder.user.management.service.implementation.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userServiceImpl;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(@RequestParam String handle) {
        UserProfileRequest request = UserProfileRequest.builder().handle(handle).build();
        return ResponseEntity.ok(userServiceImpl.getProfile(request));
    }

    @PostMapping("/delete/request")
    public ResponseEntity<MessageResponse> requestAccountDeletion(Authentication authentication) {
        return ResponseEntity.ok(userServiceImpl.requestAccountDeletion(authentication));
    }

    @PostMapping("/delete/confirm")
    public ResponseEntity<MessageResponse> confirmAccountDeletion(@RequestParam("token") String token) {
        return ResponseEntity.ok(userServiceImpl.confirmAccountDeletion(token));
    }

    @PutMapping("/update")
    public ResponseEntity<MessageResponse> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(userServiceImpl.updateProfile(request, authentication));
    }

    @PatchMapping("/profile-picture")
    public ResponseEntity<MessageResponse> updateProfilePicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userServiceImpl.changeProfilePicture(authentication, file));
    }

    @DeleteMapping("/profile-picture")
    public ResponseEntity<MessageResponse> deleteProfilePicture(Authentication authentication) {
        return ResponseEntity.ok(userServiceImpl.deleteProfilePicture(authentication));
    }

    @PostMapping("/email/request-update")
    public ResponseEntity<MessageResponse> requestEmailUpdate(
            @Valid @RequestBody UpdateEmailRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(userServiceImpl.requestEmailUpdate(request, authentication));
    }

    @PostMapping("/email/confirm")
    public ResponseEntity<MessageResponse> confirmEmailUpdate(@RequestParam String token) {
        return ResponseEntity.ok(userServiceImpl.confirmEmailUpdate(token));
    }
}
