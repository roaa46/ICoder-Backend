package com.icoder.user.management.dto.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserProfileResponse {
    @NotBlank
    private String handle;
    @NotBlank
    private String nickname;
    @NotBlank
    private String email;
    private String school;
    private String pictureUrl;
    @NotBlank
    private boolean verified;
    @NotBlank
    private int acceptedCount = 0;
    @NotBlank
    private int attemptedCount = 0;
}
