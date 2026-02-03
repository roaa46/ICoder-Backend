package com.icoder.contest.management.util;

import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.dto.ProblemSetRequest;
import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.entity.ContestProblemRelation;
import com.icoder.contest.management.enums.ContestOpenness;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.enums.ContestType;
import com.icoder.contest.management.repository.ContestRepository;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.enums.GroupRole;
import com.icoder.group.management.enums.Visibility;
import com.icoder.group.management.repository.UserGroupRoleRepository;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContestUtils {
    private final UserGroupRoleRepository userGroupRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProblemRepository problemRepository;
    private final ContestRepository contestRepository;

    public boolean isUserContestCoordinator(Long userId, Group group) {
        return userGroupRoleRepository.findRoleByUserIdAndGroupId(userId, group.getId())
                .map(role -> switch (group.getContestCoordinatorType()) {
                    case ALL_MEMBERS -> true;
                    case LEADER -> role == GroupRole.OWNER;
                    case LEADER_MANAGER -> role == GroupRole.OWNER || role == GroupRole.MANAGER;
                    default -> false;
                }).orElse(false);
    }

    public Instant parseInstant(String beginTime) {
        if (beginTime == null || beginTime.isBlank()) {
            return null;
        }
        return Instant.parse(beginTime);
    }

    public Duration parseDuration(String length) {
        if (length == null || !length.contains(":")) return Duration.ZERO;
        try {
            String[] parts = length.split(":");
            return Duration.ofHours(Long.parseLong(parts[0]))
                    .plusMinutes(Long.parseLong(parts[1]))
                    .plusSeconds(Long.parseLong(parts[2]));
        } catch (Exception e) {
            return Duration.ZERO;
        }
    }

    public void applyContestRulesBasedOnGroupVisibility(Contest contest, Group group, String rawPassword) {
        if (group.getVisibility() == Visibility.PUBLIC) {
            // Rule: Public Group -> Contest Type MUST be CLASSICAL
            contest.setContestType(ContestType.CLASSICAL);

            // Rule: Openness must be PUBLIC or PROTECTED
            if (contest.getContestOpenness() == ContestOpenness.PRIVATE) {
                throw new IllegalArgumentException("Public groups cannot have PRIVATE contests. Use PROTECTED or PUBLIC.");
            }

            // Rule: If PROTECTED, must have a password (encrypted)
            if (contest.getContestOpenness() == ContestOpenness.PROTECTED) {
                if (rawPassword == null || rawPassword.isBlank()) {
                    throw new IllegalArgumentException("Password is required for PROTECTED contests.");
                }
                contest.setPassword(passwordEncoder.encode(rawPassword));
            } else {
                contest.setPassword(null);
            }

        } else { // Visibility.PRIVATE
            contest.setContestType(ContestType.GROUP);
            contest.setContestOpenness(ContestOpenness.PRIVATE);
            contest.setPassword(null);
        }
    }

    public void checkGroupVisibility(Group group, CreateContestRequest request) {
        if (group.getVisibility() == Visibility.PRIVATE && (request.getContestOpenness() == ContestOpenness.PROTECTED
                || request.getContestOpenness() == ContestOpenness.PUBLIC)) {
            throw new IllegalArgumentException("Private groups cannot have PROTECTED or PUBLIC contests.");
        } else if (group.getVisibility() == Visibility.PUBLIC && (request.getContestOpenness() == ContestOpenness.PRIVATE)) {
            throw new IllegalArgumentException("Public groups cannot have PRIVATE contests.");
        }

        if (group.getVisibility() == Visibility.PRIVATE && request.getContestType() == ContestType.CLASSICAL)
            throw new IllegalArgumentException("Private groups cannot have CLASSICAL contests.");
        else if (group.getVisibility() == Visibility.PUBLIC && request.getContestType() == ContestType.GROUP)
            throw new IllegalArgumentException("Public groups cannot have GROUP contests.");
    }

    public ContestStatus calculateStatus(Instant beginTime, Duration length) {
        Instant now = Instant.now();

        if (beginTime == null || length == null) {
            throw new IllegalArgumentException("Begin time and length must not be null");
        }

        Instant endTime = beginTime.plus(length);

        if (now.isBefore(beginTime)) {
            return ContestStatus.SCHEDULED;
        }

        if (now.isBefore(endTime)) {
            return ContestStatus.RUNNING;
        }

        return ContestStatus.ENDED;
    }

    public Set<ContestProblemRelation> mapProblemSetToRelations(Set<ProblemSetRequest> problemSet, Contest contest) {
        Set<Long> problemIds = problemSet.stream()
                .map(p -> p.getProblemId())
                .collect(Collectors.toSet());

        Map<Long, Problem> problemMap = problemRepository.findAllById(problemIds).stream()
                .collect(Collectors.toMap(Problem::getId, p -> p));

        return problemSet.stream()
                .map(pReq -> {
                    Long pId = pReq.getProblemId();
                    Problem problem = Optional.ofNullable(problemMap.get(pId))
                            .orElseThrow(() -> new ResourceNotFoundException("Problem not found: " + pId));

                    int weight = parseWeight(pReq.getProblemWeight());

                    return ContestProblemRelation.builder()
                            .contest(contest)
                            .problem(problem)
                            .problemWeight(weight)
                            .problemAlias(pReq.getProblemAlias())
                            .build();
                }).collect(Collectors.toSet());
    }

    private int parseWeight(String weightStr) {
        if (weightStr == null || weightStr.isBlank()) return 1;
        try {
            int w = Integer.parseInt(weightStr);
            return Math.max(w, 1);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid weight format");
        }
    }

    public void validateContestRules(CreateContestRequest request, Group group) {
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

    public boolean isContestInGroup(Long contestId, Long groupId) {
        if (contestRepository.existsByIdAndGroupId(contestId, groupId))
            return true;
        throw new IllegalArgumentException("Contest not found in group");
    }

    public Group getGroup(Long contestId) {
        return contestRepository.findById(contestId)
                .map(Contest::getGroup)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));
    }

    public void validateAccessWithRole(Contest contest, boolean isCoordinator) {
        Instant now = Instant.now();

        if (now.isBefore(contest.getBeginTime()) && !isCoordinator) {
            throw new AccessDeniedException("The contest hasn't started yet! Only coordinators can view it now.");
        }
    }

    public boolean checkIfContestRunning(Contest contest) {
        Instant now = Instant.now();
        return now.isBefore(contest.getEndTime());
    }

    public boolean isUserContestOwner(Long userId, Long contestId) {
        return contestRepository.findById(contestId)
                .map(contest -> contest.getGroup().getOwnerId().equals(userId))
                .orElse(false);
    }
}
