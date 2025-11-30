package com.icoder.problem.management.mapper;

import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.entity.Problem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {SectionMapper.class, PropertyMapper.class})
public interface ProblemMapper {
    ProblemStatementResponse toStatementDTO(Problem problem);
    ProblemResponse toResponseDTO(Problem problem);
}
