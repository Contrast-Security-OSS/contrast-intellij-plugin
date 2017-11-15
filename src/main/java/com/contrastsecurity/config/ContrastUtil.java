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

import javax.swing.*;
import java.util.Map;

public class ContrastUtil {

    private final ContrastPersistentStateComponent contrastPersistentStateComponent;
    private String teamServerUrl;
    private String selectedOrganizationName;
    private String serviceKey;
    private String username;
    private Map<String, String> organizations;

    private final ImageIcon severityIconCritical = new ImageIcon(getClass().getResource("/contrastToolWindow/critical.png"));
    private final ImageIcon severityIconHigh = new ImageIcon(getClass().getResource("/contrastToolWindow/high.png"));
    private final ImageIcon severityIconMedium = new ImageIcon(getClass().getResource("/contrastToolWindow/medium.png"));
    private final ImageIcon severityIconLow = new ImageIcon(getClass().getResource("/contrastToolWindow/low.png"));
    private final ImageIcon severityIconNote = new ImageIcon(getClass().getResource("/contrastToolWindow/note.png"));
    private final ImageIcon externalLinkIcon = new ImageIcon(getClass().getResource("/contrastToolWindow/externalLink.png"));
    private final ImageIcon detailsIcon = new ImageIcon(getClass().getResource("/contrastToolWindow/details.png"));

    private final ImageIcon saveIcon = new ImageIcon(getClass().getResource("/contrastToolWindow/save.png"));
    private final ImageIcon refreshIcon = new ImageIcon(getClass().getResource("/contrastToolWindow/refresh_tab.gif"));
    private final ImageIcon filterIcon = new ImageIcon(getClass().getResource("/contrastToolWindow/filter.png"));

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

        if (StringUtils.isBlank(teamServerUrl) || StringUtils.isBlank(selectedOrganizationName) || StringUtils.isBlank(serviceKey) || StringUtils.isBlank(username) || organizations.isEmpty() || organizations.get(selectedOrganizationName) == null){
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

    public OrganizationConfig getSelectedOrganizationConfig(){
        if (StringUtils.isBlank(selectedOrganizationName) || organizations.get(selectedOrganizationName) == null){
            return null;
        }
        OrganizationConfig organizationConfig = Util.getOrganizationConfigFromString(organizations.get(selectedOrganizationName), Constants.DELIMITER);
        return organizationConfig;
    }

    public boolean isTraceLicensed(Trace trace){
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

    public ImageIcon getSeverityIconCritical() {
        return severityIconCritical;
    }

    public ImageIcon getSeverityIconHigh() {
        return severityIconHigh;
    }

    public ImageIcon getSeverityIconMedium() {
        return severityIconMedium;
    }

    public ImageIcon getSeverityIconLow() {
        return severityIconLow;
    }

    public ImageIcon getSeverityIconNote() {
        return severityIconNote;
    }

    public ImageIcon getExternalLinkIcon() {
        return externalLinkIcon;
    }

    public ImageIcon getDetailsIcon() {
        return detailsIcon;
    }

    public ImageIcon getSaveIcon() {
        return saveIcon;
    }

    public ImageIcon getRefreshIcon() {
        return refreshIcon;
    }

    public ImageIcon getFilterIcon() {
        return filterIcon;
    }
}
