package com.tiendaweb.admin.category;

import com.tiendaweb.common.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Category findByAlias(String alias);

    Category findByName(String name);

    Long countById(Integer id);

    List<Category> findByParent(Category category, Sort sort);

    @Query("SELECT c FROM Category c WHERE c.parent.id is null")
    List<Category> findRootCategories(Sort sort);

    @Query("SELECT c FROM Category c WHERE CONCAT(c.id, ' ', c.name, ' ', c.alias) LIKE %?1%")
    Page<Category> findAll(String keyword, Pageable pageable);

    @Query("UPDATE Category  c SET c.enabled = ?2 WHERE c.id = ?1")
    @Modifying
    void updateEnabledStatus(Integer id, boolean enabled);
}
