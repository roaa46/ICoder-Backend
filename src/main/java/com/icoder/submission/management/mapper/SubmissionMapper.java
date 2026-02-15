package com.icoder.submission.management.mapper;

import com.icoder.submission.management.dto.SubmissionCreateResponse;
import com.icoder.submission.management.dto.OpenSubmissionResponse;
import com.icoder.submission.management.dto.SubmissionPageResponse;
import com.icoder.submission.management.dto.SubmissionResponse;
import com.icoder.submission.management.entity.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {

    @Mapping(target = "problemCode", source = "submission.problem.problemCode")
    SubmissionCreateResponse toDTO(Submission submission);
    @Mapping(target = "userHandle", source = "submission.user.handle")
    @Mapping(target = "onlineJudge", source = "submission.onlineJudge")
    @Mapping(target = "submittedAt", source = "submission.submittedAt")
    @Mapping(target = "timeUsage", source = "submission.timeUsage", qualifiedByName = "integerToString")
    @Mapping(target = "memoryUsage", source = "submission.memoryUsage", qualifiedByName = "integerToString")
    @Mapping(target = "isOpen", source = "submission.opened")
    SubmissionResponse toSubmissionResponse(com.icoder.submission.management.entity.Submission submission);

    @Mapping(target = "problemCode", source = "submission.problem.problemCode")
    @Mapping(target = "userHandle", source = "submission.user.handle")
    @Mapping(target = "onlineJudge", source = "submission.onlineJudge")
    @Mapping(target = "submittedAt", source = "submission.submittedAt")
    @Mapping(target = "timeUsage", source = "submission.timeUsage", qualifiedByName = "integerToString")
    @Mapping(target = "memoryUsage", source = "submission.memoryUsage", qualifiedByName = "integerToString")
    @Mapping(target = "isOpen", source = "submission.opened")
    @Mapping(target = "solution", source = "submission.submissionCode")
    OpenSubmissionResponse toOpenSubmissionResponse(com.icoder.submission.management.entity.Submission submission);

    @Mapping(target = "problemCode", source = "problemCode")
    @Mapping(target = "userHandle", source = "userHandle")
    @Mapping(target = "onlineJudge", source = "submission.onlineJudge")
    @Mapping(target = "submittedAt", source = "submission.submittedAt")
    @Mapping(target = "timeUsage", source = "submission.timeUsage", qualifiedByName = "integerToString")
    @Mapping(target = "memoryUsage", source = "submission.memoryUsage", qualifiedByName = "integerToString")
    @Mapping(target = "isOpen", source = "submission.opened")
    SubmissionPageResponse toSubmissionPageResponse(com.icoder.submission.management.entity.Submission submission, String problemCode, String userHandle);

    @Named("integerToString")
    default String integerToString(Integer value) {
        return value != null ? value.toString() : null;
    }
}
