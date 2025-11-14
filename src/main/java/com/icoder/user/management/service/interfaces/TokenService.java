package com.icoder.user.management.service.interfaces;

import com.icoder.user.management.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenService {
    void revokeAllUserTokens(User user);

    void saveUserToken(User savedUser, String refreshToken);

    void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken);
}
