package com.wishlist.security;

import com.wishlist.model.User;
import com.wishlist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsername() {
        var user = User.builder()
                .id(1L)
                .username("johndoe")
                .password("hashedpassword")
                .build();

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("johndoe");

        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        assertEquals("hashedpassword", result.getPassword());
        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        var exception = assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("unknown"));

        assertEquals("User not found with username: unknown", exception.getMessage());
    }

}