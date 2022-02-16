package com.tiendaweb.admin.category;

import com.tiendaweb.common.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository repo;

    @Test
    public void testCreateRootCategory(){
        Category category = new Category("Electronics");
        Category savedCategory = repo.save(category);

        assertThat(savedCategory.getId()).isGreaterThan(0);
    }

    @Test
    public void testCreateSubCategory() {
        Category parent = new Category(7);
        Category subcategory = new Category("iPhone", parent);

        repo.save(subcategory);
        assertThat(subcategory.getId()).isGreaterThan(0);
    }

    @Test
    public void testGetCategory(){
        Category category = repo.findById(2).get();
        System.out.println(category.getName());

        Set<Category> children = category.getChildren();

        for (Category subCategory : children ) {
            System.out.println(subCategory.getName());
        }

        assertThat(children.size()).isGreaterThan(0);
    }

    @Test
    public void testPrintHierarchicalCategories() {
        Iterable<Category> categories = repo.findAll();

        for(Category category : categories) {
            if(category.getParent() == null) {
                System.out.println(category.getName());

                Set<Category> children = category.getChildren();

                for(Category subcategory : children) {
                    System.out.println("--" + subcategory.getName());
                    printChildren(subcategory, 1);
                }
            }
        }
    }

    private void printChildren(Category parent, int subLevel) {
        int newSubLevel = subLevel + 1;
        Set<Category> children = parent.getChildren();

        for(Category subCategory : children) {
            for(int i = 0; i < newSubLevel; i++) {
                System.out.print("--");
            }

            System.out.println(subCategory.getName());
            printChildren(subCategory, newSubLevel);
        }
    }

    @Test
    public void findCategoryByName() {
        String name = "Computers";

        Category category = repo.findByName(name);

        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo(name);
    }

    @Test
    public void findCategoryByAlias() {
        String alias = "hard_drive";
        Category category = repo.findByAlias(alias);

        assertThat(category).isNotNull();
        assertThat(category.getAlias()).isEqualTo(alias);
    }
}
