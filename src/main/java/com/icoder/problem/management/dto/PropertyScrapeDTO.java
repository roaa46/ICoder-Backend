package com.icoder.problem.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PropertyScrapeDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long propertyId;
    private String title;
    private String content;
    private int orderIndex;
    private boolean spoiler;
    private String contentType;
}
