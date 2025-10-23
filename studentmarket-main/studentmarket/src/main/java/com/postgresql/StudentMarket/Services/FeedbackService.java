package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Entities.Feedback;
import com.postgresql.StudentMarket.Entities.Feedback.FeedbackStatus;
import com.postgresql.StudentMarket.Entities.Feedback.IssueType;
import com.postgresql.StudentMarket.Entities.User;
import com.postgresql.StudentMarket.Repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final Path uploadDir = Paths.get("uploads/feedback");

    public void saveFeedback(User user, String issueType, String content, MultipartFile file) throws IOException {

        String storedPath = null;
        if (file != null && !file.isEmpty()) {
            Files.createDirectories(uploadDir);
            String ext = getExt(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path dest = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
            storedPath = "/uploads/feedback/" + fileName;
        }

        Feedback feedback = Feedback.builder()
                .user(user)
                .issueType(IssueType.valueOf(issueType.toUpperCase()))
                .content(content)
                .attachmentPath(storedPath)
                .status(FeedbackStatus.NEW)
                .build();

        feedbackRepository.save(feedback);
    }

    private String getExt(String name) {
        if (name == null)
            return "";
        int dot = name.lastIndexOf('.');
        return (dot >= 0) ? name.substring(dot + 1) : "";
    }
}
