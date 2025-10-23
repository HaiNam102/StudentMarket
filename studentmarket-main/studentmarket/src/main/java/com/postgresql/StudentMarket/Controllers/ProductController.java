package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Dto.ProductViewDTO;
import com.postgresql.StudentMarket.Dto.SearchReqDTO;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.UserRepository;
import com.postgresql.StudentMarket.Services.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    // Check sđt không reload trang
    @GetMapping("/api/needs-phone")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> needsPhone(
            @AuthenticationPrincipal OAuth2User principal,
            HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        Integer userId = (Integer) session.getAttribute("user_id");
        User user = null;

        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null && principal != null) {
            String email = principal.getAttribute("email");
            if (email != null) {
                user = userRepository.findByEmail(email).orElse(null);
                if (user != null)
                    session.setAttribute("user_id", user.getUserId());
            }
        }
        if (user == null) {
            // Chưa đăng nhập -> frontend sẽ đẩy sang /login
            resp.put("needLogin", true);
            return ResponseEntity.status(401).body(resp);
        }

        String phone = null;
        try {
            phone = user.getPhone();
        } catch (Exception ignored) {
        }
        if (phone == null || phone.isBlank()) {
            try {
                phone = user.getSdt();
            } catch (Exception ignored) {
            }
        }

        boolean needPhone = (phone == null || phone.isBlank());
        resp.put("needPhone", needPhone);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/dangbai")
    public String showPostForm(Model model,
                               @AuthenticationPrincipal OAuth2User principal,
                               HttpSession session,
                               RedirectAttributes ra,
                               HttpServletRequest request) {
        Integer userId = (Integer) session.getAttribute("user_id");
        User user = null;

        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null && principal != null) {
            String email = principal.getAttribute("email");
            if (email != null) {
                user = userRepository.findByEmail(email).orElse(null);
                if (user != null)
                    session.setAttribute("user_id", user.getUserId());
            }
        }
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập để đăng bài.");
            return "redirect:/login";
        }

        // Lấy phone từ user (tuỳ tên field của bạn)
        String phone = null;
        try {
            phone = user.getPhone();
        } catch (Exception ignored) {
        }
        if (phone == null || phone.isBlank()) {
            try {
                phone = user.getSdt();
            } catch (Exception ignored) {
            }
        }

        if (phone == null || phone.isBlank()) {
            // 👉 Thiếu số ĐT: quay lại trang hiện tại (referer) và bật modal
            String referer = request.getHeader("Referer");
            ra.addFlashAttribute("need_phone", true);
            ra.addFlashAttribute("warn", "Để đăng bài, bạn cần thêm số điện thoại.");
            return "redirect:" + (referer != null ? referer : "/");
        }

        return "dangbai";
    }

    @PostMapping("/dangbai")
    public String handlePost(
            @RequestParam("parent_id") Integer parentId,
            @RequestParam("child_id") Integer childId,
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam(value = "price", required = false) String priceStr,
            @RequestParam("description") String description,
            @RequestParam("province") String province,
            @RequestParam("ward") String ward,
            @RequestParam("address_detail") String addressDetail,
            @RequestParam(value = "cover_name", required = false) String coverName,
            @RequestParam(value = "order_names", required = false) String orderNamesCsv,
            @RequestParam(value = "origin", required = false) String origin,
            @RequestParam(value = "material", required = false) String material,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "accessories", required = false) String accessories,
            @RequestParam(value = "images", required = false) List<MultipartFile> files,
            @RequestParam(value = "is_free", defaultValue = "0") int isFree,
            @AuthenticationPrincipal OAuth2User principal,
            HttpSession session,
            RedirectAttributes ra // <-- thêm RedirectAttributes
    ) throws IOException {

        try {
            // Lấy userId
            Integer userId = (Integer) session.getAttribute("user_id");
            if (userId == null && principal != null) {
                String email = principal.getAttribute("email");
                if (email != null) {
                    User u = userRepository.findByEmail(email).orElse(null);
                    if (u != null)
                        userId = u.getUserId();
                }
            }
            if (userId == null)
                throw new IllegalStateException("Không xác định người dùng.");

            // Giá
            long price = 0L;
            if (isFree == 1) {
                price = 0L;
            } else {
                if (priceStr != null) {
                    String digits = priceStr.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) {
                        try {
                            price = Long.parseLong(digits);
                        } catch (NumberFormatException ex) {
                            throw new IllegalArgumentException("Giá bán không hợp lệ.");
                        }
                    } else {
                        throw new IllegalArgumentException("Vui lòng nhập Giá bán hợp lệ hoặc chọn tặng miễn phí.");
                    }
                } else {
                    throw new IllegalArgumentException("Vui lòng nhập Giá bán hợp lệ hoặc chọn tặng miễn phí.");
                }
            }

            List<String> orderNames = (orderNamesCsv != null && !orderNamesCsv.isBlank())
                    ? Arrays.asList(orderNamesCsv.split("\\s*,\\s*"))
                    : List.of();

            productService.createProduct(
                    userId, parentId, childId, name, type, price, description,
                    province, ward, addressDetail, coverName, orderNames, files,
                    origin, material, color, accessories);

            // ✅ thêm success=1 để JS show toast
            ra.addAttribute("success", 1);
            return "redirect:/dangbai";

        } catch (Exception ex) {
            // đẩy lỗi ra query để JS có thể show toast error
            ra.addAttribute("error", ex.getMessage());
            return "redirect:/dangbai";
        }
    }

    @GetMapping("/searchProduct")
    public String searchProduct(@ModelAttribute("products") SearchReqDTO search,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size);
        var result = productService.searchProducts(search, pageable);

        model.addAttribute("page", result);
        model.addAttribute("size", size);
        return "searchProduct";
    }
}
