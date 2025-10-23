package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Dto.ProductViewDTO;
import com.postgresql.StudentMarket.Dto.SearchReqDTO;
import com.postgresql.StudentMarket.Entities.*;
import com.postgresql.StudentMarket.Repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.math.BigDecimal; // <-- thêm import này
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSpecsRepository productSpecsRepository;
    private final AddressRepository addressRepository;
    private final ChildCategoryRepository childCategoryRepository;
    private final UserRepository userRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public Product createProduct(Integer userId,
                                 Integer parentId,
                                 Integer childId,
                                 String name,
                                 String type,
                                 Long price, // để nguyên Long ở tham số
                                 String description,
                                 String province,
                                 String ward,
                                 String addressDetail,
                                 String coverName,
                                 List<String> orderNames,
                                 List<MultipartFile> files,
                                 String origin,
                                 String material,
                                 String color,
                                 String accessories) throws IOException {

        // Ensure upload dir
        Path root = Paths.get(uploadDir);
        Files.createDirectories(root);

        // Resolve user & child category (nếu chỉ lưu user_id integer thì phần user này
        // chỉ để validate tồn tại)
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ChildCategory child = childCategoryRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child category not found"));

        // Product
        Product p = new Product();
        p.setUserId(userId != null ? userId : 0);

        // Address (nếu bảng address là bắt buộc)
        Address addr = Address.builder()
                .province(province)
                .ward(ward)
                .addressDetail(addressDetail)
                .product(p)
                .user(userRepository.findById(userId).orElseThrow())
                .build();
        addressRepository.save(addr);

        // ✅ GÁN parent_id
        p.setParentId(parentId);

        // child
        p.setChildCategory(child);
        p.setName(name);
        p.setType(type);

        // Convert Long -> BigDecimal
        BigDecimal priceBD = BigDecimal.valueOf(price != null ? price : 0L);
        p.setPrice(priceBD);

        p.setDescription(description);
        p.setLocation((addressDetail != null ? addressDetail : "") +
                (ward != null && !ward.isBlank() ? ", " + ward : "") +
                (province != null && !province.isBlank() ? ", " + province : ""));
        p.setStatus("ACTIVE");
        p.setIsHot(false);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        productRepository.save(p);

        if (p.getPrice() == null) {
            p.setPrice(BigDecimal.ZERO);
        }

        // Save images with order (cover + thứ tự)
        if (files != null && !files.isEmpty()) {
            // lọc rỗng và giới hạn 6
            List<MultipartFile> clean = files.stream()
                    .filter(f -> f != null && !f.isEmpty())
                    .limit(6)
                    .toList();

            // fallback order nếu orderNames null
            List<String> namesInOrder = (orderNames != null && !orderNames.isEmpty())
                    ? orderNames
                    : clean.stream().map(MultipartFile::getOriginalFilename).toList();

            // đảm bảo các file thật có trong orderNames; nối phần còn lại cuối danh sách
            List<String> finalOrder = new ArrayList<>();
            for (String n : namesInOrder) {
                if (n != null && clean.stream()
                        .anyMatch(f -> n.equalsIgnoreCase(f.getOriginalFilename()))) {
                    finalOrder.add(n);
                }
            }
            for (MultipartFile f : clean) {
                String n = f.getOriginalFilename();
                if (finalOrder.stream().noneMatch(x -> x.equalsIgnoreCase(n))) {
                    finalOrder.add(n);
                }
            }

            // chọn cover: ưu tiên coverName
            String coverBaseName = (coverName != null && !coverName.isBlank())
                    ? coverName
                    : finalOrder.get(0);

            List<ProductImage> toSave = new ArrayList<>();

            for (int i = 0; i < finalOrder.size(); i++) {
                String fname = finalOrder.get(i);
                MultipartFile mf = clean.stream()
                        .filter(f -> fname.equalsIgnoreCase(f.getOriginalFilename()))
                        .findFirst().orElse(null);
                if (mf == null)
                    continue;

                String stored = System.currentTimeMillis() + "_"
                        + mf.getOriginalFilename().replaceAll("\\s+", "_");
                Path dest = root.resolve(stored);
                Files.write(dest, mf.getBytes());

                String publicUrl = "/uploads/" + stored;

                ProductImage img = ProductImage.builder()
                        .product(p)
                        .imageUrl(publicUrl)
                        .isCover(fname.equalsIgnoreCase(coverBaseName))
                        .displayOrder(i)
                        .build();
                toSave.add(img);
            }

            if (!toSave.isEmpty()) {
                // bảo vệ: nếu không có cái nào isCover=true, đặt phần tử đầu làm cover
                if (toSave.stream().noneMatch(ProductImage::isCover)) {
                    toSave.get(0).setCover(true);
                }

                // lưu theo display_order asc
                toSave.sort(Comparator.comparingInt(ProductImage::getDisplayOrder));
                productImageRepository.saveAll(toSave);

                // cập nhật products.image_url = ảnh cover
                ProductImage cover = toSave.stream().filter(ProductImage::isCover).findFirst()
                        .orElse(toSave.get(0));
                p.setImageUrl(cover.getImageUrl()); // ✅ đặt ảnh bìa vào products.image_url
                productRepository.save(p);
            }
        }

        // Specs
        ProductSpecs specs = ProductSpecs.builder()
                .product(p)
                .origin(origin)
                .material(material)
                .color(color)
                .accessories(accessories)
                .build();
        productSpecsRepository.save(specs);

        return p;
    }

    public Page<ProductViewDTO> searchProducts(SearchReqDTO searchReqDTO, Pageable pageable) {
        String nameProduct = (searchReqDTO.getName() == null || searchReqDTO.getName().isBlank()) ? null : searchReqDTO.getName().trim();
        String location = (searchReqDTO.getLocation() == null || searchReqDTO.getLocation().isBlank()) ? null : searchReqDTO.getLocation().trim();

        var page = productRepository.searchProducts(nameProduct, searchReqDTO.getChildCategoryId(), location, pageable);
        return page.map(ProductViewDTO::fromEntityToDto);
    }
}
