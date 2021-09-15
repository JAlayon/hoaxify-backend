package com.alayon.hoaxify;

import java.util.stream.IntStream;

import com.alayon.hoaxify.user.dto.UserRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.alayon.hoaxify.user.model.User;
import com.alayon.hoaxify.user.service.UserService;

@SpringBootApplication
public class HoaxifyApplication {

	public static void main(final String[] args) {
		SpringApplication.run(HoaxifyApplication.class, args);
	}

	@Bean
	@Profile("dev")
	CommandLineRunner run(final UserService userService) {
		return (args) -> {
			IntStream.rangeClosed(1, 15).mapToObj(i -> {
				final UserRequest user = new UserRequest();
				user.setUsername("user" + i);
				user.setDisplayname("displayname" + i);
				user.setPassword("P4ssword");
				return user;
			}).forEach(userService::save);
		};
	}

}