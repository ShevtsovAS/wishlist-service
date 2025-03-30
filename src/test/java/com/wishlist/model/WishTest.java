package com.wishlist.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WishTest {

    @Test
    void markAsCompleted() {
        // given
        Wish wish = Wish.builder()
                .title("Test wish")
                .completed(false)
                .build();

        // when
        wish.markAsCompleted();

        // then
        assertTrue(wish.isCompleted());
        assertNotNull(wish.getCompletedAt());
        assertTrue(wish.getCompletedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}