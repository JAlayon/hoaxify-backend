package com.alayon.hoaxify.user.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.alayon.hoaxify.user.dto.UserRequest;
import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.alayon.hoaxify.commons.CurrentUser;
import com.alayon.hoaxify.commons.GenericResponse;
import com.alayon.hoaxify.error.ApiError;
import com.alayon.hoaxify.user.dto.UserDto;
import com.alayon.hoaxify.user.dto.UserUpdateDto;

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
	public Page<UserDto> getUsers(@CurrentUser final User loggedInUser, final Pageable pageable) {
		return userService.getUsers(loggedInUser, pageable).map(UserDto::new);
	}

	@GetMapping("users/{username}")
	public UserDto getUserByUserName(@PathVariable final String username) {
		final User user = userService.getByUsername(username);
		return new UserDto(user);
	}

	@PutMapping("/users/{id:[0-9]+}")
	@PreAuthorize("#id == principal.id")
	public UserDto updateUser(@PathVariable final long id,
			@Valid @RequestBody(required = false) final UserUpdateDto userUpdate) {
		final User userUpdated = userService.update(id, userUpdate);
		return new UserDto(userUpdated);
	}

}
