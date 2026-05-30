package com.icoder.problem.management.mapper;

import com.icoder.problem.management.dto.ContentScrapeDTO;
import com.icoder.problem.management.entity.SectionContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ContentMapper {
    SectionContent toEntity(ContentScrapeDTO dto);

    @Mapping(source = "id", target = "contentId")
    ContentScrapeDTO toDTO(SectionContent entity);

//    List<SectionContent> toListEntity(List<ContentScrapeDTO> list);
//
//    List<ContentScrapeDTO> toListDTO(List<SectionContent> list);
}
