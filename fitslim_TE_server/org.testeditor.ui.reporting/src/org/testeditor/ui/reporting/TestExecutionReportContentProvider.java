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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.testeditor.ui.reporting.core.model.TestExecutionAction;
import org.testeditor.ui.reporting.core.model.TestExecutionReport;
import org.testeditor.ui.reporting.core.service.TestExecutionReportingService;

public class TestExecutionReportContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		TestExecutionReportingService service = (TestExecutionReportingService) inputElement;
		return service.getTestExecutionReports().toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TestExecutionReport) {
			return ((TestExecutionReport) parentElement).getActions().toArray();
		}
		if (parentElement instanceof TestExecutionAction) {
			return new String[] { ((TestExecutionAction) parentElement).getOutput() };
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof TestExecutionReport) {
			return !((TestExecutionReport) element).getActions().isEmpty();
		}
		if (element instanceof TestExecutionAction) {
			return true;
		}
		return false;
	}

}
