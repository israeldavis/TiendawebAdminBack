package com.tiendaweb.admin.user;

import com.tiendaweb.admin.FileUploadUtil;
import com.tiendaweb.admin.filter.CustomAuthenticationFilter;
import com.tiendaweb.common.entity.Role;
import com.tiendaweb.common.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Primary
@Transactional
public class UserService implements UserDetailsService {

    public static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final int USERS_PER_PAGE = 5;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> listAll() {
        return userRepo.findAll(Sort.by("firstName").ascending());
    }

    public Page<User> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
        Sort sort = Sort.by(sortField);

        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNum - 1, USERS_PER_PAGE, sort);

        if(keyword != null && !keyword.equals("null")) {
            return userRepo.findAll(keyword, pageable);
        }

        return userRepo.findAll(pageable);
    }

    public User getUserById(Integer id) {
        Optional<User> optionalUser =  userRepo.findById(id);
        if(!optionalUser.isPresent()) {
            return null;
        }
        return optionalUser.get();
    }

    public User getUserByEmail(String email) {
        User user = userRepo.findByEmail(email);
        if(user == null) {
            return null;
        }
        return user;
    }

    public void deleteUserById(Integer id) {
        userRepo.deleteById(id);
    }

    public User updateAccount(Integer id, String firstName, String lastName, String password,  MultipartFile image) throws IOException {
        User existingUserInDB = getUserById(id);

        if(!password.isEmpty()) {
            existingUserInDB.setPassword(password);
            encodePassword(existingUserInDB);
        }

        String fileName = "";

        if(image != null) {
            if(!image.isEmpty()) {
                fileName = StringUtils.cleanPath(image.getOriginalFilename());

                String uploadDir = "TiendawebWebParent/TiendawebBackEnd/user-photos/" + id;

                FileUploadUtil.cleanDir(uploadDir);
                FileUploadUtil.saveFile(uploadDir, fileName, image);
            }

            if(!fileName.equals("")) {
                existingUserInDB.setPhotos(fileName);
            }
        }

        existingUserInDB.setFirstName(firstName);
        existingUserInDB.setLastName(lastName);

        return userRepo.save(existingUserInDB);
    }

    public User updateUser(Integer id, String email, String firstName, String lastName, String password, Boolean enabled,
                           Set<Role> roles, String photosFileName, MultipartFile image) {

        //log.info("Guardando nuevo usuario {} en la BD", user.getName());
        boolean isUpdatingUser = (id != null && id != 0);

        if(isUpdatingUser) {
            User existingUser = getUserById(id);
            existingUser.setEmail(email);
            existingUser.setFirstName(firstName);
            existingUser.setLastName(lastName);
            existingUser.setRoles(roles);
            existingUser.setEnabled(enabled);
            if(!photosFileName.equals("")) {
                existingUser.setPhotos(photosFileName);
            }

            if(password != null || !password.equals("null") || !password.isEmpty()){
                existingUser.setPassword(password);
                encodePassword(existingUser);
            }

            return userRepo.save(existingUser);

        }
        return new User();
    }

    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }


    public User saveUser( String email, String firstName, String lastName, String password, Boolean enabled,
                         Set<Role> roles, String photosFileName, MultipartFile image) {

        User newUser = new User(email, password, firstName, lastName);
        newUser.setRoles(roles);
        newUser.setEnabled(enabled);
        newUser.setPhotos(photosFileName);

        encodePassword(newUser);
        return userRepo.save(newUser);


    }

    private void encodePassword(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }

    public boolean isEmailUnique(Integer id, String email) {
        User userByEmail = userRepo.findByEmail(email);

        if(userByEmail == null) return true;

        boolean isCreatingNew = (id == 0);

        if(isCreatingNew) {
            if(userByEmail != null) return false;
        } else {
            if(userByEmail.getId() != id) {
                return false;
            }
        }

        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username);
        if(user == null) {
            throw new UsernameNotFoundException("Usuario no encontrado en la BD");
        } else {
            logger.info("Usuario {} encontrado en la BD", username);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    public void updateUserEnabledStatus(Integer id, boolean enabled) {
        userRepo.updateEnabledStatus(id, enabled);
    }

}
