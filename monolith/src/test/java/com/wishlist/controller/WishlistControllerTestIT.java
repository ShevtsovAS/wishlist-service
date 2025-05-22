package com.wishlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wishlist.dto.WishDTO;
import com.wishlist.dto.WishlistDTO;
import com.wishlist.exception.ResourceNotFoundException;
import com.wishlist.model.User;
import com.wishlist.security.JwtTokenProvider;
import com.wishlist.service.AuthService;
import com.wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc(addFilters = false)
class WishlistControllerTestIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private WishlistService wishlistService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setup() {
        var user = new User();
        user.setId(1L);
        when(authService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void getUserWishesTest() throws Exception {
        var wish = WishDTO.builder().id(1L).title("Test Wish").build();
        var wishlistDTO = WishlistDTO.builder()
                .wishes(List.of(wish))
                .totalItems(1L)
                .totalPages(1)
                .currentPage(0)
                .build();

        when(wishlistService.getUserWishes(anyLong(), any())).thenReturn(wishlistDTO);

        mockMvc.perform(get("/wishes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.wishes[0].title").value("Test Wish"));

        verify(wishlistService, times(1)).getUserWishes(eq(1L), any(Pageable.class));
    }

    @Test
    void createWishTest() throws Exception {
        var newWish = WishDTO.builder().title("New Wish").build();
        var savedWish = WishDTO.builder().id(5L).title("New Wish").build();

        when(wishlistService.createWish(any(WishDTO.class))).thenReturn(savedWish);

        mockMvc.perform(post("/wishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newWish)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("New Wish"));

        verify(wishlistService, times(1)).createWish(newWish);
    }

    @Test
    void getWishByIdTest() throws Exception {
        var wish = WishDTO.builder().id(2L).title("By ID").build();
        when(wishlistService.getUserWishById(2L, 1L)).thenReturn(wish);

        mockMvc.perform(get("/wishes/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("By ID"));

        verify(wishlistService).getUserWishById(2L, 1L);
    }

    @Test
    void updateWishTest() throws Exception {
        var updatedWish = WishDTO.builder().id(3L).title("Updated Wish").build();
        when(wishlistService.updateWish(anyLong(), any())).thenReturn(updatedWish);

        mockMvc.perform(put("/wishes/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedWish)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.title").value("Updated Wish"));

        verify(wishlistService).updateWish(3L, updatedWish);
    }

    @Test
    void deleteWishTest() throws Exception {
        mockMvc.perform(delete("/wishes/4"))
                .andExpect(status().isNoContent());

        verify(wishlistService).deleteWish(4L);
    }

    @Test
    void markWishAsCompletedTest() throws Exception {
        var completed = WishDTO.builder().id(5L).completed(true).build();
        when(wishlistService.markWishAsCompleted(5L)).thenReturn(completed);

        mockMvc.perform(patch("/wishes/5/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        verify(wishlistService).markWishAsCompleted(5L);
    }

    @Test
    void getCompletedWishesTest() throws Exception {
        var wishes = List.of(WishDTO.builder().completed(true).build());
        when(wishlistService.getCompletedWishes(1L)).thenReturn(wishes);

        mockMvc.perform(get("/wishes/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].completed").value(true));

        verify(wishlistService).getCompletedWishes(1L);
    }

    @Test
    void getPendingWishesTest() throws Exception {
        var wishes = List.of(WishDTO.builder().completed(false).build());
        when(wishlistService.getPendingWishes(1L)).thenReturn(wishes);

        mockMvc.perform(get("/wishes/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].completed").value(false));

        verify(wishlistService).getPendingWishes(1L);
    }

    @Test
    void getWishesByCategoryTest() throws Exception {
        var wishes = List.of(WishDTO.builder().category("Books").build());
        when(wishlistService.getWishesByCategory("Books", 1L)).thenReturn(wishes);

        mockMvc.perform(get("/wishes/category/Books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Books"));

        verify(wishlistService).getWishesByCategory("Books", 1L);
    }

    @Test
    void searchWishes_returnsList() throws Exception {
        var wishes = List.of(WishDTO.builder().title("bike").build());
        when(wishlistService.searchWishes("bike")).thenReturn(wishes);

        mockMvc.perform(get("/wishes/search")
                        .param("term", "bike"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("bike"));

        verify(wishlistService).searchWishes("bike");
    }

    @Test
    void getWishByIdNotFoundReturns404() throws Exception {
        var wishId = 999L;
        when(wishlistService.getUserWishById(wishId, 1L)).thenThrow(new ResourceNotFoundException("Wish not found with id: " + wishId));

        mockMvc.perform(get("/wishes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWishBlankTitleReturnsBadRequest() throws Exception {
        WishDTO invalidWish = WishDTO.builder().title("").build();

        mockMvc.perform(post("/wishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidWish)))
                .andExpect(status().isBadRequest());
    }

}