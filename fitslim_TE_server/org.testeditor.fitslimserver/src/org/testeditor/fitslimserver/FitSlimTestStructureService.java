/*******************************************************************************
 * Copyright (c) 2012 - 2014 Signal Iduna Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Signal Iduna Corporation - initial API and implementation
 * akquinet AG
 *******************************************************************************/
package org.testeditor.fitslimserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.testeditor.core.exceptions.SystemException;
import org.testeditor.core.model.testresult.TestResult;
import org.testeditor.core.model.teststructure.ScenarioSuite;
import org.testeditor.core.model.teststructure.TestCase;
import org.testeditor.core.model.teststructure.TestCompositeStructure;
import org.testeditor.core.model.teststructure.TestScenario;
import org.testeditor.core.model.teststructure.TestStructure;
import org.testeditor.core.model.teststructure.TestSuite;
import org.testeditor.core.model.teststructure.TestType;
import org.testeditor.core.services.interfaces.TestEditorGlobalConstans;
import org.testeditor.core.services.interfaces.TestServerService;
import org.testeditor.core.services.interfaces.TestStructureService;
import org.testeditor.fitnesse.resultreader.FitNesseResultReader;
import org.testeditor.fitnesse.resultreader.FitNesseResultReaderFactory;
import org.testeditor.fitnesse.util.FitNesseRestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * Fitnesse Filesystem based implementation of the TestStructureService.
 * 
 */
public class FitSlimTestStructureService implements TestStructureService {

	private static final Logger LOGGER = Logger.getLogger(FitSlimTestStructureService.class);
	private TestServerService testServerService;

