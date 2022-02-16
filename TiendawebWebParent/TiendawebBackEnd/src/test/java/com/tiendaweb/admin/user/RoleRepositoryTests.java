package com.tiendaweb.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.tiendaweb.common.entity.Role;
import org.springframework.test.annotation.Rollback;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class RoleRepositoryTests {
	
	@Autowired
	private RoleRepository repo;
	
	@Test
	public void testCreateFirstRole() {
		
		Role roleAdmin = new Role("Admin", "Maneja todo");
		Role savedRole = repo.save(roleAdmin);
		
		assertThat(savedRole.getId()).isGreaterThan(0);
		
	}

	@Test
	public void testCreateRestRoles() {
		Role roleSalesperson = new Role("Salesperson",
				"Maneja product price, customers, shipping, orders and report");
		Role roleEditor = new Role("Editor",
				"Maneja categories, brands, products, articles and menus");
		Role roleShipper = new Role("Shipper",
				"View orders, view products and update order status");
		Role roleAssistant = new Role("Assistant",
				"Maneja questions and reviews");

		repo.saveAll(List.of(roleSalesperson, roleEditor, roleShipper, roleAssistant));
	}

}
