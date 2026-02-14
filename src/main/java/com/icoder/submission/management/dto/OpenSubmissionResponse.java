package com.icoder.submission.management.dto;

import com.icoder.submission.management.dto.SubmissionResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenSubmissionResponse extends SubmissionResponse {
    private String solution;
}

