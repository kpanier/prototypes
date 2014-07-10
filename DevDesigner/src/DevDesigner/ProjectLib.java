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

public class ProjectLib {

	private String name;

	private LibGroup actiongroup = new LibGroup("ActionGroups (Masken)");
	private LibGroup widgets = new LibGroup("UI Widget Types");
	private LibGroup tecBin = new LibGroup("Technical Bindings");

	private LibGroup elementList = new LibGroup("Element Liste");;
	
	public ProjectLib(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public List<LibGroup> getLibGroups() {
		ArrayList<LibGroup> list = new ArrayList<LibGroup>();
		list.add(actiongroup);
		list.add(widgets);
		list.add(tecBin);
		list.add(elementList);
		return list;
	}

	public void addActionGroup(String string) {
		actiongroup.addChild(string);		
	}

	public void addWidgetType(String string) {
		LibGroup widget = new LibGroup(string);
		widgets.addChild(widget);
		widget.addChild("Klicke");
		widget.addChild("wähle aus");
		widget.addChild("gib ein");
	}

	public void addTechnicalBinding(String string) {
		tecBin.addChild(string);		
	}
	
}
