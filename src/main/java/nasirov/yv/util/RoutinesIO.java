package nasirov.yv.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.springframework.util.ResourceUtils.getFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NotDirectoryException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.WrappedObjectMapperI;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

/**
 * IO operations Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoutinesIO {

	private final WrappedObjectMapperI wrappedObjectMapperI;

	public void marshalToFile(String pathToFile, Object value) {
		wrappedObjectMapperI.marshal(new File(pathToFile), value);
	}

	public <T, C extends Collection> C unmarshalFromFile(String pathToFile, Class<T> targetClass, Class<C> collection) {
		String content = readFromFile(pathToFile);
		return wrappedObjectMapperI.unmarshal(content, targetClass, collection);
	}

	public <T, C extends Collection> C unmarshalFromFile(File file, Class<T> targetClass, Class<C> collection) {
		String content = readFromFile(file);
		return wrappedObjectMapperI.unmarshal(content, targetClass, collection);
	}

	public <T, C extends Collection> C unmarshalFromResource(Resource resource, Class<T> targetClass, Class<C> collection) {
		String content = readFromResource(resource);
		return wrappedObjectMapperI.unmarshal(content, targetClass, collection);
	}

	@SneakyThrows
	public String readFromFile(String pathToFile) {
		return readFileToString(getFile(pathToFile), UTF_8);
	}

	@SneakyThrows
	public String readFromFile(File file) {
		return readFileToString(file, UTF_8);
	}

	public String readFromResource(Resource resource) {
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

	public void mkDir(String dirPath) {
		try {
			FileUtils.forceMkdir(new File(dirPath));
		} catch (IOException e) {
			log.error("EXCEPTION WHILE CREATING DIRECTORY", e);
		}
	}

	public boolean isDirectoryExists(String dirPath) throws NotDirectoryException {
		File dir = new File(dirPath);
		boolean isExists = dir.exists();
		if (dir.exists() && !dir.isDirectory()) {
			throw new NotDirectoryException(dirPath + " is not a directory!");
		}
		return isExists;
	}

	public boolean removeDir(String dirPath) {
		return FileSystemUtils.deleteRecursively(new File(dirPath));
	}

	public void marshalToFileInTheFolder(String folderName, String fileName, Object content) {
		try {
			if (!isDirectoryExists(folderName)) {
				mkDir(folderName);
			}
			String prefix = folderName + File.separator;
			marshalToFile(prefix + fileName, content);
			log.info("Successfully marshaled {} to {}", fileName, folderName);
		} catch (NotDirectoryException e) {
			log.error("Exception while marshal file {} to dir {}", fileName, folderName, e);
		}
	}
}
