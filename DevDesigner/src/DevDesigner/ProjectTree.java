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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class ProjectTree {

	
	private TreeViewer treeViewer;

	@PostConstruct
	public void createControls(Composite parent) {
		System.out.println("hey hoh");
		parent.setLayout(new FillLayout());
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new ITreeContentProvider() {
			
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
				if(element instanceof ProjectLib)
					return true;
				if(element instanceof LibGroup)
					return true;
				return false;
			}
			
			@Override
			public Object getParent(Object element) {
				return null;
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				return ((List<ProjectLib>)inputElement).toArray();
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				if(parentElement instanceof ProjectLib){
					ProjectLib l = (ProjectLib) parentElement;
					return l.getLibGroups().toArray();
				}
				if(parentElement instanceof LibGroup){
					return ((LibGroup)parentElement).getElemnts().toArray();
				}
				return null;
			}
		});
		List<ProjectLib>  projects = new ArrayList<ProjectLib>();
		ProjectLib lib = new ProjectLib("TestEditorTests");
		lib.addActionGroup("View: TestExplorer");
		lib.addActionGroup("View: TestFall");
		lib.addActionGroup("View: TestHistorie");
		lib.addWidgetType("Button");
		lib.addWidgetType("Eingabefeld");
		lib.addWidgetType("Baum");
		lib.addWidgetType("Combobox");
		lib.addTechnicalBinding("starte Anwednung");
		lib.addTechnicalBinding("beende Anwendung");
		lib.addTechnicalBinding("suche Text");
		projects.add(lib);
		lib = new ProjectLib("DemoWebTests");
		projects.add(lib);
		treeViewer.setInput(projects);
	}
	
}
