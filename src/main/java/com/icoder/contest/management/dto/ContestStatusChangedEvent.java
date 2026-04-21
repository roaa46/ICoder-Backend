package com.icoder.contest.management.dto;

import com.icoder.contest.management.enums.ContestStatus;

public record ContestStatusChangedEvent(Long contestId, ContestStatus newStatus) {}
