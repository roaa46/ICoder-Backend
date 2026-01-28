package com.icoder.contest.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    private String password;
    private Boolean historyRank;
    private List<ContestProblemRequest> problemSet;
}
