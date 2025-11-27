package com.icoder.group.management.entity;

import com.icoder.core.enums.ContestCoordinatorType;
import com.icoder.core.enums.Visibility;
import com.icoder.user.management.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, updatable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContestCoordinatorType contestCoordinatorType;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String pictureUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private User owner;
}
