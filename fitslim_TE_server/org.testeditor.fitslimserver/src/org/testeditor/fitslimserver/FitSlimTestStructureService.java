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
import org.testeditor.core.model.teststructure.TestCase;
import org.testeditor.core.model.teststructure.TestCompositeStructure;
import org.testeditor.core.model.teststructure.TestStructure;
import org.testeditor.core.model.teststructure.TestSuite;
import org.testeditor.core.services.interfaces.TestStructureService;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fitnesse.ContextConfigurator;
import fitnesse.FitNesseContext;
import fitnesse.PluginException;

public class FitSlimTestStructureService implements TestStructureService {

	private static final Logger LOGGER = Logger.getLogger(FitSlimTestStructureService.class);

	@Override
	public void loadTestStructuresChildrenFor(TestCompositeStructure testCompositeStructure) throws SystemException {
		String pathInProject = testCompositeStructure.getFullName().replaceAll("\\.", "/");
		Path path = Paths.get(Platform.getLocation().toFile().toPath().toString() + "/" + pathInProject);
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

	protected TestStructure createTestStructureFrom(File propertyFile) throws SystemException {
		TestStructure result = null;
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = documentBuilder.parse(propertyFile);
			if (document.getFirstChild().getNodeName().equals("properties")) {
				NodeList nodeList = document.getFirstChild().getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++) {
					System.out.println();
					if (nodeList.item(i).getNodeName().equals("Test")) {
						result = new TestCase();
						break;
					}
					if (nodeList.item(i).getNodeName().equals("Suite")) {
						result = new TestSuite();
						((TestSuite) result)
								.setChildCount(propertyFile.getParentFile().listFiles(getDirectoryFilter()).length);
						((TestSuite) result).setLazyLoader(getTestProjectLazyLoader((TestCompositeStructure) result));
						break;
					}
				}
				if (result != null) {
					result.setName(propertyFile.getParentFile().getName());
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
		try {
			FitNesseContext context = ContextConfigurator.systemDefaults().makeFitNesseContext();

		} catch (IOException | PluginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
}
