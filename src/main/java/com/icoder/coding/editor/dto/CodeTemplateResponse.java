package com.icoder.coding.editor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CodeTemplateResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long templateId;
    private String templateName;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer languageId;
    private String code;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private String monacoName;
}
