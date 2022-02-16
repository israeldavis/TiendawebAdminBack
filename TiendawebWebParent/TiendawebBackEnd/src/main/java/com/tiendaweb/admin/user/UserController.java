package com.tiendaweb.admin.user;

import com.tiendaweb.admin.FileUploadUtil;
import com.tiendaweb.common.entity.Role;
import com.tiendaweb.common.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping("/users")
    public Page<User> listAll() {
        return listByPage(1, "firstName", "asc", null);
    }

    @GetMapping("/users/page/{pageNum}")
    public Page<User> listByPage(@PathVariable("pageNum") int pageNum,
                                 @RequestParam(name = "sortField", required = false) String sortField,
                                 @RequestParam(name = "sortDir", required = false) String sortDir,
                                 @RequestParam(name = "keyword", required = false) String keyword) {
        //System.out.println("Sort field: " + sortField);
        //System.out.println("Sort Dir: " + sortDir);
        Page<User> page = service.listByPage(pageNum, sortField, sortDir, keyword);

        return page;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Integer id) {
        return ResponseEntity.ok().body(service.getUserById(id));
    }

    @GetMapping("/users/getByEmail/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable("email") String email) {
        User user = service.getUserByEmail(email);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/roles")
    public List<Role> listAllRoles() {
        return service.getAllRoles();
    }

    @PostMapping("/users/save")
    public ResponseEntity<User> saveUser(@RequestParam("email") String email,
                                         @RequestParam("firstName") String firstName,
                                         @RequestParam("lastName") String lastName,
                                         @RequestParam("password") String password,
                                         @RequestParam("enabled") Boolean enabled,
                                         @RequestParam("roles") String roles,
                                         @RequestParam(value="image", required = false) MultipartFile image) throws IOException {

        Set<Role> rolesFromForm = new HashSet<>();
        if(roles != null) {
            String[] rolesIds = roles.split(" ");

            for(int i = 0; i<rolesIds.length; i++){
                rolesFromForm.add(new Role(Integer.parseInt(rolesIds[i])));
            }
        }


        String fileName = "";

        if(!image.isEmpty()) {
            fileName = StringUtils.cleanPath(image.getOriginalFilename());

        }

        User usedSaved = service.saveUser(email, firstName, lastName, password, enabled, rolesFromForm, fileName, image);

        if(!image.isEmpty()) {

            String uploadDir = "TiendawebWebParent/TiendawebBackEnd/user-photos/" + usedSaved.getId();

            FileUploadUtil.cleanDir(uploadDir);
            FileUploadUtil.saveFile(uploadDir, fileName, image);
        }

        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/save").toString());
        return ResponseEntity.created(uri).body(usedSaved);
    }

    @PostMapping("/users/check_email/{id}")
    public ResponseEntity<Boolean> checkDuplicateEmail(@PathVariable("id") Integer id, @RequestBody String email) {
        boolean result = service.isEmailUnique(id, email);
        /*EmailDuplicated duplicated = null;
        if(result == true) {
       */
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/users/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Integer id,
                                           @RequestParam("email") String email,
                                           @RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("password") String password,
                                           @RequestParam("enabled") Boolean enabled,
                                           @RequestParam("roles") String roles,
                                           @RequestParam(value="image", required = false) MultipartFile image
                                           ) throws IOException {

        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/save").toString());

        Set<Role> rolesFromForm = new HashSet<>();

        if(roles != null) {
            String[] rolesIds = roles.split(" ");

            for(int i = 0; i<rolesIds.length; i++){
                rolesFromForm.add(new Role(Integer.parseInt(rolesIds[i])));
            }
        }

        String fileName = "";

        if(image != null) {
            if(!image.isEmpty()) {
                fileName = StringUtils.cleanPath(image.getOriginalFilename());

                String uploadDir = "TiendawebWebParent/TiendawebBackEnd/user-photos/" + id;

                FileUploadUtil.cleanDir(uploadDir);
                FileUploadUtil.saveFile(uploadDir, fileName, image);
            }
        }


        User userUpdated = service.updateUser(
                id,
                email,
                firstName,
                lastName,
                password,
                enabled,
                rolesFromForm,
                fileName,
                image
                );
        return ResponseEntity.created(uri).body(userUpdated);
    }

    @DeleteMapping("/users/delete/{id}")
    public void deleteUser(@PathVariable Integer id) {
        service.deleteUserById(id);
    }

    @GetMapping("/users/{id}/enabled/{status}")
    public void updateUserEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean enabled){
        service.updateUserEnabledStatus(id, enabled);
    }

    @GetMapping(path = "/users/image/{id}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("id") String id, @PathVariable("fileName") String fileName) throws IOException {

        Path path = Paths.get(
                System.getProperty("user.home") + "/Documents/TiendaWeb/TiendawebProject/TiendawebWebParent/TiendawebBackEnd/user-photos/" +
                        id +
                        "/" +
                        fileName);
        byte[] imagenBytes = Files.readAllBytes(path);

        return imagenBytes;
    }

    /*@GetMapping("/users/export/csv")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        List<User> listUsers = service.listAll();
        UserCsvExporter exporter = new UserCsvExporter();
        exporter.export(listUsers, response);
    } */

    @GetMapping("/users/export/csv")
    public ResponseEntity<Resource> exportCSV() throws IOException {
        final List<User> listUsers = service.listAll();
        UserCsvExporter exporter = new UserCsvExporter();

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dateFormatter.format(new Date());
        String filename = "users_" + timestamp + ".csv";

        final InputStreamResource resource = new InputStreamResource(exporter.load(listUsers));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    @GetMapping("/users/export/excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        List<User> listUsers = service.listAll();

        UserExcelExporter exporter = new UserExcelExporter();
        exporter.export(listUsers, response);
    }

    @GetMapping("/users/export/pdf")
    public void exportPDF(HttpServletResponse response) throws IOException {
        List<User> listUsers = service.listAll();

        UserPDFExporter exporter = new UserPDFExporter();
        exporter.export(listUsers, response);
    }

}
