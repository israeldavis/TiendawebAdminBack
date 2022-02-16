package com.tiendaweb.admin.brand;

import com.tiendaweb.common.entity.Brand;
import com.tiendaweb.common.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class BrandRepositoryTest {

    @Autowired
    private BrandRepository repo;

    @Test
    public void testCreateBrand() {
        Category cellPhonesCategory = new Category(4);
        Set<Category> categories = new HashSet<>();
        categories.add(cellPhonesCategory);

        Brand samsungBrand = new Brand("Samsung", "default.png", categories);

        Brand savedBrand = repo.save(samsungBrand);

        assertThat(savedBrand.getId()).isGreaterThan(0);
    }

    @Test
    public void testCreateBrandWithTwoCategories() {
        Category memoryCategory = new Category(24);
        Category internalHardDrivesCategory = new Category(29);
        Set<Category> categories = new HashSet<>();
        categories.add(memoryCategory);
        categories.add(internalHardDrivesCategory);

        Brand samsungBrand = new Brand("Samsung", "default.png", categories);

        Brand savedBrand = repo.save(samsungBrand);

        assertThat(savedBrand.getId()).isGreaterThan(0);
    }

    @Test
    public void testListAllBrands() {
        List<Brand> listBrands = repo.findAll();
        listBrands.forEach(System.out::println);
    }

    @Test
    public void testGetBrandById() {
        Brand brand = repo.findById(2).get();
        System.out.println(brand);
        assertThat(brand).isNotNull();
    }

    @Test
    public void testUpdateBrandDetails() {
        Brand brand = repo.getById(3);
        brand.setName("Samsung Electronics");

        repo.save(brand);
    }

    @Test
    public void testDeleteBrand() {
        Integer brandId = 2;
        repo.deleteById(brandId);
    }
}
