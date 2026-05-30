package com.icoder.problem.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icoder.core.utils.LowercaseEnumSerializer;
import com.icoder.core.utils.UppercaseEnumDeserializer;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long contentId;
    private String content;
    @JsonSerialize(using = LowercaseEnumSerializer.class)
    @JsonDeserialize(using = UppercaseEnumDeserializer.class)
    private FormatType formatType;
    private int orderIndex;
}
