package com.icoder.coding.editor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CodeTemplateRequest {
    @NotBlank
    private String templateName;
    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer languageId;
    @NotBlank
    private String code;
    private boolean enabled;
    @NotNull
    private Instant createdAndUpdatedAt;
}
