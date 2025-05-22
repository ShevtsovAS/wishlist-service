package com.wishlist.controller;

import com.wishlist.dto.AuthRequest;
import com.wishlist.dto.AuthResponse;
import com.wishlist.dto.SignupRequest;
import com.wishlist.dto.UserDTO;
import com.wishlist.model.User;
import com.wishlist.service.AuthService;
import com.wishlist.service.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for user registration, login and user data retrieval")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    @Operation(
        summary = "Login",
        description = "Authenticate user and get JWT token. This endpoint doesn't require authorization."
    )
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.authenticateUser(authRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    @Operation(
        summary = "Register a new user",
        description = "Create a new user account. This endpoint doesn't require authorization."
    )
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        authService.registerUser(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get current user data",
        description = "Retrieve information about the currently authenticated user. Requires JWT token.",
        security = @SecurityRequirement(name = "JWT Authentication")
    )
    public ResponseEntity<UserDTO> getCurrentUser() {
        User user = authService.getCurrentUser();
        UserDTO userDTO = userMapper.toDto(user);
        return ResponseEntity.ok(userDTO);
    }
}