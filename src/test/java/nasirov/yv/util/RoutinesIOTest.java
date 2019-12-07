package nasirov.yv.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.AnimediaMALTitleReferences;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileSystemUtils;

/**
 * Created by nasirov.yv
 */


public class RoutinesIOTest extends AbstractTest {

	private static final String FILE_NAME = "test123.txt";

	private static final String DIR_NAME = "test123";

	@Value("classpath:__files/routinesIOtestFile.json")
	private Resource routinesIOtestResource;

	private File testFile;

	private File testDir;

	@Before
	@Override
	public void setUp() {
		super.setUp();
		testFile = new File(FILE_NAME);
		testDir = new File(DIR_NAME);
		clear(testDir);
		clear(testFile);
	}

	@After
	@Override
	public void tearDown() {
		super.tearDown();
		clear(testDir);
		clear(testFile);
	}

	@Test
	public void marshalToFile() {
		List<AnimediaMALTitleReferences> collectionToMarshal = new ArrayList<>();
		routinesIO.marshalToFile(FILE_NAME, collectionToMarshal);
		assertEquals(collectionToMarshal, routinesIO.unmarshalFromFile(FILE_NAME, AnimediaMALTitleReferences.class, ArrayList.class));
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
	public void unmarshalFromResource() {
		unmarshalFromDifferentSources(null, null, routinesIOtestResource);
	}

	@Test
	public void writeToFileAppendTrue() {
		String firstString = "first string";
		String secondString = "second string";
		routinesIO.writeToFile(FILE_NAME, firstString, true);
		routinesIO.writeToFile(FILE_NAME, secondString, true);
		assertTrue(testFile.exists());
		String readFromFileStringPath = routinesIO.readFromFile(FILE_NAME);
		String readFromTestFile = routinesIO.readFromFile(testFile);
		assertEquals(readFromFileStringPath, readFromTestFile);
		String finalString = firstString + System.lineSeparator() + secondString + System.lineSeparator();
		assertEquals(finalString, readFromTestFile);
		assertEquals(finalString, readFromFileStringPath);
	}

	@Test
	public void writeToFileAppendFalse() {
		String firstString = "first string";
		String secondString = "second string";
		routinesIO.writeToFile(FILE_NAME, firstString, false);
		routinesIO.writeToFile(FILE_NAME, secondString, false);
		assertTrue(testFile.exists());
		String readFromFileStringPath = routinesIO.readFromFile(FILE_NAME);
		String readFromTestFile = routinesIO.readFromFile(testFile);
		assertEquals(readFromFileStringPath, readFromTestFile);
		String expected = secondString + System.lineSeparator();
		assertEquals(expected, readFromTestFile);
		assertEquals(expected, readFromFileStringPath);
	}

	@Test
	public void writeToFileException() {
		assertTrue(testDir.mkdir());
		assertFalse(testDir.isFile());
		String firstString = "first string";
		routinesIO.writeToFile(DIR_NAME, firstString, true);
	}

	@Test
	public void readFromFile() {
		String firstString = "first string";
		routinesIO.writeToFile(FILE_NAME, firstString, true);
		String readFromFileStringPath = routinesIO.readFromFile(FILE_NAME);
		String readFromTestFile = routinesIO.readFromFile(testFile);
		assertEquals(readFromFileStringPath, readFromTestFile);
		String expected = firstString + System.lineSeparator();
		assertEquals(expected, readFromTestFile);
		assertEquals(expected, readFromFileStringPath);
	}

	@Test(expected = IOException.class)
	public void readFromFileInputStringPathException() {
		assertTrue(testDir.mkdir());
		assertFalse(testDir.isFile());
		routinesIO.readFromFile(DIR_NAME);
	}

	@Test(expected = IOException.class)
	public void readFromFileException() {
		assertTrue(testDir.mkdir());
		assertFalse(testDir.isFile());
		routinesIO.readFromFile(testDir);
	}

	@Test
	public void readFromResource() {
		Set<AnimediaMALTitleReferences> unmarshalledFromFile = wrappedObjectMapperI.unmarshal(routinesIO.readFromResource(routinesIOtestResource),
				AnimediaMALTitleReferences.class,
				LinkedHashSet.class);
		checkTestContent(unmarshalledFromFile);
	}

	@Test(expected = MismatchedInputException.class)
	public void readFromResourceException() {
		ClassPathResource resourcesNotFound = new ClassPathResource("resourcesNotFound");
		assertFalse(resourcesNotFound.exists());
		routinesIO.unmarshalFromResource(resourcesNotFound, AnimediaMALTitleReferences.class, LinkedHashSet.class);
	}

	@Test
	public void mkDirException() throws Exception {
		assertTrue(testDir.createNewFile());
		routinesIO.mkDir(DIR_NAME);
	}

	@Test
	public void mkDir() {
		routinesIO.mkDir(DIR_NAME);
		assertTrue(testDir.exists());
		assertTrue(testDir.isDirectory());
	}

	@Test
	public void isDirectoryExists() throws Exception {
		assertTrue(testDir.mkdir());
		assertTrue(testDir.exists());
		assertTrue(testDir.isDirectory());
		routinesIO.isDirectoryExists(DIR_NAME);
	}

	@Test
	public void removeDir() {
		assertTrue(testDir.mkdir());
		assertTrue(testDir.exists());
		assertTrue(testDir.isDirectory());
		routinesIO.removeDir(DIR_NAME);
		assertFalse(testDir.exists());
	}

	private File createTestFileFromResourcesTestFile(String fileName) throws IOException {
		File testFile = new File(fileName);
		FileSystemUtils.copyRecursively(routinesIOtestResource.getFile(), testFile);
		return testFile;
	}

	private void unmarshalFromDifferentSources(String fileName, File testFile, Resource testResource) {
		Set<AnimediaMALTitleReferences> unmarshalledFromFile = null;
		if (fileName != null) {
			unmarshalledFromFile = routinesIO.unmarshalFromFile(fileName, AnimediaMALTitleReferences.class, LinkedHashSet.class);
		} else if (testFile != null) {
			unmarshalledFromFile = routinesIO.unmarshalFromFile(testFile, AnimediaMALTitleReferences.class, LinkedHashSet.class);
		} else if (testResource != null) {
			unmarshalledFromFile = routinesIO.unmarshalFromResource(testResource, AnimediaMALTitleReferences.class, LinkedHashSet.class);
			testResource.exists();
		}
		assertNotNull(unmarshalledFromFile);
		assertFalse(unmarshalledFromFile.isEmpty());
		AnimediaMALTitleReferences onePunch7 = AnimediaMALTitleReferences.builder()
				.url("anime/vanpanchmen")
				.dataList("7")
				.firstEpisode("1")
				.titleOnMAL("one punch man specials")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("6")
				.minConcretizedEpisodeOnMAL("1")
				.maxConcretizedEpisodeOnMAL("6")
				.currentMax("6")
				.build();
		AnimediaMALTitleReferences onePunch7_2 = AnimediaMALTitleReferences.builder()
				.url("anime/vanpanchmen")
				.dataList("7")
				.firstEpisode("7")
				.titleOnMAL("one punch man: road to hero")
				.minConcretizedEpisodeOnAnimedia("7")
				.maxConcretizedEpisodeOnAnimedia("7")
				.minConcretizedEpisodeOnMAL("1")
				.maxConcretizedEpisodeOnMAL("1")
				.currentMax("7")
				.build();
		assertEquals(2, unmarshalledFromFile.size());
		assertEquals(1,
				unmarshalledFromFile.stream()
						.filter(ref -> ref.equals(onePunch7))
						.count());
		assertEquals(1,
				unmarshalledFromFile.stream()
						.filter(ref -> ref.equals(onePunch7_2))
						.count());
	}

	@Test
	public void testMarshalToFileInTheFolderOk() {
		String tempFolderName = "temp";
		List<AnimediaMALTitleReferences> testContent = getTestContent();
		String testFilename = routinesIOtestResource.getFilename();
		routinesIO.marshalToFileInTheFolder(tempFolderName, testFilename, testContent);
		File testDir = new File(tempFolderName);
		File testFileInTestDir = new File(testDir, testFilename);
		Set<AnimediaMALTitleReferences> unmarshalledFromFile = wrappedObjectMapperI.unmarshal(routinesIO.readFromFile(testFileInTestDir),
				AnimediaMALTitleReferences.class,
				LinkedHashSet.class);
		checkTestContent(unmarshalledFromFile);
		FileSystemUtils.deleteRecursively(testDir);
	}

	@Test
	public void testMarshalToFileInTheFolderInvalidDir() throws IOException {
		assertTrue(testFile.createNewFile());
		List<AnimediaMALTitleReferences> testContent = getTestContent();
		String testFilename = routinesIOtestResource.getFilename();
		routinesIO.marshalToFileInTheFolder(FILE_NAME, testFilename, testContent);
		assertFalse(testFile.isDirectory());
	}

	private void checkTestContent(Set<AnimediaMALTitleReferences> unmarshalledFromFile) {
		List<AnimediaMALTitleReferences> contentFromTestFile = getTestContent();
		AnimediaMALTitleReferences onePunch7 = contentFromTestFile.stream()
				.filter(title -> title.getTitleOnMAL()
						.equals("one punch man specials"))
				.findAny()
				.get();
		AnimediaMALTitleReferences onePunch7_2 = contentFromTestFile.stream()
				.filter(title -> title.getTitleOnMAL()
						.equals("one punch man: road to " + "hero"))
				.findAny()
				.get();
		assertNotNull(unmarshalledFromFile);
		assertFalse(unmarshalledFromFile.isEmpty());
		assertEquals(2, unmarshalledFromFile.size());
		assertEquals(1,
				unmarshalledFromFile.stream()
						.filter(ref -> ref.equals(onePunch7))
						.count());
		assertEquals(1,
				unmarshalledFromFile.stream()
						.filter(ref -> ref.equals(onePunch7_2))
						.count());
	}

	private List<AnimediaMALTitleReferences> getTestContent() {
		ArrayList<AnimediaMALTitleReferences> result = new ArrayList<>();
		AnimediaMALTitleReferences onePunch7 = AnimediaMALTitleReferences.builder()
				.url("anime/vanpanchmen")
				.dataList("7")
				.firstEpisode("1")
				.titleOnMAL("one punch man specials")
				.minConcretizedEpisodeOnAnimedia("1")
				.maxConcretizedEpisodeOnAnimedia("6")
				.minConcretizedEpisodeOnMAL("1")
				.maxConcretizedEpisodeOnMAL("6")
				.currentMax("6")
				.build();
		AnimediaMALTitleReferences onePunch7_2 = AnimediaMALTitleReferences.builder()
				.url("anime/vanpanchmen")
				.dataList("7")
				.firstEpisode("7")
				.titleOnMAL("one punch man: road to hero")
				.minConcretizedEpisodeOnAnimedia("7")
				.maxConcretizedEpisodeOnAnimedia("7")
				.minConcretizedEpisodeOnMAL("1")
				.maxConcretizedEpisodeOnMAL("1")
				.currentMax("7")
				.build();
		result.add(onePunch7);
		result.add(onePunch7_2);
		return result;
	}

	@SneakyThrows
	private void clear(File file) {
		if (file != null && file.exists()) {
			if (file.isDirectory()) {
				FileSystemUtils.deleteRecursively(file);
			} else {
				FileUtils.forceDelete(file);
			}
		}
	}

}