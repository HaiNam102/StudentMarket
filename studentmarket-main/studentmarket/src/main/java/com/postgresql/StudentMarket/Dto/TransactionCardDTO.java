package com.postgresql.StudentMarket.Dto;

import com.postgresql.StudentMarket.Entities.Transaction;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionCardDTO {

    private Integer id;
    private String  title;       // Tên sản phẩm hiển thị
    private String  priceLabel;  // "12.500.000đ" | "Miễn phí"
    private String  thumbUrl;    // Ảnh sản phẩm
    private String  ownerName;   // Tên người bán
    private String  note;        // Ghi chú
    private LocalDateTime createdAt;
    private String  status;      // REQUESTING / IN_PROGRESS / COMPLETED / CANCELLED

    // ✅ FE cần để xác định người bị đánh giá
    private Integer sellerId;
    private Integer buyerId;

    public static TransactionCardDTO of(Transaction t) {
        String price = (t.getTotalAmount() == null || t.getTotalAmount().doubleValue() == 0)
                ? "Miễn phí"
                : String.format("%,.0fđ", t.getTotalAmount());

        String sellerName = (t.getSeller() != null && t.getSeller().getFullName() != null)
                ? t.getSeller().getFullName()
                : "Người bán";

        String productTitle = null;
        String thumb = null;

        try {
            if (t.getDetails() != null && !t.getDetails().isEmpty()) {
                var validProducts = t.getDetails().stream()
                        .filter(d -> d.getProduct() != null && d.getProduct().getName() != null)
                        .toList();

                if (!validProducts.isEmpty()) {
                    productTitle = validProducts.stream()
                            .map(d -> d.getProduct().getName())
                            .collect(java.util.stream.Collectors.joining(", "));

                    var first = validProducts.get(0);
                    if (first.getProduct().getImageUrl() != null) {
                        thumb = first.getProduct().getImageUrl().trim();
                    }
                }
            }
        } catch (Exception ignored) { }

        if (thumb != null && thumb.startsWith("/")) {
            thumb = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path(thumb)
                    .toUriString();
        }
        if (thumb == null || thumb.isBlank()) {
            thumb = "https://via.placeholder.com/160";
        }

        if (productTitle == null || productTitle.isBlank()) {
            productTitle = "Sản phẩm #" + t.getTransactionId();
        }

        return TransactionCardDTO.builder()
                .id(t.getTransactionId())
                .title(productTitle)
                .priceLabel(price)
                .thumbUrl(thumb)
                .ownerName(sellerName)
                .note(t.getNote())
                .createdAt(t.getCreatedAt())
                .status(t.getStatus().name())
                // ✅ map thêm 2 trường này
                .sellerId(t.getSeller() != null ? t.getSeller().getUserId() : null)
                .buyerId(t.getBuyer()  != null ? t.getBuyer().getUserId()  : null)
                .build();
    }
}
