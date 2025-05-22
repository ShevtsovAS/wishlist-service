package com.wishlist.service;

import com.wishlist.dto.WishDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class CacheServiceImplTest {

    @Mock
    private RedisTemplate<String, WishDTO> wishRedisTemplate;

    @Mock
    private ListOperations<String, WishDTO> listOperations;

    @Mock
    private ValueOperations<String, WishDTO> valueOperations;

    @InjectMocks
    private CacheServiceImpl cacheService;

    @BeforeEach
    void setUp() throws Exception {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            when(wishRedisTemplate.opsForList()).thenReturn(listOperations);
            when(wishRedisTemplate.opsForValue()).thenReturn(valueOperations);
        }
    }

    @Test
    void cacheUserWishes() {
        var userId = 42L;
        var wish1 = WishDTO.builder().id(1L).priority(1).title("A").build();
        var wish2 = WishDTO.builder().id(2L).priority(2).title("B").build();
        var wishes = List.of(wish1, wish2);

        var wishListOps = mock(ListOperations.class);
        when(wishRedisTemplate.opsForList()).thenReturn(wishListOps);

        var sort = Sort.by(Sort.Order.asc("priority"));

        cacheService.cacheUserWishes(userId, wishes, sort);

        var expectedKey = "userWishes::42::sort=priority::asc";
        verify(wishRedisTemplate).delete(expectedKey);
        verify(wishListOps).rightPushAll(expectedKey, wishes);
        verify(wishRedisTemplate).expire(eq(expectedKey), any());
    }

    @Test
    void getUserWishesPage() {
        var userId = 42L;
        var wish1 = WishDTO.builder().id(1L).title("A").build();
        var wish2 = WishDTO.builder().id(2L).title("B").build();
        var allWishes = List.of(wish1, wish2);

        var sort = Sort.by(Sort.Order.asc("priority"));
        var pageable = PageRequest.of(0, 2, sort);

        when(wishRedisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("userWishes::42::sort=priority::asc", 0, 1)).thenReturn(allWishes);

        var result = cacheService.getUserWishesPage(userId, pageable);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getTitle());
        assertEquals("B", result.get(1).getTitle());
    }

    @Test
    void getUserWishesTotalCount() {
        var userId = 42L;
        var sort = Sort.by(Sort.Order.asc("priority"));
        var expectedKey = "userWishes::42::sort=priority::asc";

        when(wishRedisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.size(expectedKey)).thenReturn(10L);

        long count = cacheService.getUserWishesTotalCount(userId, sort);

        assertEquals(10L, count);
        verify(listOperations).size(expectedKey);
    }

    @Test
    void evictUserWishesCache() {
        var userId = 99L;
        var keys = Set.of(
                "userWishes::99::sort=createdAt::asc",
                "userWishes::99::sort=priority::desc"
        );

        when(wishRedisTemplate.keys("userWishes::99::*")).thenReturn(keys);

        cacheService.evictUserWishesCache(userId);

        verify(wishRedisTemplate).delete(keys);
    }

    @Test
    void evictUserCompletedWishesCache() {
        var userId = 5L;
        var mockCache = mock(Cache.class);
        var cacheManager = mock(CacheManager.class);

        CacheService service = new CacheServiceImpl(cacheManager, wishRedisTemplate);

        when(cacheManager.getCache("completedWishes")).thenReturn(mockCache);

        service.evictUserCompletedWishesCache(userId);

        verify(mockCache).evict(userId);
    }

    @Test
    void evictUserPendingWishesCache() {
        var userId = 7L;
        var mockCache = mock(Cache.class);
        var cacheManager = mock(CacheManager.class);

        CacheService service = new CacheServiceImpl(cacheManager, wishRedisTemplate);

        when(cacheManager.getCache("pendingWishes")).thenReturn(mockCache);

        service.evictUserPendingWishesCache(userId);

        verify(mockCache).evict(userId);
    }

    @Test
    void evictUserCategoryWishesCache() {
        var userId = 8L;
        var mockCache = mock(Cache.class);
        var cacheManager = mock(CacheManager.class);

        CacheService service = new CacheServiceImpl(cacheManager, wishRedisTemplate);

        when(cacheManager.getCache("categoryWishes")).thenReturn(mockCache);

        service.evictUserCategoryWishesCache(userId);

        verify(mockCache).evict(userId);
    }

    @Test
    void evictWishCache() {
        var wishId = 10L;
        var userId = 20L;
        var cacheKey = wishId + "::" + userId;

        var mockCache = mock(Cache.class);
        var cacheManager = mock(CacheManager.class);
        CacheService service = new CacheServiceImpl(cacheManager, wishRedisTemplate);

        when(cacheManager.getCache("wish")).thenReturn(mockCache);

        service.evictWishCache(wishId, userId);

        verify(mockCache).evict(cacheKey);
    }

    @Test
    void evictAllCaches() {
        var cache1 = mock(Cache.class);
        var cache2 = mock(Cache.class);
        var cacheManager = mock(CacheManager.class);

        CacheService service = new CacheServiceImpl(cacheManager, wishRedisTemplate);

        when(cacheManager.getCacheNames()).thenReturn(Set.of("cache1", "cache2"));
        when(cacheManager.getCache("cache1")).thenReturn(cache1);
        when(cacheManager.getCache("cache2")).thenReturn(cache2);

        service.evictAllCaches();

        verify(cache1).clear();
        verify(cache2).clear();
    }
}