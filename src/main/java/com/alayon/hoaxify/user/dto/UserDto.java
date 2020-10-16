package com.alayon.hoaxify.user.dto;

import com.alayon.hoaxify.user.User;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {

	private long id;
	private String username;
	private String displayName;
	private String image;

	public UserDto(final User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.displayName = user.getDisplayname();
		this.image = user.getImage();
	}
}
