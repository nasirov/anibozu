package nasirov.yv.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NotDirectoryException;
import java.util.Collection;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.WrappedObjectMapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;

/**
 * IO operations
 * Created by nasirov.yv
 */
@Slf4j
@UtilityClass
public class RoutinesIO {


	public static void marshalToFile(String pathToFile, Object value) {
		WrappedObjectMapper.marshal(new File(pathToFile), value);
	}

	public static <T, C extends Collection> C unmarshalFromFile(String pathToFile, Class<T> targetClass, Class<C> collection) {
		String content = readFromFile(pathToFile);
		return WrappedObjectMapper.unmarshal(content, targetClass, collection);
	}

	public static <T, C extends Collection> C unmarshalFromFile(File file, Class<T> targetClass, Class<C> collection) {
		String content = readFromFile(file);
		return WrappedObjectMapper.unmarshal(content, targetClass, collection);
	}

	public static <T, C extends Collection> C unmarshalFromResource(Resource resource, Class<T> targetClass, Class<C> collection) {
		String content = readFromResource(resource);
		return WrappedObjectMapper.unmarshal(content, targetClass, collection);
	}

	public static <T extends CharSequence> void writeToFile(String pathToFile, T value, boolean append) {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(pathToFile), append))) {
			bufferedWriter.append(value).append(System.lineSeparator());
			bufferedWriter.flush();
		} catch (IOException e) {
			log.error("ERROR WHILE WRITING TO FILE " + pathToFile, e);
		}
	}


	public static String readFromFile(String pathToFile) {
		StringBuilder stringBuilder = new StringBuilder();
		String lineSeparator = System.lineSeparator();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(ResourceUtils.getFile(pathToFile)))) {
			String s;
			while ((s = bufferedReader.readLine()) != null) {
				stringBuilder.append(s).append(lineSeparator);
			}
			stringBuilder.delete(stringBuilder.lastIndexOf(lineSeparator), stringBuilder.length());
		} catch (Exception e) {
			log.error("ERROR WHILE READING FROM FILE " + pathToFile, e);
		}
		return stringBuilder.toString();
	}

	public static String readFromFile(File file) {
		StringBuilder stringBuilder = new StringBuilder();
		String lineSeparator = System.lineSeparator();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
			String s;
			while ((s = bufferedReader.readLine()) != null) {
				stringBuilder.append(s).append(System.lineSeparator());
			}
			stringBuilder.delete(stringBuilder.lastIndexOf(lineSeparator), stringBuilder.length());
		} catch (Exception e) {
			log.error("ERROR WHILE READING FROM FILE " + file, e);
		}
		return stringBuilder.toString();
	}


	public static String readFromResource(Resource resource) {
		String fromFile = "";
		try (BufferedInputStream byteArrayInputStream = new BufferedInputStream(resource.getInputStream());
				ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream()) {
			int readByte;
			byte[] data = new byte[1024];
			while ((readByte = byteArrayInputStream.read(data, 0, data.length)) != -1) {
				bufferedOutputStream.write(data, 0, readByte);
				bufferedOutputStream.flush();
			}
			fromFile = new String(bufferedOutputStream.toByteArray(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.error("EXCEPTION WHILE READING FROM RESOURCE " + resource, e);
		}
		return fromFile;
	}

	public static void mkDir(String dirPath) {
		try {
			FileUtils.forceMkdir(new File(dirPath));
		} catch (IOException e) {
			log.error("EXCEPTION WHILE CREATING DIRECTORY", e);
		}
	}

	public static boolean isDirectoryExists(String dirPath) throws NotDirectoryException {
		File dir = new File(dirPath);
		boolean isExists = dir.exists();
		if (isExists && !dir.isDirectory()) {
			throw new NotDirectoryException(dirPath + " is not a directory!");
		}
		return isExists;
	}

	public static boolean removeDir(String dirPath) {
		return FileSystemUtils.deleteRecursively(new File(dirPath));
	}

	public static void marshalToFileInTheFolder(String folderName, String fileName, Object content) {
		try {
			if (!isDirectoryExists(folderName)) {
				mkDir(folderName);
			}
			String prefix = folderName + File.separator;
			marshalToFile(prefix + fileName, content);
		} catch (NotDirectoryException e) {
			log.error("{} IS NOT A DIRECTORY!", folderName);
		}
	}
}
