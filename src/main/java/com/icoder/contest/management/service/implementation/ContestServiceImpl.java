package com.icoder.contest.management.service.implementation;

import com.icoder.contest.management.dto.ProblemSetRequest;
import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.entity.ContestProblemRelation;
import com.icoder.contest.management.enums.ContestOpenness;
import com.icoder.contest.management.mapper.ContestMapper;
import com.icoder.contest.management.repository.ContestProblemRelationRepository;
import com.icoder.contest.management.repository.ContestRepository;
import com.icoder.contest.management.service.interfaces.ContestService;
import com.icoder.contest.management.util.ContestUtils;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.enums.Visibility;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.repository.ProblemRepository;
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
    private final ProblemRepository problemRepository;
    private final ContestProblemRelationRepository contestProblemRelationRepository;

    @Override
    @Transactional
    public MessageResponse createContest(CreateContestRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        Long groupId = Long.parseLong(request.getGroupId());

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
        validateContestRules(request, group);

        Contest contest = contestMapper.toEntity(request);
        contest.setBeginTime(contestUtils.parseInstant(request.getBeginTime()));
        contest.setLength(contestUtils.parseDuration(request.getLength()));
        contest.setGroup(group);
        contest.setContestStatus(contestUtils.calculateStatus(contest.getBeginTime(), contest.getLength()));
        contest.setHistoryRank(request.getHistoryRank() == null || request.getHistoryRank());

        // 3. Apply Visibility & Logic Rules (Public vs Private Group)
        contestUtils.applyContestRulesBasedOnGroupVisibility(contest, group, request.getPassword());

        Contest savedContest = contestRepository.save(contest);
        saveContestProblems(request.getProblemSet(), savedContest);

        log.info("Contest '{}' created successfully with {} problems.", savedContest.getTitle(), request.getProblemSet() != null ? request.getProblemSet().size() : 0);
        return new MessageResponse("Contest created successfully with " +
                (request.getProblemSet() != null ? request.getProblemSet().size() : 0) + " problems.");
    }

    @Override
    @Transactional
    public MessageResponse updateContest(Long contestId, CreateContestRequest request) {

        Long userId = securityUtils.getCurrentUserId();

        Contest existingContest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        Long groupId = Long.parseLong(request.getGroupId());
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (!contestUtils.isUserContestCoordinator(userId, group)) {
            throw new org.springframework.security.access.AccessDeniedException("User is not a contest coordinator");
        }
        if (request.getProblemSet() == null || request.getProblemSet().isEmpty()) {
            throw new IllegalArgumentException("A contest must have at least one problem.");
        }
        validateContestRules(request, group);

        contestMapper.updateContestFromDto(request, existingContest);
        existingContest.setBeginTime(contestUtils.parseInstant(request.getBeginTime()));
        existingContest.setLength(contestUtils.parseDuration(request.getLength()));
        existingContest.setGroup(group);
        existingContest.setContestStatus(contestUtils.calculateStatus(existingContest.getBeginTime(), existingContest.getLength()));
        existingContest.setHistoryRank(request.getHistoryRank() == null || request.getHistoryRank());
        contestUtils.applyContestRulesBasedOnGroupVisibility(existingContest, group, request.getPassword());

        contestProblemRelationRepository.deleteByContestId(contestId);
        saveContestProblems(request.getProblemSet(), existingContest);

        contestRepository.save(existingContest);

        return new MessageResponse("Contest updated successfully.");
    }

    private void saveContestProblems(List<ProblemSetRequest> problemSet, Contest contest) {
        log.info("Saving contest problems for contest: {}", contest.getTitle());
        List<ContestProblemRelation> relations = problemSet.stream()
                .map(pReq -> {
                    Problem problem = problemRepository.findById(Long.parseLong(pReq.getProblemId()))
                            .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + pReq.getProblemId()));

                    int weight;
                    try {
                        weight = (pReq.getProblemWeight() == null || pReq.getProblemWeight().isBlank())
                                ? 1 : Integer.parseInt(pReq.getProblemWeight());
                    } catch (NumberFormatException e) {
                        log.error("Invalid weight format for problem: {}", pReq.getProblemId());
                        throw new IllegalArgumentException("Invalid weight format for problem: " + pReq.getProblemId());
                    }

                    return new ContestProblemRelation(
                            null,
                            contest,
                            problem,
                            Math.max(weight, 1),
                            pReq.getProblemAlias()
                    );
                }).toList();

        contestProblemRelationRepository.saveAll(relations);
    }

    private void validateContestRules(CreateContestRequest request, Group group) {
        ContestOpenness contestOpenness = ContestOpenness.valueOf(request.getContestOpenness().name());

        if (group.getVisibility() == Visibility.PRIVATE && contestOpenness != ContestOpenness.PRIVATE) {
            throw new IllegalArgumentException("Private groups can only have private contests.");
        }

        if (contestOpenness == ContestOpenness.PROTECTED) {
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new IllegalArgumentException("Password is required for protected contests.");
            }
        }
    }
}
