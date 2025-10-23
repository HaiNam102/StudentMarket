package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Dto.ProductDTO;
import com.postgresql.StudentMarket.Entities.Product;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.UserRepository;
import com.postgresql.StudentMarket.Services.FavoriteService;
import com.postgresql.StudentMarket.Utils.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserRepository userRepository;

    // API để toggle yêu thích
    @PostMapping("/api/favorites/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestParam Long productId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            response.put("success", false);
            response.put("message", "Bạn cần đăng nhập để thêm vào yêu thích!");
            return ResponseEntity.ok(response);
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        if (email == null) {
            response.put("success", false);
            response.put("message", "Không tìm thấy thông tin người dùng!");
            return ResponseEntity.ok(response);
        }

        // Lấy userId từ email (cần implement logic này)
        Integer userId = getUserIdFromEmail(email);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Không tìm thấy người dùng!");
            return ResponseEntity.ok(response);
        }

        try {
            boolean isFavorite = favoriteService.toggleFavorite(userId, productId);
            
        // Lấy danh sách favorites cập nhật để trả về cho frontend (hiển thị toàn bộ)
        List<Product> favorites = favoriteService.getFavoriteProducts(userId);
        List<ProductDTO> favoriteDTOs = favorites.stream()
            .map(ProductMapper::toDTO)
            .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("isFavorite", isFavorite);
            response.put("favorites", favoriteDTOs);
            // include total count so frontend can display the real number (not only top5)
            long total = favoriteService.getFavoriteCount(userId);
            response.put("total", total);
            response.put("message", isFavorite ? "Đã thêm vào yêu thích!" : "Đã xóa khỏi yêu thích!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // API để kiểm tra trạng thái yêu thích
    @GetMapping("/api/favorites/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkFavorite(
            @RequestParam Long productId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            response.put("isFavorite", false);
            return ResponseEntity.ok(response);
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        if (email == null) {
            response.put("isFavorite", false);
            return ResponseEntity.ok(response);
        }

        Integer userId = getUserIdFromEmail(email);
        if (userId == null) {
            response.put("isFavorite", false);
            return ResponseEntity.ok(response);
        }

        boolean isFavorite = favoriteService.isFavorite(userId, productId);
        response.put("isFavorite", isFavorite);
        
        return ResponseEntity.ok(response);
    }

    // API để lấy danh sách yêu thích cho dropdown
    @GetMapping("/api/favorites/dropdown")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFavoritesForDropdown(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            response.put("success", false);
            response.put("favorites", List.of());
            return ResponseEntity.ok(response);
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        if (email == null) {
            response.put("success", false);
            response.put("favorites", List.of());
            return ResponseEntity.ok(response);
        }

        Integer userId = getUserIdFromEmail(email);
        if (userId == null) {
            response.put("success", false);
            response.put("favorites", List.of());
            return ResponseEntity.ok(response);
        }

        try {
        // Return full favorites (not limited to top5) so dropdown shows all
        List<Product> favorites = favoriteService.getFavoriteProducts(userId);
        List<ProductDTO> favoriteDTOs = favorites.stream()
            .map(ProductMapper::toDTO)
            .collect(Collectors.toList());

        long total = favoriteService.getFavoriteCount(userId);

        response.put("success", true);
        response.put("favorites", favoriteDTOs);
        response.put("total", total);
        } catch (Exception e) {
            response.put("success", false);
            response.put("favorites", List.of());
            response.put("total", 0);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // Trang tổng quan yêu thích
    @GetMapping("/tongquan")
    public String tongquan(Authentication authentication, Model model) {
        try {
            // Tạm thời cho phép truy cập mà không cần đăng nhập để test
            if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
                model.addAttribute("favoriteProducts", new ArrayList<>());
                model.addAttribute("favoriteCount", 0);
                model.addAttribute("isLoggedIn", false);
                return "tongquan";
            }

            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            
            if (email == null) {
                model.addAttribute("favoriteProducts", new ArrayList<>());
                model.addAttribute("favoriteCount", 0);
                model.addAttribute("isLoggedIn", false);
                return "tongquan";
            }

            Integer userId = getUserIdFromEmail(email);
            if (userId == null) {
                model.addAttribute("favoriteProducts", new ArrayList<>());
                model.addAttribute("favoriteCount", 0);
                model.addAttribute("isLoggedIn", false);
                return "tongquan";
            }

            List<Product> favorites = favoriteService.getFavoriteProducts(userId);
            model.addAttribute("favoriteProducts", favorites);
            model.addAttribute("favoriteCount", favorites.size());
            model.addAttribute("isLoggedIn", true);
        } catch (Exception e) {
            // Log error và trả về trang lỗi
            System.err.println("Error in tongquan: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("favoriteProducts", new ArrayList<>());
            model.addAttribute("favoriteCount", 0);
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "tongquan";
    }


    // Helper method để lấy userId từ email
    private Integer getUserIdFromEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(User::getUserId).orElse(null);
    }
}
