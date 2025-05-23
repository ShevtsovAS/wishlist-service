package com.wishlist.controller;

import com.wishlist.dto.WishDTO;
import com.wishlist.dto.WishlistDTO;
import com.wishlist.service.AuthService;
import com.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Wishes", description = "API for managing user's wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get user's wishlist")
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
    @Operation(summary = "Get wish by ID")
    public ResponseEntity<WishDTO> getWishById(@PathVariable Long wishId) {
        var userId = authService.getCurrentUser().getId();
        var wishDTO = wishlistService.getUserWishById(wishId, userId);
        return ResponseEntity.ok(wishDTO);
    }

    @PostMapping
    @Operation(summary = "Create a new wish")
    public ResponseEntity<WishDTO> createWish(@Valid @RequestBody WishDTO wishDTO) {
        WishDTO createdWish = wishlistService.createWish(wishDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWish);
    }

    @PutMapping("/{wishId}")
    @Operation(summary = "Update existing wish")
    public ResponseEntity<WishDTO> updateWish(
            @PathVariable Long wishId,
            @Valid @RequestBody WishDTO wishDTO) {
        var updatedWish = wishlistService.updateWish(wishId, wishDTO);
        return ResponseEntity.ok(updatedWish);
    }

    @DeleteMapping("/{wishId}")
    @Operation(summary = "Delete a wish")
    public ResponseEntity<Void> deleteWish(@PathVariable Long wishId) {
        wishlistService.deleteWish(wishId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{wishId}/complete")
    @Operation(summary = "Mark wish as completed")
    public ResponseEntity<WishDTO> markWishAsCompleted(@PathVariable Long wishId) {
        var completedWish = wishlistService.markWishAsCompleted(wishId);
        return ResponseEntity.ok(completedWish);
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed wishes")
    public ResponseEntity<List<WishDTO>> getCompletedWishes() {
        var userId = authService.getCurrentUser().getId();
        var completedWishes = wishlistService.getCompletedWishes(userId);
        return ResponseEntity.ok(completedWishes);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending wishes")
    public ResponseEntity<List<WishDTO>> getPendingWishes() {
        var userId = authService.getCurrentUser().getId();
        var pendingWishes = wishlistService.getPendingWishes(userId);
        return ResponseEntity.ok(pendingWishes);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get wishes by category")
    public ResponseEntity<List<WishDTO>> getWishesByCategory(@PathVariable String category) {
        var userId = authService.getCurrentUser().getId();
        var wishesByCategory = wishlistService.getWishesByCategory(category, userId);
        return ResponseEntity.ok(wishesByCategory);
    }

    @GetMapping("/search")
    @Operation(summary = "Search wishes by keyword")
    public ResponseEntity<List<WishDTO>> searchWishes(@RequestParam String term) {
        var searchResults = wishlistService.searchWishes(term);
        return ResponseEntity.ok(searchResults);
    }
}