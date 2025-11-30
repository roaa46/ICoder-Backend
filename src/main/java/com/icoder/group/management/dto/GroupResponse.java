package com.icoder.group.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.group.enums.ContestCoordinatorType;
import com.icoder.group.enums.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GroupResponse {
    private String code;
    private String name;
    private Visibility visibility;
    private ContestCoordinatorType contestCoordinatorType;
    private String description;
    private String pictureUrl;
    private Instant createdAt;
}
