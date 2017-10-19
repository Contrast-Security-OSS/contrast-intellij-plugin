package com.contrastsecurity.config;

import com.contrastsecurity.core.Constants;
import com.contrastsecurity.core.Util;
import com.contrastsecurity.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class ContrastUtil {

    private final ContrastPersistentStateComponent contrastPersistentStateComponent;
    private String teamServerUrl;
    private String selectedOrganizationName;
    private String serviceKey;
    private String username;
    private Map<String, String> organizations;

    private Util util = new Util();

    public ContrastUtil() {
        contrastPersistentStateComponent = ContrastPersistentStateComponent.getInstance();

        teamServerUrl = contrastPersistentStateComponent.getTeamServerUrl();
        selectedOrganizationName = contrastPersistentStateComponent.getSelectedOrganizationName();
        serviceKey = contrastPersistentStateComponent.getServiceKey();
        username = contrastPersistentStateComponent.getUsername();
        organizations = contrastPersistentStateComponent.getOrganizations();
    }

    public ExtendedContrastSDK getContrastSDK() {

        if (StringUtils.isBlank(teamServerUrl) || StringUtils.isBlank(selectedOrganizationName) || StringUtils.isBlank(serviceKey) || StringUtils.isBlank(username) || organizations.isEmpty() || organizations.get(selectedOrganizationName) == null){
            return null;
        }

        OrganizationConfig organizationConfig = util.getOrganizationConfigFromString(organizations.get(selectedOrganizationName), Constants.DELIMITER);
        String apiKey = organizationConfig.getApiKey();

        ExtendedContrastSDK sdk = new ExtendedContrastSDK(username, serviceKey, apiKey, teamServerUrl);
        sdk.setReadTimeout(5000);

        return sdk;
    }

    public OrganizationConfig getSelectedOrganizationConfig(){
        if (StringUtils.isBlank(selectedOrganizationName) || organizations.get(selectedOrganizationName) == null){
            return null;
        }
        OrganizationConfig organizationConfig = util.getOrganizationConfigFromString(organizations.get(selectedOrganizationName), Constants.DELIMITER);
        return organizationConfig;
    }

}
