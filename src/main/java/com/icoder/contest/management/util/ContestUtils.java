package com.icoder.contest.management.util;

import com.icoder.group.management.entity.Group;
import com.icoder.group.management.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
public class ContestUtils {
    private final GroupRepository groupRepository;
    public boolean isUserContestCoordinator(Long userId, Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        return false;
    }
}
