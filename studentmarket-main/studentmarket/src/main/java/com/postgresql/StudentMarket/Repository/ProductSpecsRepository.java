
package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.ProductSpecs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSpecsRepository extends JpaRepository<ProductSpecs, Long> {}
