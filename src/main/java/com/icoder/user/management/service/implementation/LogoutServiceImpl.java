package com.icoder.user.management.service.implementation;

import com.icoder.user.management.repository.TokenRepository;
import com.icoder.user.management.service.interfaces.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutServiceImpl implements LogoutHandler {
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final RedisTemplate redisTemplate;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.warn(">>> logging out");
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7);
        var storedToken = tokenRepository.findByToken(token).orElse(null);
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            Duration ttl = jwtService.getRemainingTime(token);
            if (!ttl.isZero()) {
                redisTemplate.opsForValue().set("jwt:token:" + token, "REVOKED", ttl);
            }
        }
    }
}
