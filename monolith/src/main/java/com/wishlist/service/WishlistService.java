package com.wishlist.service;

import com.wishlist.dto.WishDTO;
import com.wishlist.dto.WishlistDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WishlistService {

    WishlistDTO getUserWishes(Long userId, Pageable pageable);

    WishDTO getUserWishById(Long wishId, Long userId);

    WishDTO createWish(WishDTO wishDTO);

    WishDTO updateWish(Long wishId, WishDTO wishDTO);

    void deleteWish(Long wishId);

    WishDTO markWishAsCompleted(Long wishId);

    List<WishDTO> getCompletedWishes(Long userId);

    List<WishDTO> getPendingWishes(Long userId);

    List<WishDTO> getWishesByCategory(String category, Long userId);

    List<WishDTO> searchWishes(String searchTerm);
}