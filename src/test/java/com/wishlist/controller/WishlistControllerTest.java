package com.wishlist.controller;

import com.wishlist.dto.WishDTO;
import com.wishlist.dto.WishlistDTO;
import com.wishlist.model.User;
import com.wishlist.service.AuthService;
import com.wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WishlistControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private WishlistService wishlistService;

    @InjectMocks
    private WishlistController wishlistController;

    @BeforeEach
    void setup() {
        var user = new User();
        user.setId(1L);
        when(authService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void getUserWishesTest() {
        // Arrange
        var wishList = List.of(WishDTO.builder().id(100L).title("Sample Wish").build());
        var wishlistDTO = WishlistDTO.builder()
                .wishes(wishList)
                .totalItems(1L)
                .totalPages(1)
                .currentPage(0)
                .build();
        when(wishlistService.getUserWishes(eq(1L), any(Pageable.class)))
                .thenReturn(wishlistDTO);

        // Act
        var response = wishlistController.getUserWishes(0, 10, "createdAt", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(wishlistDTO, response.getBody());

        // Verify
        verify(authService, times(1)).getCurrentUser();
        verify(wishlistService, times(1)).getUserWishes(eq(1L), any(Pageable.class));
    }

    @Test
    void createWishTest() {
        // Arrange
        var newWish = WishDTO.builder().title("New Wish").build();
        var savedWish = WishDTO.builder().id(5L).title("New Wish").build();
        when(wishlistService.createWish(newWish)).thenReturn(savedWish);

        // Act
        var response = wishlistController.createWish(newWish);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedWish, response.getBody());

        // Verify
        verify(wishlistService, times(1)).createWish(newWish);
        verify(authService, never()).getCurrentUser();
    }

    @Test
    void getWishByIdTest() {
        // Arrange
        var wish = WishDTO.builder().id(1L).title("Test Wish").build();
        when(wishlistService.getUserWishById(1L, 1L)).thenReturn(wish);

        // Act
        var response = wishlistController.getWishById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(wish, response.getBody());

        // Verify
        verify(wishlistService).getUserWishById(1L, 1L);
    }

    @Test
    void updateWishTest() {
        // Arrange
        var updatedWish = WishDTO.builder().id(1L).title("Updated").build();
        when(wishlistService.updateWish(eq(1L), any(WishDTO.class))).thenReturn(updatedWish);

        // Act
        var response = wishlistController.updateWish(1L, updatedWish);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedWish, response.getBody());

        // Verify
        verify(wishlistService).updateWish(1L, updatedWish);
    }

    @Test
    void deleteWishTest() {
        // Arrange
        doNothing().when(wishlistService).deleteWish(1L);

        // Act
        var response = wishlistController.deleteWish(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify
        verify(wishlistService).deleteWish(1L);
    }

    @Test
    void markWishAsCompletedTest() {
        // Arrange
        var completedWish = WishDTO.builder().id(1L).completed(true).build();
        when(wishlistService.markWishAsCompleted(1L)).thenReturn(completedWish);

        // Act
        var response = wishlistController.markWishAsCompleted(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isCompleted());

        // Verify
        verify(wishlistService).markWishAsCompleted(1L);
    }

    @Test
    void getCompletedWishesTest() {
        // Arrange
        var completed = List.of(WishDTO.builder().completed(true).build());
        when(wishlistService.getCompletedWishes(1L)).thenReturn(completed);

        // Act
        var response = wishlistController.getCompletedWishes();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().getFirst().isCompleted());

        // Verify
        verify(authService).getCurrentUser();
        verify(wishlistService).getCompletedWishes(1L);
    }

    @Test
    void getPendingWishesTest() {
        // Arrange
        var pending = List.of(WishDTO.builder().completed(false).build());
        when(wishlistService.getPendingWishes(1L)).thenReturn(pending);

        // Act
        var response = wishlistController.getPendingWishes();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().getFirst().isCompleted());

        // Verify
        verify(authService).getCurrentUser();
        verify(wishlistService).getPendingWishes(1L);
    }

    @Test
    void getWishesByCategoryTest() {
        // Arrange
        var wishes = List.of(WishDTO.builder().category("Books").build());
        when(wishlistService.getWishesByCategory("Books", 1L)).thenReturn(wishes);

        // Act
        var response = wishlistController.getWishesByCategory("Books");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Books", response.getBody().getFirst().getCategory());

        // Verify
        verify(authService).getCurrentUser();
        verify(wishlistService).getWishesByCategory("Books", 1L);
    }

    @Test
    void searchWishesTest() {
        // Arrange
        var searchResults = List.of(WishDTO.builder().title("bike").build());
        when(wishlistService.searchWishes("bike")).thenReturn(searchResults);

        // Act
        var response = wishlistController.searchWishes("bike");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("bike", response.getBody().getFirst().getTitle());

        // Verify
        verify(wishlistService).searchWishes("bike");
    }

}