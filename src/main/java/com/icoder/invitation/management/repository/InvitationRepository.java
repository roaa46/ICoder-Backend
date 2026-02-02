package com.icoder.invitation.management.repository;

import com.icoder.invitation.management.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long>{
    Optional<Invitation> findByToken(String token);
}
