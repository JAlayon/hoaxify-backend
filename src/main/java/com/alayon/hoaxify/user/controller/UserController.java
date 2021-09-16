package com.alayon.hoaxify.user.controller;

import javax.validation.Valid;

import com.alayon.hoaxify.user.dto.UserRequest;
import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alayon.hoaxify.commons.CurrentUser;
import com.alayon.hoaxify.commons.GenericResponse;
import com.alayon.hoaxify.user.dto.UserResponse;
import com.alayon.hoaxify.user.dto.UserUpdateRequest;

import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/")
public class UserController {

	@Autowired
	UserService userService;

	@PostMapping("users")
	public GenericResponse createUser(@Valid @RequestBody final UserRequest userRequest) {
		userService.save(userRequest);
		return new GenericResponse("user saved");
	}

	@GetMapping("users")
	public Page<UserResponse> getUsers(@CurrentUser final User loggedInUser, final Pageable pageable) {
		return userService.getUsers(loggedInUser, pageable).map(UserResponse::new);
	}

	@GetMapping("users/{username}")
	public UserResponse getUserByUserName(@PathVariable final String username) {
		final User user = userService.getByUsername(username);
		return new UserResponse(user);
	}

	@PutMapping("/users/{id:[0-9]+}")
	@PreAuthorize("#id == principal.id")
	public UserResponse updateUser(@PathVariable final long id,
								   @Valid @RequestBody(required = false) final UserUpdateRequest userUpdate) {
		final User userUpdated = userService.update(id, userUpdate);
		return new UserResponse(userUpdated);
	}

}
