package com.icoder.user.management.service.interfaces;

import com.icoder.user.management.dto.auth.UpdateEmailRequest;
import com.icoder.user.management.dto.user.UserProfileRequest;
import com.icoder.user.management.dto.user.UserProfileResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileResponse getProfile(Authentication authentication);

    void requestAccountDeletion(Authentication authentication);

    void confirmAccountDeletion(String token);

    UserProfileResponse updateProfile(UserProfileRequest request, Authentication authentication);

    UserProfileResponse changeProfilePicture(Authentication authentication, MultipartFile file);

    void requestEmailUpdate(UpdateEmailRequest request, Authentication authentication);

    void confirmEmailUpdate(String token);
}
