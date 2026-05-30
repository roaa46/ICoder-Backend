package com.icoder.meeting.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.meeting.management.enums.MeetingType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateMeetingRequest {

    private String title;

    @NotNull
    private MeetingType meetingType;

    @NotNull
    private Long groupId;

    private Long contestId;

    @NotNull
    private Boolean instant;

    private Instant scheduledStartTime;
}