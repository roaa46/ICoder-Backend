package com.icoder.contest.management.service.interfaces;

import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.dto.GroupContestsResponse;
import com.icoder.core.dto.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContestService {
    MessageResponse createContest(CreateContestRequest request);

    MessageResponse updateContest(String contestId, CreateContestRequest request);

    void deleteContest(String contestId, String groupId);

    Page<GroupContestsResponse> viewContestsInGroup(String groupId, Pageable pageable);
}
