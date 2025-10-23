package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.ChildCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChildCategoryRepository extends JpaRepository<ChildCategory, Integer> {
    List<ChildCategory> findByParent_ParentIdOrderByNameAsc(Integer parentId);

    List<ChildCategory> findAllByOrderByNameAsc();
}