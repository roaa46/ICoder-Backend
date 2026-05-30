package com.icoder.user.management.mapper;

import com.icoder.user.management.dto.auth.RegisterRequest;
import com.icoder.user.management.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(RegisterRequest request);
}
