package com.tiendaweb.admin.category;

import com.tiendaweb.admin.FileUploadUtil;
import com.tiendaweb.common.entity.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class CategoryService {
    public static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private static final int CATEGORIES_PER_PAGE = 4;

    @Autowired
    private CategoryRepository repo;

    public List<Category> listByPage(int pageNum, String sortDir) {
        Sort sort = Sort.by("name");

        if(sortDir == null || sortDir.isEmpty()) {
            sort = sort.ascending();
        } else if(sortDir.equals("asc")) {
            sort = sort.ascending();
        } else if(sortDir.equals("desc")) {
            sort = sort.descending();
        }

        List<Category> rootCategories = repo.findByParent(null, sort);
        return listHierarchicalCategories(rootCategories, sortDir);
    }

    private List<Category> listHierarchicalCategories(List<Category> rootCategories, String sortDir) {
        List<Category> hierarchicalCategories = new ArrayList<>();

        for(Category rootCategory : rootCategories) {
            hierarchicalCategories.add(Category.copyFull(rootCategory));

            Set<Category> children = sortSubCategories(rootCategory.getChildren(), sortDir);

            for(Category subCategory : children) {
                String name = "--" + subCategory.getName();
                hierarchicalCategories.add(Category.copyFull(subCategory, name));

                listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1, sortDir);
            }
        }

        return hierarchicalCategories;
    }

    private void listSubHierarchicalCategories(List<Category> hierarchicalCategories,
                                               Category parent,
                                               int subLevel,
                                               String sortDir) {
        Set<Category> children = sortSubCategories(parent.getChildren(), sortDir);
        int newSubLevel = subLevel + 1;

        for(Category subCategory : children) {
            String name = "";
            for( int i = 0; i < newSubLevel; i++){
                name += "--";
            }

            name += subCategory.getName();

            hierarchicalCategories.add(Category.copyFull(subCategory, name));

            listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel, sortDir);
        }
    }

    private SortedSet<Category> sortSubCategories(Set<Category> children) {
        return sortSubCategories(children, "asc");
    }

    private SortedSet<Category> sortSubCategories(Set<Category> children, String sortDir) {
        SortedSet<Category> sortedChildren = new TreeSet<>(new Comparator<Category>() {
            @Override
            public int compare(Category cat1, Category cat2) {
                if(sortDir.equals("asc")) {
                    return cat1.getName().compareTo(cat2.getName());
                } else {
                    return cat2.getName().compareTo(cat1.getName());
                }

            }
        });

        sortedChildren.addAll(children);
        return sortedChildren;
    }

    public Page<Category> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
        Sort sort = Sort.by(sortField);

        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNum - 1, CATEGORIES_PER_PAGE, sort);

        if(keyword != null && !keyword.equals("null") || !keyword.equals("")) {
            return repo.findAll(keyword, pageable);
        }

        return repo.findAll(pageable);
    }

    public Category getCategoryById(Integer id) {
        Optional<Category> optionalCategory = repo.findById(id);
        if(!optionalCategory.isPresent()) {
            return null;
        }
        return optionalCategory.get();
    }

    public Category getCategoryByAlias(String alias) {
        Category category = repo.findByAlias(alias);
        if(category == null) {
            return null;
        }
        return category;
    }

    public void deleteCategoryById(Integer id) {
        repo.deleteById(id);
    }

    public Category saveCategory(String name, String alias, Category parent, Boolean enabled,  MultipartFile image) throws IOException {
        Category newCategory = new Category();
        newCategory.setName(name);
        newCategory.setAlias(alias);
        newCategory.setParent(parent);
        newCategory.setEnabled(enabled);
        String fileName = "";
        if(image != null) {
            if (!image.isEmpty()) {
                fileName = StringUtils.cleanPath(image.getOriginalFilename());
                newCategory.setImage(fileName);
            }
        }

        Category savedCategory = repo.save(newCategory);

        if(image != null) {
            if(!image.isEmpty()) {
                fileName = StringUtils.cleanPath(image.getOriginalFilename());

                String uploadDir = "TiendawebWebParent/TiendawebBackEnd/category-images/" + savedCategory.getId();

                FileUploadUtil.cleanDir(uploadDir);
                FileUploadUtil.saveFile(uploadDir, fileName, image);
            }
        }
        return savedCategory;
    }

    public Category updateCategory(Integer id, String name, String alias, Category parent, Boolean enabled, MultipartFile image) throws IOException {
        Category categoryInDB = repo.getById(id);
        if(name != null || !name.equals("")) {
            categoryInDB.setName(name);
        }
        if(alias != null || !alias.equals("")) {
            categoryInDB.setAlias(alias);
        }
        if(parent != null ) {
            categoryInDB.setParent(parent);
        }
        categoryInDB.setEnabled(enabled);
        String fileName = "";
        if(image != null) {
            if (!image.isEmpty()) {
                fileName = StringUtils.cleanPath(image.getOriginalFilename());
                categoryInDB.setImage(fileName);
            }
        }

        Category savedCategory = repo.save(categoryInDB);

        if(image != null) {
            if(!image.isEmpty()) {
                String uploadDir = "TiendawebWebParent/TiendawebBackEnd/category-images/" + id;

                FileUploadUtil.cleanDir(uploadDir);
                FileUploadUtil.saveFile(uploadDir, fileName, image);
            }
        }

        return repo.save(categoryInDB);
    }

    public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
        repo.updateEnabledStatus(id, enabled);
    }

    public List<Category> listCategoriesUsedInForm() {
        List<Category> categoriesUsedInForm = new ArrayList<>();
        List<Category> categoriesInDB = repo.findRootCategories(Sort.by("name").ascending());

        for(Category category : categoriesInDB) {
            if(category.getParent() == null) {
                categoriesUsedInForm.add(new Category(category.getId(), category.getName()));

                Set<Category> children = sortSubCategories(category.getChildren());

                for(Category subcategory : children) {
                    String name = "--" + subcategory.getName();
                    categoriesUsedInForm.add(new Category(subcategory.getId(), name));
                    listSubcategoriesUsedInForm(categoriesUsedInForm, subcategory, 1);
                }
            }
        }

        return categoriesUsedInForm;
    }

    private void listSubcategoriesUsedInForm(List<Category> categoriesUsedInForm, Category parent, int subLevel) {
        int newSubLevel = subLevel + 1;
        Set<Category> children = sortSubCategories(parent.getChildren());

        for (Category subCategory : children) {
            String name = "";
            for (int i = 0; i < newSubLevel; i++) {
                name += "--";
            }
            name += subCategory.getName();

            categoriesUsedInForm.add( new Category(subCategory.getId(), name));
            listSubcategoriesUsedInForm(categoriesUsedInForm, subCategory, newSubLevel);
        }
    }

    public List<Category> getCategoryTree() {
        return repo.findByParent(null, Sort.by("name").ascending());
    }

    public boolean checkUnique(Integer id, String name, String alias) {
        boolean isCreatingNew = (id == null || id == 0);

        Category categoryByName = repo.findByName(name);

        if(isCreatingNew) {
            if(categoryByName != null) {
                return false;
            } else {
                Category categoryByAlias = repo.findByAlias(alias);
                if(categoryByAlias != null) {
                    return false;
                }
            }
        } else { // el id es numero, por lo que esta editando
            if(categoryByName != null && categoryByName.getId() != id) { // el nombre existe pero no es el de la misma category sino otra
                return false;
            }

            Category categoryByAlias = repo.findByAlias(alias);
            if(categoryByAlias != null && categoryByAlias.getId() != id){
                return false;
            }
        }

        return true;
    }
}
