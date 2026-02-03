package com.icoder.coding.editor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Status {
    // 1: In Queue, 2: Processing, 3: Accepted, 6: Compilation Error
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Integer id;
    private String description;
}
