package com.wishlist.service;

import com.wishlist.dto.AuthRequest;
import com.wishlist.dto.SignupRequest;
import com.wishlist.exception.ResourceNotFoundException;
import com.wishlist.exception.UnauthorizedException;
import com.wishlist.model.User;
import com.wishlist.repository.UserRepository;
import com.wishlist.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() throws Exception {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            SecurityContextHolder.setContext(securityContext);
        }
    }

    @Test
    void authenticateUserTest() {
        // given
        var username = "user1";
        var password = "password123";
        var user = User.builder().id(42L).username(username).build();
        var authRequest = new AuthRequest(username, password);

        when(authentication.getName()).thenReturn(username);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("mocked-jwt-token");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // when
        var result = authService.authenticateUser(authRequest);

        // then
        assertNotNull(result);
        assertEquals("mocked-jwt-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(user.getUsername(), result.getUsername());

        verify(authenticationManager).authenticate(any());
        verify(jwtTokenProvider).generateToken(authentication);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void registerUserTest() {
        // given
        var signupRequest = new SignupRequest("newuser", "newuser@example.com", "securePass");
        var encodedPassword = "encodedPassword123";

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("securePass")).thenReturn(encodedPassword);

        var savedUser = User.builder()
                .id(1L)
                .username("newuser")
                .email("newuser@example.com")
                .password(encodedPassword)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        var result = authService.registerUser(signupRequest);

        // then
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getUsername(), result.getUsername());
        assertEquals(savedUser.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getCurrentUserTest() {
        // given
        String email = "test@example.com";
        String username = "user1";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);

        var user = User.builder().id(1L).email(email).username(username).build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // when
        var result = authService.getCurrentUser();

        // then
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    void getCurrentUser_shouldThrow_ifNoAuth() {
        // given
        when(securityContext.getAuthentication()).thenReturn(null);

        // when + then
        assertThrows(UnauthorizedException.class, authService::getCurrentUser, "Unauthorized");
    }

    @Test
    void getCurrentUser_shouldThrow_ifUserNotFound() {
        // given
        String username = "ghostuser";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class, authService::getCurrentUser, "Current user not found");
    }

    @Test
    void authenticateUser_shouldThrow_ifUserNotFound() {
        // given
        var username = "unknown_user";
        var password = "somepass";
        var authRequest = new AuthRequest(username, password);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("token-does-not-matter");
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(authentication.getName()).thenReturn(username);

        // when + then
        var ex = assertThrows(ResourceNotFoundException.class, () -> authService.authenticateUser(authRequest));
        assertEquals("User not found with username: " + username, ex.getMessage());

        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void registerUser_shouldThrow_ifUsernameExists() {
        // given
        var signupRequest = new SignupRequest("existingUser", "email@example.com", "pass");

        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        // when + then
        var ex = assertThrows(IllegalArgumentException.class,
                () -> authService.registerUser(signupRequest));

        assertEquals("Username is already taken!", ex.getMessage());
        verify(userRepository).existsByUsername("existingUser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_shouldThrow_ifEmailExists() {
        // given
        var signupRequest = new SignupRequest("newuser", "taken@example.com", "pass");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // when + then
        var ex = assertThrows(IllegalArgumentException.class,
                () -> authService.registerUser(signupRequest));

        assertEquals("Email is already in use!", ex.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("taken@example.com");
        verify(userRepository, never()).save(any());
    }

}