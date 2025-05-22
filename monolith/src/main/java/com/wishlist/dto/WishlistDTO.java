package com.wishlist.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class WishlistDTO {

    private List<WishDTO> wishes;
    private long totalItems;
    private int totalPages;
    private int currentPage;
}
