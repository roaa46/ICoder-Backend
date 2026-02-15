package com.icoder.submission.management.repository;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.entity.BotAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotAccountRepository extends JpaRepository<BotAccount, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) // to handle race condition
    Optional<BotAccount> findFirstByJudgeTypeAndActiveTrueAndInUseFalseOrderByLastUsedAtAsc(OJudgeType type);

    boolean existsByUsernameAndJudgeType(String username, OJudgeType judgeType);
}