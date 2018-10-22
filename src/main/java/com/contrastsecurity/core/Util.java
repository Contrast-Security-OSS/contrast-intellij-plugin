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

import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.http.TraceFilterForm;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static TraceFilterForm getTraceFilterForm(final int offset, final int limit) {
        return getTraceFilterForm(null, offset, limit);
    }

    public static TraceFilterForm getTraceFilterForm(final Long selectedServerId, final int offset, final int limit) {
        final TraceFilterForm form = new TraceFilterForm();
        if (selectedServerId != null) {
            final List<Long> serverIds = new ArrayList<>();
            serverIds.add(selectedServerId);
            form.setServerIds(serverIds);
        }
        form.setOffset(offset);
        form.setLimit(limit);

        return form;
    }

    public static String getStringFromOrganizationConfig(OrganizationConfig organizationConfig, String delimiter) {
        String organization = organizationConfig.getTeamServerUrl() + delimiter +
                organizationConfig.getUsername() + delimiter +
                organizationConfig.getServiceKey() + delimiter +
                organizationConfig.getApiKey() + delimiter + organizationConfig.getUuid();
        return organization;
    }

    public static OrganizationConfig getOrganizationConfigFromString(String organization, String delimiter) {
        OrganizationConfig organizationConfig = null;

        if (StringUtils.isNotBlank(organization)) {
            String[] org = StringUtils.split(organization, delimiter);
            if (org.length == 5) {
                organizationConfig = new OrganizationConfig(org[0], org[1], org[2], org[3], org[4]);
            }
        }
        return organizationConfig;
    }
}
