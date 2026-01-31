package com.icoder.contest.management.mapper;

import com.icoder.contest.management.dto.ContestDetailsResponse;
import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.dto.GroupContestsResponse;
import com.icoder.contest.management.dto.ProblemSetResponse;
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

    ContestDetailsResponse toContestDetailsDto(Contest contest);

    @Mapping(target = "id", source = "problem.id")
    @Mapping(target = "title", source = "problem.problemTitle")
    @Mapping(target = "origin", source = "problem.problemLink")
    ProblemSetResponse toProblemSetResponse(ContestProblemRelation relation);
}
