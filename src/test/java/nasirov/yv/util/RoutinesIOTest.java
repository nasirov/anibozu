package nasirov.yv.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import nasirov.yv.AbstractTest;
import nasirov.yv.parser.WrappedObjectMapper;
import nasirov.yv.serialization.AnimediaMALTitleReferences;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileSystemUtils;

/**
 * Created by nasirov.yv
 */
public class RoutinesIOTest extends AbstractTest {

	@Test
	public void marshalToFile() throws Exception {
		String fileName = "test123.txt";
		File testFile = new File(fileName);
		assertFalse(testFile.exists());
		List<AnimediaMALTitleReferences> collectionToMarshal = new ArrayList<>();
		RoutinesIO.marshalToFile(fileName, collectionToMarshal);
		assertTrue(testFile.exists());
		FileSystemUtils.deleteRecursively(testFile);
	}

	@Test
	public void unmarshalFromFileParameterStringFilePath() throws Exception {
		String fileName = routinesIOtestResource.getFilename();
		File testFile = createTestFileFromResourcesTestFile(fileName);
		unmarshalFromDifferentSources(fileName, null, null);
		FileSystemUtils.deleteRecursively(testFile);
	}
	@Test
	public void unmarshalFromFileParameterFile() throws Exception {
		File testFile = createTestFileFromResourcesTestFile(routinesIOtestResource.getFilename());
		unmarshalFromDifferentSources(null, testFile, null);
		FileSystemUtils.deleteRecursively(testFile);
	}

	@Test
	public void unmarshalFromResource() throws Exception {
		unmarshalFromDifferentSources(null, null, routinesIOtestResource);
	}
	@Test
	public void writeToFileAppendTrue() throws Exception {
		String fileName = "test123.txt";
		File testFile = new File(fileName);
		assertFalse(testFile.exists());
		String firstString = "first string";
		String secondString = "second string";
		RoutinesIO.writeToFile(fileName, firstString, true);
		RoutinesIO.writeToFile(fileName, secondString, true);
		assertTrue(testFile.exists());
		String readFromFileStringPath = RoutinesIO.readFromFile(fileName);
		String readFromTestFile = RoutinesIO.readFromFile(testFile);
		assertEquals(readFromFileStringPath, readFromTestFile);
		String finalString = firstString + System.lineSeparator() + secondString + System.lineSeparator();
		assertEquals(finalString, readFromTestFile);
		assertEquals(finalString, readFromFileStringPath);
		FileSystemUtils.deleteRecursively(testFile);
	}

	@Test
	public void writeToFileAppendFalse() throws Exception {
		String fileName = "test123.txt";
		File testFile = new File(fileName);
		assertFalse(testFile.exists());
		String firstString = "first string";
		String secondString = "second string";
		RoutinesIO.writeToFile(fileName, firstString, false);
		RoutinesIO.writeToFile(fileName, secondString, false);
		assertTrue(testFile.exists());
		String readFromFileStringPath = RoutinesIO.readFromFile(fileName);
		String readFromTestFile = RoutinesIO.readFromFile(testFile);
		assertEquals(readFromFileStringPath, readFromTestFile);
		String finalString = secondString + System.lineSeparator();
		assertEquals(finalString, readFromTestFile);
		assertEquals(finalString, readFromFileStringPath);
		FileSystemUtils.deleteRecursively(testFile);
	}

	@Test
	public void writeToFileException() throws Exception {
		String dirName = "test123";
		File testDir = new File(dirName);
		assertTrue(testDir.mkdir());
		assertFalse(testDir.isFile());
		String firstString = "first string";
		RoutinesIO.writeToFile(dirName, firstString, true);
		FileSystemUtils.deleteRecursively(testDir);
		assertFalse(testDir.exists());
	}

	@Test
	public void readFromFile() throws Exception {
		String fileName = "test123.txt";
		File testFile = new File(fileName);
		assertFalse(testFile.exists());
		String firstString = "first string";
		RoutinesIO.writeToFile(fileName, firstString, true);
		String readFromFileStringPath = RoutinesIO.readFromFile(fileName);
		String readFromTestFile = RoutinesIO.readFromFile(testFile);
		assertEquals(readFromFileStringPath, readFromTestFile);
		String finalString = firstString + System.lineSeparator();
		assertEquals(finalString, readFromTestFile);
		assertEquals(finalString, readFromFileStringPath);
		FileSystemUtils.deleteRecursively(testFile);
		assertFalse(testFile.exists());
	}

	@Test
	public void readFromFileException() throws Exception {
		String fileName = "test123";
		File testDir = new File(fileName);
		assertTrue(testDir.mkdir());
		assertFalse(testDir.isFile());
		RoutinesIO.readFromFile(fileName);
		RoutinesIO.readFromFile(testDir);
		FileSystemUtils.deleteRecursively(testDir);
		assertFalse(testDir.exists());
	}

