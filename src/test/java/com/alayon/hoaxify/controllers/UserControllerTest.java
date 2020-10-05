package com.alayon.hoaxify.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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
		final ResponseEntity<?> response = testRestTemplate.postForEntity(API_USERS, user, null);
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
		final ResponseEntity<GenericResponse> response = testRestTemplate.postForEntity(API_USERS, user,
				GenericResponse.class);
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

	private User getValidUser() {
		final User user = new User();
		user.setUsername("test-user");
		user.setDisplayname("display-name");
		user.setPassword("P4ssword");
		return user;
	}

}
