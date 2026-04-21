package com.icoder.problem.management.mapper;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.entity.ProblemUserRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SectionMapper.class, PropertyMapper.class})
public interface ProblemMapper {

    @Mapping(source = "problem.id", target = "problemId")
    @Mapping(source = "relation.solved", target = "solved")
    @Mapping(source = "relation.attempted", target = "attempted")
    @Mapping(source = "relation.favorite", target = "favorite")
    ProblemResponse toResponseDTO(Problem problem, ProblemUserRelation relation);

    @Mapping(source = "id", target = "problemId")
    ProblemResponse toResponseDTO(Problem problem);

    @Mapping(source = "id", target = "problemId")
    ProblemStatementResponse toStatementDTO(Problem problem);
}
