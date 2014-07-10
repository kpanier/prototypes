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

public class LibGroup {

	private String name;
	private List<Object>  elemnts = new ArrayList<Object>();

	public LibGroup(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public void addChild(Object str){
		elemnts.add(str);
	}

	public List<Object> getElemnts() {
		return elemnts;
	}
}
