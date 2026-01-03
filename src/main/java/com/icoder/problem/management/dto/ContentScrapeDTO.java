package com.icoder.problem.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.icoder.problem.management.enums.FormatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ContentScrapeDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long contentId;
    private String content;
    @JsonSerialize(using = ToStringSerializer.class)
    private FormatType formatType;
    private int orderIndex;
}
