package com.icoder.problem.management.enums;

import com.icoder.core.exception.OnlineJudgeException;

public enum OJudgeType {
    CODEFORCES,
    AT_CODER,
    CSES;

    public static OJudgeType fromString(String value) {
        if (value == null) {
            throw new OnlineJudgeException("onlineJudge cannot be null");
        }

        switch (value.trim().toLowerCase()) {
            case "codeforces":
            case "cf":
                return CODEFORCES;

            case "atcoder":
            case "ac":
                return AT_CODER;

            case "cses":
                return CSES;

            default:
                throw new OnlineJudgeException("Unknown online judge: " + value);
        }
    }
}
