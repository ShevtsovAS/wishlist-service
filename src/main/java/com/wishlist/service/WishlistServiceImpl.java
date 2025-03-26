package com.wishlist.service;

import com.wishlist.dto.WishDTO;
import com.wishlist.dto.WishlistDTO;
import com.wishlist.exception.ResourceNotFoundException;
import com.wishlist.exception.UnauthorizedException;
import com.wishlist.model.User;
import com.wishlist.model.Wish;
import com.wishlist.repository.WishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wishlist.service.CacheServiceImpl.USER_WISHES_CACHE_NAME;
import static com.wishlist.service.CacheServiceImpl.WISH_CACHE_NAME;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishRepository wishRepository;
    private final AuthService authService;
    private final CacheService cacheService;

    @Override
    @Cacheable(value = USER_WISHES_CACHE_NAME, key = "#userId")
    public WishlistDTO getUserWishes(Long userId, Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        if (!currentUser.getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view these wishes");
        }

        Page<Wish> wishPage = wishRepository.findByUserId(userId, pageable);

        List<WishDTO> wishes = wishPage.getContent().stream()
                .map(this::convertToDTO)
                .toList();

        return WishlistDTO.builder()
                .wishes(wishes)
                .totalItems(wishPage.getTotalElements())
                .totalPages(wishPage.getTotalPages())
                .currentPage(pageable.getPageNumber())
                .build();
    }

    @Override
    @Cacheable(value = WISH_CACHE_NAME, key = "#wishId + '_' + #userId")
    public WishDTO getUserWishById(Long wishId, Long userId) {
        Wish wish = wishRepository.findByIdAndUserId(wishId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wish not found with id: " + wishId));
        return convertToDTO(wish);
    }

    @Override
    @Transactional
    public WishDTO createWish(WishDTO wishDTO) {
        User currentUser = authService.getCurrentUser();

        Wish wish = Wish.builder()
                .title(wishDTO.getTitle())
                .description(wishDTO.getDescription())
                .completed(false)
                .priority(wishDTO.getPriority())
                .category(wishDTO.getCategory())
                .dueDate(wishDTO.getDueDate())
                .user(currentUser)
                .build();

        Wish savedWish = wishRepository.save(wish);
        cacheService.evictUserWishesCache(currentUser.getId());
        return convertToDTO(savedWish);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"wish", "userWishes"}, allEntries = true)
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
        Wish updatedWish = wishRepository.save(wish);

        // Evict caches
        cacheService.evictWishCache(wishId);
        cacheService.evictUserWishesCache(currentUser.getId());

        return convertToDTO(updatedWish);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"wish", "userWishes"}, allEntries = true)
    public void deleteWish(Long wishId) {
        User currentUser = authService.getCurrentUser();

        Wish wish = wishRepository.findByIdAndUserId(wishId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wish not found with id: " + wishId));

        wishRepository.delete(wish);

        // Evict caches
        cacheService.evictWishCache(wishId);
        cacheService.evictUserWishesCache(currentUser.getId());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"wish", "userWishes"}, allEntries = true)
    public WishDTO markWishAsCompleted(Long wishId) {
        User currentUser = authService.getCurrentUser();

        Wish wish = wishRepository.findByIdAndUserId(wishId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wish not found with id: " + wishId));

        wish.markAsCompleted();
        Wish updatedWish = wishRepository.save(wish);

        // Evict caches
        cacheService.evictWishCache(wishId);
        cacheService.evictUserWishesCache(currentUser.getId());

        return convertToDTO(updatedWish);
    }

    @Override
    public List<WishDTO> getCompletedWishes() {
        User currentUser = authService.getCurrentUser();
        return wishRepository.findByUserIdAndCompletedTrue(currentUser.getId()).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<WishDTO> getPendingWishes() {
        User currentUser = authService.getCurrentUser();
        return wishRepository.findByUserIdAndCompletedFalse(currentUser.getId()).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<WishDTO> getWishesByCategory(String category) {
        User currentUser = authService.getCurrentUser();
        return wishRepository.findByUserIdAndCategory(currentUser.getId(), category).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<WishDTO> searchWishes(String searchTerm) {
        User currentUser = authService.getCurrentUser();
        return wishRepository.searchUserWishes(currentUser.getId(), searchTerm).stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Helper method to convert Wish entity to WishDTO
    private WishDTO convertToDTO(Wish wish) {
        return WishDTO.builder()
                .id(wish.getId())
                .title(wish.getTitle())
                .description(wish.getDescription())
                .completed(wish.isCompleted())
                .priority(wish.getPriority())
                .category(wish.getCategory())
                .dueDate(wish.getDueDate())
                .completedAt(wish.getCompletedAt())
                .createdAt(wish.getCreatedAt())
                .updatedAt(wish.getUpdatedAt())
                .build();
    }
}