package com.icoder.contest.management.mapper;

import com.icoder.contest.management.dto.*;
import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.entity.ContestProblemRelation;
import com.icoder.core.utils.DateTimeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {DateTimeMapper.class, ContestUserRelationMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContestMapper {
    @Mapping(target = "beginTime", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "length", ignore = true)
    @Mapping(target = "historyRank", ignore = true)
    @Mapping(target = "contestStatus", ignore = true)
    @Mapping(target = "problemRelation", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateContestFromDto(CreateContestRequest dto, @MappingTarget Contest entity);

    GroupContestsResponse toGroupContestDto(Contest contest);

    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "ownerHandle", ignore = true)
    @Mapping(target = "groupId", source = "contest.group.id")
    @Mapping(target = "groupName", source = "contest.group.name")
    ContestDetailsResponse toContestDetailsDto(Contest contest);

    @Mapping(target = "id", source = "problem.id")
    @Mapping(target = "title", source = "problem.problemTitle")
    @Mapping(target = "origin", source = "problem.problemLink")
    ProblemSetResponse toProblemSetResponse(ContestProblemRelation relation);

    @Mapping(target = "type", source = "contest.contestType")
    @Mapping(target = "status", source = "contest.contestStatus")
    @Mapping(target = "length", source = "contest.length")
    @Mapping(target = "groupId", source = "contest.group.id")
    @Mapping(target = "groupName", source = "contest.group.name")
    ContestResponse toContestResponse(Contest contest);
}
