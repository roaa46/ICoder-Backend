package com.icoder.contest.management.service.interfaces;

import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.core.dto.MessageResponse;

public interface ContestService {
    MessageResponse createContest(CreateContestRequest request);
}
