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

import java.util.Date;

public class TestExecutionAction {

	private Date eventTime;
	private String action;
	private String output;

	public TestExecutionAction(Date eventTime, String action, String output) {
		this.eventTime = eventTime;
		this.action = action;
		this.output = output;
	}

	public Date getEventTime() {
		return eventTime;
	}

	public String getAction() {
		return action;
	}

	public String getOutput() {
		return output;
	}

	@Override
	public String toString() {
		return action;
	}

}
