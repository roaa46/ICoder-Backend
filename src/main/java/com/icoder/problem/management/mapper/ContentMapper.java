package com.icoder.problem.management.mapper;

import com.icoder.problem.management.dto.ContentScrapeDTO;
import com.icoder.problem.management.entity.SectionContent;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ContentMapper {

    SectionContent toEntity(ContentScrapeDTO dto);

    ContentScrapeDTO toDTO(SectionContent entity);

//    List<SectionContent> toListEntity(List<ContentScrapeDTO> list);
//
//    List<ContentScrapeDTO> toListDTO(List<SectionContent> list);
}
