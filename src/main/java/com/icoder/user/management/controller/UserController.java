package com.icoder.user.management.controller;

import com.icoder.core.dto.MessageResponse;
import com.icoder.user.management.dto.auth.UpdateEmailRequest;
import com.icoder.user.management.dto.user.UpdateUserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileResponse;
import com.icoder.user.management.service.implementation.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userServiceImpl;

    @GetMapping
    @Operation(
            summary = "View user profile",
            description = "Retrieves the public profile information for a user identified by their handle."
    )
    public ResponseEntity<UserProfileResponse> getProfile(@RequestParam String handle) {
        UserProfileRequest request = UserProfileRequest.builder().handle(handle).build();
        return ResponseEntity.ok(userServiceImpl.getProfile(request));
    }

    @PostMapping("/delete/request")
    @Operation(
            summary = "Request account deletion",
            description = "Initiates the account deletion process by sending a confirmation link to the user's email."
    )
    public ResponseEntity<MessageResponse> requestAccountDeletion(Principal principal) {
        return ResponseEntity.ok(userServiceImpl.requestAccountDeletion(principal));
    }

    @PostMapping("/delete/confirm")
    @Operation(
            summary = "Confirm account deletion",
            description = "Completes the account deletion process using the token sent to the user's email."
    )
    public ResponseEntity<MessageResponse> confirmAccountDeletion(@RequestParam("token") String token) {
        return ResponseEntity.ok(userServiceImpl.confirmAccountDeletion(token));
    }

    @PutMapping("/update")
    @Operation(
            summary = "Update user profile",
            description = "Allows the authenticated user to update their profile details. Requires current password verification."
    )
    public ResponseEntity<MessageResponse> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            Principal principal) {
        return ResponseEntity.ok(userServiceImpl.updateProfile(request, principal));
    }

    @PatchMapping("/profile-picture")
    @Operation(
            summary = "Update profile picture",
            description = "Uploads and sets a new profile picture for the authenticated user. Only accepts image file types (PNG, JPEG, JPG, GIF)."
    )
    public ResponseEntity<MessageResponse> updateProfilePicture(
            Principal principal,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userServiceImpl.changeProfilePicture(principal, file));
    }

    @DeleteMapping("/profile-picture")
    @Operation(
            summary = "Delete profile picture",
            description = "Removes the current profile picture of the authenticated user."
    )
    public ResponseEntity<MessageResponse> deleteProfilePicture(Principal principal) {
        return ResponseEntity.ok(userServiceImpl.deleteProfilePicture(principal));
    }

    @PostMapping("/email/request-update")
    @Operation(
            summary = "Request email update",
            description = "Initiates the email update process by sending a verification link to the new email address. Requires current password verification."
    )
    public ResponseEntity<MessageResponse> requestEmailUpdate(
            @Valid @RequestBody UpdateEmailRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(userServiceImpl.requestEmailUpdate(request, principal));
    }

    @PostMapping("/email/confirm")
    @Operation(
            summary = "Confirm email update",
            description = "Completes the email update using the verification token sent to the new email address."
    )
    public ResponseEntity<MessageResponse> confirmEmailUpdate(@RequestParam String token) {
        return ResponseEntity.ok(userServiceImpl.confirmEmailUpdate(token));
    }
}
