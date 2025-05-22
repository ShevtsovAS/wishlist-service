package com.wishlist.dto;

import lombok.*;

// Authentication Response DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class AuthResponse {

    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long userId;
    private String username;

    @SuppressWarnings("unused")
    public AuthResponse(String accessToken, Long userId, String username) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
    }
}
