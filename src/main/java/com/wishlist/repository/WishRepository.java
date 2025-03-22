package com.wishlist.repository;

import com.wishlist.model.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WishRepository extends JpaRepository<Wish, Long> {

    Page<Wish> findByUserId(Long userId, Pageable pageable);

    List<Wish> findByUserIdAndCompletedFalse(Long userId);

    List<Wish> findByUserIdAndCompletedTrue(Long userId);

    @Query("SELECT w FROM Wish w WHERE w.user.id = :userId AND w.id = :wishId")
    Optional<Wish> findByIdAndUserId(@Param("wishId") Long wishId, @Param("userId") Long userId);

    @Query("SELECT w FROM Wish w WHERE w.user.id = :userId AND w.category = :category")
    List<Wish> findByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    @SuppressWarnings("unused")
    @Query("SELECT w FROM Wish w WHERE w.user.id = :userId AND w.dueDate < :date AND w.completed = false")
    List<Wish> findOverdueWishes(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    @Query("SELECT w FROM Wish w WHERE w.user.id = :userId AND " +
            "(LOWER(w.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(w.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Wish> searchUserWishes(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);
}