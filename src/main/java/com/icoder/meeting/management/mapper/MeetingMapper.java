package com.icoder.meeting.management.mapper;

import com.icoder.meeting.management.dto.MeetingResponse;
import com.icoder.meeting.management.entity.Meeting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MeetingMapper {

    @Mapping(target = "creatorHandle", source = "creator.handle")
    @Mapping(target = "creatorId", source = "creator.id")
    MeetingResponse toResponse(Meeting meeting);
}
