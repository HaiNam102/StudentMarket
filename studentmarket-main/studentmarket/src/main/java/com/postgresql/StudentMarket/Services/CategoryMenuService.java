package com.postgresql.StudentMarket.Services;

import com.postgresql.StudentMarket.Dto.ChildCategoryDTO;
import com.postgresql.StudentMarket.Dto.ParentCategoryDTO;
import com.postgresql.StudentMarket.Dto.ParentWithChildren;
import com.postgresql.StudentMarket.Dto.ParentWithChildrenDTO;
import com.postgresql.StudentMarket.Entities.ParentCategory;
import com.postgresql.StudentMarket.Repository.ChildCategoryRepository;
import com.postgresql.StudentMarket.Repository.ParentCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryMenuService {

    private final ParentCategoryRepository parentRepo;
    private final ChildCategoryRepository childCategoryRepository;

    public CategoryMenuService(ParentCategoryRepository parentRepo,ChildCategoryRepository childCategoryRepository) {
        this.parentRepo = parentRepo;
        this.childCategoryRepository = childCategoryRepository;
    }

    // Lấy danh mục cha và danh mục con để hiển thị menu
    // Lấy danh mục cha và danh mục con để hiển thị menu
    public List<ParentWithChildren> getMenuData() {
        List<ParentCategory> parents = parentRepo.fetchAllWithChildren();

        return parents.stream()
                // Đưa "Khác" xuống cuối, còn lại sort theo tên không phân biệt hoa thường
                .sorted(
                        java.util.Comparator
                                .comparing((ParentCategory p) -> "Khác".equalsIgnoreCase(p.getName()) ? 1 : 0)
                                .thenComparing(p -> p.getName().toLowerCase())
                )
                .map(p -> new ParentWithChildren(
                        p.getParentId(),
                        p.getName(),
                        p.getIconPath(), // đã map iconPath
                        (p.getChildren() == null ? java.util.List.<com.postgresql.StudentMarket.Dto.ParentWithChildren.ChildItem>of()
                                : p.getChildren().stream()
                                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                                .map(c -> new ParentWithChildren.ChildItem(c.getChildId(), c.getName()))
                                .toList())
                ))
                .toList();
    }

    public List<ParentCategoryDTO> findAllParent() {
        return parentRepo.findAll().stream()
                .map(c -> new ParentCategoryDTO(c.getParentId(), c.getName()))
                .toList();
    }

    public List<ChildCategoryDTO> findChildrenByParent(Integer parentId) {
        return childCategoryRepository.findAllByParentId(parentId).stream()
                .map(c -> new ChildCategoryDTO(c.getChildId(), c.getName()))
                .toList();
    }
}
