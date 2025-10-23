package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images ORDER BY p.createdAt DESC")
    List<Product> findAllWithImages();

    // theo danh mục con
    @Query("SELECT p FROM Product p WHERE p.childCategory.childId = :childId")
    List<Product> findAllByChildId(Integer childId);

    // theo danh mục cha
    @Query("SELECT p FROM Product p WHERE p.childCategory.parent.parentId = :parentId")
    List<Product> findAllByParentId(Integer parentId);

    // Lọc theo danh mục con
    @EntityGraph(attributePaths = {"childCategory"})
    // tuỳ chọn, tránh N+1
    List<Product> findByChildCategory_ChildId(Integer childId);

    // Lọc theo danh mục cha
    @EntityGraph(attributePaths = {"childCategory", "childCategory.parent"})
    List<Product> findByChildCategory_Parent_ParentId(Integer parentId);

    List<Product> findTop12ByParentIdAndProductIdNotOrderByCreatedAtDesc(Integer parentId, Long productId);

    //đếm số bài đã đăng của người bán
    long countByUserId(Integer userId);

    // (tuỳ chọn) sort
    List<Product> findByChildCategory_ChildIdOrderByCreatedAtDesc(Integer childId);

    List<Product> findByChildCategory_Parent_ParentIdOrderByCreatedAtDesc(Integer parentId);

    List<Product> findByUserIdOrderByCreatedAtDesc(Integer userId);

//    @EntityGraph(attributePaths = {"childCategory", "images"})
    @Query(
            value = """
                    SELECT p FROM Product p
                    LEFT JOIN p.childCategory c
                    WHERE (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
                      AND (:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%')))
                      AND (:childCategoryId IS NULL OR c.id = :childCategoryId)
                    ORDER BY p.createdAt DESC
                    """
    )
    Page<Product> searchProducts(
            @Param("q") String q,
            @Param("childCategoryId") Long childCategoryId,
            @Param("location") String location,
            Pageable pageable
    );
}
