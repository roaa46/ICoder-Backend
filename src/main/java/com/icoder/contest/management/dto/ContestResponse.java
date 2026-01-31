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

import java.time.Duration;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ContestResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String title;

    private Instant beginTime;

    private Duration length;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long groupId;

    private String groupName;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private ContestStatus status;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private ContestType type;
}
