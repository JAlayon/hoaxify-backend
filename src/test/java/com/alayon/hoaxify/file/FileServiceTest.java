package com.alayon.hoaxify.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.alayon.hoaxify.config.AppConfiguration;
import com.alayon.hoaxify.file.FileService;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class FileServiceTest {

	FileService fileService;

	AppConfiguration appConfiguration;

	@Before
	public void init() {
		appConfiguration = new AppConfiguration();
		appConfiguration.setUploadPath("uploads-test");

		fileService = new FileService(appConfiguration);

		new File(appConfiguration.getUploadPath()).mkdir();
		new File(appConfiguration.getFullProfileImagePath()).mkdir();
		new File(appConfiguration.getFullAttachmentPath()).mkdir();
	}

	@After
	public void cleanup() throws IOException {
		FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagePath()));
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentPath()));
	}

	@Test
	public void detectType_whenPngFileProvided_returnsImagePng() throws IOException {
		final ClassPathResource resourceFile = new ClassPathResource("test-png.png");
		final byte[] fileArr = FileUtils.readFileToByteArray(resourceFile.getFile());
		final String fileType = fileService.detectType(fileArr);
		assertThat(fileType).isEqualToIgnoringCase("image/png");
	}
}
