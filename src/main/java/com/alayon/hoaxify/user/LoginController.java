package com.alayon.hoaxify.user;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

	@PostMapping("/api/v1/login")
	public Map<String, Object> handleLogin(final Authentication authentication) {
		final User userLogged = (User) authentication.getPrincipal();
		return Collections.singletonMap("id", userLogged.getId());
	}

}
