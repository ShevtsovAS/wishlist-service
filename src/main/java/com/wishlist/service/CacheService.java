package com.wishlist.service;

import com.wishlist.dto.WishDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface CacheService {

    void cacheUserWishes(Long userId, List<WishDTO> wishes, Sort sort);

    List<WishDTO> getUserWishesPage(Long userId, Pageable pageable);

    long getUserWishesTotalCount(Long userId, Sort sort);

    void evictUserWishesCache(Long userId);

    void evictWishCache(Long wishId, Long userId);

    @SuppressWarnings("unused")
    void evictAllCaches();
}