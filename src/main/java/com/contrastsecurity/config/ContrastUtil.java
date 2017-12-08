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
package com.contrastsecurity.config;

import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.Util;
import com.contrastsecurity.core.cache.ContrastCache;
import com.contrastsecurity.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.models.Trace;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class ContrastUtil {

    private final ContrastPersistentStateComponent contrastPersistentStateComponent;
    private String teamServerUrl;
    private String selectedOrganizationName;
    private String serviceKey;
    private String username;
    private Map<String, String> organizations;

    private ContrastCache contrastCache;

    public ContrastUtil() {

        contrastPersistentStateComponent = ContrastPersistentStateComponent.getInstance();
        teamServerUrl = contrastPersistentStateComponent.getTeamServerUrl();
        selectedOrganizationName = contrastPersistentStateComponent.getSelectedOrganizationName();
        serviceKey = contrastPersistentStateComponent.getServiceKey();
        username = contrastPersistentStateComponent.getUsername();
        organizations = contrastPersistentStateComponent.getOrganizations();
        contrastCache = new ContrastCache();
    }

    public ExtendedContrastSDK getContrastSDK() {

        if (StringUtils.isBlank(teamServerUrl) || StringUtils.isBlank(selectedOrganizationName) || StringUtils.isBlank(serviceKey) || StringUtils.isBlank(username) || organizations.isEmpty() || organizations.get(selectedOrganizationName) == null) {
            return null;
        }

        OrganizationConfig organizationConfig = Util.getOrganizationConfigFromString(organizations.get(selectedOrganizationName), Constants.DELIMITER);
        String apiKey = organizationConfig.getApiKey();

        ExtendedContrastSDK sdk = new ExtendedContrastSDK(username, serviceKey, apiKey, teamServerUrl);
//        sdk.setReadTimeout(5000);

        return sdk;
    }

    public ContrastCache getContrastCache() {
        return contrastCache;
    }

    public OrganizationConfig getSelectedOrganizationConfig() {
        if (StringUtils.isBlank(selectedOrganizationName) || organizations.get(selectedOrganizationName) == null) {
            return null;
        }
        OrganizationConfig organizationConfig = Util.getOrganizationConfigFromString(organizations.get(selectedOrganizationName), Constants.DELIMITER);
        return organizationConfig;
    }

    public boolean isTraceLicensed(Trace trace) {
        boolean licensed = true;

        String title = trace.getTitle();
        int indexOfUnlicensed = title.indexOf(Constants.UNLICENSED);
        if (indexOfUnlicensed != -1) {
            licensed = false;
        }
        return licensed;
    }

    public String getTeamServerUrl() {
        return teamServerUrl;
    }
}