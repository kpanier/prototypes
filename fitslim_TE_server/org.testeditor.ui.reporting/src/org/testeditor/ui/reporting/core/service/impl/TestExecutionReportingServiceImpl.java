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
package org.testeditor.ui.reporting.core.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.testeditor.ui.reporting.TestExecutionReportConstants;
import org.testeditor.ui.reporting.core.model.TestExecutionReport;
import org.testeditor.ui.reporting.core.service.TestExecutionReportingService;

public class TestExecutionReportingServiceImpl extends ContextFunction implements TestExecutionReportingService {

	private static final Logger LOGGER = Logger.getLogger(TestExecutionReportingServiceImpl.class);
	private IEventBroker eventBroker;
	private Set<TestExecutionReport> testExecutionLog = new HashSet<TestExecutionReport>();
	private TestExecutionReport runningTest;

	@Override
	public void cancelTest() {

	}

	@Override
	public TestExecutionReport startingTest(String name) {
		TestExecutionReport report = new TestExecutionReport(name, new Date());
		testExecutionLog.add(report);
		eventBroker.post(TestExecutionReportConstants.NEW_TEST_STARTED, name);
		runningTest = report;
		return report;
	}

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		LOGGER.info("TestExecutionReportingServiceImpl bindet to the Eclipse Context.");
		eventBroker = context.get(IEventBroker.class);
		return this;
	}

	@Override
	public Set<TestExecutionReport> getTestExecutionReports() {
		return testExecutionLog;
	}

	@Override
	public void reportAction(String action, String output) {
		eventBroker.post(TestExecutionReportConstants.ADD_TEST_ACTION, action);
		runningTest.add(new Date(), action, output);
	}

	@Override
	public TestExecutionReport getRunningTestExecution() {
		return runningTest;
	}

	@Override
	public Appender getLogAppender() {
		return new Appender() {

			@Override
			public void setName(String name) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setLayout(Layout layout) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setErrorHandler(ErrorHandler errorHandler) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean requiresLayout() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Layout getLayout() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Filter getFilter() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ErrorHandler getErrorHandler() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void doAppend(LoggingEvent event) {
				runningTest.addLog(event.getMessage().toString());
			}

			@Override
			public void close() {
				// TODO Auto-generated method stub

			}

			@Override
			public void clearFilters() {
				// TODO Auto-generated method stub

			}

			@Override
			public void addFilter(Filter newFilter) {
				// TODO Auto-generated method stub

			}
		};
	}

}
