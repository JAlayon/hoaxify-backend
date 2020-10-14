package com.alayon.hoaxify.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

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
import com.alayon.hoaxify.user.User;
import com.alayon.hoaxify.user.UserRepository;
import com.alayon.hoaxify.user.UserService;

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
	public void postLogin_whithoutUserCredentials_receiveUnauthorized() {
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
	public void postLogin_withIncorrectCredentials_receiveApiError() {
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
		final User user = TestUtil.getValidUser();
		userService.save(user);
		authentication();
		final ResponseEntity<?> response = login(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void postLogin_withValidCredentials_receiveLoggedInUserId() {
		final User user = TestUtil.getValidUser();
		final User userInDb = userService.save(user);
		authentication();
		final ResponseEntity<Map<String, Object>> response = login(
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		final Map<String, Object> body = response.getBody();
		final Integer userId = (Integer) body.get("id");
		assertThat(userId.longValue()).isEqualTo(userInDb.getId());
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
