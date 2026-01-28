package com.icoder.contest.management.mapper;

import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.entity.Contest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", ignore = true)
    @Mapping(target = "beginTime", ignore = true)
    @Mapping(target = "length", ignore = true)
    @Mapping(target = "historyRank", ignore = true)
    @Mapping(target = "contestStatus", ignore = true)
    @Mapping(target = "problemRelation", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateContestFromDto(CreateContestRequest dto, @MappingTarget Contest entity);
}
