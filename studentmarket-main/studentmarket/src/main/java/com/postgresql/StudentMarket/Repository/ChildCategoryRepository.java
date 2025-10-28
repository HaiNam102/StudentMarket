package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.ChildCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChildCategoryRepository extends JpaRepository<ChildCategory, Integer> {
    List<ChildCategory> findByParent_ParentIdOrderByNameAsc(Integer parentId);

    List<ChildCategory> findAllByOrderByNameAsc();

    @Query("SELECT c FROM ChildCategory c WHERE c.parent.parentId = :parentId ORDER BY c.name ASC")
    List<ChildCategory> findAllByParentId(@Param("parentId") Integer parentId);
}