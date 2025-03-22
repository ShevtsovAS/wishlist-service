package com.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {

    private List<WishDTO> wishes;
    private long totalItems;
    private int totalPages;
    private int currentPage;
}
