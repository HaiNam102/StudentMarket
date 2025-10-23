package com.postgresql.StudentMarket.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // ánh xạ tới bảng users (entity User)

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false, length = 20)
    private IssueType issueType;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "attachment_path")
    private String attachmentPath;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FeedbackStatus status = FeedbackStatus.NEW;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // ENUM nội bộ tương ứng MySQL ENUM('BUG','FEATURE','UIUX','OTHER')
    public enum IssueType {
        BUG, FEATURE, UIUX, OTHER
    }

    // ENUM nội bộ tương ứng ENUM('NEW','OPEN','DONE')
    public enum FeedbackStatus {
        NEW, OPEN, DONE
    }
}
