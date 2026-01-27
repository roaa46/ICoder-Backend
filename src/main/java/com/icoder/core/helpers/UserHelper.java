package com.icoder.core.helpers;

import com.icoder.core.security.CustomUserDetails;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserHelper {
    private final UserRepository userRepository;

    public Optional<User> findByHandle(String handle) {
        return userRepository.findByHandle(handle);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getId();
    }
}
