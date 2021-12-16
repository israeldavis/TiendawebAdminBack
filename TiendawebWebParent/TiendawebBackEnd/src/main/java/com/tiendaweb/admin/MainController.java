package com.tiendaweb.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	
	@GetMapping("")
	public String viewHomePage() {
		return "Welcome to Admin Home Page";
	}

}
