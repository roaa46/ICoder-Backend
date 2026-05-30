package com.icoder.coding.editor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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

public class LanguageResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer id;
    private String name;
    private boolean isArchived;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sourceFile;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String compileCmd;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String runCmd;
    private String monacoName;
}
