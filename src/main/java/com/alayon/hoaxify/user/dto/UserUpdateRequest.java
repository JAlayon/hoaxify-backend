package com.alayon.hoaxify.user.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.alayon.hoaxify.user.validation.ProfileImage;

import lombok.Data;

@Data
public class UserUpdateRequest {

	@NotNull
	@Size(min = 4, max = 255)
	private String displayName;

	@ProfileImage
	private String image;
}
