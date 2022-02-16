package com.tiendaweb.admin.category;

import com.tiendaweb.common.entity.Category;
import org.springframework.data.domain.Page;

import java.util.List;

public class CategoryPaginatorData {
    private Page<Category> pageCategories;
    private List<Category> listCategories;

    public Page<Category> getPageCategories() {
        return pageCategories;
    }

    public void setPageCategories(Page<Category> pageCategories) {
        this.pageCategories = pageCategories;
    }

    public List<Category> getListCategories() {
        return listCategories;
    }

    public void setListCategories(List<Category> listCategories) {
        this.listCategories = listCategories;
    }
}
