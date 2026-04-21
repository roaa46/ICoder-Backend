package com.icoder.activity.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icoder.activity.management.enums.ActivityType;
import com.icoder.core.utils.LowercaseEnumSerializer;
import com.icoder.submission.management.enums.SubmissionVerdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityLogResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String userHandle;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private ActivityType activityType;

    private String entityType;

    private SubmissionVerdict verdict;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long entityId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Instant createdAt;

}
