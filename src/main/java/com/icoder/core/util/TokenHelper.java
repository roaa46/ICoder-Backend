package com.icoder.core.util;

import com.icoder.core.security.CustomUserDetails;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.implementation.JwtServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenHelper {
    private final JwtServiceImpl jwtServiceImpl;
    private final UserRepository userRepository;

    public ValidatedTokenResult validateAndExtract(String token) {
        String handle = jwtServiceImpl.extractUserHandle(token);
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!jwtServiceImpl.isTokenValid(token, new CustomUserDetails(user))) {
            throw new IllegalStateException("Invalid or expired token");
        }

        String type = jwtServiceImpl.extractClaim(token, claims -> (String) claims.get("type"));
        return new ValidatedTokenResult(user, type);
    }

    public record ValidatedTokenResult(User user, String type) {
    }
}
