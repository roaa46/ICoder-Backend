package com.icoder.contest.management.mapper;

import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.entity.Contest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface ContestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "beginTime", ignore = true)
    @Mapping(target = "length", ignore = true)
    Contest toEntity(CreateContestRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "beginTime", ignore = true)
    @Mapping(target = "length", ignore = true)
    void updateContestFromDto(CreateContestRequest dto, @MappingTarget Contest entity);
}
