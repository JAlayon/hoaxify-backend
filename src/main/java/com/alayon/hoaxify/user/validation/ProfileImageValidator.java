package com.alayon.hoaxify.user.validation;

import java.util.Base64;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.alayon.hoaxify.file.FileService;

public class ProfileImageValidator implements ConstraintValidator<ProfileImage, String> {

	@Autowired
	FileService fileService;

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}

		final byte[] decodedBytes = Base64.getDecoder().decode(value);
		final String fileType = fileService.detectType(decodedBytes);
		if (fileType.equalsIgnoreCase("image/png") || fileType.equalsIgnoreCase("image/jpeg")) {
			return true;
		}
		return false;
	}

}
