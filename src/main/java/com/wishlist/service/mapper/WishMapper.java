package com.wishlist.service.mapper;

import com.wishlist.dto.WishDTO;
import com.wishlist.model.User;
import com.wishlist.model.Wish;

public interface WishMapper {

    Wish map(WishDTO wishDTO, User user);

    WishDTO map(Wish wish);
}
