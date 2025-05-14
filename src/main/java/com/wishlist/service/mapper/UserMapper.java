package com.wishlist.service.mapper;

import com.wishlist.dto.UserDTO;
import com.wishlist.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapper {
    
    public UserDTO toDto(User user) {
        return Optional.ofNullable(user).map(this::toUserDto).orElse(null);
    }

    private UserDTO toUserDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
} 