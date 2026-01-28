package com.icoder.contest.management.util;

import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.enums.ContestOpenness;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.enums.ContestType;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.enums.GroupRole;
import com.icoder.group.management.enums.Visibility;
import com.icoder.group.management.repository.UserGroupRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ContestUtils {
    private final UserGroupRoleRepository userGroupRoleRepository;
    private final PasswordEncoder passwordEncoder;

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
        if (group.getVisibility().name().toUpperCase().matches(Visibility.PUBLIC.name())) {
            // Rule: Public Group -> Contest Type MUST be CLASSICAL
            contest.setContestType(ContestType.CLASSICAL);

            // Rule: Openness must be PUBLIC or PROTECTED
            if (contest.getContestOpenness().name().toUpperCase().matches(ContestOpenness.PRIVATE.name())) {
                throw new IllegalArgumentException("Public groups cannot have PRIVATE contests. Use PROTECTED or PUBLIC.");
            }

            // Rule: If PROTECTED, must have a password (encrypted)
            if (contest.getContestOpenness().name().toUpperCase().matches(ContestOpenness.PROTECTED.name())) {
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

}
