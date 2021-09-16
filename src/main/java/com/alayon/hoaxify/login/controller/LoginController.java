package com.alayon.hoaxify.login.controller;

import com.alayon.hoaxify.user.jsonviews.Views;
import com.alayon.hoaxify.user.model.User;
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
