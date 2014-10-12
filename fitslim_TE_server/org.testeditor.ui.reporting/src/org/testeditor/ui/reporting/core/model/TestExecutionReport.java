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
package org.testeditor.ui.reporting.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestExecutionReport {

	private String testName;
	private Date startTime;
	private List<TestExecutionAction> actions = new ArrayList<TestExecutionAction>();
	private StringBuilder log = new StringBuilder();

	public TestExecutionReport(String name, Date date) {
		this.testName = name;
		this.startTime = date;
	}

	public String getTestName() {
		return testName;
	}

	public Date getStartTime() {
		return startTime;
	}

	@Override
	public String toString() {
		return testName + " started on: " + startTime;
	}

	public void add(Date eventTime, String action, String output) {
		TestExecutionAction executionAction = new TestExecutionAction(eventTime, action, output);
		actions.add(executionAction);
	}

	public List<TestExecutionAction> getActions() {
		return actions;
	}

	public void addLog(String string) {
		log.append(string);
	}

	public String getLog() {
		return log.toString();
	}
}
