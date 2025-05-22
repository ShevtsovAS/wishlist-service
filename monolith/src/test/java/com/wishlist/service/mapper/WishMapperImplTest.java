package com.wishlist.service.mapper;

import com.wishlist.dto.WishDTO;
import com.wishlist.model.User;
import com.wishlist.model.Wish;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WishMapperImplTest {

    private final WishMapper mapper = new WishMapperImpl();

    @Test
    void shouldMapWishToDto() {
        var wish = Wish.builder()
                .id(1L)
                .title("Learn Java")
                .description("Master core concepts")
                .priority(1)
                .category("Education")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        var dto = mapper.map(wish);

        assertNotNull(dto);
        assertEquals(wish.getId(), dto.getId());
        assertEquals(wish.getTitle(), dto.getTitle());
        assertEquals(wish.getDescription(), dto.getDescription());
        assertEquals(wish.getPriority(), dto.getPriority());
        assertEquals(wish.getCategory(), dto.getCategory());
        assertEquals(wish.isCompleted(), dto.isCompleted());
    }

    @Test
    void shouldMapDtoToWish() {
        var user = User.builder().id(1L).build();
        var dto = WishDTO.builder()
                .id(2L)
                .title("Read a book")
                .description("Read Clean Code")
                .priority(2)
                .category("Books")
                .completed(false)
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();

        var wish = mapper.map(dto, user);

        assertNotNull(wish);
        assertEquals(dto.getTitle(), wish.getTitle());
        assertEquals(dto.getDescription(), wish.getDescription());
        assertEquals(dto.getPriority(), wish.getPriority());
        assertEquals(dto.getCategory(), wish.getCategory());
        assertEquals(dto.isCompleted(), wish.isCompleted());
        assertEquals(dto.getDueDate(), wish.getDueDate());
        assertEquals(user, wish.getUser());
    }

}