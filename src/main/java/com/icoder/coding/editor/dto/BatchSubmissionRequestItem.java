package com.icoder.coding.editor.dto;

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
public class BatchSubmissionRequestItem {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private int languageId;
    private String sourceCode;
    private String stdin;
    private String expectedOutput;
}
