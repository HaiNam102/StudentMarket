package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.ParentCategory;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentCategoryRepository extends JpaRepository<ParentCategory, Integer> {

    // Cách 1: EntityGraph để load children
    @EntityGraph(attributePaths = "children")
    List<ParentCategory> findAllByOrderByNameAsc();

    // Cách 2: JPQL join fetch (nếu muốn chắc chắn 1 query)
    @Query("SELECT DISTINCT p FROM ParentCategory p LEFT JOIN FETCH p.children ORDER BY p.name ASC")
    List<ParentCategory> fetchAllWithChildren();

    @EntityGraph(attributePaths = "children")
    List<ParentCategory> findAll();
}
