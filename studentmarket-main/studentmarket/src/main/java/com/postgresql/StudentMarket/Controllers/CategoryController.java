package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Dto.ChildCategoryDTO;
import com.postgresql.StudentMarket.Dto.ParentCategoryDTO;
import com.postgresql.StudentMarket.Dto.ParentWithChildrenDTO;
import com.postgresql.StudentMarket.Services.CategoryMenuService;
import com.postgresql.StudentMarket.Services.ChildCategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryMenuService categoryService;

    // Toàn bộ parent để render select box
    @ModelAttribute("parents")
    public List<ParentCategoryDTO> parents() {
        return categoryService.findAllParent();
    }

    //chọn childCategory theo parent
    @ModelAttribute("childrenByParent")
    public List<ChildCategoryDTO> childrenByParent(
            @RequestParam(name = "parentId", required = false) Integer parentId) {
        if (parentId == null) {
            return List.of();
        }
        var list = categoryService.findChildrenByParent(parentId);
        return list;
    }

    // Test JSON
//    @GetMapping("/test/parents")
//    @ResponseBody
//    public List<ParentCategoryDTO> testParents() {
//        return categoryService.findAllParent();
//    }
//
//    @GetMapping("/test/children")
//    @ResponseBody
//    public List<ChildCategoryDTO> testChildren(@RequestParam Integer parentId) {
//        var list = categoryService.findChildrenByParent(parentId);
//        return list;
//    }

}