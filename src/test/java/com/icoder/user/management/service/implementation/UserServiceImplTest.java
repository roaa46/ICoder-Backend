package com.icoder.user.management.service.implementation;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.dto.PictureUrlResponse;
import com.icoder.core.exception.ApiException;
import com.icoder.core.utils.ImageService;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.core.utils.TokenHelper;
import com.icoder.user.management.dto.auth.UpdateEmailRequest;
import com.icoder.user.management.dto.user.UpdateUserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileResponse;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.enums.TokenType;
import com.icoder.user.management.mapper.UserMapper;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.EmailVerificationService;
import com.icoder.user.management.service.interfaces.JwtService;
import com.icoder.user.management.service.interfaces.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private TokenService tokenService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenHelper tokenHelper;
    @Mock
    private Cloudinary cloudinary;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private ImageService imageService;
    @Mock
    private MultipartFile file;
    @Mock
    private Uploader uploader;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserProfileRequest userProfileRequest;
    private UserProfileResponse userProfileResponse;
    private UpdateUserProfileRequest updateUserProfileRequest;
    private UpdateEmailRequest updateEmailRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "profilePictureFolder", "users/profile-pictures");

        user = new User();
        user.setId(1L);
        user.setHandle("test");
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setNickname("Roaa");
        user.setSchool("FCI");
        user.setVerified(true);
        user.setPictureUrl("https://cdn.example.com/old-picture.jpg");

        userProfileRequest = new UserProfileRequest();
        userProfileRequest.setHandle("test");

        userProfileResponse = UserProfileResponse.builder()
                .handle("test")
                .nickname("Roaa")
                .school("FCI")
                .build();

        updateUserProfileRequest = new UpdateUserProfileRequest();
        updateUserProfileRequest.setCurrentPassword("plainPassword");
        updateUserProfileRequest.setNickname("New Roaa");
        updateUserProfileRequest.setSchool("New School");

        updateEmailRequest = new UpdateEmailRequest();
        updateEmailRequest.setCurrentPassword("plainPassword");
        updateEmailRequest.setNewEmail("new@example.com");
    }

    @Nested
    @DisplayName("getProfile()")
    class GetProfileTests {

        @Test
        @DisplayName("should return profile when user exists")
        void getProfile_shouldReturnProfile_whenUserExists() {
            when(userRepository.findByHandle("test")).thenReturn(Optional.of(user));
            when(userMapper.toDTO(user)).thenReturn(userProfileResponse);

            UserProfileResponse result = userService.getProfile(userProfileRequest);

            assertNotNull(result);
            assertEquals("test", result.getHandle());
            assertEquals("Roaa", result.getNickname());

            verify(userRepository).findByHandle("test");
            verify(userMapper).toDTO(user);
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user does not exist")
        void getProfile_shouldThrowException_whenUserNotFound() {
            when(userRepository.findByHandle("test")).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userService.getProfile(userProfileRequest)
            );

            assertEquals("User not found", ex.getMessage());

            verify(userRepository).findByHandle("test");
            verifyNoInteractions(userMapper);
        }
    }

    @Nested
    @DisplayName("requestAccountDeletion()")
    class RequestAccountDeletionTests {

        @Test
        @DisplayName("should send account deletion email")
        void requestAccountDeletion_shouldSendEmail() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            MessageResponse result = userService.requestAccountDeletion();

            assertEquals("A confirmation email has been sent to your email.", result.getMessage());

            verify(securityUtils).getCurrentUserId();
            verify(userRepository).findById(1L);
            verify(emailVerificationService).sendAccountDeletionEmail(user);
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when current user not found")
        void requestAccountDeletion_shouldThrowException_whenUserNotFound() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userService.requestAccountDeletion()
            );

            assertEquals("User not found", ex.getMessage());

            verify(emailVerificationService, never()).sendAccountDeletionEmail(any());
        }
    }

    @Nested
    @DisplayName("confirmAccountDeletion()")
    class ConfirmAccountDeletionTests {

        @Test
        @DisplayName("should throw ApiException when token is expired")
        void confirmAccountDeletion_shouldThrowApiException_whenTokenExpired() {
            String token = "expired-token";
            when(jwtService.isTokenExpired(token)).thenReturn(true);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> userService.confirmAccountDeletion(token)
            );

            assertEquals("Verification link has expired", ex.getMessage());

            verify(jwtService).isTokenExpired(token);
            verifyNoInteractions(tokenHelper, tokenService, imageService);
            verify(userRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should throw ApiException when token type is invalid")
        void confirmAccountDeletion_shouldThrowApiException_whenTokenTypeInvalid() {
            String token = "valid-token";
            TokenHelper.ValidatedTokenResult result = mock(TokenHelper.ValidatedTokenResult.class);

            when(jwtService.isTokenExpired(token)).thenReturn(false);
            when(tokenHelper.validateAndExtract(token)).thenReturn(result);
            when(result.type()).thenReturn("EMAIL_UPDATE");

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> userService.confirmAccountDeletion(token)
            );

            assertEquals("Invalid token type for account deletion", ex.getMessage());

            verify(userRepository, never()).delete(any());
            verify(tokenService, never()).revokeAllUserTokens(any());
        }

        @Test
        @DisplayName("should delete account and picture when token is valid and picture exists")
        void confirmAccountDeletion_shouldDeleteAccountAndPicture_whenValid() throws IOException {
            String token = "valid-token";
            TokenHelper.ValidatedTokenResult result = mock(TokenHelper.ValidatedTokenResult.class);

            when(jwtService.isTokenExpired(token)).thenReturn(false);
            when(tokenHelper.validateAndExtract(token)).thenReturn(result);
            when(result.type()).thenReturn("ACCOUNT_DELETION");
            when(result.user()).thenReturn(user);

            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                MessageResponse response = userService.confirmAccountDeletion(token);

                assertEquals("Your account has been successfully deleted", response.getMessage());

                verify(tokenService).revokeAllUserTokens(user);
                verify(userRepository).delete(user);
                verify(imageService).deleteImageFromCloudinary(user.getPictureUrl(), "users/profile-pictures");
                mocked.verify(SecurityContextHolder::clearContext);
            }
        }

        @Test
        @DisplayName("should delete account even if picture deletion fails")
        void confirmAccountDeletion_shouldDeleteAccount_whenImageDeletionFails() throws IOException {
            String token = "valid-token";
            TokenHelper.ValidatedTokenResult result = mock(TokenHelper.ValidatedTokenResult.class);

            when(jwtService.isTokenExpired(token)).thenReturn(false);
            when(tokenHelper.validateAndExtract(token)).thenReturn(result);
            when(result.type()).thenReturn("ACCOUNT_DELETION");
            when(result.user()).thenReturn(user);
            doThrow(new RuntimeException("Cloudinary error"))
                    .when(imageService)
                    .deleteImageFromCloudinary(user.getPictureUrl(), "users/profile-pictures");

            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                MessageResponse response = userService.confirmAccountDeletion(token);

                assertEquals("Your account has been successfully deleted", response.getMessage());

                verify(tokenService).revokeAllUserTokens(user);
                verify(userRepository).delete(user);
                mocked.verify(SecurityContextHolder::clearContext);
            }
        }

        @Test
        @DisplayName("should delete account without trying to delete image when picture url is blank")
        void confirmAccountDeletion_shouldDeleteAccountWithoutImageDeletion_whenNoPicture() throws IOException {
            String token = "valid-token";
            TokenHelper.ValidatedTokenResult result = mock(TokenHelper.ValidatedTokenResult.class);
            user.setPictureUrl(" ");

            when(jwtService.isTokenExpired(token)).thenReturn(false);
            when(tokenHelper.validateAndExtract(token)).thenReturn(result);
            when(result.type()).thenReturn("ACCOUNT_DELETION");
            when(result.user()).thenReturn(user);

            try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
                MessageResponse response = userService.confirmAccountDeletion(token);

                assertEquals("Your account has been successfully deleted", response.getMessage());

                verify(tokenService).revokeAllUserTokens(user);
                verify(userRepository).delete(user);
                verify(imageService, never()).deleteImageFromCloudinary(anyString(), anyString());
                mocked.verify(SecurityContextHolder::clearContext);
            }
        }
    }

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfileTests {

        @Test
        @DisplayName("should throw UsernameNotFoundException when user not found")
        void updateProfile_shouldThrowException_whenUserNotFound() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userService.updateProfile(updateUserProfileRequest)
            );

            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("should throw ApiException when current password is incorrect")
        void updateProfile_shouldThrowApiException_whenPasswordIncorrect() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(false);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> userService.updateProfile(updateUserProfileRequest)
            );

            assertEquals("Current password is incorrect", ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ApiException when no field is changed")
        void updateProfile_shouldThrowApiException_whenNothingChanged() {
            updateUserProfileRequest.setNickname("Roaa");
            updateUserProfileRequest.setSchool("FCI");

            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> userService.updateProfile(updateUserProfileRequest)
            );

            assertEquals("You must change at least one field", ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should update changed fields and save user")
        void updateProfile_shouldSaveUser_whenFieldsChanged() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);

            MessageResponse response = userService.updateProfile(updateUserProfileRequest);

            assertEquals("Your data has been successfully changed", response.getMessage());
            assertEquals("New Roaa", user.getNickname());
            assertEquals("New School", user.getSchool());

            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("uploadProfilePicture()")
    class UploadProfilePictureTests {

        @Test
        @DisplayName("should throw UsernameNotFoundException when user not found")
        void uploadProfilePicture_shouldThrowException_whenUserNotFound() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userService.uploadProfilePicture(file)
            );

            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("should throw ApiException when picture type is invalid")
        void uploadProfilePicture_shouldThrowApiException_whenPictureTypeInvalid() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            doThrow(new IllegalStateException("Invalid picture type"))
                    .when(imageService)
                    .checkPictureType(file);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> userService.uploadProfilePicture(file)
            );

            assertEquals("Invalid picture type", ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should upload picture and save new url")
        void uploadProfilePicture_shouldUploadPictureAndSaveUrl() throws Exception {
            byte[] bytes = "fake-image".getBytes();
            Map<String, Object> uploadResult = Map.of("secure_url", "https://cdn.example.com/new-picture.jpg");

            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cloudinary.uploader()).thenReturn(uploader);
            when(file.getBytes()).thenReturn(bytes);
            when(uploader.upload(eq(bytes), anyMap())).thenReturn(uploadResult);

            MessageResponse response = userService.uploadProfilePicture(file);

            assertEquals("Your profile picture has been successfully changed", response.getMessage());
            assertEquals("https://cdn.example.com/new-picture.jpg", user.getPictureUrl());

            verify(imageService).checkPictureType(file);
            verify(imageService).deleteImageFromCloudinary("https://cdn.example.com/old-picture.jpg", "users/profile-pictures");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should upload picture without deleting old one when old url is null")
        void uploadProfilePicture_shouldUploadWithoutDeletingOldPicture_whenOldPictureNull() throws Exception {
            byte[] bytes = "fake-image".getBytes();
            Map<String, Object> uploadResult = Map.of("secure_url", "https://cdn.example.com/new-picture.jpg");
            user.setPictureUrl(null);

            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cloudinary.uploader()).thenReturn(uploader);
            when(file.getBytes()).thenReturn(bytes);
            when(uploader.upload(eq(bytes), anyMap())).thenReturn(uploadResult);

            MessageResponse response = userService.uploadProfilePicture(file);

            assertEquals("Your profile picture has been successfully changed", response.getMessage());
            assertEquals("https://cdn.example.com/new-picture.jpg", user.getPictureUrl());

            verify(imageService, never()).deleteImageFromCloudinary(anyString(), anyString());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw IllegalStateException when upload fails with IOException")
        void uploadProfilePicture_shouldThrowIllegalStateException_whenUploadFails() throws Exception {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cloudinary.uploader()).thenReturn(uploader);
            when(file.getBytes()).thenThrow(new IOException("IO failed"));

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> userService.uploadProfilePicture(file)
            );

            assertEquals("Failed to upload profile picture", ex.getMessage());

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("requestEmailUpdate()")
    class RequestEmailUpdateTests {

        @Test
        @DisplayName("should throw UsernameNotFoundException when current user not found")
        void requestEmailUpdate_shouldThrowException_whenUserNotFound() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userService.requestEmailUpdate(updateEmailRequest)
            );

            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("should throw ApiException when current password is incorrect")
        void requestEmailUpdate_shouldThrowApiException_whenPasswordIncorrect() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(false);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> userService.requestEmailUpdate(updateEmailRequest)
            );

            assertEquals("Current password is incorrect", ex.getMessage());

            verify(emailVerificationService, never()).sendEmailUpdateVerificationEmail(any(), anyString());
        }

        @Test
        @DisplayName("should throw ApiException when new email is already used")
        void requestEmailUpdate_shouldThrowApiException_whenEmailAlreadyUsed() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> userService.requestEmailUpdate(updateEmailRequest)
            );

            assertEquals("Email is already used", ex.getMessage());

            verify(emailVerificationService, never()).sendEmailUpdateVerificationEmail(any(), anyString());
        }

        @Test
        @DisplayName("should send verification email to new email")
        void requestEmailUpdate_shouldSendVerificationEmail() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

            MessageResponse response = userService.requestEmailUpdate(updateEmailRequest);

            assertEquals("Verification link sent to new email.", response.getMessage());

            verify(emailVerificationService).sendEmailUpdateVerificationEmail(user, "new@example.com");
        }
    }

    @Nested
    @DisplayName("confirmEmailUpdate()")
    class ConfirmEmailUpdateTests {

        @Test
        @DisplayName("should throw ApiException when token is expired")
        void confirmEmailUpdate_shouldThrowApiException_whenTokenExpired() {
            String token = "expired-token";
            when(jwtService.isTokenExpired(token)).thenReturn(true);

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> userService.confirmEmailUpdate(token)
            );

            assertEquals("Verification link has expired", ex.getMessage());

            verify(tokenHelper, never()).validateAndExtract(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalStateException when token type is invalid")
        void confirmEmailUpdate_shouldThrowIllegalStateException_whenTokenTypeInvalid() {
            String token = "valid-token";
            TokenHelper.ValidatedTokenResult result = mock(TokenHelper.ValidatedTokenResult.class);

            when(jwtService.isTokenExpired(token)).thenReturn(false);
            when(tokenHelper.validateAndExtract(token)).thenReturn(result);
            when(result.type()).thenReturn("ACCOUNT_DELETION");

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> userService.confirmEmailUpdate(token)
            );

            assertEquals("Invalid token type", ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should update email and save user when token is valid")
        void confirmEmailUpdate_shouldUpdateEmail_whenTokenValid() {
            String token = "valid-token";
            TokenHelper.ValidatedTokenResult result = mock(TokenHelper.ValidatedTokenResult.class);

            when(jwtService.isTokenExpired(token)).thenReturn(false);
            when(tokenHelper.validateAndExtract(token)).thenReturn(result);
            when(result.type()).thenReturn(TokenType.EMAIL_UPDATE.name());
            when(result.user()).thenReturn(user);
            when(jwtService.extractClaim(eq(token), any())).thenReturn("new@example.com");

            MessageResponse response = userService.confirmEmailUpdate(token);

            assertEquals("Email updated successfully.", response.getMessage());
            assertEquals("new@example.com", user.getEmail());

            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("deleteProfilePicture()")
    class DeleteProfilePictureTests {

        @Test
        @DisplayName("should throw UsernameNotFoundException when user not found")
        void deleteProfilePicture_shouldThrowException_whenUserNotFound() {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userService.deleteProfilePicture()
            );

            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("should throw ApiException when user does not have profile picture")
        void deleteProfilePicture_shouldThrowApiException_whenNoPicture() {
            user.setPictureUrl("  ");

            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            ApiException ex = assertThrows(
                    ApiException.class,
                    () -> userService.deleteProfilePicture()
            );

            assertEquals("User does not have a profile picture", ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should delete profile picture and save user")
        void deleteProfilePicture_shouldDeletePictureAndSaveUser() throws IOException {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            MessageResponse response = userService.deleteProfilePicture();

            assertEquals("Your profile picture has been successfully deleted", response.getMessage());
            assertNull(user.getPictureUrl());

            verify(userRepository).save(user);
            verify(imageService).deleteImageFromCloudinary("https://cdn.example.com/old-picture.jpg", "users/profile-pictures");
        }

        @Test
        @DisplayName("should still return success when cloudinary deletion fails")
        void deleteProfilePicture_shouldStillReturnSuccess_whenImageDeletionFails() throws IOException {
            when(securityUtils.getCurrentUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            doThrow(new RuntimeException("Cloudinary error"))
                    .when(imageService)
                    .deleteImageFromCloudinary("https://cdn.example.com/old-picture.jpg", "users/profile-pictures");

            MessageResponse response = userService.deleteProfilePicture();

            assertEquals("Your profile picture has been successfully deleted", response.getMessage());
            assertNull(user.getPictureUrl());

            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("viewProfilePicture()")
    class ViewProfilePictureTests {

        @Test
        @DisplayName("should return picture url when user exists")
        void viewProfilePicture_shouldReturnPictureUrl_whenUserExists() {
            when(userRepository.findByHandle("test")).thenReturn(Optional.of(user));

            PictureUrlResponse response = userService.viewProfilePicture("test");

            assertNotNull(response);
            assertEquals("https://cdn.example.com/old-picture.jpg", response.getPictureUrl());

            verify(userRepository).findByHandle("test");
        }

        @Test
        @DisplayName("should throw UsernameNotFoundException when user not found")
        void viewProfilePicture_shouldThrowException_whenUserNotFound() {
            when(userRepository.findByHandle("test")).thenReturn(Optional.empty());

            UsernameNotFoundException ex = assertThrows(
                    UsernameNotFoundException.class,
                    () -> userService.viewProfilePicture("test")
            );

            assertEquals("User not found", ex.getMessage());
        }
    }
}