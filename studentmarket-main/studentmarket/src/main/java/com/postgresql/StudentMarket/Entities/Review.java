package com.postgresql.StudentMarket.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "review",
    uniqueConstraints = @UniqueConstraint(name = "uq_review_once", columnNames = {"transaction_id", "reviewer_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_transaction"))
    private Transaction transaction;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_reviewer"))
    private User reviewer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reviewee_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_reviewee"))
    private User reviewee;

    @Column(nullable = false)
    private Integer rating; // 1..5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt = LocalDateTime.now();
}
