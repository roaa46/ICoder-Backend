package com.icoder.group.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icoder.core.utils.LowercaseEnumSerializer;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String id;
    private String code;
    private String name;
    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private Visibility visibility;
    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private ContestCoordinatorType contestCoordinatorType;
    private String description;
    private String pictureUrl;
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long groupMembersCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ownerId;
}
