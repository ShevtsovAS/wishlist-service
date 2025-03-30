package com.wishlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// Authentication Request DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class AuthRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}

