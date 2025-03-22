package com.wishlist.service;

import com.wishlist.dto.AuthRequest;
import com.wishlist.dto.AuthResponse;
import com.wishlist.dto.SignupRequest;
import com.wishlist.model.User;

public interface AuthService {

    AuthResponse authenticateUser(AuthRequest authRequest);

    @SuppressWarnings("UnusedReturnValue")
    User registerUser(SignupRequest signupRequest);

    User getCurrentUser();
}