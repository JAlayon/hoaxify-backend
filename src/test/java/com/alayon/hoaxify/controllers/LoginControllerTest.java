package com.alayon.hoaxify.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.alayon.hoaxify.error.ApiError;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginControllerTest {

	private static final String API_V1_LOGIN = "/api/v1/login";

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void postLogin_whithoutUserCredentials_receiveUnauthorized() {
		final ResponseEntity<?> response = login(null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void postLogin_withIncorrectCredentials_receiveUnauthorized() {
		authentication();
		final ResponseEntity<?> response = login(null);
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
		final ResponseEntity<?> response = login(null);
		assertThat(response.getHeaders().containsKey("WWW-Authenticate")).isFalse();
	}

	private void authentication() {
		testRestTemplate.getRestTemplate().getInterceptors()
				.add(new BasicAuthenticationInterceptor("test-user", "P4ssword"));
	}

	public <T> ResponseEntity<T> login(final Class<T> responseType) {
		return testRestTemplate.postForEntity(API_V1_LOGIN, null, responseType);
	}
}
