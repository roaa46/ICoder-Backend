package com.icoder.submission.management.mapper;

import com.icoder.submission.management.dto.SubmissionCreateResponse;
import com.icoder.submission.management.entity.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {
    @Mapping(target = "problemCode", source = "submission.problem.problemCode")
    SubmissionCreateResponse toDTO(Submission submission);
}
