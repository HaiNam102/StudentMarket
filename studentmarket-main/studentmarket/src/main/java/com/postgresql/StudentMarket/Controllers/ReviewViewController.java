package com.postgresql.StudentMarket.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReviewViewController {

    @GetMapping("/danhgia")
    public String showReviewPage() {
        // Spring Boot sẽ tự tìm file: src/main/resources/templates/danhgia.html
        return "danhgia";
    }
    @GetMapping("/danhsachdanhgia")
    public String reviewListPage() {
        // Tên file HTML (trừ đuôi .html) nằm trong src/main/resources/templates/
        return "reviews_list";
    }
}
