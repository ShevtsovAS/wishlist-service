package com.wishlist.service.mapper;

import com.wishlist.dto.WishDTO;
import com.wishlist.model.User;
import com.wishlist.model.Wish;
import org.springframework.stereotype.Component;

@Component
public class WishMapperImpl implements WishMapper {

    @Override
    public Wish map(WishDTO wishDTO, User user) {
        return Wish.builder()
                .title(wishDTO.getTitle())
                .description(wishDTO.getDescription())
                .priority(wishDTO.getPriority())
                .category(wishDTO.getCategory())
                .dueDate(wishDTO.getDueDate())
                .user(user)
                .build();
    }

    @Override
    public WishDTO map(Wish wish) {
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
