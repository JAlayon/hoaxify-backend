package com.alayon.hoaxify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "hoaxify")
@Data
public class AppConfiguration {

	private String uploadPath;
	private String profileImages;
	private String attachments;

	public String getFullProfileImagePath() {
		return new StringBuilder().append(uploadPath).append("/").append(profileImages).toString();
	}

	public String getFullAttachmentPath() {
		return new StringBuilder().append(uploadPath).append("/").append(attachments).toString();
	}
}
