package com.alayon.hoaxify.user.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = UniqueUsernameValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUsername {

	/**
	 * These fields are required by bean validation specification.
	 *
	 */

	String message() default "{hoaxify.constraints.username.UniqueUsername.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
