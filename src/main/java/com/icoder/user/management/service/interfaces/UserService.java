package com.icoder.user.management.service.interfaces;

import com.icoder.core.dto.MessageResponse;
import com.icoder.user.management.dto.auth.UpdateEmailRequest;
import com.icoder.user.management.dto.user.UpdateUserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

public interface UserService {
    UserProfileResponse getProfile(UserProfileRequest userProfileRequest);

    MessageResponse requestAccountDeletion(Principal principal);

    MessageResponse confirmAccountDeletion(String token);

    MessageResponse updateProfile(UpdateUserProfileRequest request, Principal principal);

    MessageResponse changeProfilePicture(Principal principal, MultipartFile file);

    MessageResponse requestEmailUpdate(UpdateEmailRequest request, Principal principal);

    MessageResponse confirmEmailUpdate(String token);

    MessageResponse deleteProfilePicture(Principal principal);
}
