package com.icoder.contest.management.service.implementation;

import com.icoder.contest.management.dto.*;
import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.entity.ContestProblemRelation;
import com.icoder.contest.management.entity.ContestUserRelation;
import com.icoder.contest.management.enums.ContestOpenness;
import com.icoder.contest.management.enums.ContestRole;
import com.icoder.contest.management.enums.ContestStatus;
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
import com.icoder.group.management.entity.UserGroupRole;
import com.icoder.group.management.enums.Visibility;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.group.management.repository.UserGroupRoleRepository;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
    private final PasswordEncoder passwordEncoder;
    private final UserGroupRoleRepository userGroupRoleRepository;

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
        Instant now = Instant.now();
        if (contest.getEndTime() != null && contest.getEndTime().isAfter(now)) {
            response.setRemainingTime(Duration.between(now, contest.getEndTime()));
        }

        return response;
    }

    @Override
    public List<ProblemSetResponse> viewProblemSet(Long contestId) {
        Contest contest = contestRepository.findByIdWithGroupAndProblems(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));
        Long userId = securityUtils.getCurrentUserId();
        Set<Long> solvedProblemIds = submissionRepository.findSolvedProblemIdsByUserIdAndContestId(userId, contestId);
        boolean isCoordinator = contestUtils.isUserContestCoordinator(userId, contest.getGroup());
        contestUtils.validateAccessWithRole(contest, isCoordinator);
        boolean isContestRunning = contestUtils.checkIfContestRunning(contest);

        List<ContestProblemRelation> sortedRelations = contest.getProblemRelation().stream()
                .sorted(Comparator.comparing(ContestProblemRelation::getId))
                .toList();

        List<ProblemSetResponse> result = new ArrayList<>();
        for (int i = 0; i < sortedRelations.size(); i++) {
            ContestProblemRelation relation = sortedRelations.get(i);
            ProblemSetResponse response = contestMapper.toProblemSetResponse(relation);
            response.setSolved(solvedProblemIds.contains(relation.getProblem().getId()));
            if (isContestRunning && !isCoordinator) {
                response.setOrigin(null);
            }
            String title = relation.getProblem().getProblemTitle();
            response.setTitle(relation.getProblemAlias() != null && !relation.getProblemAlias().isEmpty() ? response.getProblemAlias() : title);
            response.setProblemNumber((char) ('A' + i));
            result.add(response);
        }
        return result;
    }

    @Override
    public Page<ContestResponse> viewAllContests(String contestTitle, String groupName, ContestStatus status, ContestOpenness openness, Pageable pageable) {

        Long currentUserId = securityUtils.getCurrentUserId();

        Specification<Contest> spec = new SpecBuilder<Contest>()
                .with("title", ":", contestTitle)
                .with("group.name", ":", groupName)
                .with("contestStatus", ":", status)
                .with("contestOpenness", ":", openness)
                .build();

        Specification<Contest> visibilitySpec = (root, query, cb) -> {
            Predicate isPublic = cb.equal(
                    root.get("group").get("visibility"), Visibility.PUBLIC
            );

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<UserGroupRole> ugr = subquery.from(UserGroupRole.class);
            subquery.select(ugr.get("group").get("id"))
                    .where(cb.equal(ugr.get("user").get("id"), currentUserId));

            Predicate isPrivateAndMember = cb.and(
                    cb.equal(root.get("group").get("visibility"), Visibility.PRIVATE),
                    root.get("group").get("id").in(subquery)
            );

            return cb.or(isPublic, isPrivateAndMember);
        };

        if (spec == null) spec = Specification.where(visibilitySpec);
        else spec = spec.and(visibilitySpec);

        return contestRepository.findAll(spec, pageable)
                .map(contest -> {
                    ContestResponse response = contestMapper.toContestResponse(contest);
                    Instant now = Instant.now();
                    if (contest.getEndTime() != null && contest.getEndTime().isAfter(now)) {
                        response.setRemainingTime(Duration.between(now, contest.getEndTime()));
                    }
                    return response;
                });
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
                .orElseGet(() -> {
                    ContestUserRelation newRel = ContestUserRelation.builder()
                            .contest(contest)
                            .user(submission.getUser())
                            .role(ContestRole.PARTICIPANT)
                            .build();
                    return contestUserRelationRepository.save(newRel);
                });

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

    @Override
    @Transactional
    public MessageResponse joinProtectedContest(Long contestId, JoinProtectedContestRequest request) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));
        if (passwordEncoder.matches(request.password(), contest.getPassword())) {
            log.info("Contest {} password matched for user {}", contestId, securityUtils.getCurrentUserId());
            Long userId = securityUtils.getCurrentUserId();
            ContestUserRelation userRelation = contestUserRelationRepository.findByContestIdAndUserId(contestId, userId)
                    .orElseGet(() -> ContestUserRelation.builder()
                            .contest(contest)
                            .user(userRepository.findById(userId)
                                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId)))
                            .role(ContestRole.PARTICIPANT)
                            .build());
            userRelation.setRole(ContestRole.PARTICIPANT);
            contestUserRelationRepository.save(userRelation);
            return new MessageResponse("Contest joined successfully.");
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid password.");
    }

    @Override
    public MessageResponse checkProtectedContestMembership(Long userId, Long groupId) {
        if (userGroupRoleRepository.existInGroup(userId, groupId)) {
            return new MessageResponse("User is a member of this group. Forward to contest details.");
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this group. Forward to joinProtectedContest.");
    }
}
