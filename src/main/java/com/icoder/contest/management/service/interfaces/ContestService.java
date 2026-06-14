package com.icoder.contest.management.service.interfaces;

import com.icoder.contest.management.dto.*;
import com.icoder.contest.management.enums.ContestOpenness;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.core.dto.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ContestService {
    MessageResponse createContest(CreateContestRequest request);

    MessageResponse updateContest(Long contestId, CreateContestRequest request);

    void deleteContest(Long contestId);

    ContestDetailsResponse viewContestDetails(Long contestId);

    Set<ProblemSetResponse> viewProblemSet(Long contestId);

    Page<ContestResponse> viewAllContests(String contestTitle, String groupName, ContestStatus status, ContestOpenness openness, Pageable pageable);

    void updateContestStatistics(Long submissionId);

    MessageResponse joinProtectedContest(Long contestId, JoinProtectedContestRequest request);

    MessageResponse checkProtectedContestMembership(Long userId, Long groupId);
}
