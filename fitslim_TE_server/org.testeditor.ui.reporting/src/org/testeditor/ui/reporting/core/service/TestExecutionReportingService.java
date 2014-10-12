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
package org.testeditor.ui.reporting.core.service;

import java.util.Set;

import org.apache.log4j.Appender;
import org.testeditor.ui.reporting.core.model.TestExecutionReport;

public interface TestExecutionReportingService {

	// void reportActionStart(String name, Object[] convertedArgs);

	void cancelTest();

	TestExecutionReport startingTest(String name);

	Set<TestExecutionReport> getTestExecutionReports();

	void reportAction(String action, String output);

	TestExecutionReport getRunningTestExecution();

	Appender getLogAppender();

}
