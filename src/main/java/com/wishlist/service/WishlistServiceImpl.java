package com.wishlist.service;

import com.wishlist.dto.WishDTO;
import com.wishlist.dto.WishlistDTO;
import com.wishlist.exception.ResourceNotFoundException;
import com.wishlist.model.User;
import com.wishlist.model.Wish;
import com.wishlist.repository.WishRepository;
import com.wishlist.service.mapper.WishMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wishlist.service.CacheServiceImpl.*;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishRepository wishRepository;
    private final AuthService authService;
    private final CacheService cacheService;
    private final WishMapper wishMapper;

    @Override
    public WishlistDTO getUserWishes(Long userId, Pageable pageable) {
        var userWishes = cacheService.getUserWishesPage(userId, pageable);

        if (userWishes.isEmpty()) {
            updateUserWishesCache(userId, pageable.getSort());
            userWishes = cacheService.getUserWishesPage(userId, pageable);
        }

        var totalItems = cacheService.getUserWishesTotalCount(userId, pageable.getSort());
        return buildResult(userWishes, totalItems, pageable);
    }

    @Override
    @Cacheable(value = WISH_CACHE_NAME, key = "#wishId + '::' + #userId")
    public WishDTO getUserWishById(Long wishId, Long userId) {
        Wish wish = wishRepository.findByIdAndUserId(wishId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wish not found with id: " + wishId));
        return wishMapper.map(wish);
    }

    @Override
    @Transactional
    public WishDTO createWish(WishDTO wishDTO) {
        User currentUser = authService.getCurrentUser();

        Wish wish = wishMapper.map(wishDTO, currentUser);

        Wish savedWish = wishRepository.save(wish);
        cacheService.evictUserWishesCache(currentUser.getId());
        cacheService.evictUserPendingWishesCache(currentUser.getId());
        cacheService.evictUserCategoryWishesCache(currentUser.getId());
        return wishMapper.map(savedWish);
    }

    @Override
    @Transactional
    public WishDTO updateWish(Long wishId, WishDTO wishDTO) {
        User currentUser = authService.getCurrentUser();

        Wish wish = wishRepository.findByIdAndUserId(wishId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wish not found with id: " + wishId));

        wish.setTitle(wishDTO.getTitle());
        wish.setDescription(wishDTO.getDescription());
        wish.setPriority(wishDTO.getPriority());
        wish.setCategory(wishDTO.getCategory());
        wish.setDueDate(wishDTO.getDueDate());

        // Don't update completed status here, use markWishAsCompleted instead
        var updatedWish = getUpdatedWish(wish, currentUser);

        return wishMapper.map(updatedWish);
    }

    private Wish getUpdatedWish(Wish wish, User currentUser) {
        Wish updatedWish = wishRepository.save(wish);

        // Evict caches
        cacheService.evictWishCache(wish.getId(), currentUser.getId());
        cacheService.evictUserWishesCache(currentUser.getId());
        cacheService.evictUserCompletedWishesCache(currentUser.getId());
        cacheService.evictUserPendingWishesCache(currentUser.getId());
        cacheService.evictUserCategoryWishesCache(currentUser.getId());
        return updatedWish;
    }

    @Override
    @Transactional
    public void deleteWish(Long wishId) {
        User currentUser = authService.getCurrentUser();

        Wish wish = wishRepository.findByIdAndUserId(wishId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wish not found with id: " + wishId));

        wishRepository.delete(wish);

        // Evict caches
        cacheService.evictWishCache(wishId, currentUser.getId());
        cacheService.evictUserWishesCache(currentUser.getId());
        if (wish.isCompleted()) {
            cacheService.evictUserCompletedWishesCache(currentUser.getId());
        } else {
            cacheService.evictUserPendingWishesCache(currentUser.getId());
        }
        cacheService.evictUserCategoryWishesCache(currentUser.getId());
    }

    @Override
    @Transactional
    public WishDTO markWishAsCompleted(Long wishId) {
        User currentUser = authService.getCurrentUser();

        Wish wish = wishRepository.findByIdAndUserId(wishId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wish not found with id: " + wishId));

        wish.markAsCompleted();
        var updatedWish = getUpdatedWish(wish, currentUser);

        return wishMapper.map(updatedWish);
    }

    @Override
    @Cacheable(value = COMPLETED_WISHES_CACHE_NAME, key = "#userId")
    public List<WishDTO> getCompletedWishes(Long userId) {
        User currentUser = authService.getCurrentUser();
        return wishRepository.findByUserIdAndCompletedTrue(currentUser.getId()).stream()
                .map(wishMapper::map)
                .toList();
    }

    @Override
    @Cacheable(value = PENDING_WISHES_CACHE_NAME, key = "#userId")
    public List<WishDTO> getPendingWishes(Long userId) {
        User currentUser = authService.getCurrentUser();
        return wishRepository.findByUserIdAndCompletedFalse(currentUser.getId()).stream()
                .map(wishMapper::map)
                .toList();
    }

    @Override
    @Cacheable(value = CATEGORY_WISHES_CACHE_NAME, key = "#category + '::' + #userId")
    public List<WishDTO> getWishesByCategory(String category, Long userId) {
        User currentUser = authService.getCurrentUser();
        return wishRepository.findByUserIdAndCategory(currentUser.getId(), category).stream()
                .map(wishMapper::map)
                .toList();
    }

    @Override
    public List<WishDTO> searchWishes(String searchTerm) {
        User currentUser = authService.getCurrentUser();
        return wishRepository.searchUserWishes(currentUser.getId(), searchTerm).stream()
                .map(wishMapper::map)
                .toList();
    }

    private static WishlistDTO buildResult(List<WishDTO> wishes, long totalItems, Pageable pageable) {
        var pageSize = pageable.getPageSize();
        var currentPage = pageable.getPageNumber();
        var totalPages = (int) Math.ceil((double) totalItems / pageSize);

        return WishlistDTO.builder()
                .wishes(wishes)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .build();
    }

    private void updateUserWishesCache(Long userId, Sort sort) {
        var allUserWishesSorted = wishRepository.findByUserId(userId, Pageable.unpaged(sort)).stream()
                .map(wishMapper::map)
                .toList();
        cacheService.cacheUserWishes(userId, allUserWishesSorted, sort);
    }

}