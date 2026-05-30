package com.icoder.meeting.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.meeting.management.enums.MeetingStatus;
import com.icoder.meeting.management.enums.MeetingType;
import java.time.Instant;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MeetingResponse {

    private Long id;

    private String title;

    private String roomName;

    private MeetingType type;

    private MeetingStatus status;

    private boolean official;

    private Instant scheduledStartTime;

    private String creatorHandle;

    private Long creatorId;

    private Instant createdAt;

    private Instant endedAt;
}
