package com.alayon.hoaxify.controllers;

import com.alayon.hoaxify.user.User;

public class TestUtil {

	public static User getValidUser() {
		final User user = new User();
		user.setUsername("test-user");
		user.setDisplayname("display-name");
		user.setPassword("P4ssword");
		return user;
	}
}