	@Override
	public void loadTestStructuresChildrenFor(TestCompositeStructure testCompositeStructure) throws SystemException {
		Path path = Paths.get(getPathToTestStructureDirectory(testCompositeStructure));
		try {
			for (Path file : Files.newDirectoryStream(path)) {
				if (file.toFile().isDirectory()) {
					String name = file.toFile().getName();
					if (!name.startsWith(".")) {
						File[] listFiles = file.toFile().listFiles(getPropertyFiler());
						if (listFiles.length > 0) {
							TestStructure structure = createTestStructureFrom(listFiles[0]);
							if (structure != null) {
								testCompositeStructure.addChild(structure);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("Unable to scan directory", e);
			throw new SystemException("Unable to scan directory", e);
		}
	}

	/**
	 * Creates the Path to the Directory of the TestStructure in the FileSystem
	 * as a string.
	 * 
	 * @param testStructure
	 *            to be used for lookup.
	 * @return the path as string to the TestStructure.
	 */
	public String getPathToTestStructureDirectory(TestStructure testStructure) {
		StringBuilder sb = new StringBuilder();
		sb.append(getPathToProject(testStructure));
		String pathInProject = testStructure.getFullName().replaceAll("\\.", "/");
		sb.append(File.separator).append("FitNesseRoot").append(File.separator).append(pathInProject);
		return sb.toString();
	}

	/**
	 * Creates the Path to the Directory of the TestResults of the given
	 * TestStructure in the FileSystem as a string.
	 * 
	 * @param testStructure
	 *            to be used for lookup.
	 * @return the path as string to the TestResults.
	 */
	public String getPathToTestResults(TestStructure testStructure) {
		StringBuilder sb = new StringBuilder();
		sb.append(getPathToProject(testStructure));
		sb.append(File.separator).append("FitNesseRoot").append(File.separator).append("files").append(File.separator)
				.append("testResults").append(File.separator).append(testStructure.getFullName());
		return sb.toString();
	}

	/**
	 * Creates an instance of a subclass of TestStructure based on the
	 * informations in the property file. if the property file contains the
	 * property: test it creates a TestCase. if the property file contains the
	 * property: suite it creates a TestSuite. if the property file contains the
	 * property: suites it creates a ScenarioSuite.
	 * 
	 * @param propertyFile
	 *            FitNesse xml file with the properties of a TestPage.
	 * @return a instance of subclass of TestStructure.
	 * @throws SystemException
	 *             on IOExcpetion or XML processing.
	 */
	protected TestStructure createTestStructureFrom(File propertyFile) throws SystemException {
		TestStructure result = null;
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = documentBuilder.parse(propertyFile);
			boolean isSuites = false;
			if (document.getFirstChild().getNodeName().equals("properties")) {
				NodeList nodeList = document.getFirstChild().getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					if (nodeList.item(i).getNodeName().equals("Test")) {
						result = new TestCase();
						break;
					}
					if (nodeList.item(i).getNodeName().equals("Suite")) {
						result = new TestSuite();
						break;
					}
					if (nodeList.item(i).getNodeName().equals("Suites")) {
						isSuites = true;
					}
				}
				String testStructureName = propertyFile.getParentFile().getName();
				if (result == null) {
					if (isSuites || testStructureName.equalsIgnoreCase(TestEditorGlobalConstans.TEST_SCENARIO_SUITE)
							|| testStructureName.equalsIgnoreCase(TestEditorGlobalConstans.TEST_KOMPONENTS)) {
						result = new ScenarioSuite();
					} else {
						result = new TestScenario();
					}
				}
				result.setName(testStructureName.trim());
				if (result instanceof TestCompositeStructure) {
					((TestCompositeStructure) result).setChildCount(propertyFile.getParentFile().listFiles(
							getDirectoryFilter()).length);
					((TestCompositeStructure) result)
							.setLazyLoader(getTestProjectLazyLoader((TestCompositeStructure) result));
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LOGGER.error("Error lodaingg properties of teststructrue", e);
			throw new SystemException(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 
	 * @return directory filter to ignore directory with . as prefix.
	 */
	private FilenameFilter getDirectoryFilter() {
		return new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith(".")) {
					return false;
				}
				return dir.isDirectory();
			}
		};
	}

	/**
	 * 
	 * @return Filename filter to get only properties.xml.
	 */
	private FilenameFilter getPropertyFiler() {
		return new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.equals("properties.xml");
			}
		};
	}

	@Override
	public void createTestStructure(TestStructure testStructure) throws SystemException {
		Path pathToTestStructure = Paths.get(getPathToTestStructureDirectory(testStructure));
		if (Files.exists(pathToTestStructure)) {
			throw new SystemException("TestStructure allready exits");
		}
		try {
			Files.createDirectories(pathToTestStructure);
			Files.write(Paths.get(pathToTestStructure.toString() + File.separator + "content.txt"), testStructure
					.getSourceCode().getBytes());

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element props = doc.createElement("properties");
			doc.appendChild(props);
			props.appendChild(createTrueElement(doc, "Edit"));
			props.appendChild(createTrueElement(doc, "Files"));
			props.appendChild(createTrueElement(doc, "Properties"));
			props.appendChild(createTrueElement(doc, "RecentChanges"));
			props.appendChild(createTrueElement(doc, "Refactor"));
			props.appendChild(createTrueElement(doc, "Search"));
			props.appendChild(createTrueElement(doc, "Versions"));
			props.appendChild(createTrueElement(doc, "WhereUsed"));

			String type = testStructure.getPageType();
			if (type.equals(new ScenarioSuite().getPageType())) {
				type = "Suites";
			}
			props.appendChild(doc.createElement(type));

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(pathToTestStructure + File.separator + "properties.xml"));

			transformer.transform(source, result);

		} catch (IOException | ParserConfigurationException | TransformerException e) {
			LOGGER.error("Error creating teststructrue in filesystem", e);
			throw new SystemException(e.getMessage(), e);
		}
	}

	/**
	 * Creates XML Nodes with a True TextNode as child of the node.
	 * 
	 * @param doc
	 *            used for creation.
	 * @param name
	 *            of the new node
	 * @return a new node.
	 */
	private Node createTrueElement(Document doc, String name) {
		Element element = doc.createElement(name);
		element.appendChild(doc.createTextNode("true"));
		return element;
	}

	@Override
	public void removeTestStructure(TestStructure testStructure) throws SystemException {
		try {
			Files.walkFileTree(Paths.get(getPathToTestStructureDirectory(testStructure)),
					new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							Files.delete(file);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							Files.delete(dir);
							return FileVisitResult.CONTINUE;
						}

					});
			LOGGER.trace("Deleted teststructrue: " + testStructure);
		} catch (IOException e) {
			LOGGER.error("Error deleting teststructrue: " + testStructure, e);
			throw new SystemException("Error deleting teststructrue: " + testStructure + "\n" + e.getMessage(), e);
		}
	}

	@Override
	public void renameTestStructure(TestStructure testStructure, String newName) throws SystemException {
		// TODO Auto-generated method stub

	}

	@Override
	public TestResult executeTestStructure(TestStructure testStructure, IProgressMonitor monitor)
			throws SystemException, InterruptedException {
		TestResult testResult = null;
		try {
			testServerService.startTestServer(testStructure.getRootElement());
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("Error starting TestServer for teststructrue: " + testStructure, e);
			throw new SystemException("Error starting TestServer for teststructrue: " + testStructure + "\n"
					+ e.getMessage(), e);
		}

		testResult = new FitNesseRestClient().execute(testStructure, monitor);

		try {
			testServerService.stopTestServer(testStructure.getRootElement());
		} catch (IOException e) {
			LOGGER.error("Error stopping TestServer for teststructrue: " + testStructure, e);
			throw new SystemException("Error stopping TestServer for teststructrue: " + testStructure + "\n"
					+ e.getMessage(), e);
		}
		return testResult;
	}

