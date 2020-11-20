package com.alayon.hoaxify.controllers;

import org.junit.Ignore;

import com.alayon.hoaxify.user.User;
import com.alayon.hoaxify.user.dto.UserUpdateDto;

@Ignore
public class TestUtil {

	public static User getValidUser() {
		final User user = new User();
		user.setUsername("test-user");
		user.setDisplayname("display-name");
		user.setPassword("P4ssword");
		user.setImage("profile-image.png");
		return user;
	}

	public static User getValidUser(final String username) {
		final User user = getValidUser();
		user.setUsername(username);
		return user;
	}

	public static UserUpdateDto getValidUserUpdate() {
		final UserUpdateDto userUpdate = new UserUpdateDto();
		userUpdate.setDisplayName("new display name");
		return userUpdate;
	}
}
