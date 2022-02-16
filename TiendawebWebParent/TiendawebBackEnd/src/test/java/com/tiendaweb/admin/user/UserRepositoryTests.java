package com.tiendaweb.admin.user;

import com.tiendaweb.common.entity.Role;
import com.tiendaweb.common.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class UserRepositoryTests {

    @Autowired
    private UserRepository repo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testCreateUserWithOneRole() {
        Role roleAdmin = entityManager.find(Role.class, 1);
        User userAdmin = new User("dvences90@gmail.com", "david2021", "David", "Vences");
        userAdmin.addRole(roleAdmin);

        User savedUser = repo.save(userAdmin);

        assertThat(savedUser.getId()).isGreaterThan(0);
    }

    @Test
    public void testCreateNewUserWithTwoRoles() {
        User userTwoRoles = new User("ravi@gmail.com", "$2a$10$7FUsW.Y8QlHwktLpEHHh..jnCZSTA.htbiNTUp60pQ.LzTkRTcP4G", "Ravi", "Kumar");
        Role roleEditor = new Role(3);
        Role roleAssistant = new Role(5);

        userTwoRoles.addRole(roleEditor);
        userTwoRoles.addRole(roleAssistant);

        User savedUser = repo.save(userTwoRoles);

        assertThat(savedUser.getId()).isGreaterThan(0);
    }

    @Test
    public void testListAllUsers() {
        List<User> listUsers = repo.findAll();
        listUsers.forEach(user -> System.out.println(user));
    }

    @Test
    public void testGetUserById() {
        User userAdmin = repo.findById(1).get();
        System.out.println(userAdmin);
        assertThat(userAdmin).isNotNull();
    }

    @Test
    public void testUpdateUserDetails() {
        User userAdmin = repo.findById(1).get();
        userAdmin.setEnabled(true);
        userAdmin.setEmail("israelvencesmondragon@gmail.com");

        repo.save(userAdmin);
    }

    @Test
    public void testUpdateUserRoles() {
        User userRavi = repo.findById(2).get();
        Role roleEditor = new Role(3);
        Role roleSalesperson = new Role(2);

        userRavi.getRoles().remove(roleEditor);
        userRavi.addRole(roleSalesperson);

        repo.save(userRavi);
    }

    @Test
    public void testDeleteUser() {
        Integer userId = 2;
        repo.deleteById(userId);
    }

    @Test
    public void testGetUserByEmail() {
        String email = "ravi@gmail.com";

        User user = repo.findByEmail(email);

        assertThat(user).isNotNull();
    }

    @Test
    public void testCountById() {
        Integer id = 1;
        Long countById = repo.countById(id);

        assertThat(countById).isNotNull().isGreaterThan(0);
    }

    @Test
    public void testDisableUser() {
        Integer id = 1;
        repo.updateEnabledStatus(id, false);
    }

    @Test
    public void testEnableUser() {
        Integer id = 3;
        repo.updateEnabledStatus(id, true);
    }

    @Test
    public void testListFirstPage() {
        int pageNumber = 1;
        int pageSize = 4;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> page = repo.findAll(pageable);

        List<User> listUsers = page.getContent();

        listUsers.forEach(user -> System.out.println(user));

        assertThat(listUsers.size()).isEqualTo(pageSize);
    }

    @Test
    public void testSearchUsers() {
        String keyword = "bruce";
        int pageNumber = 0;
        int pageSize = 4;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> page = repo.findAll(keyword, pageable);

        List<User> listUsers = page.getContent();

        listUsers.forEach( user -> System.out.println(user));

        assertThat(listUsers.size()).isGreaterThan(0);
    }
}
