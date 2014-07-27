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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.junit.Test;
import org.testeditor.core.model.teststructure.TestCase;
import org.testeditor.core.model.teststructure.TestCompositeStructure;
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

	@Test
	public void testCreateTestStructureFromTestSuiteProperties() throws Exception {
		FitSlimTestStructureService service = new FitSlimTestStructureService();
		URL url = FileLocator.toFileURL(this.getClass().getResource("ts_properties.xml"));
		TestStructure testStructure = service.createTestStructureFrom(new File(url.toURI()));
		assertTrue(testStructure instanceof TestSuite);
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

}
