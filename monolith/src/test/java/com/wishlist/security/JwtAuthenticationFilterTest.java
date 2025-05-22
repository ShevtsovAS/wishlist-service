package com.wishlist.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void doFilterInternal_shouldAuthenticateUser_whenValidJwtProvided() throws ServletException, IOException {
        // given
        var jwt = "valid.jwt.token";
        var username = "testuser";

        var userDetails = new User(username, "password", Collections.emptyList());

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwt);
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        when(jwtTokenProvider.validateToken(jwt)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(jwt)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // when
        filter.doFilterInternal(request, response, chain);

        // then
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authentication);
        assertEquals(username, authentication.getName());

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotAuthenticate_whenJwtInvalid() throws ServletException, IOException {
        // given
        var jwt = "invalid.jwt.token";
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwt);
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        when(jwtTokenProvider.validateToken(jwt)).thenReturn(false);

        // when
        filter.doFilterInternal(request, response, chain);

        // then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldSkipAuthentication_whenNoJwtProvided() throws ServletException, IOException {
        // given
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);

        // when
        filter.doFilterInternal(request, response, chain);

        // then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }
}