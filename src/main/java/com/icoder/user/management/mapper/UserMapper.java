package com.icoder.user.management.mapper;

import com.icoder.user.management.dto.auth.RegisterRequest;
import com.icoder.user.management.dto.user.UserProfileResponse;
import com.icoder.user.management.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "id", target = "userId")
    UserProfileResponse toDTO(User user);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "acceptedCount", ignore = true)
    @Mapping(target = "attemptedCount", ignore = true)
    @Mapping(target = "pictureUrl", ignore = true)
    @Mapping(target = "lastVerificationEmailSentAt", ignore = true)
    User toEntity(RegisterRequest request);
}
