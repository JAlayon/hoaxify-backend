package com.alayon.hoaxify.file;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.alayon.hoaxify.config.AppConfiguration;

@Service
public class FileService {

	AppConfiguration appConfiguration;

	public FileService(final AppConfiguration appConfiguration) {
		this.appConfiguration = appConfiguration;
	}

	public String saveProfileImage(final String base64Image) throws IOException {
		final String imageName = UUID.randomUUID().toString().replaceAll("-", "");
		final byte[] decodeBytes = Base64.getDecoder().decode(base64Image);
		final File target = new File(appConfiguration.getFullProfileImagePath() + "/" + imageName);
		FileUtils.writeByteArrayToFile(target, decodeBytes);
		return imageName;
	}
}
