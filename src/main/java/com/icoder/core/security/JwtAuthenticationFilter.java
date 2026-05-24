package com.icoder.core.security;

import com.icoder.user.management.repository.TokenRepository;
import com.icoder.user.management.service.interfaces.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (shouldSkipFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = resolveToken(request);

        log.info("JWT token: {}", jwtToken != null ? "[PRESENT]" : "[ABSENT]");

        if (jwtToken != null) {
            processAuthentication(jwtToken, request);
        }

        filterChain.doFilter(request, response);
    }

    /* ----- Helpers ----- */

    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/register") ||
                path.equals("/api/v1/auth/verify") ||
                path.startsWith("/api/v1/auth/password/forget") ||
                path.startsWith("/api/v1/auth/password/reset");
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

    private void processAuthentication(String jwtToken, HttpServletRequest request) {
        String username = jwtService.extractUserHandle(jwtToken);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (isTokenValidInSystem(jwtToken, userDetails)) {
                setSecurityContext(userDetails, request);
            }
        }
    }

    private boolean isTokenValidInSystem(String token, UserDetails userDetails) {
        String redisKey = "jwt:token:" + token;

        String cachedStatus = redisTemplate.opsForValue().get(redisKey);
        log.info("Checking Redis for key: {}, found: {}", redisKey, cachedStatus);

        if ("REVOKED".equals(cachedStatus)) {
            return false;
        } else if ("VALID".equals(cachedStatus)) {
            return jwtService.isTokenValid(token, userDetails);
        }

        // DB fallback
        boolean isDbValid = tokenRepository.findByToken(token)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);

        if (!isDbValid) return false;

        boolean isJwtValid = jwtService.isTokenValid(token, userDetails);
        if (!isJwtValid) return false;

        Duration ttl = jwtService.getRemainingTime(token);
        if (ttl.isZero()) return false;

        // Only cache after full validation passes
        redisTemplate.opsForValue().set(redisKey, "VALID", ttl);
        return true;
    }

    private void setSecurityContext(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}