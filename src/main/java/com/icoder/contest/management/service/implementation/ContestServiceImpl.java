package com.icoder.contest.management.service.implementation;

import com.icoder.contest.management.dto.ContestDetailsResponse;
import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.dto.ProblemSetResponse;
import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.entity.ContestProblemRelation;
import com.icoder.contest.management.mapper.ContestMapper;
import com.icoder.contest.management.repository.ContestRepository;
import com.icoder.contest.management.service.interfaces.ContestService;
import com.icoder.contest.management.util.ContestUtils;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestServiceImpl implements ContestService {
    private final ContestRepository contestRepository;
    private final ContestUtils contestUtils;
    private final SecurityUtils securityUtils;
    private final GroupRepository groupRepository;
    private final ContestMapper contestMapper;

    @Override
    @Transactional
    public MessageResponse createContest(CreateContestRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        Long groupId = request.getGroupId();

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

        Duration contesestDuration = contestUtils.parseDuration(request.getLength());

        Contest contest = Contest.builder()
                .group(group)
                .title(request.getTitle())
                .description(request.getDescription())
                .beginTime(request.getBeginTime())
                .endTime(request.getBeginTime().plus(contesestDuration))
                .length(contesestDuration)
                .historyRank(request.getHistoryRank() == null || request.getHistoryRank())
                .createdAt(Instant.now())
                .build();

        contest.setContestStatus(contestUtils.calculateStatus(contest.getBeginTime(), contest.getLength()));

        contestUtils.checkGroupVisibility(group, request);
        contestUtils.applyContestRulesBasedOnGroupVisibility(contest, group, request.getPassword());

        Set<ContestProblemRelation> relations = contestUtils.mapProblemSetToRelations(request.getProblemSet(), contest);
        contest.setProblemRelation(relations);

        contestRepository.save(contest);

        return new MessageResponse("Contest created successfully with " + relations.size() + " problems.");
    }

    @Override
    @Transactional
    public MessageResponse updateContest(Long contestId, CreateContestRequest request) {
        Long userId = securityUtils.getCurrentUserId();

        Contest existingContest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        Long groupId = request.getGroupId();
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (!contestUtils.isUserContestCoordinator(userId, group)) {
            throw new org.springframework.security.access.AccessDeniedException("User is not a contest coordinator");
        }

        if (request.getProblemSet() == null || request.getProblemSet().isEmpty()) {
            throw new IllegalArgumentException("A contest must have at least one problem.");
        }

        Duration contesestDuration = contestUtils.parseDuration(request.getLength());

        contestMapper.updateContestFromDto(request, existingContest);
        existingContest.setBeginTime(request.getBeginTime());
        existingContest.setEndTime(request.getBeginTime().plus(contesestDuration));
        existingContest.setLength(contesestDuration);
        existingContest.setContestStatus(contestUtils.calculateStatus(existingContest.getBeginTime(), existingContest.getLength()));
        existingContest.setHistoryRank(request.getHistoryRank() == null || request.getHistoryRank());

        contestUtils.checkGroupVisibility(group, request);
        contestUtils.applyContestRulesBasedOnGroupVisibility(existingContest, group, request.getPassword());

        Set<ContestProblemRelation> newRelations = contestUtils.mapProblemSetToRelations(request.getProblemSet(), existingContest);

        existingContest.getProblemRelation().clear();
        existingContest.getProblemRelation().addAll(newRelations);

        contestRepository.save(existingContest);

        return new MessageResponse("Contest updated successfully.");
    }

    @Override
    @Transactional
    public void deleteContest(Long contestId) {
        Long userId = securityUtils.getCurrentUserId();

        Group group = contestUtils.getGroup(contestId);

        if (!contestUtils.isUserContestCoordinator(userId, group)) {
            throw new org.springframework.security.access.AccessDeniedException("User is not a contest coordinator");
        }

        contestUtils.isContestInGroup(contestId, group.getId());

        contestRepository.deleteById(contestId);
    }

    @Override
    public ContestDetailsResponse viewContestDetails(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));
        return contestMapper.toContestDetailsDto(contest);
    }

    @Override
    public Set<ProblemSetResponse> viewProblemSet(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                        .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));
        contestUtils.validateAccess(contest);

        return contest.getProblemRelation().stream()
                .map(contestMapper::toProblemSetResponse)
                .collect(Collectors.toSet());
    }
}
