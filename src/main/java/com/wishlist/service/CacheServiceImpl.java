package com.wishlist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    public static final String WISH_CACHE_NAME = "wish";
    public static final String USER_WISHES_CACHE_NAME = "userWishes";

    private final CacheManager cacheManager;

    @Override
    public void evictUserWishesCache(Long userId) {
        Optional.ofNullable(cacheManager.getCache(USER_WISHES_CACHE_NAME))
                .orElseThrow(() -> new IllegalStateException("Couldn't create cache " + USER_WISHES_CACHE_NAME))
                .evict(userId);
    }

    @Override
    public void evictWishCache(Long wishId) {
        Optional.ofNullable(cacheManager.getCache(WISH_CACHE_NAME))
                .orElseThrow(() -> new IllegalStateException("Couldn't create cache " + WISH_CACHE_NAME))
                .evict(wishId);
    }

    @Override
    public void evictAllCaches() {
        cacheManager.getCacheNames().forEach(this::clearCache);
    }

    private void clearCache(String cacheName) {
        Optional.ofNullable(cacheManager.getCache(cacheName)).ifPresent(Cache::clear);
    }
}
