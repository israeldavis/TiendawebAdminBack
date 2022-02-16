package com.tiendaweb.admin.brand;

import com.tiendaweb.common.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Brand findByName(String name);

    @Query("SELECT b FROM Brand b WHERE b.name LIKE %?1% ")
    Page<Brand> findAll(String keyword, Pageable pageable);
}
