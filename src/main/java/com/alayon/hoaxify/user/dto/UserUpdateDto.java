package com.alayon.hoaxify.user.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UserUpdateDto {

	@NotNull
	@Size(min = 4, max = 255)
	private String displayName;

	private String image;
}
