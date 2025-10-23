package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    // Kiểm tra sản phẩm đã được yêu thích chưa
    boolean existsByUserIdAndProductId(Integer userId, Long productId);
    
    // Lấy danh sách sản phẩm yêu thích của user
    @Query("SELECT f FROM Favorite f JOIN FETCH f.product WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdWithProduct(@Param("userId") Integer userId);
    
    // Lấy favorite theo user và product
    Optional<Favorite> findByUserIdAndProductId(Integer userId, Long productId);
    
    // Đếm số lượng yêu thích của user
    long countByUserId(Integer userId);
    
    // Xóa yêu thích
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.userId = :userId AND f.productId = :productId")
    void deleteByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Long productId);
    
    // Lấy top 5 sản phẩm yêu thích gần nhất
    List<Favorite> findTop5ByUserIdOrderByCreatedAtDesc(Integer userId);
}
