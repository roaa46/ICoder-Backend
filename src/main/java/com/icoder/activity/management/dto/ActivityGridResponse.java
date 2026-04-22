package com.icoder.activity.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ActivityGridResponse {
    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private long acceptedCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private long attemptedCount;
}
