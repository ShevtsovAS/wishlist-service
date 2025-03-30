package com.wishlist.repository;

import com.wishlist.model.User;
import com.wishlist.model.Wish;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WishRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WishRepository wishRepository;

    @Test
    @DisplayName("should find wishes by user id with pagination")
    void findByUserId() {
        var user = persistUser("user1");
        wishRepository.save(Wish.builder().title("A").user(user).build());
        wishRepository.save(Wish.builder().title("B").user(user).build());

        var page = wishRepository.findByUserId(user.getId(), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    @DisplayName("should find completed and pending wishes")
    void findByUserIdCompletedAndPending() {
        var user = persistUser("user2");
        wishRepository.save(Wish.builder().title("Done").user(user).completed(true).build());
        wishRepository.save(Wish.builder().title("Todo").user(user).completed(false).build());

        var done = wishRepository.findByUserIdAndCompletedTrue(user.getId());
        var todo = wishRepository.findByUserIdAndCompletedFalse(user.getId());

        assertEquals(1, done.size());
        assertEquals(1, todo.size());
    }

    @Test
    @DisplayName("should find wish by id and user id")
    void findByIdAndUserId() {
        var user = persistUser("user3");
        var wish = wishRepository.save(Wish.builder().title("Secret").user(user).build());

        var found = wishRepository.findByIdAndUserId(wish.getId(), user.getId());

        assertTrue(found.isPresent());
        assertEquals("Secret", found.get().getTitle());
    }

    @Test
    @DisplayName("should find wishes by category")
    void findByUserIdAndCategory() {
        var user = persistUser("user4");
        wishRepository.save(Wish.builder().title("Shopping").user(user).category("home").build());

        var found = wishRepository.findByUserIdAndCategory(user.getId(), "home");

        assertEquals(1, found.size());
        assertEquals("Shopping", found.getFirst().getTitle());
    }

    @Test
    @DisplayName("should find overdue wishes")
    void findOverdueWishes() {
        var user = persistUser("user5");
        wishRepository.save(Wish.builder().title("Late").user(user).dueDate(LocalDateTime.now().minusDays(2)).completed(false).build());
        wishRepository.save(Wish.builder().title("Done").user(user).dueDate(LocalDateTime.now().minusDays(2)).completed(true).build());

        var overdue = wishRepository.findOverdueWishes(user.getId(), LocalDateTime.now());

        assertEquals(1, overdue.size());
        assertEquals("Late", overdue.getFirst().getTitle());
    }

    @Test
    @DisplayName("should perform full-text search")
    void searchUserWishes() {
        var user = persistUser("user6");
        wishRepository.save(Wish.builder().title("Read book").description("about Java").user(user).build());
        wishRepository.save(Wish.builder().title("Write code").description("Spring Boot project").user(user).build());

        List<Wish> result = wishRepository.searchUserWishes(user.getId(), "java");

        assertEquals(1, result.size());
        assertEquals("Read book", result.getFirst().getTitle());
    }

    private User persistUser(String username) {
        var user = User.builder()
                .username(username)
                .email(username + "@mail.com")
                .password("pw")
                .build();
        return userRepository.save(user);
    }
}