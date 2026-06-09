package com.icoder.submission.management.mapper;

import com.icoder.submission.management.dto.*;
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
    SubmissionResponse toSubmissionResponse(Submission submission);

    @Mapping(target = "problemCode", source = "submission.problem.problemCode")
    @Mapping(target = "userHandle", source = "submission.user.handle")
    @Mapping(target = "onlineJudge", source = "submission.onlineJudge")
    @Mapping(target = "submittedAt", source = "submission.submittedAt")
    @Mapping(target = "timeUsage", source = "submission.timeUsage", qualifiedByName = "integerToString")
    @Mapping(target = "memoryUsage", source = "submission.memoryUsage", qualifiedByName = "integerToString")
    @Mapping(target = "isOpen", source = "submission.opened")
    @Mapping(target = "solution", source = "submission.submissionCode")
    OpenSubmissionResponse toOpenSubmissionResponse(Submission submission);

    @Mapping(target = "problemCode", source = "problemCode")
    @Mapping(target = "userHandle", source = "userHandle")
    @Mapping(target = "onlineJudge", source = "submission.onlineJudge")
    @Mapping(target = "submittedAt", source = "submission.submittedAt")
    @Mapping(target = "timeUsage", source = "submission.timeUsage", qualifiedByName = "integerToString")
    @Mapping(target = "memoryUsage", source = "submission.memoryUsage", qualifiedByName = "integerToString")
    @Mapping(target = "isOpen", source = "submission.opened")
    SubmissionPageResponse toSubmissionPageResponse(Submission submission, String problemCode, String userHandle, Long userId);

    @Mapping(target = "userHandle", source = "submission.user.handle")
    @Mapping(target = "userId", source = "submission.user.id")
    @Mapping(target = "onlineJudge", source = "submission.onlineJudge")
    @Mapping(target = "problemId", source = "submission.problem.id")
    @Mapping(target = "problemAlias", source = "problemAlias")
    @Mapping(target = "verdict", source = "submission.verdict")
    @Mapping(target = "language", source = "submission.language")
    @Mapping(target = "timeUsage", source = "submission.timeUsage", qualifiedByName = "integerToString")
    @Mapping(target = "memoryUsage", source = "submission.memoryUsage", qualifiedByName = "integerToString")
    @Mapping(target = "submittedAt", source = "submission.submittedAt")
    @Mapping(target = "isOpen", source = "submission.opened")
    @Mapping(target = "remoteRunId", source = "submission.remoteRunId")
    ContestSubmissionsResponse toContestSubmissionsResponse(Submission submission, String problemAlias);

    @Named("integerToString")
    default String integerToString(Integer value) {
        return value != null ? value.toString() : null;
    }
}
