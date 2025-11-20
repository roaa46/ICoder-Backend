package com.icoder.user.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.user.management.dto.auth.UpdateEmailRequest;
import com.icoder.user.management.dto.user.UpdateUserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileResponse getProfile(UserProfileRequest userProfileRequest);

    MessageResponse requestAccountDeletion(Authentication authentication);

    MessageResponse confirmAccountDeletion(String token);

    MessageResponse updateProfile(UpdateUserProfileRequest request, Authentication authentication);

    MessageResponse changeProfilePicture(Authentication authentication, MultipartFile file);

    MessageResponse requestEmailUpdate(UpdateEmailRequest request, Authentication authentication);

    MessageResponse confirmEmailUpdate(String token);

    MessageResponse deleteProfilePicture(Authentication authentication);
}
