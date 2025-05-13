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

    private static final Long USER_ID = 1L;
    
    @Mock
    private AuthService authService;

    @Mock
    private WishlistService wishlistService;

    @InjectMocks
    private WishlistController wishlistController;

    @BeforeEach
    void setup() {
        User testUser = new User();
        testUser.setId(USER_ID);
        when(authService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    void shouldReturnUserWishesWithPagination() {
        // Arrange
        var wishList = List.of(WishDTO.builder().id(100L).title("Sample Wish").build());
        var wishlistDTO = WishlistDTO.builder()
                .wishes(wishList)
                .totalItems(1L)
                .totalPages(1)
                .currentPage(0)
                .build();
        when(wishlistService.getUserWishes(eq(USER_ID), any(Pageable.class)))
                .thenReturn(wishlistDTO);

        // Act
        var response = wishlistController.getUserWishes(0, 10, "createdAt", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(wishlistDTO, response.getBody());

        // Verify
        verify(authService).getCurrentUser();
        verify(wishlistService).getUserWishes(eq(USER_ID), any(Pageable.class));
    }

    @Test
    void shouldCreateNewWishAndReturnWithId() {
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
        verify(wishlistService).createWish(newWish);
    }

    @Test
    void shouldReturnWishByIdWhenExists() {
        // Arrange
        var wishId = 1L;
        var wish = WishDTO.builder().id(wishId).title("Test Wish").build();
        when(wishlistService.getUserWishById(USER_ID, wishId)).thenReturn(wish);

        // Act
        var response = wishlistController.getWishById(wishId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(wish, response.getBody());

        // Verify
        verify(authService).getCurrentUser();
        verify(wishlistService).getUserWishById(USER_ID, wishId);
    }

    @Test
    void shouldUpdateWishAndReturnUpdatedEntity() {
        // Arrange
        var wishId = 1L;
        var updatedWish = WishDTO.builder().id(wishId).title("Updated").build();
        when(wishlistService.updateWish(eq(wishId), any(WishDTO.class))).thenReturn(updatedWish);

        // Act
        var response = wishlistController.updateWish(wishId, updatedWish);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedWish, response.getBody());

        // Verify
        verify(wishlistService).updateWish(wishId, updatedWish);
    }

    @Test
    void shouldDeleteWishAndReturnNoContent() {
        // Arrange
        var wishId = 1L;
        doNothing().when(wishlistService).deleteWish(wishId);

        // Act
        var response = wishlistController.deleteWish(wishId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        // Verify
        verify(wishlistService).deleteWish(wishId);
    }

    @Test
    void shouldMarkWishAsCompletedAndReturnUpdatedWish() {
        // Arrange
        var wishId = 1L;
        var completedWish = WishDTO.builder().id(wishId).completed(true).build();
        when(wishlistService.markWishAsCompleted(wishId)).thenReturn(completedWish);

        // Act
        var response = wishlistController.markWishAsCompleted(wishId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isCompleted());

        // Verify
        verify(wishlistService).markWishAsCompleted(wishId);
    }

    @Test
    void shouldReturnAllCompletedWishesForCurrentUser() {
        // Arrange
        var completed = List.of(WishDTO.builder().completed(true).build());
        when(wishlistService.getCompletedWishes(USER_ID)).thenReturn(completed);

        // Act
        var response = wishlistController.getCompletedWishes();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().getFirst().isCompleted());

        // Verify
        verify(authService).getCurrentUser();
        verify(wishlistService).getCompletedWishes(USER_ID);
    }

    @Test
    void shouldReturnAllPendingWishesForCurrentUser() {
        // Arrange
        var pending = List.of(WishDTO.builder().completed(false).build());
        when(wishlistService.getPendingWishes(USER_ID)).thenReturn(pending);

        // Act
        var response = wishlistController.getPendingWishes();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().getFirst().isCompleted());

        // Verify
        verify(authService).getCurrentUser();
        verify(wishlistService).getPendingWishes(USER_ID);
    }

    @Test
    void shouldReturnWishesByCategory() {
        // Arrange
        var category = "Books";
        var wishes = List.of(WishDTO.builder().category(category).build());
        when(wishlistService.getWishesByCategory(category, USER_ID)).thenReturn(wishes);

        // Act
        var response = wishlistController.getWishesByCategory(category);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(category, response.getBody().getFirst().getCategory());

        // Verify
        verify(authService).getCurrentUser();
        verify(wishlistService).getWishesByCategory(category, USER_ID);
    }

    @Test
    void shouldSearchWishesByKeyword() {
        // Arrange
        var keyword = "bike";
        var searchResults = List.of(WishDTO.builder().title(keyword).build());
        when(wishlistService.searchWishes(keyword)).thenReturn(searchResults);

        // Act
        var response = wishlistController.searchWishes(keyword);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(keyword, response.getBody().getFirst().getTitle());

        // Verify
        verify(wishlistService).searchWishes(keyword);
    }
}