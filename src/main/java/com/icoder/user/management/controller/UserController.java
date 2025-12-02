package com.icoder.user.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.user.management.dto.auth.UpdateEmailRequest;
import com.icoder.user.management.dto.user.UpdateUserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileResponse;
import com.icoder.user.management.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "View user profile",
            description = "Retrieves the public profile information for a user identified by their handle."
    )
    public ResponseEntity<UserProfileResponse> getProfile(@RequestParam String handle) {
        UserProfileRequest request = UserProfileRequest.builder().handle(handle).build();
        return ResponseEntity.ok(userService.getProfile(request));
    }

    @PostMapping("/delete/request")
    @Operation(
            summary = "Request account deletion",
            description = "Initiates the account deletion process by sending a confirmation link to the user's email."
    )
    public ResponseEntity<MessageResponse> requestAccountDeletion() {
        return ResponseEntity.ok(userService.requestAccountDeletion());
    }

    @PostMapping("/delete/confirm")
    @Operation(
            summary = "Confirm account deletion",
            description = "Completes the account deletion process using the token sent to the user's email."
    )
    public ResponseEntity<MessageResponse> confirmAccountDeletion(@RequestParam("token") String token) {
        return ResponseEntity.ok(userService.confirmAccountDeletion(token));
    }

    @PutMapping("/update")
    @Operation(
            summary = "Update user profile",
            description = "Allows the authenticated user to update their profile details. Requires current password verification."
    )
    public ResponseEntity<MessageResponse> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @PatchMapping("/profile-picture")
    @Operation(
            summary = "Update profile picture",
            description = "Uploads and sets a new profile picture for the authenticated user. Only accepts image file types (PNG, JPEG, JPG, GIF)."
    )
    public ResponseEntity<MessageResponse> updateProfilePicture(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.changeProfilePicture(file));
    }

    @DeleteMapping("/profile-picture")
    @Operation(
            summary = "Delete profile picture",
            description = "Removes the current profile picture of the authenticated user."
    )
    public ResponseEntity<MessageResponse> deleteProfilePicture() {
        return ResponseEntity.ok(userService.deleteProfilePicture());
    }

    @PostMapping("/email/request-update")
    @Operation(
            summary = "Request email update",
            description = "Initiates the email update process by sending a verification link to the new email address. Requires current password verification."
    )
    public ResponseEntity<MessageResponse> requestEmailUpdate(@Valid @RequestBody UpdateEmailRequest request) {
        return ResponseEntity.ok(userService.requestEmailUpdate(request));
    }

    @PostMapping("/email/confirm")
    @Operation(
            summary = "Confirm email update",
            description = "Completes the email update using the verification token sent to the new email address."
    )
    public ResponseEntity<MessageResponse> confirmEmailUpdate(@RequestParam String token) {
        return ResponseEntity.ok(userService.confirmEmailUpdate(token));
    }
}
