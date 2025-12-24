package com.icoder.coding.editor.dto;

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
public class Status {
    // 1: In Queue, 2: Processing, 3: Accepted, 6: Compilation Error
    @JsonSerialize(using = ToStringSerializer.class)
    private Integer id;
    private String description;
}
