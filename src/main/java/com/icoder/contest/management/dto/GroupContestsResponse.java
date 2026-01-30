package com.icoder.contest.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GroupContestsResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long participantsCount;

    private Instant beginTime;

    private Instant endTime;

    private String length;
}
