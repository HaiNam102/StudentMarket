// src/main/java/com/postgresql/StudentMarket/Controllers/ProductDetailController.java
package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Entities.Product;
import com.postgresql.StudentMarket.Entities.ProductImage;
import com.postgresql.StudentMarket.Entities.ProductSpecs;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Entities.Address;
import com.postgresql.StudentMarket.Repository.ProductRepository;
import com.postgresql.StudentMarket.Repository.ProductImageRepository;
import com.postgresql.StudentMarket.Repository.ProductSpecsRepository;
import com.postgresql.StudentMarket.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSpecsRepository productSpecsRepository;
    private final UserRepository userRepository;

    // ===== Helpers trạng thái hoạt động (chuẩn 1 phút) =====
    private boolean isOnline1m(LocalDateTime lastSeen) {
        return lastSeen != null &&
               Duration.between(lastSeen, LocalDateTime.now()).getSeconds() < 60; // < 1 phút
    }

    private String humanizeLastSeen(LocalDateTime lastSeen) {
        if (lastSeen == null) return "Chưa từng hoạt động";
        Duration d = Duration.between(lastSeen, LocalDateTime.now());
        long sec = d.getSeconds();
        if (sec < 60) return "Vừa xong";
        long min = sec / 60;
        if (min < 60) return "Hoạt động " + min + " phút trước";
        long hrs = min / 60;
        if (hrs < 24) return "Hoạt động " + hrs + " giờ trước";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "Hoạt động " + lastSeen.format(fmt);
    }

    @GetMapping("/chitietbaidang/{id}")
    public String viewProductDetail(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));

        // Ảnh chính & gallery
        List<ProductImage> images =
                productImageRepository.findByProduct_ProductIdOrderByDisplayOrderAsc(id);
        ProductImage coverImage = images.stream().filter(ProductImage::isCover).findFirst()
                .orElseGet(() -> images.isEmpty() ? null : images.get(0));

        // Specs & địa chỉ
        ProductSpecs specs = productSpecsRepository.findById(id).orElse(null);
        Address address = product.getAddress(); // mapping 1-1

        // ====== THÔNG TIN NGƯỜI BÁN ======
        User seller = null;
        Integer sellerId = null;
        try {
            sellerId = product.getUserId();
        } catch (Exception ignore) { /* nếu field khác tên, đổi cho đúng */ }

        if (sellerId != null) {
            seller = userRepository.findById(sellerId).orElse(null);
        }

        long sellerTotalPosted = 0L;
        if (sellerId != null) {
            try {
                sellerTotalPosted = productRepository.countByUserId(sellerId);
            } catch (Exception ignore) { /* không chặn view nếu lỗi đếm */ }
        }

        // ====== BÀI ĐĂNG TƯƠNG TỰ (cùng danh mục cha) ======
        List<Product> similar;
        Integer parentId = null;
        try {
            parentId = product.getParentId();
        } catch (Exception ignore) {
            // ví dụ: parentId = product.getChildCategory().getParent().getParentId();
        }

        if (parentId != null) {
            try {
                similar = productRepository
                        .findTop12ByParentIdAndProductIdNotOrderByCreatedAtDesc(parentId, id);
            } catch (Exception e) {
                similar = productRepository.findAllByParentId(parentId).stream()
                        .filter(p -> !Objects.equals(p.getProductId(), id))
                        .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                        .limit(12)
                        .collect(Collectors.toList());
            }

            // Nạp nhanh ảnh cho từng sản phẩm tương tự
            for (Product sp : similar) {
                List<ProductImage> imgs =
                        productImageRepository.findByProduct_ProductIdOrderByDisplayOrderAsc(sp.getProductId());
                sp.setImages(imgs);
            }
        } else {
            similar = Collections.emptyList();
        }

        // ====== Trạng thái hoạt động người bán (để hiển thị ngay khi render) ======
        LocalDateTime sellerLastSeen = (seller != null ? seller.getLastSeenAt() : null);
        boolean sellerIsOnline = isOnline1m(sellerLastSeen);
        String sellerLastSeenHuman = humanizeLastSeen(sellerLastSeen);

        // ISO cho JS cập nhật live mỗi 1 phút (nếu bạn không dùng #temporals)
        String sellerLastSeenIso = null;
        if (sellerLastSeen != null) {
            sellerLastSeenIso = sellerLastSeen.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        }

        // ====== Model attributes ======
        model.addAttribute("product", product);
        model.addAttribute("images", images);
        model.addAttribute("coverImage", coverImage);
        model.addAttribute("specs", specs);
        model.addAttribute("address", address);
        model.addAttribute("seller", seller);
        model.addAttribute("sellerTotalPosted", sellerTotalPosted);
        model.addAttribute("products", similar); // danh sách “Bài đăng tương tự”

        // Trạng thái hoạt động cho template
        model.addAttribute("sellerIsOnline", sellerIsOnline);
        model.addAttribute("sellerLastSeenHuman", sellerLastSeenHuman);
        model.addAttribute("sellerLastSeenIso", sellerLastSeenIso);

        return "chitietbaidang";
    }
    
}
