package com.alayon.hoaxify.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.alayon.hoaxify.config.AppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StaticResourceTest {

	@Autowired
	AppConfiguration appConfig;

	@Autowired
	MockMvc mock;

	@After
	public void cleanup() throws IOException {
		FileUtils.cleanDirectory(new File(appConfig.getFullProfileImagePath()));
		FileUtils.cleanDirectory(new File(appConfig.getFullAttachmentPath()));
	}

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

	@Test
	public void getStaticFile_whenImageExistInProfileUploadFolder_receiveOk() throws Exception {
		final String fileName = "user-profile.png";
		final File source = new ClassPathResource(fileName).getFile();
		final File target = new File(appConfig.getFullProfileImagePath() + "/" + fileName);
		FileUtils.copyFile(source, target);

		mock.perform(get("/images/" + appConfig.getProfileImagesFolder() + "/" + fileName)).andExpect(status().isOk());
	}

	@Test
	public void getStaticFile_whenImageExistInAttachmentFolder_receiveOk() throws Exception {
		final String fileName = "user-profile.png";
		final File source = new ClassPathResource(fileName).getFile();
		final File target = new File(appConfig.getFullAttachmentPath() + "/" + fileName);
		FileUtils.copyFile(source, target);

		mock.perform(get("/images/" + appConfig.getAttachmentsFolder() + "/" + fileName)).andExpect(status().isOk());
	}

	@Test
	public void getStaticFile_whenKImageDoesNotExist_receiveNotFound() throws Exception {
		mock.perform(get("/images/" + appConfig.getAttachmentsFolder() + "/no-image.png"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void getStaticFile_whenImageExistInAttachmentFolder_receiveOkWithCacheHeaders() throws Exception {
		final String fileName = "user-profile.png";
		final File source = new ClassPathResource(fileName).getFile();
		final File target = new File(appConfig.getFullAttachmentPath() + "/" + fileName);
		FileUtils.copyFile(source, target);

		final MvcResult result = mock.perform(get("/images/" + appConfig.getAttachmentsFolder() + "/" + fileName))
				.andExpect(status().isOk()).andReturn();

		final String cacheControl = result.getResponse().getHeaderValue("Cache-Control").toString();
		assertThat(cacheControl).containsIgnoringCase("max-age=31536000");
	}
}
