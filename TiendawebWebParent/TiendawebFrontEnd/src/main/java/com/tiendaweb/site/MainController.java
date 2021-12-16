package com.tiendaweb.site;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
	
	@GetMapping("")
	public String viewHomePage() {
		return "<h1>Welcome to Front Home Page</h1>";
	}

}
