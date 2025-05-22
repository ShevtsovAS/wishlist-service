package com.wishlist.service;

import com.wishlist.dto.WishDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    @Value("${spring.cache.redis.time-to-live:600000}")
    private long timeToLive;

    public static final String WISH_CACHE_NAME = "wish";
    public static final String COMPLETED_WISHES_CACHE_NAME = "completedWishes";
    public static final String PENDING_WISHES_CACHE_NAME = "pendingWishes";
    public static final String CATEGORY_WISHES_CACHE_NAME = "categoryWishes";
    public static final String USER_WISHES_CACHE_NAME = "userWishes";

    private final CacheManager cacheManager;
    private final RedisTemplate<String, WishDTO> wishRedisTemplate;

    @Override
    public void cacheUserWishes(Long userId, List<WishDTO> wishes, Sort sort) {
        if (!wishes.isEmpty()) {
            var sortOrder = getUserWishesSortOrder(sort);
            var key = buildKey(userId, sortOrder.getProperty(), sortOrder.getDirection());
            wishRedisTemplate.delete(key);
            wishRedisTemplate.opsForList().rightPushAll(key, wishes);
            wishRedisTemplate.expire(key, Duration.ofMillis(timeToLive));
        }
    }

    @Override
    public List<WishDTO> getUserWishesPage(Long userId, Pageable pageable) {
        var sortOrder = getUserWishesSortOrder(pageable.getSort());
        var key = buildKey(userId, sortOrder.getProperty(), sortOrder.getDirection());

        var start = pageable.getOffset();
        var end = start + pageable.getPageSize() - 1;

        var result = wishRedisTemplate.opsForList().range(key, start, end);
        return result != null ? result : List.of();
    }

    @Override
    public long getUserWishesTotalCount(Long userId, Sort sort) {
        var sortOrder = getUserWishesSortOrder(sort);
        var key = buildKey(userId, sortOrder.getProperty(), sortOrder.getDirection());
        var size = wishRedisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }

    @Override
    public void evictUserWishesCache(Long userId) {
        var keys = wishRedisTemplate.keys(String.format("%s::%s::*", USER_WISHES_CACHE_NAME, userId));
        if (!keys.isEmpty()) {
            wishRedisTemplate.delete(keys);
        }
    }

    @Override
    public void evictUserCompletedWishesCache(Long userId) {
        Optional.ofNullable(cacheManager.getCache(COMPLETED_WISHES_CACHE_NAME))
                .orElseThrow(() -> new IllegalStateException("Couldn't create cache " + COMPLETED_WISHES_CACHE_NAME))
                .evict(userId);
    }

    @Override
    public void evictUserPendingWishesCache(Long userId) {
        Optional.ofNullable(cacheManager.getCache(PENDING_WISHES_CACHE_NAME))
                .orElseThrow(() -> new IllegalStateException("Couldn't create cache " + PENDING_WISHES_CACHE_NAME))
                .evict(userId);
    }

    @Override
    public void evictUserCategoryWishesCache(Long userId) {
        Optional.ofNullable(cacheManager.getCache(CATEGORY_WISHES_CACHE_NAME))
                .orElseThrow(() -> new IllegalStateException("Couldn't create cache " + CATEGORY_WISHES_CACHE_NAME))
                .evict(userId);
    }

    @Override
    public void evictWishCache(Long wishId, Long userId) {
        Optional.ofNullable(cacheManager.getCache(WISH_CACHE_NAME))
                .orElseThrow(() -> new IllegalStateException("Couldn't create cache " + WISH_CACHE_NAME))
                .evict(wishId + "::" + userId);
    }

    @Override
    public void evictAllCaches() {
        cacheManager.getCacheNames().forEach(this::clearCache);
    }

    private void clearCache(String cacheName) {
        Optional.ofNullable(cacheManager.getCache(cacheName)).ifPresent(Cache::clear);
    }

    private static Sort.Order getUserWishesSortOrder(Sort pageable) {
        return pageable.stream().findFirst()
                .orElse(new Sort.Order(Sort.Direction.ASC, "createdAt"));
    }

    private String buildKey(Long userId, String sortBy, Sort.Direction direction) {
        return String.format("%s::%s::sort=%s::%s", USER_WISHES_CACHE_NAME, userId, sortBy, direction.name().toLowerCase(Locale.ENGLISH));
    }
}
