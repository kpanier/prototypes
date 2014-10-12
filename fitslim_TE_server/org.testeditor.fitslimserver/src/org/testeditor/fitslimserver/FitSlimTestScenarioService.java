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

import java.util.List;

import org.apache.log4j.Logger;
import org.testeditor.core.exceptions.SystemException;
import org.testeditor.core.model.teststructure.ScenarioSuite;
import org.testeditor.core.model.teststructure.TestComponent;
import org.testeditor.core.model.teststructure.TestCompositeStructure;
import org.testeditor.core.model.teststructure.TestProject;
import org.testeditor.core.model.teststructure.TestScenario;
import org.testeditor.core.model.teststructure.TestStructure;
import org.testeditor.core.services.interfaces.TestEditorGlobalConstans;
import org.testeditor.core.services.interfaces.TestScenarioService;
import org.testeditor.core.services.interfaces.TestStructureContentService;
import org.testeditor.core.services.interfaces.TestStructureService;

public class FitSlimTestScenarioService implements TestScenarioService {

	private static final Logger LOGGER = Logger.getLogger(FitSlimTestScenarioService.class);
	private TestStructureService testStructureService;
	private TestStructureContentService testStructureContentService;

	@Override
	public boolean isLinkToScenario(TestProject testProject, String linkToFile) throws SystemException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getUsedOfTestSceneario(TestScenario testScenario) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDescendantFromTestScenariosSuite(TestStructure testStructure) {
		return testStructure instanceof ScenarioSuite || testStructure.getParent() instanceof ScenarioSuite;
	}

	@Override
	public TestScenario getScenarioByFullName(TestProject testProject, String includeOfScenario) throws SystemException {
		String[] includeOfScenarioStrings = includeOfScenario.split("\\.");
		if (includeOfScenarioStrings.length >= 0 && testProject.getName().equalsIgnoreCase(includeOfScenarioStrings[0])) {
			return findTestStructureInOffspringOfProject(includeOfScenario, testProject);
		}
		return null;
	}

	/**
	 * 
	 * @param includeOfScenario
	 *            the include as a String
	 * @param parent
	 *            the {@link TestCompositeStructure}
	 * @return the TestScenario or null, if not found
	 * @throws SystemException
	 *             by reading the scenario
	 */
	private TestScenario findTestStructureInOffspringOfProject(String includeOfScenario, TestCompositeStructure parent)
			throws SystemException {
		TestScenario testScenario = (TestScenario) parent.getTestChildByFullName(includeOfScenario);
		if (testScenario.getTestComponents().isEmpty()) {
			readTestScenario(testScenario, testStructureService.getTestStructureAsText(testScenario));
		}
		return testScenario;
	}

	@Override
	public boolean isSuiteForScenarios(TestStructure element) {
		return isReservedNameForRootSceanrioSuite(element.getName());
	}

	@Override
	public boolean isReservedNameForRootSceanrioSuite(String pageName) {
		return pageName.equalsIgnoreCase(TestEditorGlobalConstans.TEST_SCENARIO_SUITE)
				|| pageName.equalsIgnoreCase(TestEditorGlobalConstans.TEST_KOMPONENTS);
	}

	@Override
	public void readTestScenario(TestScenario testScenario, String testStructureText) throws SystemException {
		if (!testStructureText.isEmpty()) {
			List<TestComponent> testComponents = testStructureContentService.parseFromString(testScenario,
					testStructureText);
			testScenario.setTestComponents(testComponents);
		}
	}

	/**
	 * Bind the Service.
	 * 
	 * @param service
	 *            the TestStructureService used in this service.
	 */
	public void bind(TestStructureService service) {
		if (service.getId().equals(FitSlimTestServerConstants.PLUGIN_ID)) {
			testStructureService = service;
			LOGGER.info("Bind TestStructureService: " + service);
		}
	}

	/**
	 * Removes the testStrucutreService.
	 * 
	 * @param service
	 *            to be removed.
	 */
	public void unBind(TestStructureService service) {
		if (service.getId().equals(FitSlimTestServerConstants.PLUGIN_ID)) {
			testStructureService = null;
			LOGGER.info("Unbind TestStructureService");
		}
	}

	/**
	 * 
	 * @param testStructureContentService
	 *            used in this service
	 * 
	 */
	public void bind(TestStructureContentService testStructureContentService) {
		if (testStructureContentService.getId().equals(FitSlimTestServerConstants.PLUGIN_ID)) {
			this.testStructureContentService = testStructureContentService;
			LOGGER.info("Bind TestStructureContentService:" + testStructureContentService);
		}
	}

	/**
	 * Removes the TestStructureContentService.
	 * 
	 * 
	 * @param testStructureContentService
	 *            removed from system
	 */
	public void unBind(TestStructureContentService testStructureContentService) {
		if (testStructureContentService.getId().equals(FitSlimTestServerConstants.PLUGIN_ID)) {
			this.testStructureContentService = null;
			LOGGER.info("Unbind TestStructureContentService");
		}
	}

}
