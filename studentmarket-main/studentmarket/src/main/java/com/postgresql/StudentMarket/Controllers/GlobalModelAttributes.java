package com.postgresql.StudentMarket.Controllers;

import com.postgresql.StudentMarket.Dto.ParentWithChildren;
import com.postgresql.StudentMarket.Services.CategoryMenuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributes {

    private final CategoryMenuService categoryMenuService;

    public GlobalModelAttributes(CategoryMenuService categoryMenuService) {
        this.categoryMenuService = categoryMenuService;
    }

    @ModelAttribute("categories")
    public List<ParentWithChildren> categories() {
        return categoryMenuService.getMenuData();
    }
}
