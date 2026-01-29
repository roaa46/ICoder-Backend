package com.icoder.coding.editor.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SubmissionResult {
    private String stdout;

    @JsonSerialize(using = ToStringSerializer.class)
    private Float time;

    @JsonSerialize(using = ToStringSerializer.class)
    private Integer memory;

    private String stderr;

    private String token;

    private String compile_output;

    private String message;

    private Status status;
}
