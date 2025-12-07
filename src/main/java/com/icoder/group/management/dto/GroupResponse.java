package com.icoder.group.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.group.management.enums.ContestCoordinatorType;
import com.icoder.group.management.enums.Visibility;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
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
