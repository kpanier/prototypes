 
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

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.jface.gridviewer.GridViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ElementListPart {
	
	static class Person {
		private String firstname;
		private String lastname;
		
		public Person(String firstname, String lastname) {
			this.firstname = firstname;
			this.lastname = lastname;
		}
	}
	
	@PostConstruct
	void initUI(Composite parent) {
		final GridTableViewer v = new GridTableViewer(parent);
		v.getGrid().setHeaderVisible(true);
		v.getGrid().setCellSelectionEnabled(true);
		v.getGrid().setRowHeaderVisible(true);
		
		final TextCellEditor textEditor = new TextCellEditor(v.getGrid()) {
			@Override
			public void activate(
					ColumnViewerEditorActivationEvent activationEvent) {
				if( activationEvent.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED ) {
					((Text)getControl()).setText(((char)activationEvent.keyCode)+"");	
				}
			}
		};
		
		ColumnViewerEditorActivationStrategy strat = new ColumnViewerEditorActivationStrategy(v) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				if( event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION ) {
					return true;
				} else if( event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED ) {
					KeyEvent evt = (KeyEvent) event.sourceEvent;
					if( evt.character >= 'A' && evt.character <= 'z' ) {
						return true;	
					} else if( evt.keyCode == SWT.CR ) {
						return true;
					}
				} else if( event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL ) {
					return true;
				}
				
				return false;
			}
		};
		
		GridViewerEditor.create(v, strat, 
				GridViewerEditor.TABBING_HORIZONTAL
				|GridViewerEditor.TABBING_VERTICAL
				|GridViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				|GridViewerEditor.KEYBOARD_ACTIVATION);
		
		{
			GridViewerColumn c = new GridViewerColumn(v,SWT.NONE);
			c.getColumn().setText("Locator");
			c.getColumn().setWidth(200);
			c.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return ((Person)element).firstname;
				}
			});
			c.setEditingSupport(new EditingSupport(v) {
				
				@Override
				protected void setValue(Object element, Object value) {
					((Person)element).firstname = (String) value;
					v.update(element, null);
				}
				
				@Override
				protected Object getValue(Object element) {
					return ((Person)element).firstname;
				}
				
				@Override
				protected CellEditor getCellEditor(Object element) {
					return textEditor;
				}
				
				@Override
				protected boolean canEdit(Object element) {
					return true;
				}
			});
		}
		
		{
			GridViewerColumn c = new GridViewerColumn(v,SWT.NONE);
			c.getColumn().setText("UI Element ID");
			c.getColumn().setWidth(200);
			c.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return ((Person)element).lastname;
				}
			});
			c.setEditingSupport(new EditingSupport(v) {
				
				@Override
				protected void setValue(Object element, Object value) {
					((Person)element).lastname = (String) value;
					v.update(element, null);
				}
				
				@Override
				protected Object getValue(Object element) {
					return ((Person)element).lastname;
				}
				
				@Override
				protected CellEditor getCellEditor(Object element) {
					return textEditor;
				}
				
				@Override
				protected boolean canEdit(Object element) {
					return true;
				}
			});
		}
		
		v.setContentProvider(ArrayContentProvider.getInstance());
		v.setInput(createInput());
	}
	
	private List<Person> createInput() {
		return Arrays.asList(new Person("Test-Explorer Baum", "TE_MY_TE_TREE"), new Person("Ausführen Knopf","TE_RUN_PFEIL"));
	}

	
	
	
}