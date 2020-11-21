package com.alayon.hoaxify.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

	@Autowired
	AppConfiguration appConfig;

	@Bean
	CommandLineRunner createUploadFolder() {
		return args -> {
			createNonExistingFolder(appConfig.getUploadPath());
			createNonExistingFolder(appConfig.getFullProfileImagePath());
			createNonExistingFolder(appConfig.getFullAttachmentPath());
		};

	}

	private void createNonExistingFolder(final String path) {
		final File folder = new File(path);
		final boolean folderExists = folder.exists() && folder.isDirectory();
		if (!folderExists) {
			folder.mkdir();
		}
	}
}
