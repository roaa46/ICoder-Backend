package com.icoder.contest.management.mapper;

import com.icoder.problem.management.mapper.PropertyMapper;
import com.icoder.problem.management.mapper.SectionMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {SectionMapper.class, PropertyMapper.class})
public interface ContestMapper {
}
