package com.wishlist.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "VGhpcy1pczEyMzQ1Njc4OTAta2V5LXNlY3JldC1rZXktZm9yLXRlc3RzLg=="; // base64

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        // Inject values manually
        var jwtSecretField = JwtTokenProvider.class.getDeclaredFields();
        for (var field : jwtSecretField) {
            field.setAccessible(true);
            try {
                if (field.getName().equals("jwtSecret")) field.set(jwtTokenProvider, secret);
                if (field.getName().equals("jwtExpirationMs")) field.set(jwtTokenProvider, 3600000);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void generateToken() {
        UserDetails userDetails = new User("tester", "pass", Collections.emptyList());
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = jwtTokenProvider.generateToken(auth);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("tester", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void getUsernameFromToken() {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        String token = Jwts.builder()
                .subject("mockuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(key)
                .compact();

        assertEquals("mockuser", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void validateToken() {
        String invalidToken = "this.is.not.a.valid.jwt";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void validateToken_shouldReturnFalse_forExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secret));
        String expiredToken = Jwts.builder()
                .subject("expireduser")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 5_000))
                .signWith(key)
                .compact();

        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

}