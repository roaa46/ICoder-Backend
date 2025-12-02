package com.icoder.core.util;

import com.icoder.core.security.CustomUserDetails;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenHelper {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ValidatedTokenResult validateAndExtract(String token) {
        String handle = jwtService.extractUserHandle(token);
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!jwtService.isTokenValid(token, new CustomUserDetails(
                user.getId(),
                user.getHandle(),
                user.getPassword(),
                user.isVerified()
        ))) {
            throw new IllegalStateException("Invalid or expired token");
        }

        String type = jwtService.extractClaim(token, claims -> (String) claims.get("type"));
        return new ValidatedTokenResult(user, type);
    }

    public record ValidatedTokenResult(User user, String type) {
    }
}
