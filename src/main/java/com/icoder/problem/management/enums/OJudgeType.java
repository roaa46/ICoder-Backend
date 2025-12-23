package com.icoder.problem.management.enums;

import com.icoder.core.exception.OnlineJudgeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum OJudgeType {
    CODEFORCES,
    AT_CODER,
    CSES;

    public static OJudgeType fromString(String value) {
        if (value == null) {
            log.warn("onlineJudge cannot be null");
            throw new OnlineJudgeException("onlineJudge cannot be null");
        }

        return switch (value.trim().toLowerCase()) {
            case "codeforces", "cf" -> CODEFORCES;
            case "atcoder", "ac" -> AT_CODER;
            case "cses" -> CSES;
            default -> throw new OnlineJudgeException("Unknown online judge: " + value);
        };
    }
}
