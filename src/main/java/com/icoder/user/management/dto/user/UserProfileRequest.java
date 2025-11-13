package com.icoder.user.management.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {
    @NotBlank
    String currentPassword;
    private String nickname;
    private String email;
    private String school;
    private String pictureURL;
}
