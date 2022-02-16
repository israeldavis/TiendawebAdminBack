package com.tiendaweb.admin.user;

import com.tiendaweb.admin.FileUploadUtil;
import com.tiendaweb.common.entity.Role;
import com.tiendaweb.common.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@RestController
public class AccountController {

    @Autowired
    private UserService service;

    @GetMapping("/users/account/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Integer id) {
        User user = service.getUserById(id);

        return ResponseEntity.ok().body(user);
    }

    @PutMapping("/account/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Integer id,
                                           @RequestParam("email") String email,
                                           @RequestParam("firstName") String firstName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("password") String password,
                                           @RequestParam("enabled") Boolean enabled,
                                           @RequestParam("roles") String roles,
                                           @RequestParam(value="image", required = false) MultipartFile image
    ) throws IOException {

        User userUpdated = service.updateAccount(
                id,
                firstName,
                lastName,
                password,
                image
        );

        return ResponseEntity.ok().body(userUpdated);
    }
}
