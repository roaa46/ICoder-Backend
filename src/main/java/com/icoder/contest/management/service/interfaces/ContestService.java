package com.icoder.contest.management.service.interfaces;

import com.icoder.contest.management.dto.ContestDetailsResponse;
import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.dto.ProblemSetResponse;
import com.icoder.core.dto.MessageResponse;

import java.util.Set;

public interface ContestService {
    MessageResponse createContest(CreateContestRequest request);

    MessageResponse updateContest(Long contestId, CreateContestRequest request);

    void deleteContest(Long contestId);

    ContestDetailsResponse viewContestDetails(Long contestId);

    Set<ProblemSetResponse> viewProblemSet(Long contestId);
}
