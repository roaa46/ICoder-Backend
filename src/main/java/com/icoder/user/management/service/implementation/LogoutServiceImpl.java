package com.icoder.user.management.service.implementation;

import com.icoder.user.management.entity.Token;
import com.icoder.user.management.repository.TokenRepository;
import com.icoder.user.management.service.interfaces.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutServiceImpl implements LogoutHandler {
    private final TokenRepository tokenRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.warn(">>> logging out");

        String jwt = resolveToken(request);
        if (jwt == null) return;

        Long userId = tokenRepository.findUserIdByToken(jwt).orElse(null);
        if (userId == null) {
            log.warn("Token not found in DB, skipping logout.");
            return;
        }

        List<Token> userTokens = tokenRepository.findAllValidTokensByUser(userId);

        if (userTokens.isEmpty()) {
            log.warn("Logout called but no valid tokens found for userId={}", userId);
            return;
        }

        userTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(userTokens);

        userTokens.forEach(token -> {
            Duration remaining = jwtService.getRemainingTime(token.getToken());
            String redisKey = "jwt:token:" + token.getToken();
            if (!remaining.isZero()) {
                redisTemplate.opsForValue().set(redisKey, "REVOKED", remaining);
            } else {
                redisTemplate.delete(redisKey); // already expired, just clean up
            }
        });

        log.info("User {} logged out — {} token(s) revoked.", userId, userTokens.size());
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "access_token".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}