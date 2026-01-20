package com.icoder.coding.editor.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;
    private String templateName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer languageId;
    private String code;
    private boolean enabled;
    private Instant createdAndUpdatedAt;
    private String monacoName;
}
