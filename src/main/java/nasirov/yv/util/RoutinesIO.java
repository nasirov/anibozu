package nasirov.yv.util;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.parser.WrappedObjectMapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.NotDirectoryException;
import java.util.Collection;

/**
 * IO operations
 * Created by nasirov.yv
 */
@Component
@Slf4j
public class RoutinesIO {
	private WrappedObjectMapper wrappedObjectMapper;
	
	@Autowired
	public RoutinesIO(WrappedObjectMapper wrappedObjectMapper) {
		this.wrappedObjectMapper = wrappedObjectMapper;
	}
	
	public void marshalToFile(String pathToFile, Object value) {
		wrappedObjectMapper.marshal(new File(pathToFile), value);
	}
	
	public void marshalToResources(String pathToFile, Object value) {
		try {
			File file = ResourceUtils.getFile(pathToFile);
			wrappedObjectMapper.marshal(file, value);
		} catch (IOException e) {
			log.error("Error while marshalling to file " + pathToFile, e);
		}
	}
	
	public <T, C extends Collection> C unmarshalFromFile(String pathToFile, Class<T> targetClass, Class<C> collection) {
		String content = readFromFile(pathToFile);
		return wrappedObjectMapper.unmarshal(content, targetClass, collection);
	}
	
	public <T, C extends Collection> C unmarshalFromResource(String resourceName, Class<T> targetClass, Class<C> collection) {
		String content = readFromResource(resourceName);
		return wrappedObjectMapper.unmarshal(content, targetClass, collection);
	}
	
	public <T, C extends Collection> C unmarshalFromResource(Resource resource, Class<T> targetClass, Class<C> collection) {
		String content = readFromResource(resource);
		return wrappedObjectMapper.unmarshal(content, targetClass, collection);
	}
	
	public <T> T unmarshalFromFile(String pathToFile, Class<T> targetClass) {
		String content = readFromFile(pathToFile);
		return wrappedObjectMapper.unmarshal(content, targetClass);
	}
	
	public <T> T unmarshalFromResource(String resourceName, Class<T> targetClass) {
		String content = readFromResource(resourceName);
		return wrappedObjectMapper.unmarshal(content, targetClass);
	}
	
	public <T extends CharSequence> void writeToFile(String pathToFile, T value, boolean append) {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(pathToFile), append))) {
			bufferedWriter.append(value);
			bufferedWriter.flush();
		} catch (IOException e) {
			log.error("Error while writing to file " + pathToFile, e);
		}
	}
	
	public <T extends CharSequence> void writeToFile(File file, T value, boolean append) {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, append))) {
			bufferedWriter.append(value);
			bufferedWriter.flush();
		} catch (IOException e) {
			log.error("Error while writing to file " + file, e);
		}
	}
	
	public String readFromFile(String pathToFile) {
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
	
	public String readFromFile(File file) {
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
	
	public String readFromResource(String name) {
		InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(name);
		if (systemResourceAsStream == null) {
			throw new NullPointerException("Resource " + name + " not found!");
		}
		String fromFile = null;
		try (BufferedInputStream byteArrayInputStream = new BufferedInputStream(systemResourceAsStream);
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
	
	public String readFromResource(Resource resource) {
		if (resource == null) {
			throw new NullPointerException("Resource is null!");
		} else if (!resource.exists()) {
			throw new NullPointerException("Resource doesn't exists!");
		}
		String fromFile = null;
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
	
	public boolean isResourceExist(String resourceName) {
		return ClassLoader.getSystemResourceAsStream(resourceName) != null;
	}
	
	public <T extends CharSequence> void writeToResources(String resourceName, T value, boolean append) {
		if (isResourceExist(resourceName)) {
			try {
				File file = ResourceUtils.getFile(resourceName);
				writeToFile(file, value, append);
			} catch (FileNotFoundException e) {
				log.error("Resource {} is not found!", resourceName);
			}
		}
	}
	
	public void mkDir(String dirPath) {
		try {
			FileUtils.forceMkdir(new File(dirPath));
		} catch (IOException e) {
			log.error("Exception while creating directory", e);
		}
	}
	
	public boolean isDirectoryExists(String dirPath) throws NotDirectoryException {
		File dir = new File(dirPath);
		boolean isExists = dir.exists();
		if (isExists && !dir.isDirectory()) {
			throw new NotDirectoryException(dirPath + " is not a directory!");
		}
		return isExists;
	}
	
	public boolean removeDir(String dirPath) {
		return FileSystemUtils.deleteRecursively(new File(dirPath));
	}
}
