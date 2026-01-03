package com.icoder.problem.management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SectionScrapeDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sectionId;
    @JsonInclude(JsonInclude.Include.NON_NULL) // title is null in the problem statement section in CF & CSES
    private String title;
    private int orderIndex;
    private List<ContentScrapeDTO> contents;
}
