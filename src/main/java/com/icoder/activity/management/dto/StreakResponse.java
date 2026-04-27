package com.icoder.activity.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StreakResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private int currentStreak;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private int maxStreak;
    private Instant lastAcceptedAt;
    private String todayUtc;
}
