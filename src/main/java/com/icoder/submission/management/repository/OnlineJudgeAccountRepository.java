package com.icoder.submission.management.repository;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.entity.OnlineJudgeAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface OnlineJudgeAccountRepository extends JpaRepository<OnlineJudgeAccount, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) // بيمنع أي Thread تاني يقرأ الـ Row ده لحد ما الـ Transaction تخلص
    Optional<OnlineJudgeAccount> findFirstByJudgeTypeAndIsActiveTrueAndInUseFalseOrderByLastUsedAtAsc(OJudgeType type);

    boolean existsByUsernameAndJudgeType(String username, OJudgeType judgeType);
}