	/**
	 * 
	 * @param testStructure
	 *            used to get the TestProcject and looks it's location in the
	 *            filesystem.
	 * @return path as string of the root element of the given teststructure.
	 */
	public String getPathToProject(TestStructure testStructure) {
		StringBuilder sb = new StringBuilder();
		sb.append(Platform.getLocation().toFile().toPath().toString()).append(File.separator)
				.append(testStructure.getRootElement().getName());
		return sb.toString();
	}

	@Override
	public String getTestStructureAsText(TestStructure testStructure) throws SystemException {
		try {
			Path pathToTestStructure = Paths.get(getPathToTestStructureDirectory(testStructure));
			return new String(Files.readAllBytes(Paths.get(pathToTestStructure.toString() + File.separator
					+ "content.txt")));
		} catch (IOException e) {
			LOGGER.error("Error reading content of teststructrue: " + testStructure, e);
			throw new SystemException("Error reading content of teststructrue: " + testStructure + "\n"
					+ e.getMessage(), e);
		}
	}

	@Override
	public String getLogData(TestStructure testStructure) throws SystemException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TestResult> getTestHistory(TestStructure testStructure) throws SystemException {
		List<TestResult> result = new ArrayList<TestResult>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			Path testResultsDirOfTestStructure = Paths.get(getPathToTestResults(testStructure));
			LOGGER.trace("Reading Testhistory from " + testResultsDirOfTestStructure);
			DirectoryStream<Path> stream = Files.newDirectoryStream(testResultsDirOfTestStructure);
			for (Path path : stream) {
				FitNesseResultReader reader = FitNesseResultReaderFactory.getReader(TestType.valueOf(testStructure
						.getPageType().toUpperCase()));
				TestResult testResult = reader.readTestResult(new FileInputStream(path.toFile()));
				String timestampString = path.getFileName().toString().substring(0, 14);
				LOGGER.trace("Reading Testhistory with Timestamp " + timestampString);
				testResult.setResultDate(sdf.parse(timestampString));
				testResult.setFullName(testStructure.getFullName());
				result.add(testResult);
			}
		} catch (IOException | ParseException e) {
			LOGGER.error("Error reading testresults of teststructrue: " + testStructure, e);
			throw new SystemException("Error reading testresults of teststructrue: " + testStructure + "\n"
					+ e.getMessage(), e);
		}
		return result;
	}

	@Override
	public boolean isReservedName(String name) {
		return getSpecialPages().contains(name);
	}

	/**
	 * FitNesse has some reserved Words for special pages. This pages are used
	 * for example as test preparation. See:
	 * http://fitnesse.org/FitNesse.UserGuide.SpecialPages
	 * 
	 * @return a Set of reserved Names in FitNesse.
	 */
	private Set<String> getSpecialPages() {
		Set<String> specialPages = new HashSet<String>();
		specialPages.add("PageHeader");
		specialPages.add("PageFooter");
		specialPages.add("SetUp");
		specialPages.add("TearDown");
		specialPages.add("SuiteSetUp");
		specialPages.add("SuiteTearDown");
		specialPages.add("ScenarioLibrary");
		specialPages.add("TemplateLibrary");
		specialPages.add("Suites");
		return specialPages;
	}

	@Override
	public void clearHistory(TestStructure testStructure) throws SystemException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isTestStructureInHirachieOfChildTestStructure(TestStructure changedTestStructure,
			TestStructure childTestStructure) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Runnable getTestProjectLazyLoader(final TestCompositeStructure toBeLoadedLazy) {
		return new Runnable() {

			@Override
			public void run() {
				try {
					loadTestStructuresChildrenFor(toBeLoadedLazy);
				} catch (SystemException e) {
					LOGGER.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}

		};
	}

	@Override
	public String getId() {
		return FitSlimTestServerConstants.PLUGIN_ID;
	}

	@Override
	public boolean hasTestExecutionReport() {
		return false;
	}

	/**
	 * Binds the osgi service <code>TestServerService</code> to this service.
	 * The service is used to launch the TestServer for execution.
	 * 
	 * @param testServerService
	 *            to be bind.
	 */
	public void bind(TestServerService testServerService) {
		LOGGER.info("Binding " + testServerService);
		this.testServerService = testServerService;
	}

	/**
	 * Removes the osgi service <code>TestServerService</code> from this
	 * service.
	 * 
	 * @param testServerService
	 *            is ignored.
	 */
	public void unBind(TestServerService testServerService) {
		this.testServerService = null;
	}

}
