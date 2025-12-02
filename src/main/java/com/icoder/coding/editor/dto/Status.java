package com.icoder.coding.editor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    // 1: In Queue, 2: Processing, 3: Accepted, 6: Compilation Error
    private Integer id;
    private String description;
}
