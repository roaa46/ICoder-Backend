package com.icoder.user.management.service.implementation;

import com.cloudinary.Cloudinary;
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
import com.icoder.user.management.service.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final TokenHelper tokenHelper;
    private final Cloudinary cloudinary;
    private final SecurityUtils securityUtils;
    private final ImageService imageService;
    private final CacheManager cacheManager;
    @Value("${profile.picture.folder}")
    private String profilePictureFolder;

    @Override
    @Cacheable(value = "user_profile", key = "#userProfileRequest.handle")
    public UserProfileResponse getProfile(UserProfileRequest userProfileRequest) {
        User user = userRepository.findByHandle(userProfileRequest.getHandle())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userMapper.toDTO(user);
    }

    @Override
    public MessageResponse requestAccountDeletion() {
        User user = userRepository.findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        emailVerificationService.sendAccountDeletionEmail(user);
        return new MessageResponse("A confirmation email has been sent to your email.");
    }

    @Transactional
    @Override
    public MessageResponse confirmAccountDeletion(String token) {

        if (jwtService.isTokenExpired(token)) {
            throw new ApiException("Verification link has expired");
        }

        TokenHelper.ValidatedTokenResult result = tokenHelper.validateAndExtract(token);

        if (!"ACCOUNT_DELETION".equals(result.type())) {
            throw new ApiException("Invalid token type for account deletion");
        }

        User user = result.user();
        String pictureUrl = user.getPictureUrl();

        tokenService.revokeAllUserTokens(user);
        userRepository.delete(user);

        SecurityContextHolder.clearContext();

        if (pictureUrl != null && !pictureUrl.isBlank()) {
            try {
                imageService.deleteImageFromCloudinary(pictureUrl, profilePictureFolder);
            } catch (Exception e) {
                log.warn("Failed to delete user image from Cloudinary during account deletion", e);
            }
        }

        return new MessageResponse("Your account has been successfully deleted");
    }


    @Transactional
    @Override
    public MessageResponse updateProfile(UpdateUserProfileRequest request) {
        User user = userRepository.findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        validateCurrentPassword(request.getCurrentPassword(), user.getPassword());
        String oldHandle = user.getHandle();
        boolean isUpdated = applyProfileChanges(user, request);
        if (!isUpdated) {
            throw new ApiException("You must change at least one field");
        }
        userRepository.save(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                var cache = cacheManager.getCache("user_profile");
                if (cache != null) {
                    cache.evict(user.getHandle());

                    if (!user.getHandle().equals(oldHandle)) {
                        cache.evict(oldHandle);
                    }
                }
            }
        });

        return new MessageResponse("Your data has been successfully changed");
    }

    private boolean applyProfileChanges(User user, UpdateUserProfileRequest request) {
        boolean changed = false;

        if (isNewValue(request.getNickname(), user.getNickname())) {
            user.setNickname(request.getNickname());
            changed = true;
        }

        if (isNewValue(request.getSchool(), user.getSchool())) {
            user.setSchool(request.getSchool());
            changed = true;
        }

        return changed;
    }

    private boolean isNewValue(String newValue, String currentValue) {
        return newValue != null && !newValue.equals(currentValue);
    }

    @Transactional
    @Override
    public MessageResponse uploadProfilePicture(MultipartFile file) {

        User user = userRepository.findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try {
            imageService.checkPictureType(file);
        } catch (IllegalStateException ex) {
            throw new ApiException(ex.getMessage());
        }

        try {
            if (user.getPictureUrl() != null) {
                imageService.deleteImageFromCloudinary(user.getPictureUrl(), profilePictureFolder);
            }

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", "users/profile-pictures",
                            "resource_type", "image"
                    )
            );

            String imageUrl = uploadResult.get("secure_url").toString();

            user.setPictureUrl(imageUrl);
            userRepository.save(user);

            return new MessageResponse("Your profile picture has been successfully changed");

        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload profile picture", e);
        }
    }

    private void validateCurrentPassword(String currentPassword, String userPassword) {
        if (!passwordEncoder.matches(currentPassword, userPassword)) {
            throw new ApiException(
                    "Current password is incorrect",
                    Map.of("field", "current_password")
            );
        }
    }

    @Transactional
    @Override
    public MessageResponse requestEmailUpdate(UpdateEmailRequest request) {
        User user = userRepository.findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        validateCurrentPassword(request.getCurrentPassword(), user.getPassword());
        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new ApiException(
                    "Email is already used",
                    Map.of("field", "email", "value", request.getNewEmail())
            );
        }
        emailVerificationService.sendEmailUpdateVerificationEmail(user, request.getNewEmail());
        return new MessageResponse("Verification link sent to new email.");
    }

    @Transactional
    @Override
    public MessageResponse confirmEmailUpdate(String token) {
        if (jwtService.isTokenExpired(token)) {
            throw new ApiException("Verification link has expired");
        }
        var result = tokenHelper.validateAndExtract(token);
        TokenType tokenType = TokenType.valueOf(result.type());
        if (tokenType != TokenType.EMAIL_UPDATE) {
            throw new IllegalStateException("Invalid token type");
        }
        String newEmail = jwtService.extractClaim(token, claims -> (String) claims.get("newEmail"));
        result.user().setEmail(newEmail);
        userRepository.save(result.user());
        return new MessageResponse("Email updated successfully.");
    }

    @Transactional
    @Override
    public MessageResponse deleteProfilePicture() {

        User user = userRepository.findById(securityUtils.getCurrentUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String pictureUrl = user.getPictureUrl();
        if (pictureUrl == null || pictureUrl.isBlank()) {
            throw new ApiException("User does not have a profile picture");
        }

        user.setPictureUrl(null);
        userRepository.save(user);

        try {
            imageService.deleteImageFromCloudinary(pictureUrl, profilePictureFolder);
        } catch (Exception e) {
            log.warn("Failed to delete profile image from Cloudinary", e);
        }

        return new MessageResponse("Your profile picture has been successfully deleted");
    }

    @Override
    public PictureUrlResponse viewProfilePicture(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return PictureUrlResponse.builder()
                .pictureUrl(user.getPictureUrl())
                .build();
    }
}