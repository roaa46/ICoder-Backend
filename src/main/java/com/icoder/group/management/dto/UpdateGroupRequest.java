package com.icoder.group.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.group.management.enums.ContestCoordinatorType;
import com.icoder.group.management.enums.Visibility;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateGroupRequest {
    @NotNull
    private Long groupId;
    private String name;
    private Visibility visibility;
    private ContestCoordinatorType contestCoordinatorType;
    private String description;
}
