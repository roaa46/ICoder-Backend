package com.icoder.user.management.service.implementation;

import com.icoder.user.management.enums.TokenType;
import com.icoder.user.management.entity.Token;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.TokenRepository;
import com.icoder.user.management.service.interfaces.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;
    @Value("${token.expiration}")
    private Long tokenExpiration;
    @Value("${refresh.token.expiration}")
    private Long refreshTokenExpiration;

    @Transactional
    public void revokeAllUserTokens(User user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokens(user.getId());
        if (validUserTokens.isEmpty())
            return;

        validUserTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });

        tokenRepository.saveAll(validUserTokens);
    }

    @Transactional
    public void saveUserToken(User savedUser, String refreshToken) {
        Token token = Token.builder()
                .user(savedUser)
                .token(refreshToken)
                .tokenType(TokenType.BEARER)
                .isExpired(false)
                .isRevoked(false)
                .createdAt(Instant.now())
                .build();
        tokenRepository.save(token);
    }

    public void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        if (response == null)
            return;
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false); // change to true in production
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) (tokenExpiration / 1000));

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); //change to true in production
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (refreshTokenExpiration / 1000));

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }
}
