package com.icoder.submission.management.dto;

import com.icoder.submission.management.entity.BotAccount;

public record SubmissionContext(BotAccount account, String sessionId) {
}
