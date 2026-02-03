package com.icoder.contest.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.enums.ContestType;
import com.icoder.core.utils.LowercaseEnumSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ContestDetailsResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String title;
    private String description;
    private Instant beginTime;
    private Instant endTime;
    private String length;
    private boolean historyRank;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private ContestStatus contestStatus;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private ContestType contestType;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ownerId;
    private String ownerHandle;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long groupId;
    private String groupName;
}
