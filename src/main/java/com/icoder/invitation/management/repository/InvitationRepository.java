package com.icoder.invitation.management.repository;

import com.icoder.invitation.management.entity.Invitation;
import com.icoder.invitation.management.enums.InvitationStatus;
import com.icoder.invitation.management.enums.InvitationType;
import com.icoder.user.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long>{
    Optional<Invitation> findByToken(String token);

    @Query("SELECT COUNT(i) > 0 FROM Invitation i WHERE i.targetId = :targetId " +
           "AND i.type = :type AND i.recipient = :recipient " +
           "AND i.status = :status AND i.expiryDate > :now")
    boolean existsPendingInvitation(@Param("targetId") Long targetId,
                                     @Param("type") InvitationType type,
                                     @Param("recipient") User recipient,
                                     @Param("status") InvitationStatus status,
                                     @Param("now") Instant now);
}


