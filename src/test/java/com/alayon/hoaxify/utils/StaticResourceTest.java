package com.alayon.hoaxify.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.alayon.hoaxify.config.AppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StaticResourceTest {

	@Autowired
	AppConfiguration appConfig;

	@Test
	public void checkStaticFolder_whenAppIsInitialized_uploadFolderMostExist() {
		final File uploadFolder = new File(appConfig.getUploadPath());
		final boolean uploadFolderExists = uploadFolder.exists() && uploadFolder.isDirectory();
		assertThat(uploadFolderExists).isTrue();
	}

	@Test
	public void checkStaticFolder_whenAppIsInitialized_profileImageSubFolderMustExist() {
		final String profileImageFolderPath = appConfig.getFullProfileImagePath();
		final File profileImageFolder = new File(profileImageFolderPath);
		final boolean profileFolderExists = profileImageFolder.exists() && profileImageFolder.isDirectory();
		assertThat(profileFolderExists).isTrue();
	}

	@Test
	public void checkStaticFolder_whenAppIsInitialized_attachmentsSubFolderMustExist() {
		final String attachmentsFolderPath = appConfig.getFullAttachmentPath();
		final File attachmentsFolder = new File(attachmentsFolderPath);
		final boolean attachmentsFolderExist = attachmentsFolder.exists() && attachmentsFolder.isDirectory();
		assertThat(attachmentsFolderExist).isTrue();
	}
}
