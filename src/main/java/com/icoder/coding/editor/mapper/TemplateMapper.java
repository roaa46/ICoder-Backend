package com.icoder.coding.editor.mapper;

import com.icoder.coding.editor.dto.CodeTemplateRequest;
import com.icoder.coding.editor.dto.CodeTemplateResponse;
import com.icoder.coding.editor.entity.CodeTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TemplateMapper {
    CodeTemplate toEntity(CodeTemplateRequest request);
    @Mapping(source = "id", target = "templateId")
    CodeTemplateResponse toDTO(CodeTemplate template);
}
