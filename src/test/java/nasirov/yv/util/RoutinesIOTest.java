package nasirov.yv.util;

import static nasirov.yv.utils.ReferencesBuilder.buildConcretizedAndOngoingReference;
import static nasirov.yv.utils.ReferencesBuilder.getAnnouncementReference;
import static nasirov.yv.utils.ReferencesBuilder.getConcretizedReferenceWithEpisodesRange;
import static nasirov.yv.utils.ReferencesBuilder.getRegularReferenceNotUpdated;
import static nasirov.yv.utils.TestConstants.TEMP_FOLDER_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import nasirov.yv.AbstractTest;
import nasirov.yv.data.animedia.TitleReference;
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

	@Value("classpath:__files/github/references.json")
	private Resource routinesIOtestResource;

	private File testFile;

	private File testDir;

	@Before
	@SneakyThrows
	@Override
	public void setUp() {
		super.setUp();
		testFile = new File(FILE_NAME);
		testDir = new File(DIR_NAME);
		clear(testDir);
		clear(testFile);
		clear(new File(routinesIOtestResource.getFilename()));
	}

	@After
	@SneakyThrows
	@Override
	public void tearDown() {
		super.tearDown();
		clear(testDir);
		clear(testFile);
		clear(new File(routinesIOtestResource.getFilename()));
	}

	@Test
	public void marshalToFile() {
		List<TitleReference> collectionToMarshal = new ArrayList<>();
		routinesIO.marshalToFile(FILE_NAME, collectionToMarshal);
		assertEquals(collectionToMarshal, routinesIO.unmarshalFromFile(FILE_NAME, TitleReference.class, ArrayList.class));
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
		Set<TitleReference> unmarshalledFromFile = wrappedObjectMapper.unmarshal(routinesIO.readFromResource(routinesIOtestResource),
				TitleReference.class,
				LinkedHashSet.class);
		checkTestContent(unmarshalledFromFile);
	}

	@Test(expected = MismatchedInputException.class)
	public void readFromResourceException() {
		ClassPathResource resourcesNotFound = new ClassPathResource("resourcesNotFound");
		assertFalse(resourcesNotFound.exists());
		routinesIO.unmarshalFromResource(resourcesNotFound, TitleReference.class, LinkedHashSet.class);
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
		Set<TitleReference> unmarshalledFromFile = null;
		if (fileName != null) {
			unmarshalledFromFile = routinesIO.unmarshalFromFile(fileName, TitleReference.class, LinkedHashSet.class);
		} else if (testFile != null) {
			unmarshalledFromFile = routinesIO.unmarshalFromFile(testFile, TitleReference.class, LinkedHashSet.class);
		} else if (testResource != null) {
			unmarshalledFromFile = routinesIO.unmarshalFromResource(testResource, TitleReference.class, LinkedHashSet.class);
			testResource.exists();
		}
		checkTestContent(unmarshalledFromFile);
	}

	@Test
	public void testMarshalToFileInTheFolderOk() {
		List<TitleReference> testContent = getTestContent();
		String testFilename = routinesIOtestResource.getFilename();
		routinesIO.marshalToFileInTheFolder(TEMP_FOLDER_NAME, testFilename, testContent);
		File testDir = new File(TEMP_FOLDER_NAME);
		File testFileInTestDir = new File(testDir, testFilename);
		Set<TitleReference> unmarshalledFromFile = wrappedObjectMapper.unmarshal(routinesIO.readFromFile(testFileInTestDir), TitleReference.class,
				LinkedHashSet.class);
		checkTestContent(unmarshalledFromFile);
		FileSystemUtils.deleteRecursively(testDir);
	}

	@Test
	public void testMarshalToFileInTheFolderInvalidDir() throws IOException {
		assertTrue(testFile.createNewFile());
		List<TitleReference> testContent = getTestContent();
		String testFilename = routinesIOtestResource.getFilename();
		routinesIO.marshalToFileInTheFolder(FILE_NAME, testFilename, testContent);
		assertFalse(testFile.isDirectory());
	}

	private void checkTestContent(Set<TitleReference> unmarshalledFromFile) {
		assertNotNull(unmarshalledFromFile);
		assertFalse(unmarshalledFromFile.isEmpty());
		TitleReference concretizedTitleWithEpisodesRange = getConcretizedReferenceWithEpisodesRange();
		TitleReference concretizedAndOngoingReference = buildConcretizedAndOngoingReference();
		TitleReference announcementReference = getAnnouncementReference();
		TitleReference regularReferenceNotUpdated = getRegularReferenceNotUpdated();
		assertEquals(4, unmarshalledFromFile.size());
		checkUnmarshaled(unmarshalledFromFile, concretizedTitleWithEpisodesRange);
		checkUnmarshaled(unmarshalledFromFile, concretizedAndOngoingReference);
		checkUnmarshaled(unmarshalledFromFile, announcementReference);
		checkUnmarshaled(unmarshalledFromFile, regularReferenceNotUpdated);
	}

	private void checkUnmarshaled(Set<TitleReference> unmarshalledFromFile, TitleReference titleReference) {
		assertEquals(1,
				unmarshalledFromFile.stream()
						.filter(ref -> ref.equals(titleReference))
						.count());
	}

	private List<TitleReference> getTestContent() {
		ArrayList<TitleReference> result = new ArrayList<>();
		TitleReference concretizedTitleWithEpisodesRange = getConcretizedReferenceWithEpisodesRange();
		TitleReference concretizedAndOngoingReference = buildConcretizedAndOngoingReference();
		TitleReference announcementReference = getAnnouncementReference();
		TitleReference regularReferenceNotUpdated = getRegularReferenceNotUpdated();
		result.add(concretizedTitleWithEpisodesRange);
		result.add(concretizedAndOngoingReference);
		result.add(announcementReference);
		result.add(regularReferenceNotUpdated);
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