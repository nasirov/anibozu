package nasirov.yv.util;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.WrappedObjectMapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.NotDirectoryException;
import java.util.Collection;

/**
 * IO operations
 * Created by nasirov.yv
 */
@Slf4j
public class RoutinesIO {
	private RoutinesIO() {
	}
	
	public static void marshalToFile(@NotNull String pathToFile, @NotNull Object value) {
		WrappedObjectMapper.marshal(new File(pathToFile), value);
	}
	
	public static void marshalToResources(@NotNull String pathToFile, @NotNull Object value) {
		try {
			File file = ResourceUtils.getFile(pathToFile);
			WrappedObjectMapper.marshal(file, value);
		} catch (IOException e) {
			log.error("Error while marshalling to file " + pathToFile, e);
		}
	}
	
	public static <T, C extends Collection> C unmarshalFromFile(@NotNull String pathToFile, @NotNull Class<T> targetClass, @NotNull Class<C> collection) {
		String content = readFromFile(pathToFile);
		return WrappedObjectMapper.unmarshal(content, targetClass, collection);
	}
	
	public static <T, C extends Collection> C unmarshalFromResource(@NotNull String resourceName, @NotNull Class<T> targetClass, @NotNull Class<C> collection) {
		String content = readFromResource(resourceName);
		return WrappedObjectMapper.unmarshal(content, targetClass, collection);
	}
	
	public static <T, C extends Collection> C unmarshalFromResource(@NotNull Resource resource, @NotNull Class<T> targetClass, @NotNull Class<C> collection) {
		String content = readFromResource(resource);
		return WrappedObjectMapper.unmarshal(content, targetClass, collection);
	}
	
	public static <T> T unmarshalFromFile(@NotNull String pathToFile, @NotNull Class<T> targetClass) {
		String content = readFromFile(pathToFile);
		return WrappedObjectMapper.unmarshal(content, targetClass);
	}
	
	public static <T> T unmarshalFromResource(@NotNull String resourceName, @NotNull Class<T> targetClass) {
		String content = readFromResource(resourceName);
		return WrappedObjectMapper.unmarshal(content, targetClass);
	}
	
	public static <T extends CharSequence> void writeToFile(String pathToFile, T value, boolean append) {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(pathToFile), append))) {
			bufferedWriter.append(value).append(System.lineSeparator());
			bufferedWriter.flush();
		} catch (IOException e) {
			log.error("Error while writing to file " + pathToFile, e);
		}
	}
	
	public static <T extends CharSequence> void writeToFile(File file, T value, boolean append) {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, append))) {
			bufferedWriter.append(value).append(System.lineSeparator());
			bufferedWriter.flush();
		} catch (IOException e) {
			log.error("Error while writing to file " + file, e);
		}
	}
	
	@NotNull
	public static String readFromFile(String pathToFile) {
		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(pathToFile)))) {
			String s;
			while ((s = bufferedReader.readLine()) != null) {
				stringBuilder.append(s).append(System.lineSeparator());
			}
			return stringBuilder.toString();
		} catch (Exception e) {
			log.error("Error while reading from file " + pathToFile, e);
		}
		return stringBuilder.toString();
	}
	
	public static String readFromFile(File file) {
		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
			String s;
			while ((s = bufferedReader.readLine()) != null) {
				stringBuilder.append(s).append(System.lineSeparator());
			}
			return stringBuilder.toString();
		} catch (Exception e) {
			log.error("Error while reading from file " + file, e);
		}
		return stringBuilder.toString();
	}
	
	@NotNull
	public static String readFromResource(@NotNull String name) {
		String fromFile = "";
		try (BufferedInputStream byteArrayInputStream = new BufferedInputStream(ClassLoader.getSystemResourceAsStream(name));
			 ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream()) {
			int nRead;
			byte[] data = new byte[1024];
			while ((nRead = byteArrayInputStream.read(data, 0, data.length)) != -1) {
				bufferedOutputStream.write(data, 0, nRead);
				bufferedOutputStream.flush();
			}
			fromFile = new String(bufferedOutputStream.toByteArray(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.error("Error while reading from resource " + name, e);
		}
		return fromFile;
	}
	
	@NotNull
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
			log.error("Exception while reading from resource " + resource, e);
		}
		return fromFile;
	}
	
	public static void mkDir(String dirPath) {
		try {
			FileUtils.forceMkdir(new File(dirPath));
		} catch (IOException e) {
			log.error("Exception while creating directory", e);
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
}
