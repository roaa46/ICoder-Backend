package com.icoder.contest.management.service.implementation;

import com.icoder.contest.management.dto.ContestDetailsResponse;
import com.icoder.contest.management.dto.ContestResponse;
import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.dto.ProblemSetResponse;
import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.entity.ContestProblemRelation;
import com.icoder.contest.management.entity.ContestUserRelation;
import com.icoder.contest.management.enums.ContestRole;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.enums.ContestType;
import com.icoder.contest.management.mapper.ContestMapper;
import com.icoder.contest.management.repository.ContestProblemRelationRepository;
import com.icoder.contest.management.repository.ContestRepository;
import com.icoder.contest.management.repository.ContestUserRelationRepository;
import com.icoder.contest.management.service.interfaces.ContestService;
import com.icoder.contest.management.util.ContestUtils;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.core.specification.SpecBuilder;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
    private final UserRepository userRepository;
    private final ContestUserRelationRepository contestUserRelationRepository;
    private final ContestProblemRelationRepository contestProblemRelationRepository;
    private final SubmissionRepository submissionRepository;

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
                .contestOpenness(request.getContestOpenness())
                .contestType(request.getContestType())
                .historyRank(request.getHistoryRank() == null || request.getHistoryRank())
                .createdAt(Instant.now())
                .build();

        contest.setContestStatus(contestUtils.calculateStatus(contest.getBeginTime(), contest.getLength()));

        contestUtils.checkGroupVisibility(group, request);
        contestUtils.applyContestRulesBasedOnGroupVisibility(contest, group, request.getPassword());

        Set<ContestProblemRelation> relations = contestUtils.mapProblemSetToRelations(request.getProblemSet(), contest);
        contest.setProblemRelation(relations);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        ContestUserRelation userRelation = ContestUserRelation.builder()
                .user(user)
                .contest(contest)
                .role(ContestRole.OWNER)
                .build();
        contest.addUserRelation(userRelation);

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

        ContestUserRelation userRelation = contestUserRelationRepository.findByContestIdAndRole(contestId, ContestRole.OWNER)
                .orElseThrow(() -> new ResourceNotFoundException("Contest owner not found for contest with id: " + contestId));
        User user = userRelation.getUser();

        ContestDetailsResponse response = contestMapper.toContestDetailsDto(contest);
        response.setOwnerId(user.getId());
        response.setOwnerHandle(user.getHandle());

        return response;
    }

    @Override
    public Set<ProblemSetResponse> viewProblemSet(Long contestId) {
        Contest contest = contestRepository.findByIdWithGroupAndProblems(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        Long userId = securityUtils.getCurrentUserId();
        Set<Long> solvedProblemIds = submissionRepository.findSolvedProblemIdsByUserIdAndContestId(userId, contestId);
        boolean isCoordinator = contestUtils.isUserContestCoordinator(userId, contest.getGroup());
        contestUtils.validateAccessWithRole(contest, isCoordinator);

        boolean isContestRunning = contestUtils.checkIfContestRunning(contest);

        return contest.getProblemRelation().stream()
                .map(relation -> {
                    ProblemSetResponse response = contestMapper.toProblemSetResponse(relation);
                    response.setSolved(solvedProblemIds.contains(relation.getProblem().getId()));
                    if (isContestRunning && !isCoordinator) {
                        response.setOrigin(null);
                    }
                    String title = relation.getProblem().getProblemTitle();
                    response.setTitle(relation.getProblemAlias() != null && !relation.getProblemAlias().isEmpty() ? response.getProblemAlias() : title);
                    return response;
                }).collect(Collectors.toSet());
    }

    @Override
    public Page<ContestResponse> viewAllContests(String contestTitle, String groupName, ContestStatus status, ContestType type, Pageable pageable) {

        Specification<Contest> spec = new SpecBuilder<Contest>()

                .with("title", ":", contestTitle)

                .with("group.name", ":", groupName)

                .with("contestStatus", ":", status)

                .with("contestType", ":", type)

                .build();
        if (spec == null) spec = Specification.where(null);

        return contestRepository.findAll(spec, pageable)
                .map(contestMapper::toContestResponse);
    }

    @Transactional
    @Override
    public void updateContestStatistics(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (submission.getContest() == null) {
            log.warn("Early Return: Submission {} has no contest attached. It was submitted as normal practice.", submissionId);
            return;
        }

        Contest contest = submission.getContest();

        if (contest.getContestStatus() != ContestStatus.RUNNING) {
            log.warn("Early Return: Contest {} status is {}. Statistics are only updated during RUNNING state.", contest.getId(), contest.getContestStatus());
            return;
        }

        log.info("Proceeding to update statistics for Contest: {}, Problem: {}, User: {}", contest.getId(), submission.getProblem().getId(), submission.getUser().getId());

        ContestProblemRelation problemRelation = contestProblemRelationRepository
                .findByContestIdAndProblemId(contest.getId(), submission.getProblem().getId())
                .orElseThrow();

        ContestUserRelation userRelation = contestUserRelationRepository
                .findByContestIdAndUserId(contest.getId(), submission.getUser().getId())
                .orElseThrow();

        problemRelation.setAttemptedCount(problemRelation.getAttemptedCount() + 1);

        if (submission.getVerdict() == SubmissionVerdict.ACCEPTED) {

            boolean alreadySolved = submissionRepository.existsByUserIdAndContestIdAndProblemIdAndVerdictAndIdNot(
                    submission.getUser().getId(),
                    contest.getId(),
                    submission.getProblem().getId(),
                    SubmissionVerdict.ACCEPTED,
                    submission.getId()
            );

            if (!alreadySolved) {
                problemRelation.setSolvedCount(problemRelation.getSolvedCount() + 1);

                int weight = problemRelation.getProblemWeight() != null ? problemRelation.getProblemWeight() : 1;
                int currentScore = userRelation.getScore() != null ? userRelation.getScore() : 0;
                userRelation.setScore(currentScore + weight);

                long minutesFromStart = Duration.between(contest.getBeginTime(), submission.getSubmittedAt()).toMinutes();

                List<SubmissionVerdict> penaltyVerdicts = List.of(
                        SubmissionVerdict.WRONG_ANSWER,
                        SubmissionVerdict.RUNTIME_ERROR,
                        SubmissionVerdict.TIME_LIMIT_EXCEEDED,
                        SubmissionVerdict.MEMORY_LIMIT_EXCEEDED
                );
                int wrongSubmissionsCount = submissionRepository.countByUserIdAndContestIdAndProblemIdAndVerdictIn(
                        submission.getUser().getId(),
                        contest.getId(),
                        submission.getProblem().getId(),
                        penaltyVerdicts
                );

                int penaltyForThisProblem = (int) minutesFromStart + (wrongSubmissionsCount * 20);
                int currentPenalty = userRelation.getPenalty() != null ? userRelation.getPenalty() : 0;
                userRelation.setPenalty(currentPenalty + penaltyForThisProblem);
                log.info("Penalty for user {}: {} minutes", submission.getUser().getId(), penaltyForThisProblem);

                if (problemRelation.getFirstAcceptedSubmission() == null) {
                    problemRelation.setFirstAcceptedSubmission(submission);
                    log.info("First Accepted recorded for problem {} by user {}",
                            problemRelation.getId(), submission.getUser().getId());
                }
            }
        }

        contestProblemRelationRepository.save(problemRelation);
        contestUserRelationRepository.save(userRelation);
    }
}
