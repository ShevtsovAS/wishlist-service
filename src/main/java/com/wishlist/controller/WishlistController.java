package com.wishlist.controller;

import com.wishlist.dto.WishDTO;
import com.wishlist.dto.WishlistDTO;
import com.wishlist.service.AuthService;
import com.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishes")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<WishlistDTO> getUserWishes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        // Get the current user's ID from the authentication service
        Long userId = authService.getCurrentUser().getId();

        WishlistDTO wishlistDTO = wishlistService.getUserWishes(userId, pageable);
        return ResponseEntity.ok(wishlistDTO);
    }

    @GetMapping("/{wishId}")
    public ResponseEntity<WishDTO> getWishById(@PathVariable Long wishId) {
        WishDTO wishDTO = wishlistService.getWishById(wishId);
        return ResponseEntity.ok(wishDTO);
    }

    @PostMapping
    public ResponseEntity<WishDTO> createWish(@Valid @RequestBody WishDTO wishDTO) {
        WishDTO createdWish = wishlistService.createWish(wishDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWish);
    }

    @PutMapping("/{wishId}")
    public ResponseEntity<WishDTO> updateWish(
            @PathVariable Long wishId,
            @Valid @RequestBody WishDTO wishDTO) {

        WishDTO updatedWish = wishlistService.updateWish(wishId, wishDTO);
        return ResponseEntity.ok(updatedWish);
    }

    @DeleteMapping("/{wishId}")
    public ResponseEntity<Void> deleteWish(@PathVariable Long wishId) {
        wishlistService.deleteWish(wishId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{wishId}/complete")
    public ResponseEntity<WishDTO> markWishAsCompleted(@PathVariable Long wishId) {
        WishDTO completedWish = wishlistService.markWishAsCompleted(wishId);
        return ResponseEntity.ok(completedWish);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<WishDTO>> getCompletedWishes() {
        Long userId = authService.getCurrentUser().getId();
        List<WishDTO> completedWishes = wishlistService.getCompletedWishes(userId);
        return ResponseEntity.ok(completedWishes);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<WishDTO>> getPendingWishes() {
        Long userId = authService.getCurrentUser().getId();
        List<WishDTO> pendingWishes = wishlistService.getPendingWishes(userId);
        return ResponseEntity.ok(pendingWishes);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<WishDTO>> getWishesByCategory(@PathVariable String category) {
        Long userId = authService.getCurrentUser().getId();
        List<WishDTO> wishesByCategory = wishlistService.getWishesByCategory(userId, category);
        return ResponseEntity.ok(wishesByCategory);
    }

    @GetMapping("/search")
    public ResponseEntity<List<WishDTO>> searchWishes(@RequestParam String term) {
        Long userId = authService.getCurrentUser().getId();
        List<WishDTO> searchResults = wishlistService.searchWishes(userId, term);
        return ResponseEntity.ok(searchResults);
    }
}