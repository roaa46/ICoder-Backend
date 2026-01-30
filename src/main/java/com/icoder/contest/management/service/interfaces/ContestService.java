package com.icoder.contest.management.service.interfaces;

import com.icoder.contest.management.dto.ContestResponse;
import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.dto.GroupContestsResponse;
import com.icoder.core.dto.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContestService {
    MessageResponse createContest(CreateContestRequest request);

    MessageResponse updateContest(Long contestId, CreateContestRequest request);

    void deleteContest(Long contestId, Long groupId);

    Page<GroupContestsResponse> viewContestsInGroup(Long groupId, Pageable pageable);

    ContestResponse viewContest(Long contestId);
}
