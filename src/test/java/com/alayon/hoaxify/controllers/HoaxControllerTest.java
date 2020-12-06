package com.alayon.hoaxify.controllers;

import static com.alayon.hoaxify.utils.TestUtil.authenticate;
import static com.alayon.hoaxify.utils.TestUtil.getValidHoax;
import static com.alayon.hoaxify.utils.TestUtil.getValidUser;
import static org.assertj.core.api.Assertions.assertThat;

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

import com.alayon.hoaxify.error.ApiError;
import com.alayon.hoaxify.hoax.Hoax;
import com.alayon.hoaxify.user.UserRepository;
import com.alayon.hoaxify.user.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")

public class HoaxControllerTest {

	private static final String API_HOAXES = "/api/v1/hoaxes";

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;

	@Before
	public void cleanup() {
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsAuthorized_receiveOk() {
		userService.save(getValidUser("user1"));
		authenticate(testRestTemplate, "user1");

		final Hoax hoax = getValidHoax();
		final ResponseEntity<Object> response = postHoax(hoax, Object.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsUnauthorized_receiveUnauthorized() {

		final Hoax hoax = getValidHoax();
		final ResponseEntity<Object> response = postHoax(hoax, Object.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void postHoax_whenHoaxIsValidAndUserIsUnauthorized_receiveApiError() {

		final Hoax hoax = getValidHoax();
		final ResponseEntity<ApiError> response = postHoax(hoax, ApiError.class);

		assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}

	private <T> ResponseEntity<T> postHoax(final Hoax hoax, final Class<T> responseType) {
		return testRestTemplate.postForEntity(API_HOAXES, hoax, responseType);
	}

}
