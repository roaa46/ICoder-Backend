package com.icoder.problem.management.mapper;

import com.icoder.problem.management.dto.PropertyScrapeDTO;
import com.icoder.problem.management.entity.ProblemProperty;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PropertyMapper {
    ProblemProperty toEntity(PropertyScrapeDTO dto);
    PropertyScrapeDTO toDTO(ProblemProperty entity);
    List<ProblemProperty> toListEntity(List<PropertyScrapeDTO> list);
    List<PropertyScrapeDTO> toListDTO(List<ProblemProperty> list);
}
