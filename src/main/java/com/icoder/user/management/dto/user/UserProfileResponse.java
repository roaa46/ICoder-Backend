package com.icoder.user.management.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    private String handle;
    private String nickname;
    private String email;
    private String school;
    private String pictureUrl;
    private boolean verified;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private int acceptedCount = 0;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private int attemptedCount = 0;
}
