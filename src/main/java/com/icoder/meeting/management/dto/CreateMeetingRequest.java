package com.icoder.meeting.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.core.utils.UppercaseEnumDeserializer;
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
    @JsonDeserialize(using = UppercaseEnumDeserializer.class)
    private MeetingType meetingType;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long groupId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long contestId;

    @NotNull
    private Boolean instant;

    private Instant scheduledStartTime;
}