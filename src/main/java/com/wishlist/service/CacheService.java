package com.wishlist.service;

public interface CacheService {

    void evictUserWishesCache(Long userId);

    void evictWishCache(Long wishId);

    @SuppressWarnings("unused")
    void evictAllCaches();
}