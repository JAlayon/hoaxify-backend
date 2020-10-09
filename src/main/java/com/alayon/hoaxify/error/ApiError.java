package com.alayon.hoaxify.error;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class ApiError {

	private final long timestamp;
	private int status;
	private String message;
	private String url;
	private Map<String, String> validationErrors;

	{
		timestamp = new Date().getTime();
	}

	public ApiError(final int status, final String message, final String url) {
		this.status = status;
		this.message = message;
		this.url = url;
	}

}
