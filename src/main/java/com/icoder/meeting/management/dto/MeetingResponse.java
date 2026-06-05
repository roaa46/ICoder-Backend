package com.icoder.meeting.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icoder.core.utils.LowercaseEnumSerializer;
import com.icoder.meeting.management.enums.MeetingStatus;
import com.icoder.meeting.management.enums.MeetingType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MeetingResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String title;

    private String roomName;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private MeetingType type;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    private MeetingStatus status;

    private boolean official;

    private Instant scheduledStartTime;

    private String creatorHandle;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long creatorId;

    private Instant createdAt;

    private Instant endedAt;
}
