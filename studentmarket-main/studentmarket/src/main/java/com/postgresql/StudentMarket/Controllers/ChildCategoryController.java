package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Dto.ChildCategoryDTO;
import com.postgresql.StudentMarket.Services.ChildCategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChildCategoryController {
    ChildCategoryService childCategoryService;

    @ModelAttribute("childCategories")
    public List<ChildCategoryDTO> childCategories() {
        return childCategoryService.findAllChildren();
    }
}