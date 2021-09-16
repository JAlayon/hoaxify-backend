package com.alayon.hoaxify.login.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.alayon.hoaxify.user.dto.UserRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.alayon.hoaxify.error.ApiError;
import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.user.repository.UserRepository;
import com.alayon.hoaxify.user.service.UserService;
import com.alayon.hoaxify.utils.TestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginControllerTest {

	private static final String API_V1_LOGIN = "/api/v1/login";

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Before
	public void cleanup() {
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}

	@Test
	public void postLogin_withoutUserCredentials_receiveUnauthorized() {
		final ResponseEntity<?> response = login(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void postLogin_withIncorrectCredentials_receiveUnauthorized() {
		authentication();
		final ResponseEntity<?> response = login(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void postLogin_withoutUserCredentials_receiveApiError() {
		final ResponseEntity<ApiError> response = login(ApiError.class);
		assertThat(response.getBody().getUrl()).isEqualTo(API_V1_LOGIN);
	}

	@Test
	public void postLogin_withoutUserCredentials_receiveApiErrorWithoutValidationErrors() {
		final ResponseEntity<String> response = login(String.class);
		assertThat(response.getBody().contains("validationErrors")).isFalse();
	}

	@Test
	public void postLogin_withIncorrectCredentials_receiveUnauthorizedWithoutWWWAuthenticationHeader() {
		authentication();
		final ResponseEntity<?> response = login(Object.class);
		assertThat(response.getHeaders().containsKey("WWW-Authenticate")).isFalse();
	}

	@Test
	public void postLogin_withValidCredentials_receiveOk() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userService.save(userRequest);
		authentication();
		final ResponseEntity<?> response = login(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void postLogin_withValidCredentials_receiveLoggedInUserId() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final User userInDb = userService.save(userRequest);
		authentication();
		final ResponseEntity<Map<String, Object>> response = login(
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		final Map<String, Object> body = response.getBody();
		final Integer userId = (Integer) body.get("id");
		assertThat(userId.longValue()).isEqualTo(userInDb.getId());
	}

	@Test
	public void postLogin_withValidCredentials_receiveLoggedInUserImage() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final User userInDb = userService.save(userRequest);
		authentication();
		final ResponseEntity<Map<String, Object>> response = login(
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		final Map<String, Object> body = response.getBody();
		final String image = (String) body.get("image");
		assertThat(image).isEqualTo(userInDb.getImage());
	}

	@Test
	public void postLogin_withValidCredentials_receiveLoggedInUserDisplayName() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final User userInDb = userService.save(userRequest);
		authentication();
		final ResponseEntity<Map<String, Object>> response = login(
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		final Map<String, Object> body = response.getBody();
		final String displayName = (String) body.get("displayname");
		assertThat(displayName).isEqualTo(userInDb.getDisplayname());
	}

	@Test
	public void postLogin_withValidCredentials_receiveLoggedInUserUsername() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		final User userInDb = userService.save(userRequest);
		authentication();
		final ResponseEntity<Map<String, Object>> response = login(
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		final Map<String, Object> body = response.getBody();
		final String username = (String) body.get("username");
		assertThat(username).isEqualTo(userInDb.getUsername());
	}

	@Test
	public void postLogin_withValidCredentials_notReceiveLoggedInUsersPassword() {
		final UserRequest userRequest = TestUtil.getValidUserForRequest();
		userService.save(userRequest);
		authentication();
		final ResponseEntity<Map<String, Object>> response = login(
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		final Map<String, Object> body = response.getBody();
		assertThat(body.containsKey("password")).isFalse();
	}

	private void authentication() {
		testRestTemplate.getRestTemplate().getInterceptors()
				.add(new BasicAuthenticationInterceptor("test-user", "P4ssword"));
	}

	public <T> ResponseEntity<T> login(final Class<T> responseType) {
		return testRestTemplate.postForEntity(API_V1_LOGIN, null, responseType);
	}

	public <T> ResponseEntity<T> login(final ParameterizedTypeReference<T> responseType) {
		return testRestTemplate.exchange(API_V1_LOGIN, HttpMethod.POST, null, responseType);
	}

}
