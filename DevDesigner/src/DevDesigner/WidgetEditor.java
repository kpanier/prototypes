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
package DevDesigner;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class WidgetEditor {

	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new FillLayout());
		createEditArea(new Composite(parent, SWT.None));
		createToolsArea(new Composite(parent, SWT.None));
	}

	private void createToolsArea(Composite parent) {
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		createActionsTool(new Composite(parent, SWT.BORDER));
	}

	private void createActionsTool(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		new Label(parent, SWT.NONE).setText("TechnicalBinding:");
		Combo combo = new Combo(parent, SWT.NORMAL);
		combo.add("klicke");
		combo.add("trage Wert ein");
		combo.add("lösche Inhalt");
		combo.add("start Anwendung");
		combo.add("expandiere");
		new Button(parent, SWT.NORMAL).setText("Hinzufügen");
	}


	private void createEditArea(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		Label label = new Label(parent, SWT.NONE);
		label.setText("Name:");
		Text text = new Text(parent, SWT.BORDER);
		text.setText("Eingabefeld");
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label ldesc = new Label(parent, SWT.None);
		ldesc.setText("Description:");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		ldesc.setLayoutData(gd);
		Text desc = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.minimumHeight = 100;
		desc.setLayoutData(gd);
		desc.setText("Das  Element Eingabefeld ist für die Eingabe von Daten zuständig. "
				+ "Bei Services kann dies auch ein einfaches Feld sein was nicht in der UI ist, "
				+ "sondern beispielweise ein XML Attrbiut für einen Webservice.");
		Label actionL = new Label(parent, SWT.NORMAL);
		actionL.setText("Actions:");
		gd = new GridData();
		gd.horizontalSpan = 2;
		actionL.setLayoutData(gd);
		TreeViewer treeviewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		treeviewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub

			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof ProjectLib)
					return true;
				if (element instanceof LibGroup)
					return true;
				return false;
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return ((List<ProjectLib>) inputElement).toArray();
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof ProjectLib) {
					ProjectLib l = (ProjectLib) parentElement;
					return l.getLibGroups().toArray();
				}
				if (parentElement instanceof LibGroup) {
					return ((LibGroup) parentElement).getElemnts().toArray();
				}
				return null;
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		treeviewer.getTree().setLayoutData(gd);
		List<Object> actions = new ArrayList<Object>();
		actions.add("Eingabe {param}");
		actions.add("Feld auswählen");
		actions.add("Prüfe ob {param} drinne steht");
		actions.add("lösche Inhalt");
		treeviewer.setInput(actions);
		treeviewer.expandAll();
	}

}