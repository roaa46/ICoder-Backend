package com.icoder.problem.management.mapper;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.entity.Problem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SectionMapper.class, PropertyMapper.class})
public interface ProblemMapper {

    @Mapping(source = "id", target = "problemId")
    ProblemStatementResponse toStatementDTO(Problem problem);

    @Mapping(source = "id", target = "problemId")
    ProblemResponse toResponseDTO(Problem problem);
}
