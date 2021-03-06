/*******************************************************************************
 * Copyright (c) 2017 Contrast Security.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License.
 * 
 * The terms of the GNU GPL version 3 which accompanies this distribution
 * and is available at https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * Contributors:
 *     Contrast Security - initial API and implementation
 *******************************************************************************/
package com.contrastsecurity.core.extended;

public class EventItem extends EventModel {

	private String type;
	private String value;
	private boolean isStacktrace;

	public EventItem() {
	}

	public EventItem(EventResource parent, String type, String value, boolean isStacktrace) {
		super();
		this.type = type;
		this.value = value;
		this.isStacktrace = isStacktrace;
		this.parent = parent;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public boolean isStacktrace() {
		return isStacktrace;
	}

	public void setStacktrace(boolean isStacktrace) {
		this.isStacktrace = isStacktrace;
	}
}
