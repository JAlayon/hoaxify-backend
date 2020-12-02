package com.alayon.hoaxify.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import com.alayon.hoaxify.config.AppConfiguration;

@Service
public class FileService {

	AppConfiguration appConfiguration;

	Tika tika;

	public FileService(final AppConfiguration appConfiguration) {
		this.appConfiguration = appConfiguration;
		tika = new Tika();
	}

	public String saveProfileImage(final String base64Image) throws IOException {
		final String imageName = UUID.randomUUID().toString().replaceAll("-", "");
		final byte[] decodeBytes = Base64.getDecoder().decode(base64Image);
		final File target = new File(appConfiguration.getFullProfileImagePath() + "/" + imageName);
		FileUtils.writeByteArrayToFile(target, decodeBytes);
		return imageName;
	}

	public String detectType(final byte[] fileArr) {
		return tika.detect(fileArr);
	}

	public void deleteProfileImage(final String image) {
		try {
			Files.deleteIfExists(Paths.get(appConfiguration.getFullProfileImagePath() + "/" + image));
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}
}
