package com.icoder.core.security;

public class SecurityConstants {

    public static final String[] PUBLIC_URLS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/verify",
            "/api/v1/auth/verify/send",
            "/api/v1/auth/password/forget/**",
            "/api/v1/auth/password/reset/**",
            "/api/v1/users/email/confirm",
            "/api/v1/users/delete/confirm",
            "/api/v1/users",
            "/api/v1/coding/editor/language",
            "/api/v1/coding/editor/languages",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/uploads/**"
    };

    private SecurityConstants() {
    }
}