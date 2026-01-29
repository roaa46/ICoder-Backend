package com.icoder.problem.management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.icoder.core.utils.LowercaseEnumSerializer;
import com.icoder.core.utils.UppercaseEnumDeserializer;
import com.icoder.problem.management.enums.OJudgeType;
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
@JsonInclude(JsonInclude.Include.NON_NULL) // contestLink is null in CSES
public class ProblemStatementResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long problemId;
    private String problemCode;
    private String problemLink;
    @JsonSerialize(using = LowercaseEnumSerializer.class)
    @JsonDeserialize(using = UppercaseEnumDeserializer.class)
    private OJudgeType onlineJudge;  // use .toString()..toLowerCase() while scrapping
    private String contestTitle;
    private String contestLink;
    private String problemTitle;
    private long solvedCount;
    private long attemptedCount;
    private List<SectionScrapeDTO> sections;
    private List<PropertyScrapeDTO> properties;
}
