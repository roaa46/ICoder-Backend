package com.icoder.group.management.mapper;

import com.icoder.group.management.dto.GroupMemberResponse;
import com.icoder.group.management.entity.UserGroupRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserGroupRoleMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.handle", target = "handle")
    @Mapping(source = "user.nickname", target = "nickname")
    @Mapping(source = "user.pictureUrl", target = "pictureUrl")
    @Mapping(source = "user.verified", target = "verified")
    @Mapping(source = "role", target = "role")
    GroupMemberResponse toMemberDTO(UserGroupRole userGroupRole);
}
