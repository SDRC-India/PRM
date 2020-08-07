package org.sdrc.fani.util;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;


@Component
public class ImageConverter {
	public static String encodingPhoto(String photoPath) {

		FileInputStream fileInputStreamReader = null;
		File filePath = new File(photoPath);
		byte[] bytes = {};
		try {
			fileInputStreamReader = new FileInputStream(filePath);
			bytes = new byte[(int) filePath.length()];
			fileInputStreamReader.read(bytes);
			fileInputStreamReader.close();
		} catch (Exception e) {

		}
		return "data:image/jpg;base64," + Base64.encodeBase64String(bytes);
	}
}
