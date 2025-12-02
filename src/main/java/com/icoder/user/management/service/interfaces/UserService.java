package com.icoder.user.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.user.management.dto.auth.UpdateEmailRequest;
import com.icoder.user.management.dto.user.UpdateUserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {
    UserProfileResponse getProfile(UserProfileRequest userProfileRequest);

    MessageResponse requestAccountDeletion();

    MessageResponse confirmAccountDeletion(String token);

    MessageResponse updateProfile(UpdateUserProfileRequest request);

    MessageResponse changeProfilePicture(MultipartFile file);

    MessageResponse requestEmailUpdate(UpdateEmailRequest request);

    MessageResponse confirmEmailUpdate(String token);

    MessageResponse deleteProfilePicture();
}
