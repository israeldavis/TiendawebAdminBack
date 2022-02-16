package com.tiendaweb.admin.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiendaweb.admin.FileUploadUtil;
import com.tiendaweb.common.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService service;

    @GetMapping("")
    public List<Category> listFirstPage(@RequestParam("sortDir") String sortDir){
        return service.listByPage(1, sortDir);
    }

    @GetMapping("/page/{pageNum}")
    public Page<Category> listByPage(@PathVariable("pageNum") int pageNum,
                                     @RequestParam(name = "sortField", required = false) String sortField,
                                     @RequestParam(name = "sortDir", required = false) String sortDir,
                                     @RequestParam(name = "keyword", required = false) String keyword) {
        Page<Category> page = service.listByPage(pageNum, sortField, sortDir, keyword);

        return page;
    }

    @GetMapping("/new")
    public List<Category> newCategory() {
        return service.listCategoriesUsedInForm();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable("id") int id){
        return ResponseEntity.ok().body(service.getCategoryById(id));
    }

    @GetMapping("/allCategoriesTree")
    public ResponseEntity<List<Category>> getAllCategoriesTree()  {
        return ResponseEntity.ok().body(service.getCategoryTree());
    }

    @PostMapping("/save")
    public ResponseEntity<Category> saveCategory(@RequestParam("name") String name,
                                                 @RequestParam("alias") String alias,
                                                 @RequestParam("parent") String parent,
                                                 @RequestParam("enabled") Boolean enabled,
                                                 @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        Category categorySelected = null;
        if(parent != null && !parent.equals("")) {
            categorySelected = new ObjectMapper().readValue(parent, Category.class);
        }

        return ResponseEntity.ok().body(service.saveCategory(name, alias, categorySelected, enabled, image));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable("id") Integer id,
            @RequestParam("name") String name,
            @RequestParam("alias") String alias,
            @RequestParam("parent") String parent,
            @RequestParam("enabled")  Boolean enabled,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        Category categorySelected = null;
        if(parent != null && !parent.equals("")) {
            categorySelected = new ObjectMapper().readValue(parent, Category.class);
        }
        return ResponseEntity.ok().body(service.updateCategory(id, name, alias, categorySelected, enabled, image ));
    }

    @GetMapping(path = "/image/{id}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("id") String id, @PathVariable("fileName") String fileName) throws IOException {

        Path path = Paths.get(
                System.getProperty("user.home") + "/Documents/TiendaWeb/TiendawebProject/TiendawebWebParent/TiendawebBackEnd/category-images/" +
                        id +
                        "/" +
                        fileName);
        byte[] imagenBytes = Files.readAllBytes(path);

        return imagenBytes;
    }

    @GetMapping("/check_unique")
    public ResponseEntity<Boolean> checkUnique(@RequestParam("id") String id,
                              @RequestParam("name") String name,
                              @RequestParam("alias") String alias) {

        boolean result = service.checkUnique(Integer.parseInt(id), name, alias);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{id}/enabled/{status}")
    public void updateCategoryEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean enabled) {
        service.updateCategoryEnabledStatus(id, enabled);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteCategory(@PathVariable("id") Integer id) {

        service.deleteCategoryById(id);
        String categoryDir = "/category-images/" + id;
        FileUploadUtil.removeDir(categoryDir);
    }
}
