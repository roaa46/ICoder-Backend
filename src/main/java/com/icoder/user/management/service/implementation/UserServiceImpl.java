package com.icoder.user.management.service.implementation;

import com.icoder.core.enums.TokenType;
import com.icoder.core.exception.ApiException;
import com.icoder.core.security.CustomUserDetails;
import com.icoder.core.util.TokenHelper;
import com.icoder.user.management.dto.auth.UpdateEmailRequest;
import com.icoder.user.management.dto.user.UserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileResponse;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.mapper.UserMapper;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtServiceImpl jwtServiceImpl;
    private final EmailVerificationServiceImpl emailVerificationServiceImpl;
    private final TokenServiceImpl tokenServiceImpl;
    private final PasswordEncoder passwordEncoder;
    private final TokenHelper tokenHelper;
    @Value("${upload.dir}")
    private String uploadDir;

    public UserProfileResponse getProfile(Authentication authentication) { // SecurityContextHolder.getContext().getAuthentication(); in service layer
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByHandle(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userMapper.toDTO(user);
    }

    public void requestAccountDeletion(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findByHandle(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        emailVerificationServiceImpl.sendAccountDeletionEmail(user);
    }

    @Transactional
    public void confirmAccountDeletion(String token) {
        var result = tokenHelper.validateAndExtract(token);
        if (!"ACCOUNT_DELETION".equals(result.type())) {
            throw new IllegalStateException("Invalid token type for deletion");
        }
        tokenServiceImpl.revokeAllUserTokens(result.user());
        userRepository.delete(result.user());
    }

    @Transactional
    public UserProfileResponse updateProfile(UserProfileRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByHandle(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            if (request.getNickname() != null && !request.getNickname().equals(user.getNickname()))
                user.setNickname(request.getNickname());
            if (request.getSchool() != null && !request.getSchool().equals(user.getSchool()))
                user.setSchool(request.getSchool());
            userRepository.save(user);
            return userMapper.toDTO(user);
        } else
            throw new ApiException(
                    "Current password is incorrect",
                    Map.of("field", "current_password")
            );
    }

    @Transactional
    public UserProfileResponse changeProfilePicture(Authentication authentication, MultipartFile file) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByHandle(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/png") ||
                        contentType.equals("image/jpeg") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/gif"))) {
            throw new IllegalStateException("Invalid file type. Only PNG, JPEG, JPG, and GIF are allowed.");
        }

        try {
            // delete old picture
            if (user.getPictureUrl() != null) {
                Path old = Paths.get(user.getPictureUrl());
                Files.deleteIfExists(old);
            }

            // create a folder if not exists
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // save new picture
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir, filename);
            Files.write(path, file.getBytes());

            // URL to return
            String fileUrl = "/uploads/" + filename;

            // store a new path in DB
            user.setPictureUrl(fileUrl);
            userRepository.save(user);

            return userMapper.toDTO(user);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload profile picture", e);
        }
    }


    @Transactional
    public void requestEmailUpdate(UpdateEmailRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByHandle(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(
                    "Current password is incorrect",
                    Map.of("field", "current_password")
            );
        }
        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new ApiException(
                    "Email is already used",
                    Map.of("field", "email", "value", request.getNewEmail())
            );
        }
        emailVerificationServiceImpl.sendEmailUpdateVerificationEmail(user, request.getNewEmail());
    }

    @Transactional
    public void confirmEmailUpdate(String token) {
        var result = tokenHelper.validateAndExtract(token);
        TokenType tokenType = TokenType.valueOf(result.type());
        if (tokenType != TokenType.EMAIL_UPDATE) {
            throw new IllegalStateException("Invalid token type");
        }
        String newEmail = jwtServiceImpl.extractClaim(token, claims -> (String) claims.get("newEmail"));
        result.user().setEmail(newEmail);
        userRepository.save(result.user());
    }

    @Transactional
    public void deleteProfilePicture(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByHandle(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getPictureUrl() == null || user.getPictureUrl().isBlank()) {
            throw new IllegalStateException("User does not have a profile picture.");
        }
        deleteImageFromStorage(user.getPictureUrl());
        user.setPictureUrl(null);
        userRepository.save(user);
    }

    private void deleteImageFromStorage(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete profile picture.", e);
        }
    }
}
