package com.alayon.hoaxify.user;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

	@Autowired
	UserRepository userRepository;

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		final User userInDb = userRepository.findByUsername(value);
		if (userInDb == null)
			return true;
		return false;
	}

}
