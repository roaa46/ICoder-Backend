package com.icoder.user.management.service.implementation;

import com.icoder.user.management.entity.Token;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.enums.TokenType;
import com.icoder.user.management.repository.TokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "tokenExpiration", 15 * 60 * 1000L);
        ReflectionTestUtils.setField(tokenService, "refreshTokenExpiration", 7 * 24 * 60 * 60 * 1000L);

        user = new User();
        user.setId(1L);
        user.setHandle("test");
        user.setEmail("test@example.com");
    }

    @Nested
    @DisplayName("revokeAllUserTokens()")
    class RevokeAllUserTokensTests {
        @Test
        @DisplayName("should return without saving when user has no valid tokens")
        void revokeAllUserTokens_shouldReturnWithoutSaving_whenNoValidTokens() {
            when(tokenRepository.findAllValidTokens(user.getId())).thenReturn(List.of());

            tokenService.revokeAllUserTokens(user);

            verify(tokenRepository).findAllValidTokens(user.getId());
            verify(tokenRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("should mark all valid tokens as expired and revoked")
        void revokeAllUserTokens_shouldMarkTokensExpiredAndRevoked() {
            Token token1 = Token.builder()
                    .token("token-1")
                    .isRevoked(false)
                    .isExpired(false)
                    .build();
            Token token2 = Token.builder()
                    .token("token-2")
                    .isRevoked(false)
                    .isExpired(false)
                    .build();
            List<Token> tokens = List.of(token1, token2);

            when(tokenRepository.findAllValidTokens(user.getId())).thenReturn(tokens);

            tokenService.revokeAllUserTokens(user);

            assertTrue(token1.isRevoked());
            assertTrue(token1.isExpired());
            assertTrue(token2.isRevoked());
            assertTrue(token2.isExpired());

            verify(tokenRepository).findAllValidTokens(user.getId());
            verify(tokenRepository).saveAll(tokens);
        }
    }

    @Nested
    @DisplayName("saveUserToken()")
    class SaveUserTokenTests {
        @Test
        @DisplayName("should create and save bearer token for user")
        void saveUserToken_shouldCreateAndSaveToken() {
            String jwtToken = "jwt_token";

            tokenService.saveUserToken(user, jwtToken);

            ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
            verify(tokenRepository).save(tokenCaptor.capture());

            Token savedToken = tokenCaptor.getValue();

            assertNotNull(savedToken);
            assertNotNull(savedToken.getCreatedAt());
            assertEquals(user, savedToken.getUser());
            assertEquals(jwtToken, savedToken.getToken());
            assertEquals(TokenType.BEARER, savedToken.getTokenType());
            assertFalse(savedToken.isRevoked());
            assertFalse(savedToken.isExpired());
        }
    }

    @Nested
    @DisplayName("addTokenCookies()")
    class AddTokenCookiesTests {
        @Test
        @DisplayName("should return when response is null")
        void addTokenCookies_shouldReturn_whenResponseIsNull() {
            String accessToken = "access_token";
            String refreshToken = "refresh_token";

            assertDoesNotThrow(() ->
                    tokenService.addTokenCookies(null, accessToken, refreshToken)
            );

            verifyNoInteractions(response);
        }

        @Test
        @DisplayName("should add access and refresh cookies with correct properties")
        void addTokenCookies_shouldAddAccessAndRefreshCookies() {
            String accessToken = "access_token";
            String refreshToken = "refresh_token";

            tokenService.addTokenCookies(response, accessToken, refreshToken);

            ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
            verify(response, times(2)).addCookie(cookieCaptor.capture());

            List<Cookie> cookies = cookieCaptor.getAllValues();
            assertEquals(2, cookies.size());

            Cookie accessCookie = cookies.get(0);
            Cookie refreshCookie = cookies.get(1);

            assertEquals("access_token", accessCookie.getName());
            assertEquals(accessToken, accessCookie.getValue());
            assertTrue(accessCookie.isHttpOnly());
            assertFalse(accessCookie.getSecure());
            assertEquals("/", accessCookie.getPath());
            assertEquals(900, accessCookie.getMaxAge());

            assertEquals("refresh_token", refreshCookie.getName());
            assertEquals(refreshToken, refreshCookie.getValue());
            assertTrue(refreshCookie.isHttpOnly());
            assertFalse(refreshCookie.getSecure());
            assertEquals("/", refreshCookie.getPath());
            assertEquals(604800, refreshCookie.getMaxAge());
        }
    }
}