	@Test
	public void readFromResource() throws Exception {
		Set<AnimediaMALTitleReferences> unmarshalledFromFile = WrappedObjectMapper
				.unmarshal(RoutinesIO.readFromResource(routinesIOtestResource), AnimediaMALTitleReferences.class, LinkedHashSet.class);
		assertNotNull(unmarshalledFromFile);
		assertFalse(unmarshalledFromFile.isEmpty());
		AnimediaMALTitleReferences onePunch7 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("1")
				.titleOnMAL("one punch man specials").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("6")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("6").currentMax("6").build();
		AnimediaMALTitleReferences onePunch7_2 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("7")
				.titleOnMAL("one punch man: road to hero").minConcretizedEpisodeOnAnimedia("7").maxConcretizedEpisodeOnAnimedia("7")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("1").currentMax("7").build();
		assertEquals(2, unmarshalledFromFile.size());
		assertEquals(1, unmarshalledFromFile.stream().filter(ref -> ref.equals(onePunch7)).count());
		assertEquals(1, unmarshalledFromFile.stream().filter(ref -> ref.equals(onePunch7_2)).count());
	}

	@Test
	public void readFromResourceException() throws Exception {
		ClassPathResource resourcesNotFound = new ClassPathResource("resourcesNotFound");
		assertFalse(resourcesNotFound.exists());
		RoutinesIO.unmarshalFromResource(resourcesNotFound, AnimediaMALTitleReferences.class, LinkedHashSet.class);
	}


	@Test
	public void mkDirException() throws Exception {
		String dirName = "test123.txt";
		File testDir = new File(dirName);
		assertFalse(testDir.exists());
		assertTrue(testDir.createNewFile());
		RoutinesIO.mkDir(dirName);
		FileSystemUtils.deleteRecursively(testDir);
		assertFalse(testDir.exists());
	}

	@Test
	public void mkDir() throws Exception {
		String dirName = "test123";
		File testDir = new File(dirName);
		assertFalse(testDir.exists());
		RoutinesIO.mkDir(dirName);
		assertTrue(testDir.exists());
		assertTrue(testDir.isDirectory());
		FileSystemUtils.deleteRecursively(testDir);
		assertFalse(testDir.exists());
	}

	@Test
	public void isDirectoryExists() throws Exception {
		String dirName = "test123";
		File testDir = new File(dirName);
		assertFalse(testDir.exists());
		assertTrue(testDir.mkdir());
		assertTrue(testDir.exists());
		assertTrue(testDir.isDirectory());
		RoutinesIO.isDirectoryExists(dirName);
		FileSystemUtils.deleteRecursively(testDir);
		assertFalse(testDir.exists());
	}
	@Test
	public void removeDir() throws Exception {
		String dirName = "test123";
		File testDir = new File(dirName);
		assertFalse(testDir.exists());
		assertTrue(testDir.mkdir());
		assertTrue(testDir.exists());
		assertTrue(testDir.isDirectory());
		RoutinesIO.removeDir(dirName);
		assertFalse(testDir.exists());
	}

	@Test
	public void testConstructor() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<?>[] declaredConstructors = RoutinesIO.class.getDeclaredConstructors();
		assertEquals(1, declaredConstructors.length);
		assertFalse(declaredConstructors[0].isAccessible());
		declaredConstructors[0].setAccessible(true);
		RoutinesIO fromPrivateConstructor = (RoutinesIO) declaredConstructors[0].newInstance();
		assertNotNull(fromPrivateConstructor);
	}

	private File createTestFileFromResourcesTestFile(String fileName) throws IOException {
		File testFile = new File(fileName);
		FileSystemUtils.copyRecursively(routinesIOtestResource.getFile(), testFile);
		return testFile;
	}

	private void unmarshalFromDifferentSources(@Nullable String fileName, @Nullable File testFile, @Nullable Resource testResource) {
		Set<AnimediaMALTitleReferences> unmarshalledFromFile = null;
		if (fileName != null) {
			unmarshalledFromFile = RoutinesIO.unmarshalFromFile(fileName, AnimediaMALTitleReferences.class, LinkedHashSet.class);
		} else if (testFile != null) {
			unmarshalledFromFile = RoutinesIO.unmarshalFromFile(testFile, AnimediaMALTitleReferences.class, LinkedHashSet.class);
		} else if (testResource != null) {
			unmarshalledFromFile = RoutinesIO.unmarshalFromResource(testResource, AnimediaMALTitleReferences.class, LinkedHashSet.class);
			testResource.exists();
		}
		assertNotNull(unmarshalledFromFile);
		assertFalse(unmarshalledFromFile.isEmpty());
		AnimediaMALTitleReferences onePunch7 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("1")
				.titleOnMAL("one punch man specials").minConcretizedEpisodeOnAnimedia("1").maxConcretizedEpisodeOnAnimedia("6")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("6").currentMax("6").build();
		AnimediaMALTitleReferences onePunch7_2 = AnimediaMALTitleReferences.builder().url("anime/vanpanchmen").dataList("7").firstEpisode("7")
				.titleOnMAL("one punch man: road to hero").minConcretizedEpisodeOnAnimedia("7").maxConcretizedEpisodeOnAnimedia("7")
				.minConcretizedEpisodeOnMAL("1").maxConcretizedEpisodeOnMAL("1").currentMax("7").build();
		assertEquals(2, unmarshalledFromFile.size());
		assertEquals(1, unmarshalledFromFile.stream().filter(ref -> ref.equals(onePunch7)).count());
		assertEquals(1, unmarshalledFromFile.stream().filter(ref -> ref.equals(onePunch7_2)).count());
	}

}