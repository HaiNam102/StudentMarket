package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Entities.Favorite;
import com.postgresql.StudentMarket.Entities.Product;
import com.postgresql.StudentMarket.Repository.FavoriteRepository;
import com.postgresql.StudentMarket.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ProductRepository productRepository;

    // Thêm sản phẩm vào yêu thích
    @Transactional
    public boolean addToFavorites(Integer userId, Long productId) {
        // Kiểm tra sản phẩm có tồn tại không
        Optional<Product> product = productRepository.findById(productId);
        if (!product.isPresent()) {
            return false;
        }

        // Kiểm tra đã yêu thích chưa
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            return false; // Đã yêu thích rồi
        }

        // Thêm vào yêu thích
        Favorite favorite = new Favorite(userId, productId);
        favoriteRepository.save(favorite);
        return true;
    }

    // Xóa sản phẩm khỏi yêu thích
    @Transactional
    public boolean removeFromFavorites(Integer userId, Long productId) {
        if (!favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            return false; // Chưa yêu thích
        }

        favoriteRepository.deleteByUserIdAndProductId(userId, productId);
        return true;
    }

    // Toggle yêu thích (thêm nếu chưa có, xóa nếu đã có)
    @Transactional
    public boolean toggleFavorite(Integer userId, Long productId) {
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            // Đã yêu thích -> xóa khỏi yêu thích
            removeFromFavorites(userId, productId);
            return false; // Trả về false vì sau khi xóa, sản phẩm không còn được yêu thích
        } else {
            // Chưa yêu thích -> thêm vào yêu thích
            addToFavorites(userId, productId);
            return true; // Trả về true vì sau khi thêm, sản phẩm được yêu thích
        }
    }

    // Kiểm tra sản phẩm có được yêu thích không
    public boolean isFavorite(Integer userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    // Lấy danh sách sản phẩm yêu thích
    public List<Favorite> getFavoritesByUserId(Integer userId) {
        return favoriteRepository.findByUserIdWithProduct(userId);
    }

    // Lấy số lượng yêu thích
    public long getFavoriteCount(Integer userId) {
        return favoriteRepository.countByUserId(userId);
    }

    // Lấy danh sách sản phẩm yêu thích (chỉ sản phẩm)
    public List<Product> getFavoriteProducts(Integer userId) {
        List<Favorite> favorites = getFavoritesByUserId(userId);
        return favorites.stream()
                .map(Favorite::getProduct)
                .toList();
    }

    // Lấy top 5 sản phẩm yêu thích gần nhất
    public List<Product> getTop5FavoriteProductsForUser(Integer userId) {
        List<Favorite> favorites = favoriteRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        return favorites.stream()
                .map(favorite -> {
                    // Lazy load product nếu cần
                    if (favorite.getProduct() == null) {
                        return productRepository.findById(favorite.getProductId()).orElse(null);
                    }
                    return favorite.getProduct();
                })
                .filter(product -> product != null)
                .toList();
    }
}
