package com.alayon.hoaxify.user;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.alayon.hoaxify.commons.CurrentUser;
import com.alayon.hoaxify.commons.GenericResponse;
import com.alayon.hoaxify.error.ApiError;
import com.alayon.hoaxify.user.dto.UserDto;

@RestController
@RequestMapping("/api/v1/")
public class UserController {

	@Autowired
	UserService userService;

	@PostMapping("users")
	public GenericResponse createUser(@Valid @RequestBody final User user) {
		userService.save(user);
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

	@ExceptionHandler({ MethodArgumentNotValidException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleValidationError(final MethodArgumentNotValidException e, final HttpServletRequest request) {
		final ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "Validation Error",
				request.getServletPath());
		final BindingResult result = e.getBindingResult();
		final Map<String, String> validationErrors = new HashMap<>();
		for (final FieldError fieldError : result.getFieldErrors()) {
			validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		error.setValidationErrors(validationErrors);
		return error;
	}
}
