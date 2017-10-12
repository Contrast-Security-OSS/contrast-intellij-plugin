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
package com.contrastsecurity.core;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.models.Organizations;
import com.contrastsecurity.sdk.ContrastSDK;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Util {
	
	private final static String LIST_DELIMITATOR = ";";

	public static Organization getDefaultOrganization(ContrastSDK sdk) throws IOException, UnauthorizedException {
		if (sdk == null) {
			return null;
		}
		Organizations organizations = sdk.getProfileDefaultOrganizations();
		return organizations.getOrganization();
	}
	
	public static String[] extractOrganizationNames(List<Organization> orgList) {
		String[] orgArray = new String[orgList.size()];
		
		for(int i = 0; i < orgList.size(); i++)
			orgArray[i] = orgList.get(i).getName();
		
		return orgArray;
	}
	
	public static TraceFilterForm getTraceFilterForm(final int offset, final int limit, String sort) {
		return getTraceFilterForm(null, offset, limit, sort);
	}
	
	public static TraceFilterForm getTraceFilterForm(final Long selectedServerId, final int offset, final int limit, String sort) {
		final TraceFilterForm form = new TraceFilterForm();
		if(selectedServerId != null) {
			final List<Long> serverIds = new ArrayList<>();
			serverIds.add(selectedServerId);
			form.setServerIds(serverIds);
		}
		
		form.setOffset(offset);
		form.setLimit(limit);
		form.setSort(sort);
		
		return form;
	}
	
	public static String[] getListFromString(String list) {
		String[] orgList;
		
		if(StringUtils.isNotBlank(list))
			orgList = StringUtils.split(list, LIST_DELIMITATOR);
		else
			return new String[0];
		
		return orgList;
	}
	
	public static String getStringFromList(String[] list) {
		StringBuffer buffer = new StringBuffer();
		
		int size = list.length;
		for(int i = 0; i < size; i++) {
			buffer.append(list[i]);
			
			if(i < size - 1)
				buffer.append(LIST_DELIMITATOR);
		}
		
		return buffer.toString();
	}

}
