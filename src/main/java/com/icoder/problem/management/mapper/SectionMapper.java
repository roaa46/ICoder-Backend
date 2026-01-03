package com.icoder.problem.management.mapper;

import com.icoder.problem.management.dto.SectionScrapeDTO;
import com.icoder.problem.management.entity.ProblemSection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ContentMapper.class})
public interface SectionMapper {
    ProblemSection toEntity(SectionScrapeDTO dto);

    @Mapping(source = "id", target = "sectionId")
    SectionScrapeDTO toDTO(ProblemSection entity);

    List<ProblemSection> toListEntity(List<SectionScrapeDTO> list);

    List<SectionScrapeDTO> toListDTO(List<ProblemSection> list);
}
