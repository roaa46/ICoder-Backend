package com.icoder.user.management.mapper;

import com.icoder.user.management.dto.user.UserProfileResponse;
import com.icoder.user.management.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserProfileResponse toDTO(User user);
}
