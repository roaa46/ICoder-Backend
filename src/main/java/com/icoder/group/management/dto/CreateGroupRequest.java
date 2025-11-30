package com.icoder.group.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.core.enums.ContestCoordinatorType;
import com.icoder.core.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateGroupRequest {
    @NotBlank
    private String name;
    @NotNull
    private Visibility visibility;
    @NotNull
    private ContestCoordinatorType contestCoordinatorType;
    @NotBlank
    private String description;
    @NotBlank
    private String pictureUrl;
}
