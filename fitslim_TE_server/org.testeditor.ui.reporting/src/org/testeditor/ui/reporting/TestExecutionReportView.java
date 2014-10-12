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
package org.testeditor.ui.reporting;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.testeditor.ui.reporting.core.model.TestExecutionReport;
import org.testeditor.ui.reporting.core.service.TestExecutionReportingService;

public class TestExecutionReportView {

	private TreeViewer testExecutionTree;

	@Inject
	private TestExecutionReportingService reportingService;

	private Text logText;

	@PostConstruct
	public void createControls(Composite parent) {
		parent.setLayout(new GridLayout(2, true));
		testExecutionTree = new TreeViewer(parent);
		testExecutionTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		testExecutionTree.setContentProvider(new TestExecutionReportContentProvider());
		testExecutionTree.setLabelProvider(new TestExecutionReportLabelProvider());
		testExecutionTree.setInput(reportingService);
		testExecutionTree.addSelectionChangedListener(getSelectionChangedListener());
		logText = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		logText.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	private ISelectionChangedListener getSelectionChangedListener() {
		return new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object element = ((TreeSelection) testExecutionTree.getSelection()).getFirstElement();
				if (element instanceof TestExecutionReport) {
					TestExecutionReport testReport = (TestExecutionReport) element;
					logText.setText(testReport.getLog());
				}
			}
		};
	}

	@Inject
	@Optional
	public void refreshView(@UIEventTopic(TestExecutionReportConstants.REFRESH_REPORTING_VIEW + "/*") String data) {
		if (testExecutionTree != null) {
			testExecutionTree.refresh();
			testExecutionTree.expandToLevel(reportingService.getRunningTestExecution(), 2);
			logText.setText(reportingService.getRunningTestExecution().getLog());
		}
	}

}
