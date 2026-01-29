package com.icoder.contest.management.service.implementation;

import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.entity.ContestProblemRelation;
import com.icoder.contest.management.mapper.ContestMapper;
import com.icoder.contest.management.repository.ContestRepository;
import com.icoder.contest.management.service.interfaces.ContestService;
import com.icoder.contest.management.util.ContestUtils;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.core.utils.ConvertFromString;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestServiceImpl implements ContestService {
    private final ContestRepository contestRepository;
    private final ContestUtils contestUtils;
    private final SecurityUtils securityUtils;
    private final GroupRepository groupRepository;
    private final ContestMapper contestMapper;
    private final ConvertFromString convertFromString;

    @Override
    @Transactional
    public MessageResponse createContest(CreateContestRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        Long groupId = convertFromString.toLong(request.getGroupId());

        if (request.getProblemSet() == null || request.getProblemSet().isEmpty()) {
            throw new IllegalArgumentException("A contest must have at least one problem.");
        }

        log.info("User {} is creating a contest for group {}", userId, groupId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (!contestUtils.isUserContestCoordinator(userId, group)) {
            log.warn("Access denied: User {} is not coordinator for group {}", userId, groupId);
            throw new org.springframework.security.access.AccessDeniedException("User is not a contest coordinator");
        }

        contestUtils.validateContestRules(request, group);

        Contest contest = Contest.builder()
                .group(group)
                .title(request.getTitle())
                .description(request.getDescription())
                .beginTime(contestUtils.parseInstant(request.getBeginTime()))
                .length(contestUtils.parseDuration(request.getLength()))
                .historyRank(request.getHistoryRank() == null || request.getHistoryRank())
                .build();

        contest.setContestStatus(contestUtils.calculateStatus(contest.getBeginTime(), contest.getLength()));

        contestUtils.checkGroupVisibility(group, request);
        contestUtils.applyContestRulesBasedOnGroupVisibility(contest, group, request.getPassword());

        List<ContestProblemRelation> relations = contestUtils.mapProblemSetToRelations(request.getProblemSet(), contest);
        contest.setProblemRelation(relations);

        contestRepository.save(contest);

        return new MessageResponse("Contest created successfully with " + relations.size() + " problems.");
    }

    @Override
    @Transactional
    public MessageResponse updateContest(String contestId, CreateContestRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        Long contestIdLong = convertFromString.toLong(contestId);
        Contest existingContest = contestRepository.findById(contestIdLong)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        Long groupId = convertFromString.toLong(request.getGroupId());
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (!contestUtils.isUserContestCoordinator(userId, group)) {
            throw new org.springframework.security.access.AccessDeniedException("User is not a contest coordinator");
        }

        if (request.getProblemSet() == null || request.getProblemSet().isEmpty()) {
            throw new IllegalArgumentException("A contest must have at least one problem.");
        }

        contestMapper.updateContestFromDto(request, existingContest);
        existingContest.setBeginTime(contestUtils.parseInstant(request.getBeginTime()));
        existingContest.setLength(contestUtils.parseDuration(request.getLength()));
        existingContest.setContestStatus(contestUtils.calculateStatus(existingContest.getBeginTime(), existingContest.getLength()));
        existingContest.setHistoryRank(request.getHistoryRank() == null || request.getHistoryRank());

        contestUtils.checkGroupVisibility(group, request);
        contestUtils.applyContestRulesBasedOnGroupVisibility(existingContest, group, request.getPassword());

        List<ContestProblemRelation> newRelations = contestUtils.mapProblemSetToRelations(request.getProblemSet(), existingContest);

        existingContest.getProblemRelation().clear();
        existingContest.getProblemRelation().addAll(newRelations);

        contestRepository.save(existingContest);

        return new MessageResponse("Contest updated successfully.");
    }

    @Override
    @Transactional
    public void deleteContest(String contestId, String groupId) {
        Long userId = securityUtils.getCurrentUserId();
        Long groupIdLong = convertFromString.toLong(groupId);
        Long contestIdLong = convertFromString.toLong(contestId);

        Group group = groupRepository.findById(groupIdLong)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        if (!contestUtils.isUserContestCoordinator(userId, group)) {
            throw new org.springframework.security.access.AccessDeniedException("User is not a contest coordinator");
        }

        contestUtils.isContestInGroup(contestIdLong, groupIdLong);

        contestRepository.deleteById(contestIdLong);
    }
}
