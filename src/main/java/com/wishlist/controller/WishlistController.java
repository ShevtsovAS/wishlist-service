package com.wishlist.controller;

import com.wishlist.dto.WishDTO;
import com.wishlist.dto.WishlistDTO;
import com.wishlist.service.AuthService;
import com.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
        var sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        var userId = authService.getCurrentUser().getId();
        var wishlistDTO = wishlistService.getUserWishes(userId, pageable);
        return ResponseEntity.ok(wishlistDTO);
    }

    @GetMapping("/{wishId}")
    public ResponseEntity<WishDTO> getWishById(@PathVariable Long wishId) {
        var userId = authService.getCurrentUser().getId();
        var wishDTO = wishlistService.getUserWishById(wishId, userId);
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
        var updatedWish = wishlistService.updateWish(wishId, wishDTO);
        return ResponseEntity.ok(updatedWish);
    }

    @DeleteMapping("/{wishId}")
    public ResponseEntity<Void> deleteWish(@PathVariable Long wishId) {
        wishlistService.deleteWish(wishId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{wishId}/complete")
    public ResponseEntity<WishDTO> markWishAsCompleted(@PathVariable Long wishId) {
        var completedWish = wishlistService.markWishAsCompleted(wishId);
        return ResponseEntity.ok(completedWish);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<WishDTO>> getCompletedWishes() {
        var userId = authService.getCurrentUser().getId();
        var completedWishes = wishlistService.getCompletedWishes(userId);
        return ResponseEntity.ok(completedWishes);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<WishDTO>> getPendingWishes() {
        var userId = authService.getCurrentUser().getId();
        var pendingWishes = wishlistService.getPendingWishes(userId);
        return ResponseEntity.ok(pendingWishes);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<WishDTO>> getWishesByCategory(@PathVariable String category) {
        var userId = authService.getCurrentUser().getId();
        var wishesByCategory = wishlistService.getWishesByCategory(category, userId);
        return ResponseEntity.ok(wishesByCategory);
    }

    @GetMapping("/search")
    public ResponseEntity<List<WishDTO>> searchWishes(@RequestParam String term) {
        var searchResults = wishlistService.searchWishes(term);
        return ResponseEntity.ok(searchResults);
    }
}