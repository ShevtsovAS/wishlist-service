package com.wishlist.service;

public interface CacheService {

    void evictUserWishesCache(Long userId);

    void evictWishCache(Long wishId, Long userId);

    @SuppressWarnings("unused")
    void evictAllCaches();
}