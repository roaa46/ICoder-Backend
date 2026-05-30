
package com.icoder.meeting.management.repository;

import com.icoder.meeting.management.entity.Meeting;
import com.icoder.meeting.management.enums.MeetingStatus;
import com.icoder.meeting.management.enums.MeetingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByGroupId(Long groupId);

    Optional<Meeting> findByRoomName(String roomName);

    boolean existsByRelatedContestIdAndTypeAndStatusIn(
            Long contestId,
            MeetingType type,
            List<MeetingStatus> statuses
    );

    long countByRelatedContestIdAndType(
            Long contestId,
            MeetingType type
    );

    List<Meeting> findAllByStatusAndScheduledStartTimeBefore(
            MeetingStatus status,
            Instant time
    );
}
