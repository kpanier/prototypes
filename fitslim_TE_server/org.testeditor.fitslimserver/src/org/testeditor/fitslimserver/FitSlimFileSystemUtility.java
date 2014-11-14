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

import org.eclipse.core.runtime.Platform;
import org.testeditor.core.model.teststructure.TestStructure;

/**
 * Utlitity Class for common File Operations.
 *
 */
public final class FitSlimFileSystemUtility {

	/**
	 * Utility Class can not be instantiated.
	 */
	private FitSlimFileSystemUtility() {

	}

	/**
	 * Creates the Path to the Directory of the TestStructure in the FileSystem
	 * as a string.
	 * 
	 * @param testStructure
	 *            to be used for lookup.
	 * @return the path as string to the TestStructure.
	 */
	public static String getPathToTestStructureDirectory(TestStructure testStructure) {
		StringBuilder sb = new StringBuilder();
		sb.append(getPathToProject(testStructure));
		String pathInProject = testStructure.getFullName().replaceAll("\\.", "/");
		sb.append(File.separator).append("FitNesseRoot").append(File.separator).append(pathInProject);
		return sb.toString();
	}

	/**
	 * 
	 * @param testStructure
	 *            used to get the TestProcject and looks it's location in the
	 *            filesystem.
	 * @return path as string of the root element of the given teststructure.
	 */
	public static String getPathToProject(TestStructure testStructure) {
		StringBuilder sb = new StringBuilder();
		sb.append(Platform.getLocation().toFile().toPath().toString()).append(File.separator)
				.append(testStructure.getRootElement().getName());
		return sb.toString();
	}

}
