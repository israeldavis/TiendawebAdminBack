package com.tiendaweb.admin.brand;

import com.tiendaweb.common.entity.Brand;
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
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private BrandService service;

    @GetMapping("")
    public Page<Brand> getAll() {
        return listByPage(1, "name", "asc", null);
    }

    @GetMapping("/page/{pageNum}")
    public Page<Brand> listByPage(@PathVariable("pageNum") int pageNum,
                                  @RequestParam(name = "sortField", required = false) String sortField,
                                  @RequestParam(name = "sortDir", required = false) String sortDir,
                                  @RequestParam(name = "keyword", required = false) String keyword) {
        Page<Brand> page = service.listByPage(pageNum, sortField, sortDir, keyword);

        return page;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok().body(service.getBrandById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<Brand> saveBrand(@RequestParam("name") String name,
                                           @RequestParam("categories") String categories,
                                           @RequestParam(value = "logo", required = false) MultipartFile logo) throws IOException {
        Set<Category> categoriesFromForm = new HashSet<>();
        if(categories != null) {
            String[] categoriesIds = categories.split(" ");

            for(int i = 0; i<categoriesIds.length; i++) {
                categoriesFromForm.add(new Category(Integer.parseInt(categoriesIds[i])));
            }
        }

        return ResponseEntity.ok().body(service.saveBrand(name, categoriesFromForm, logo));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Brand> updateBrand(
            @PathVariable("id") Integer id,
            @RequestParam("name") String name,
            @RequestParam("categories") String categories,
            @RequestParam(value = "logo", required = false) MultipartFile logo
    )  throws IOException {
        Set<Category> categoriesFromForm = new HashSet<>();
        if(categories != null) {
            String[] categoriesIds = categories.split(" ");

            for(int i = 0; i<categoriesIds.length; i++) {
                categoriesFromForm.add(new Category(Integer.parseInt(categoriesIds[i])));
            }
        }

        return ResponseEntity.ok().body(service.updateBrand(id, name, categoriesFromForm, logo));
    }

    @GetMapping(path = "/logo/{id}/{fileName}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getLogo(@PathVariable("id") String id, @PathVariable("fileName") String fileName) throws IOException {
        Path path = Paths.get(
                System.getProperty("user.home") + "/Documents/TiendaWeb/TiendawebProject/TiendawebWebParent/TiendawebBackEnd/brand-images/" +
                        id +
                        "/" +
                        fileName);
        byte[] logoBytes = Files.readAllBytes(path);

        return logoBytes;
    }

    @DeleteMapping("/delete/{id}")
    public void deleteBrand(@PathVariable("id") Integer id) {
        service.deleteBrandById(id);
    }

    @GetMapping("/check_unique")
    public ResponseEntity<Boolean> checkUnique(@RequestParam("id") String id,
                                               @RequestParam("name") String name) {
        boolean result = service.checkUnique(Integer.parseInt(id), name);
        return ResponseEntity.ok().body(result);
    }

}
