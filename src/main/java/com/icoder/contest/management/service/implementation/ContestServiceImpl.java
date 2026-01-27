package com.icoder.contest.management.service.implementation;

import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.repository.ContestRepository;
import com.icoder.contest.management.service.interfaces.ContestService;
import com.icoder.core.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestServiceImpl implements ContestService {
    private final ContestRepository contestRepository;


    @Override
    @Transactional
    public MessageResponse createContest(CreateContestRequest request) {
        Long userId = Long.parseLong(request.getUserId());
        Long groupId = Long.parseLong(request.getGroupId());
        return new MessageResponse("Contest created successfully");
    }
}
