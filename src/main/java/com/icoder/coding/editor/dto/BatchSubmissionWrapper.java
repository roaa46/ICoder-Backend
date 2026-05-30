package com.icoder.coding.editor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchSubmissionWrapper {
    private List<BatchSubmissionRequestItem> submissions;
}
