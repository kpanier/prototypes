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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.junit.After;
import org.junit.Test;
import org.testeditor.core.model.teststructure.ScenarioSuite;
import org.testeditor.core.model.teststructure.TestCase;
import org.testeditor.core.model.teststructure.TestCompositeStructure;
import org.testeditor.core.model.teststructure.TestProject;
import org.testeditor.core.model.teststructure.TestScenario;
import org.testeditor.core.model.teststructure.TestStructure;
import org.testeditor.core.model.teststructure.TestSuite;
import org.testeditor.core.services.interfaces.ServiceLookUpForTest;
import org.testeditor.core.services.interfaces.TestEditorPlugInService;
import org.testeditor.core.services.interfaces.TestStructureService;

/**
 * 
 * Tests for the Fit and Slim based Implementation of the TestStructureService.
 * 
 */
public class FitSlimTestStructureServiceTest {

	/**
	 * Tests the registration of the FitSlim based implementation at the
	 * PluginServcie.
	 */
	@Test
	public void testServiceRegistration() {
		TestEditorPlugInService plugInService = ServiceLookUpForTest.getService(TestEditorPlugInService.class);
		TestStructureService testStructureService = plugInService
				.getTestStructureServiceFor(FitSlimTestServerConstants.PLUGIN_ID);
		assertNotNull(testStructureService);
		assertTrue(testStructureService instanceof FitSlimTestStructureService);
	}

	/**
	 * Tests the correct reading of the teststructure type testcase.
	 * 
	 * @throws Exception
	 *             on test failure
	 */
	@Test
	public void testCreateTestStructureFromTestCaseProperties() throws Exception {
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		URL url = FileLocator.toFileURL(this.getClass().getResource("tc_properties.xml"));
		assertNotNull(url);
		File file = new File(url.toURI());
		assertNotNull(file);
		assertTrue(file.exists());
		TestStructure testStructure = service.createTestStructureFrom(file);
		assertTrue(testStructure instanceof TestCase);
	}

	/**
	 * Tests the correct reading of the teststructure type testsuite.
	 * 
	 * @throws Exception
	 *             on test failure
	 */
	@Test
	public void testCreateTestStructureFromTestSuiteProperties() throws Exception {
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		URL url = FileLocator.toFileURL(this.getClass().getResource("ts_properties.xml"));
		TestStructure testStructure = service.createTestStructureFrom(new File(url.toURI()));
		assertTrue(testStructure instanceof TestSuite);
	}

	/**
	 * Tests the correct reading of the teststructure type testscenariosuite.
	 * 
	 * @throws Exception
	 *             on test failure
	 */
	@Test
	public void testCreateTestStructureFromTestScenarioSuiteProperties() throws Exception {
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		URL url = FileLocator.toFileURL(this.getClass().getResource("tsecsuite_properties.xml"));
		TestStructure testStructure = service.createTestStructureFrom(new File(url.toURI()));
		assertTrue(testStructure instanceof ScenarioSuite);
	}

	/**
	 * Tests the correct reading of the teststructure type testscenario.
	 * 
	 * @throws Exception
	 *             on test failure
	 */
	@Test
	public void testCreateTestStructureFromTestScenarioProperties() throws Exception {
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		URL url = FileLocator.toFileURL(this.getClass().getResource("tsec_properties.xml"));
		TestStructure testStructure = service.createTestStructureFrom(new File(url.toURI()));
		assertTrue(testStructure instanceof TestScenario);
	}

	/**
	 * Tests the lookup of the Path to the teststructure.
	 */
	@Test
	public void testGetPathToTestStructure() {
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		TestProject testProject = new TestProject();
		testProject.setName("MyTestPrj");
		TestSuite testSuite = new TestSuite();
		testSuite.setName("MySuite");
		TestStructure testStructure = new TestCase();
		testStructure.setName("ATestCase");
		testProject.addChild(testSuite);
		testSuite.addChild(testStructure);
		String pathPart = File.separator + "FitNesseRoot" + File.separator + "MyTestPrj" + File.separator + "MySuite"
				+ File.separator + "ATestCase";
		assertTrue("Path ends with", service.getPathTo(testStructure).endsWith(pathPart));
	}

	/**
	 * Test the loading of the sub testcases of a teststructurecomposite.
	 * 
	 * @throws Exception
	 *             on Testfailure.
	 */
	@Test
	public void testLoadTestStructuresChildrenFor() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		service.loadTestStructuresChildrenFor(testProject);
		assertEquals(2, testProject.getTestChildren().size());
	}

	/**
	 * Tests receiving a runnable.
	 * 
	 * @throws Exception
	 *             on loading tests.
	 */
	@Test
	public void testGetRunnableForLazyLoading() throws Exception {
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		URL url = FileLocator.toFileURL(this.getClass().getResource("ts_properties.xml"));
		TestCompositeStructure testStructure = (TestCompositeStructure) service.createTestStructureFrom(new File(url
				.toURI()));
		Runnable runnable = service.getTestProjectLazyLoader(testStructure);
		assertNotNull(runnable);
		// runnable.run();
		// assertEquals(3, testStructure.getAllTestChildren().size());
	}

	/**
	 * Creates a Project with a small test tree for test purpose.
	 * 
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private TestProject createTestProjectsInWS() throws IllegalStateException, IOException {
		TestProject result = new TestProject();
		result.setName("tp");
		Files.createDirectories(Paths.get(Platform.getLocation().toFile().toPath().toString() + "/tp/FitNesseRoot/tp"));
		Files.createDirectories(Paths.get(Platform.getLocation().toFile().toPath().toString()
				+ "/tp/FitNesseRoot/tp/ts"));
		Files.copy(this.getClass().getResourceAsStream("ts_properties.xml"), Paths.get(Platform.getLocation().toFile()
				.toPath().toString()
				+ "/tp/FitNesseRoot/tp/ts/properties.xml"));
		Files.createDirectories(Paths.get(Platform.getLocation().toFile().toPath().toString()
				+ "/tp/FitNesseRoot/tp/tc"));
		Files.copy(this.getClass().getResourceAsStream("tc_properties.xml"), Paths.get(Platform.getLocation().toFile()
				.toPath().toString()
				+ "/tp/FitNesseRoot/tp/tc/properties.xml"));
		return result;
	}

	/**
	 * Cleans up the workspace after test execution.
	 * 
	 * @throws IOException
	 *             on deleting files
	 */
	@After
	public void cleanUPWorkspace() throws IOException {
		Path directory = Platform.getLocation().toFile().toPath();
		if (directory.toFile().isDirectory()) {
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
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
		}
	}

}
