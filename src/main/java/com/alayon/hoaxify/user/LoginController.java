package com.alayon.hoaxify.user;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alayon.hoaxify.commons.CurrentUser;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class LoginController {

	@PostMapping("/api/v1/login")
	@JsonView(Views.Base.class)
	public User handleLogin(@CurrentUser final User loggedInUser) {
		return loggedInUser;
	}

}
