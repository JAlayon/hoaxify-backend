package com.alayon.hoaxify.commons;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenericResponse {

	private String message;

	public GenericResponse(final String message) {
		this.message = message;
	}
}
