package com.wishlist.service;

import com.wishlist.dto.WishDTO;
import com.wishlist.exception.ResourceNotFoundException;
import com.wishlist.model.User;
import com.wishlist.model.Wish;
import com.wishlist.repository.WishRepository;
import com.wishlist.service.mapper.WishMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class WishlistServiceImplTest {

    @Mock
    private WishRepository wishRepository;

    @Mock
    private AuthService authService;

    @Mock
    private CacheService cacheService;

    @Spy
    WishMapperImpl wishMapper;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private User mockUser;

    @BeforeEach
    void setUp() throws Exception {
        try (AutoCloseable ignored = openMocks(this)) {
            mockUser = new User();
            mockUser.setId(1L);
            when(authService.getCurrentUser()).thenReturn(mockUser);
        }
    }

    @Test
    void createWishTest() {
        // given
        var wishDTO = WishDTO.builder()
                .title("Test Wish")
                .description("Description")
                .priority(1)
                .category("Books")
                .dueDate(LocalDateTime.now().plusDays(5))
                .build();

        var savedWish = wishMapper.map(wishDTO, mockUser);
        savedWish.setId(1L);

        // Arrange
        when(wishRepository.save(any(Wish.class))).thenReturn(savedWish);

        // when
        var result = wishlistService.createWish(wishDTO);

        // then
        assertNotNull(result);
        assertEquals("Test Wish", result.getTitle());
        verify(wishRepository, times(1)).save(any(Wish.class));
        verify(cacheService, times(1)).evictUserWishesCache(mockUser.getId());
        verify(cacheService, times(1)).evictUserPendingWishesCache(mockUser.getId());
        verify(cacheService, times(1)).evictUserCategoryWishesCache(mockUser.getId());
    }

    @Test
    void getUserWishesTest() {
        // given
        var wish = Wish.builder()
                .id(1L)
                .title("Read book")
                .completed(false)
                .user(mockUser)
                .build();
        var pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        var wishes = List.of(wish);
        Page<Wish> page = new PageImpl<>(wishes, pageable, 1);
        var expected = wishes.stream().map(wishMapper::map).toList();

        // Arrange
        when(cacheService.getUserWishesPage(mockUser.getId(), pageable)).thenReturn(List.of()).thenReturn(expected);
        when(wishRepository.findByUserId(eq(mockUser.getId()), eq(Pageable.unpaged(pageable.getSort())))).thenReturn(page);
        when(cacheService.getUserWishesTotalCount(mockUser.getId(), pageable.getSort())).thenReturn(1L);

        // when
        var result = wishlistService.getUserWishes(mockUser.getId(), pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalItems());
        assertEquals(1, result.getWishes().size());
        assertEquals("Read book", result.getWishes().getFirst().getTitle());
        verify(wishRepository, times(1)).findByUserId(mockUser.getId(), Pageable.unpaged(pageable.getSort()));
    }

    @Test
    void getUserWishByIdTest() {
        // given
        var wishId = 10L;
        var wish = Wish.builder()
                .id(wishId)
                .title("My wish")
                .user(mockUser)
                .build();

        var wishDTO = wishMapper.map(wish);

        when(wishRepository.findByIdAndUserId(wishId, mockUser.getId())).thenReturn(Optional.of(wish));
        doReturn(wishDTO).when(wishMapper).map(wish);

        // when
        var result = wishlistService.getUserWishById(wishId, mockUser.getId());

        // then
        assertNotNull(result);
        assertEquals(wishDTO.getId(), result.getId());
        assertEquals(wishDTO.getTitle(), result.getTitle());

        verify(wishRepository, times(1)).findByIdAndUserId(wishId, mockUser.getId());
        verify(wishMapper, times(2)).map(wish); // first invocation in this test
    }

    @Test
    void updateWishTest() {
        // given
        var wishId = 15L;
        var updateDTO = WishDTO.builder()
                .id(wishId)
                .title("Updated wish")
                .description("Updated description")
                .priority(2)
                .category("Books")
                .dueDate(LocalDateTime.now().plusDays(10))
                .completed(true)
                .build();

        var existingWish = Wish.builder()
                .id(wishId)
                .title("Old wish")
                .description("Old description")
                .priority(1)
                .category("Old")
                .dueDate(LocalDateTime.now().plusDays(5))
                .completed(false)
                .user(mockUser)
                .build();

        var updatedWish = Wish.builder()
                .id(wishId)
                .title(updateDTO.getTitle())
                .description(updateDTO.getDescription())
                .priority(updateDTO.getPriority())
                .category(updateDTO.getCategory())
                .dueDate(updateDTO.getDueDate())
                .completed(updateDTO.isCompleted())
                .user(mockUser)
                .build();

        var updatedDTO = wishMapper.map(updatedWish);

        when(wishRepository.findByIdAndUserId(wishId, mockUser.getId())).thenReturn(Optional.of(existingWish));
        when(wishRepository.save(any(Wish.class))).thenReturn(updatedWish);
        doReturn(updatedDTO).when(wishMapper).map(updatedWish);

        // when
        var result = wishlistService.updateWish(wishId, updateDTO);

        // then
        assertNotNull(result);
        assertEquals(updateDTO.getTitle(), result.getTitle());
        assertEquals(updateDTO.getDescription(), result.getDescription());

        verify(wishRepository).findByIdAndUserId(wishId, mockUser.getId());
        verify(wishRepository).save(any(Wish.class));
        verify(cacheService).evictUserWishesCache(mockUser.getId());
        verify(cacheService).evictUserCompletedWishesCache(mockUser.getId());
        verify(cacheService).evictUserPendingWishesCache(mockUser.getId());
        verify(cacheService).evictUserCategoryWishesCache(mockUser.getId());
    }

    @Test
    void deleteWishTest() {
        // given
        var wishId = 22L;
        var wish = Wish.builder()
                .id(wishId)
                .title("To Delete")
                .completed(true) // Important for verifying which caches are evicted
                .user(mockUser)
                .build();

        when(wishRepository.findByIdAndUserId(wishId, mockUser.getId())).thenReturn(Optional.of(wish));
        doNothing().when(wishRepository).delete(wish);

        // when
        wishlistService.deleteWish(wishId);

        // then
        verify(wishRepository).findByIdAndUserId(wishId, mockUser.getId());
        verify(wishRepository).delete(wish);
        verify(cacheService).evictUserWishesCache(mockUser.getId());
        verify(cacheService).evictUserCompletedWishesCache(mockUser.getId());
        verify(cacheService, never()).evictUserPendingWishesCache(mockUser.getId()); // wish is completed
        verify(cacheService).evictUserCategoryWishesCache(mockUser.getId());
    }

    @Test
    void markWishAsCompletedTest() {
        // given
        var wishId = 99L;
        var wish = Wish.builder()
                .id(wishId)
                .title("Complete Me")
                .completed(false)
                .user(mockUser)
                .build();

        var completedWish = Wish.builder()
                .id(wishId)
                .title("Complete Me")
                .completed(true)
                .completedAt(LocalDateTime.now())
                .user(mockUser)
                .build();

        var completedDTO = wishMapper.map(completedWish);

        when(wishRepository.findByIdAndUserId(wishId, mockUser.getId())).thenReturn(Optional.of(wish));
        when(wishRepository.save(any(Wish.class))).thenReturn(completedWish);
        doReturn(completedDTO).when(wishMapper).map(completedWish);

        // when
        var result = wishlistService.markWishAsCompleted(wishId);

        // then
        assertNotNull(result);
        assertTrue(result.isCompleted());
        assertEquals(wishId, result.getId());
        verify(wishRepository).findByIdAndUserId(wishId, mockUser.getId());
        verify(wishRepository).save(any(Wish.class));
        verify(cacheService).evictUserWishesCache(mockUser.getId());
        verify(cacheService).evictUserCompletedWishesCache(mockUser.getId());
        verify(cacheService).evictUserPendingWishesCache(mockUser.getId());
        verify(cacheService).evictUserCategoryWishesCache(mockUser.getId());
    }

    @Test
    void getCompletedWishesTest() {
        // given
        var wish = Wish.builder()
                .id(1L)
                .title("Completed Wish")
                .completed(true)
                .user(mockUser)
                .build();

        when(wishRepository.findByUserIdAndCompletedTrue(mockUser.getId())).thenReturn(List.of(wish));

        // when
        var result = wishlistService.getCompletedWishes(mockUser.getId());

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Completed Wish", result.getFirst().getTitle());

        verify(wishRepository).findByUserIdAndCompletedTrue(mockUser.getId());
    }

    @Test
    void getPendingWishesTest() {
        // given
        var wish = Wish.builder()
                .id(2L)
                .title("Pending Wish")
                .completed(false)
                .user(mockUser)
                .build();

        when(wishRepository.findByUserIdAndCompletedFalse(mockUser.getId())).thenReturn(List.of(wish));

        // when
        var result = wishlistService.getPendingWishes(mockUser.getId());

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pending Wish", result.getFirst().getTitle());

        verify(wishRepository).findByUserIdAndCompletedFalse(mockUser.getId());
    }

    @Test
    void getWishesByCategoryTest() {
        // given
        var category = "Health";
        var wish = Wish.builder()
                .id(3L)
                .title("Go to gym")
                .category(category)
                .user(mockUser)
                .build();

        when(wishRepository.findByUserIdAndCategory(mockUser.getId(), category)).thenReturn(List.of(wish));

        // when
        var result = wishlistService.getWishesByCategory(category, mockUser.getId());

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Go to gym", result.getFirst().getTitle());

        verify(wishRepository).findByUserIdAndCategory(mockUser.getId(), category);
    }

    @Test
    void searchWishesTest() {
        // given
        var searchTerm = "learn";
        var wish = Wish.builder()
                .id(4L)
                .title("Learn Spring Boot")
                .description("Master advanced features")
                .user(mockUser)
                .build();

        when(wishRepository.searchUserWishes(mockUser.getId(), searchTerm)).thenReturn(List.of(wish));

        // when
        var result = wishlistService.searchWishes(searchTerm);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Learn Spring Boot", result.getFirst().getTitle());

        verify(wishRepository).searchUserWishes(mockUser.getId(), searchTerm);
    }

    @Test
    void getUserWishById_shouldThrow_ifWishNotFound() {
        // given
        Long wishId = 404L;
        when(wishRepository.findByIdAndUserId(wishId, mockUser.getId())).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class,
                () -> wishlistService.getUserWishById(wishId, mockUser.getId()));

        verify(wishRepository).findByIdAndUserId(wishId, mockUser.getId());
    }

    @Test
    void updateWish_shouldThrow_ifWishNotFound() {
        // given
        Long wishId = 999L;
        WishDTO updateDTO = WishDTO.builder().id(wishId).title("Updated").build();

        when(wishRepository.findByIdAndUserId(wishId, mockUser.getId())).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class,
                () -> wishlistService.updateWish(wishId, updateDTO));

        verify(wishRepository).findByIdAndUserId(wishId, mockUser.getId());
    }

    @Test
    void deleteWish_shouldThrow_ifWishNotFound() {
        // given
        Long wishId = 123L;
        when(wishRepository.findByIdAndUserId(wishId, mockUser.getId())).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class,
                () -> wishlistService.deleteWish(wishId));

        verify(wishRepository).findByIdAndUserId(wishId, mockUser.getId());
    }

    @Test
    void markWishAsCompleted_shouldThrow_ifWishNotFound() {
        // given
        Long wishId = 888L;
        when(wishRepository.findByIdAndUserId(wishId, mockUser.getId())).thenReturn(Optional.empty());

        // when + then
        assertThrows(ResourceNotFoundException.class,
                () -> wishlistService.markWishAsCompleted(wishId));

        verify(wishRepository).findByIdAndUserId(wishId, mockUser.getId());
    }

}