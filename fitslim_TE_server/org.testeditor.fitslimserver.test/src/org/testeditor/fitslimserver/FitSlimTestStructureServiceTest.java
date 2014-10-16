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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.testeditor.core.exceptions.SystemException;
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
	 *             on Test failure.
	 */
	@Test
	public void testLoadTestStructuresChildrenFor() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		service.loadTestStructuresChildrenFor(testProject);
		assertEquals(2, testProject.getTestChildren().size());
	}

	/**
	 * Test the Remove of a Testcase from the filesystem. It checks fist that
	 * there is a testcase in the file system. After removing the file entry is
	 * also away.
	 * 
	 * @throws Exception
	 *             on Test failure.
	 */
	@Test
	public void testRemoveTestStructure() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		assertTrue("Directory of Testcase exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString() + "/tp/FitNesseRoot/tp/tc")));
		service.removeTestStructure(testProject.getTestChildByFullName("tp.tc"));
		assertFalse("Directory of Testcase is removed.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString() + "/tp/FitNesseRoot/tp/tc")));
	}

	/**
	 * Tests the Adding of a TestCase to an existing TestProject
	 * 
	 * @throws Exception
	 *             on Test failure.
	 */
	@Test
	public void testAddTestCaseToTestProject() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		TestCase tc = new TestCase();
		tc.setName("MyTestCase");
		testProject.addChild(tc);
		service.createTestStructure(tc);
		assertTrue(
				"Directory of Testcase exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/MyTestCase")));
		assertTrue(
				"Content of Testcase exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/MyTestCase/content.txt")));
		assertTrue(
				"Properties of Testcase exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/MyTestCase/properties.xml")));
		assertTrue(new String(Files.readAllBytes(Paths.get(Platform.getLocation().toFile().toPath().toString()
				+ "/tp/FitNesseRoot/tp/MyTestCase/properties.xml"))).contains("<Test/>"));
	}

	/**
	 * Tests Exception on adding of a allready existing TestCase to an existing
	 * TestProject
	 * 
	 * @throws Exception
	 *             on Test failure.
	 */
	@Test
	public void testAddDuplicateTestCaseToTestProject() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		TestCase tc = new TestCase();
		tc.setName("tc");
		testProject.addChild(tc);
		try {
			service.createTestStructure(tc);
			fail("Exception expected.");
		} catch (SystemException e) {
			assertTrue(e.getMessage().contains("TestStructure allready exits"));
		}
	}

	/**
	 * Tests the Adding of a TestCase to an existing TestProject
	 * 
	 * @throws Exception
	 *             on Test failure.
	 */
	@Test
	public void testAddTestCaseToTestSuite() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		TestCase tc = new TestCase();
		tc.setName("MyTestCase");
		TestStructure structure = testProject.getTestChildByFullName("tp.ts");
		((TestCompositeStructure) structure).addChild(tc);
		service.createTestStructure(tc);
		assertTrue(
				"Directory of Testcase exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/ts/MyTestCase")));
		assertTrue(
				"Properties of Testcase exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/ts/MyTestCase/properties.xml")));
		assertTrue(new String(Files.readAllBytes(Paths.get(Platform.getLocation().toFile().toPath().toString()
				+ "/tp/FitNesseRoot/tp/ts/MyTestCase/properties.xml"))).contains("<Test/>"));
	}

	/**
	 * Tests the Adding of a TestSuite to an existing TestProject
	 * 
	 * @throws Exception
	 *             on Test failure.
	 */
	@Test
	public void testAddTestStuiteToTestProject() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		TestSuite ts = new TestSuite();
		ts.setName("CiSuite");
		testProject.addChild(ts);
		service.createTestStructure(ts);
		assertTrue(
				"Directory of TestSuite exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/CiSuite")));
		assertTrue(
				"Content of TestSuite exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/CiSuite/content.txt")));
		assertTrue(
				"Properties of TestSuite exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/CiSuite/properties.xml")));
		assertTrue(
				"Property Suite exists.",
				new String(Files.readAllBytes(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/CiSuite/properties.xml"))).contains("<Suite/>"));
	}

	/**
	 * Tests the Adding of a TestScenario to an existing TestProject
	 * 
	 * @throws Exception
	 *             on Test failure.
	 */
	@Test
	public void testAddTestScenarioToTestProject() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		TestScenario tsc = new TestScenario();
		tsc.setName("Scenario");
		testProject.addChild(tsc);
		service.createTestStructure(tsc);
		assertTrue(
				"Directory of TestScenario exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/Scenario")));
		assertTrue(
				"Content of TestScenario exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/Scenario/content.txt")));
		assertTrue(
				"Properties of TestScenario exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/Scenario/properties.xml")));
		assertTrue(
				"Peroperty is Testscenario",
				new String(Files.readAllBytes(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/Scenario/properties.xml"))).contains("<TESTSCENARIO/>"));
	}

	/**
	 * Tests the Adding of a ScenarioSuite to an existing TestProject
	 * 
	 * @throws Exception
	 *             on Test failure.
	 */
	@Test
	public void testAddScenarioSuiteToTestProject() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		ScenarioSuite scs = new ScenarioSuite();
		scs.setName("ScenarioSuite");
		testProject.addChild(scs);
		service.createTestStructure(scs);
		assertTrue(
				"Directory of ScenarioSuite exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/ScenarioSuite")));
		assertTrue(
				"Content of ScenarioSuite exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/ScenarioSuite/content.txt")));
		assertTrue(
				"Properties of ScenarioSuite exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/ScenarioSuite/properties.xml")));
		assertTrue(
				"Property Suites exitsts.",
				new String(Files.readAllBytes(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/ScenarioSuite/properties.xml"))).contains("<Suites/>"));
	}

	/**
	 * Tests the Adding of a TestScenario to an existing ScenarioSuite
	 * 
	 * @throws Exception
	 *             on Test failure.
	 */
	@Test
	public void testAddTestScenarioToScenarioSuite() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		ScenarioSuite scs = new ScenarioSuite();
		scs.setName("ScenarioSuite");
		testProject.addChild(scs);
		service.createTestStructure(scs);
		TestScenario tsc = new TestScenario();
		tsc.setName("Scenario");
		scs.addChild(tsc);
		service.createTestStructure(tsc);
		assertTrue(
				"Directory of Testcase exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/ScenarioSuite/Scenario")));
		assertTrue(
				"Properties of Testcase exists.",
				Files.exists(Paths.get(Platform.getLocation().toFile().toPath().toString()
						+ "/tp/FitNesseRoot/tp/ScenarioSuite/Scenario/properties.xml")));
		assertTrue(new String(Files.readAllBytes(Paths.get(Platform.getLocation().toFile().toPath().toString()
				+ "/tp/FitNesseRoot/tp/ScenarioSuite/Scenario/properties.xml"))).contains("<TESTSCENARIO/>"));
	}

	/**
	 * Integrationtest to add and load a TestTRee.
	 * 
	 * @throws Exception
	 *             on Test failure.
	 */
	@Test
	public void testIntegrationOfAddAndReloadOfTestStructures() throws Exception {
		TestProject testProject = createTestProjectsInWS();
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		TestSuite suite = new TestSuite();
		suite.setName("MySuite");
		TestCase tc1 = new TestCase();
		tc1.setName("MyTest");
		suite.addChild(tc1);
		testProject.addChild(suite);
		TestCase tc2 = new TestCase();
		tc2.setName("SecondTest");
		testProject.addChild(tc2);
		service.createTestStructure(tc2);
		service.createTestStructure(suite);
		service.createTestStructure(tc1);
		TestProject tp = new TestProject();
		tp.setName("tp");
		service.loadTestStructuresChildrenFor(tp);
		assertTrue(tp.getAllTestChildren().contains(tc2));
		assertTrue(tp.getAllTestChildren().contains(tc1));
		assertTrue(tp.getAllTestChildren().contains(suite));
	}

	/**
	 * Test the check for reserved names.
	 */
	@Test
	public void testIsReservedName() {
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		assertTrue(service.isReservedName("SetUp"));
		assertTrue(service.isReservedName("TearDown"));
		assertFalse(service.isReservedName("MyTestCase"));
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
		TestSuite ts = new TestSuite();
		ts.setName("ts");
		result.addChild(ts);
		Files.createDirectories(Paths.get(Platform.getLocation().toFile().toPath().toString()
				+ "/tp/FitNesseRoot/tp/tc"));
		TestCase testCase = new TestCase();
		testCase.setName("tc");
		result.addChild(testCase);
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
