package com.icoder.user.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.user.management.dto.auth.UpdateEmailRequest;
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
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userServiceImpl;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userServiceImpl.getProfile(authentication));
    }

    @PostMapping("/delete/request")
    public ResponseEntity<MessageResponse> requestAccountDeletion(Authentication authentication) {
        userServiceImpl.requestAccountDeletion(authentication);
        return ResponseEntity.ok(new MessageResponse("A confirmation email has been sent to your address."));
    }

    @PostMapping("/delete/confirm")
    public ResponseEntity<MessageResponse> confirmAccountDeletion(@RequestParam("token") String token) {
        userServiceImpl.confirmAccountDeletion(token);
        return ResponseEntity.ok(new MessageResponse("Your account has been permanently deleted."));
    }

    @PutMapping("/update")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserProfileRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(userServiceImpl.updateProfile(request, authentication));
    }

    @PatchMapping("/profile-picture")
    public ResponseEntity<UserProfileResponse> updateProfilePicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        UserProfileResponse response = userServiceImpl.changeProfilePicture(authentication, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile-picture")
    public ResponseEntity<MessageResponse> deleteProfilePicture(Authentication authentication) {
        userServiceImpl.deleteProfilePicture(authentication);
        return ResponseEntity.ok(new MessageResponse("Profile picture deleted successfully."));
    }

    @PostMapping("/email/request-update")
    public ResponseEntity<MessageResponse> requestEmailUpdate(
            @Valid @RequestBody UpdateEmailRequest request,
            Authentication authentication
    ) {
        userServiceImpl.requestEmailUpdate(request, authentication);
        return ResponseEntity.ok(new MessageResponse("Verification link sent to new email."));
    }

    @PostMapping("/email/confirm")
    public ResponseEntity<MessageResponse> confirmEmailUpdate(@RequestParam String token) {
        userServiceImpl.confirmEmailUpdate(token);
        return ResponseEntity.ok(new MessageResponse("Email updated successfully."));
    }
}
