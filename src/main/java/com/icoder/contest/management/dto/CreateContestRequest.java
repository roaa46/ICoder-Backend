package com.icoder.contest.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateContestRequest {
    @NotBlank
    private String groupId;
    private String userId;
    @NotBlank
    private String title;
    private String description;
    @NotBlank
    private String beginTime;
    @NotBlank
    @Pattern(
            regexp = "^\\d{1,3}:\\d{2}:\\d{2}$",
            message = "length must be in h:mm:ss format"
    )
    private String length;
    @NotBlank
    private String contestType;
    @NotBlank
    private String contestOpenness;
    @NotBlank
    private String contestStatus;
    @NotBlank
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;
    private boolean historyRank;
    private List<ContestProblemRequest> problemSet;
}
