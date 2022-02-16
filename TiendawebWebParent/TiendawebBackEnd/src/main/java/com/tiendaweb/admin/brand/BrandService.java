package com.tiendaweb.admin.brand;

import com.tiendaweb.admin.FileUploadUtil;
import com.tiendaweb.common.entity.Brand;
import com.tiendaweb.common.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
public class BrandService {

    private static final int BRANDS_PER_PAGE = 10;

    @Autowired
    private BrandRepository repo;

    public List<Brand> getAll() {
        return repo.findAll();
    }

    public Brand getBrandById(Integer id) {
        return repo.findById(id).get();
    }

    public Brand saveBrand(String name, Set<Category> categories, MultipartFile logo) throws IOException {
        Brand newBrand = new Brand();
        newBrand.setName(name);
        newBrand.setCategories(categories);

        String fileName = "";
        if(logo != null) {
            if (!logo.isEmpty()) {
                fileName = StringUtils.cleanPath(logo.getOriginalFilename());
                newBrand.setLogo(fileName);
            }
        }

        Brand savedBrand = repo.save(newBrand);

        if(logo != null) {
            if(!logo.isEmpty()) {
                fileName = StringUtils.cleanPath(logo.getOriginalFilename());

                String uploadDir = "TiendawebWebParent/TiendawebBackEnd/brand-images/" + savedBrand.getId();

                FileUploadUtil.cleanDir(uploadDir);
                FileUploadUtil.saveFile(uploadDir, fileName, logo);
            }
        }
        return savedBrand;
    }

    public void deleteBrandById(Integer id) {
        repo.deleteById(id);
    }

    public Brand updateBrand(Integer id, String name, Set<Category> categoriesFromForm, MultipartFile logo) throws IOException {
        Brand brandInDB = repo.getById(id);
        if(name != null || !name.equals("")) {
            brandInDB.setName(name);
        }
        if(!categoriesFromForm.isEmpty()) {
            brandInDB.setCategories(categoriesFromForm);
        }

        String fileName = "";
        if(logo != null) {
            if(!logo.isEmpty()) {

                fileName = StringUtils.cleanPath(logo.getOriginalFilename());
                brandInDB.setLogo(fileName);

                String uploadDir = "TiendawebWebParent/TiendawebBackEnd/brand-images/" + id;

                FileUploadUtil.cleanDir(uploadDir);
                FileUploadUtil.saveFile(uploadDir, fileName, logo);
            }
        }
        return repo.save(brandInDB);
    }

    public boolean checkUnique(Integer id, String name) {
        boolean isCreatingNew = (id == null || id == 0);
        Brand brandByName = repo.findByName(name);

        if(isCreatingNew) {
            if(brandByName != null) return false;
        } else {
            if(brandByName != null && brandByName.getId() != id) {
                return false;
            }
        }
        return true;
    }

    public Page<Brand> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
        Pageable pageable = PageRequest.of(pageNum - 1, BRANDS_PER_PAGE, sort);

        if(keyword != null && !keyword.equals("null")) {
            return repo.findAll(keyword, pageable);
        }
        return repo.findAll(pageable);
    }
}
