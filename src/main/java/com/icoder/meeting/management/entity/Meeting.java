package com.icoder.meeting.management.entity;

import com.icoder.contest.management.entity.Contest;
import com.icoder.core.entity.BaseEntity;
import com.icoder.group.management.entity.Group;
import com.icoder.meeting.management.enums.MeetingStatus;
import com.icoder.meeting.management.enums.MeetingType;
import com.icoder.user.management.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "meetings", indexes = {
        @Index(
                name = "group_status_idx",
                columnList = "group_id,status"
        ),
        @Index(
                name = "room_idx",
                columnList = "room_name",
                unique = true
        ),
        @Index(
                name = "contest_type_status_idx",
                columnList = "contest_id,type,status"
        )
})
public class Meeting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status;

    @Column(nullable = false)
    private boolean official;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Column(name = "scheduled_start_time")
    private Instant scheduledStartTime;

    private Instant endedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private Contest relatedContest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}
