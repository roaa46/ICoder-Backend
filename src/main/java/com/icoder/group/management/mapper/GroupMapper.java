package com.icoder.group.management.mapper;

import com.icoder.group.management.dto.CreateGroupRequest;
import com.icoder.group.management.dto.GroupResponse;
import com.icoder.group.management.entity.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "ownerId" , ignore = true)
    Group toEntity(CreateGroupRequest createGroupRequest);

    GroupResponse toDTO(Group group);
}
