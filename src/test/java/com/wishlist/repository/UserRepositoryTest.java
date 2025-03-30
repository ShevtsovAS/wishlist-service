package com.wishlist.repository;

import com.wishlist.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("should find user by username")
    void findByUsername() {
        var user = User.builder()
                .username("john")
                .email("john@example.com")
                .password("1234")
                .build();

        userRepository.save(user);

        var found = userRepository.findByUsername("john");

        assertTrue(found.isPresent());
        assertEquals("john@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("should find user by email")
    void findByEmail() {
        var user = User.builder()
                .username("emma")
                .email("emma@example.com")
                .password("pw123")
                .build();

        userRepository.save(user);

        var found = userRepository.findByEmail("emma@example.com");

        assertTrue(found.isPresent());
        assertEquals("emma", found.get().getUsername());
    }

    @Test
    @DisplayName("should return true if username exists")
    void existsByUsername() {
        userRepository.save(User.builder().username("alice").email("a@a.com").password("pass").build());

        boolean exists = userRepository.existsByUsername("alice");

        assertTrue(exists);
    }

    @Test
    @DisplayName("should return true if email exists")
    void existsByEmail() {
        userRepository.save(User.builder().username("bob").email("bob@example.com").password("pw").build());

        boolean exists = userRepository.existsByEmail("bob@example.com");

        assertTrue(exists);
    }
}