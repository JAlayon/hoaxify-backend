package com.alayon.hoaxify.utils;

import com.alayon.hoaxify.user.dto.UserRequest;
import org.junit.Ignore;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;

import com.alayon.hoaxify.hoax.model.Hoax;
import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.user.dto.UserUpdateRequest;

@Ignore
public class TestUtil {

	public static UserRequest getValidUserForRequest() {
		final UserRequest user = new UserRequest();
		user.setUsername("test-user");
		user.setDisplayname("display-name");
		user.setPassword("P4ssword");
		return user;
	}

	public static User getValidUser(){
		final User user = new User();
		user.setUsername("test-user");
		user.setDisplayname("display-name");
		user.setPassword("P4ssword");
		user.setImage("my-image.png");
		return user;
	}

	public static UserRequest getValidUserForRequest(final String username) {
		final UserRequest user = getValidUserForRequest();
		user.setUsername(username);
		return user;
	}

	public static User getValidUser(final String username){
		final User user = getValidUser();
		user.setUsername(username);
		return user;
	}

	public static UserUpdateRequest getValidUserUpdate() {
		final UserUpdateRequest userUpdate = new UserUpdateRequest();
		userUpdate.setDisplayName("new display name");
		return userUpdate;
	}

	public static void authenticate(final TestRestTemplate testRestTemplate, final String username) {
		testRestTemplate.getRestTemplate().getInterceptors()
				.add(new BasicAuthenticationInterceptor(username, "P4ssword"));
	}

	public static Hoax getValidHoax() {
		final Hoax hoax = new Hoax();
		hoax.setContent("Set content for test hoax");
		return hoax;
	}

}
