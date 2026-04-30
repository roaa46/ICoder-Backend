package com.icoder.problem.management.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.icoder.core.exception.OnlineJudgeException;

public enum OJudgeType {
    CODEFORCES,
    GYM,
    AT_CODER,
    CSES;

    @JsonCreator
    public static OJudgeType fromString(String value) {
        if (value == null || value.trim().isBlank()) {
            throw new OnlineJudgeException("onlineJudge cannot be null or empty");
        }

        return switch (value.trim().toLowerCase()) {
            case "codeforces", "cf" -> CODEFORCES;
            case "gym" -> GYM;
            case "atcoder", "ac" -> AT_CODER;
            case "cses" -> CSES;
            default -> throw new OnlineJudgeException("Unknown online judge: " + value);
        };
    }
}