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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.testeditor.core.services.interfaces.TestEditorGlobalConstans;
import org.testeditor.core.services.interfaces.TestServerService;
import org.testeditor.core.services.interfaces.TestStructureService;
import org.testeditor.fitnesse.util.FitNesseRestClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FitSlimTestStructureService implements TestStructureService {

	private static final Logger LOGGER = Logger.getLogger(FitSlimTestStructureService.class);
	private TestServerService testServerService;

	@Override
	public void loadTestStructuresChildrenFor(TestCompositeStructure testCompositeStructure) throws SystemException {
		Path path = Paths.get(getPathTo(testCompositeStructure));
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

	public String getPathTo(TestStructure testStructure) {
		StringBuilder sb = new StringBuilder();
		sb.append(getPathToProject(testStructure));
		String pathInProject = testStructure.getFullName().replaceAll("\\.", File.separator);
		sb.append(File.separator).append("FitNesseRoot").append(File.separator).append(pathInProject);
		return sb.toString();
	}

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
				result.setName(testStructureName);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void removeTestStructure(TestStructure testStructure) throws SystemException {
		// TODO Auto-generated method stub

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

	public String getPathToProject(TestStructure testStructure) {
		StringBuilder sb = new StringBuilder();
		sb.append(Platform.getLocation().toFile().toPath().toString()).append(File.separator)
				.append(testStructure.getRootElement().getName());
		return sb.toString();
	}

	@Override
	public String getTestStructureAsText(TestStructure testStructure) throws SystemException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLogData(TestStructure testStructure) throws SystemException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TestResult> getTestHistory(TestStructure testStructure) throws SystemException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReservedName(String name) {
		// TODO Auto-generated method stub
		return false;
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
					LOGGER.error(e.getMessage());
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
