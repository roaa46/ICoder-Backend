package com.icoder.user.management.dto.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String handle;
    private String nickname;
    private String email;
    private String school;
    private String pictureUrl;
    private boolean verified;
    private int acceptedCount = 0;
    private int attemptedCount = 0;
}
