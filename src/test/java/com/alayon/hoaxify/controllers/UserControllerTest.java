package com.alayon.hoaxify.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.alayon.hoaxify.commons.GenericResponse;
import com.alayon.hoaxify.error.ApiError;
import com.alayon.hoaxify.user.User;
import com.alayon.hoaxify.user.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerTest {

	private static final String API_USERS = "/api/v1/users";

	@Autowired
	TestRestTemplate testRestTemplate;

	@Autowired
	UserRepository userRepository;

	@Before
	public void cleanup() {
		userRepository.deleteAll();
	}

	@Test
	public void postUser_whenUserIsValid_receiveOk() {
		final User user = getValidUser();
		final ResponseEntity<?> response = postSignup(user, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void postUser_whenUserIsValid_userSavedToDatabase() {
		final User user = getValidUser();
		testRestTemplate.postForEntity(API_USERS, user, null);
		assertThat(userRepository.count()).isEqualTo(1);

	}

	@Test
	public void postUser_whenUserIsValid_receiveSuccessMessage() {
		final User user = getValidUser();
		final ResponseEntity<GenericResponse> response = postSignup(user, GenericResponse.class);
		assertThat(response.getBody().getMessage()).isNotNull();
	}

	@Test
	public void postUser_whenUserIsValid_passwordIsHasheedInDb() {
		final User user = getValidUser();
		testRestTemplate.postForEntity(API_USERS, user, null);
		final List<User> users = userRepository.findAll();
		final User userInDb = users.get(0);
		assertThat(userInDb.getPassword()).isNotEqualTo(user.getPassword());
	}

	@Test
	public void postUser_whenUserHasNullUsername_receiveBadRequest() {
		final User user = getValidUser();
		user.setUsername(null);
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasNullDisplayname_receiveBadRequest() {
		final User user = getValidUser();
		user.setDisplayname(null);
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasNullPassword_receiveBadRequest() {
		final User user = getValidUser();
		user.setPassword(null);
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest() {
		final User user = getValidUser();
		user.setUsername("abc");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasDisplaynameWithLessThanRequired_receiveBadRequest() {
		final User user = getValidUser();
		user.setDisplayname("abc");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest() {
		final User user = getValidUser();
		user.setPassword("P4ssw");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest() {
		final User user = getValidUser();
		final String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setUsername(valueOf256Chars);
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasDisplaynameExceedsTheLengthLimit_receiveBadRequest() {
		final User user = getValidUser();
		final String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setDisplayname(valueOf256Chars);
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordExceedsTheLengthLimit_receiveBadRequest() {
		final User user = getValidUser();
		final String valueOf256Chars = IntStream.rangeClosed(1, 256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setPassword(valueOf256Chars + "A1");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithAllLowercase_receiveBadRequest() {
		final User user = getValidUser();
		user.setPassword("alllowercase");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithAllUppercase_receiveBadRequest() {
		final User user = getValidUser();
		user.setPassword("ALLUPPERCASE");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithAllNumbers_receiveBadRequest() {
		final User user = getValidUser();
		user.setPassword("123456789");
		final ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasntBody_receiveApiError() {
		final User user = new User();
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		assertThat(response.getBody().getUrl()).isEqualTo(API_USERS);
	}

	@Test
	public void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername() {
		final User user = getValidUser();
		user.setUsername(null);
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
	}

	@Test
	public void postUser_whenUserHasNullPassword_receiveMessageOfNullError() {
		final User user = getValidUser();
		user.setPassword(null);
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("password")).isEqualTo("Cannot be null");
	}

	@Test
	public void postUser_whenUserHasInvalidLengthUsername_receiveGenericMessageOfSizeError() {
		final User user = getValidUser();
		user.setUsername("abc");
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("It must have minimum 4 and maximum 255 characters");
	}

	@Test
	public void postUser_whenUserHasInvalidPasswordPattern_receiveMessageOfPasswordPatternError() {
		final User user = getValidUser();
		user.setPassword("alllowercase");
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("password"))
				.isEqualTo("Password must have at least one uppercase, one lowercase letter and one number");
	}

	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
		userRepository.save(getValidUser());
		final User user = getValidUser();
		final ResponseEntity<?> response = postSignup(user, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveMessageOfDuplicateUsername() {
		userRepository.save(getValidUser());
		final User user = getValidUser();
		final ResponseEntity<ApiError> response = postSignup(user, ApiError.class);
		final Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("username")).isEqualTo("This name is in used");

	}

	private <T> ResponseEntity<T> postSignup(final Object request, final Class<T> response) {
		return testRestTemplate.postForEntity(API_USERS, request, response);
	}

	private User getValidUser() {
		final User user = new User();
		user.setUsername("test-user");
		user.setDisplayname("display-name");
		user.setPassword("P4ssword");
		return user;
	}

}
