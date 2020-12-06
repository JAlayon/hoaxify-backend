package com.alayon.hoaxify.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.alayon.hoaxify.user.User;
import com.alayon.hoaxify.user.UserRepository;
import com.alayon.hoaxify.utils.TestUtil;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

	@Autowired
	TestEntityManager entityManager;

	@Autowired
	UserRepository userRepository;

	@Test
	public void findByUsername_whenUserExists_returnUser() {
		final User user = TestUtil.getValidUser();

		entityManager.persist(user);

		final User userInDB = userRepository.findByUsername("test-user");
		assertThat(userInDB).isNotNull();
	}

	@Test
	public void findByUsername_whenUserDoesntExist_returnsNull() {
		final User userInDB = userRepository.findByUsername("user-name");
		assertThat(userInDB).isNull();

	}

}
