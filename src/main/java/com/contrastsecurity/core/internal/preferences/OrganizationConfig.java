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
package com.contrastsecurity.core.internal.preferences;

public class OrganizationConfig {

    private String teamServerUrl;
    private String username;
    private String serviceKey;
    private String apiKey;
    private String uuid;
    private String authHeader;

    public OrganizationConfig(String teamServerUrl, String username, String serviceKey, String apiKey, String uuid, String authHeader) {
        this.teamServerUrl = teamServerUrl;
        this.username = username;
        this.serviceKey = serviceKey;
        this.apiKey = apiKey;
        this.uuid = uuid;
        this.authHeader = authHeader;
    }

    public String getTeamServerUrl() {
        return teamServerUrl;
    }

    public void setTeamServerUrl(String teamServerUrl) {
        this.teamServerUrl = teamServerUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }
}
