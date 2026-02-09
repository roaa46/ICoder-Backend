package com.icoder.submission.management.service.interfaces;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.entity.OnlineJudgeAccount;

public interface OnlineJudgeAccountService {
    OnlineJudgeAccount getAvailableAccount(OJudgeType judgeType);
    void releaseAccount(Long accountId);
    void addAccount(OnlineJudgeAccount account);
    void deleteAccount(Long id);
}
