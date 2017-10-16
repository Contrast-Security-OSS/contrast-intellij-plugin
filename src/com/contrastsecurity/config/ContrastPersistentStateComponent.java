package com.contrastsecurity.config;

import com.contrastsecurity.core.Constants;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@State(
        name="ContrastPersistentStateComponent",
        storages = {
                @Storage("$APP_CONFIG$/contrast.xml")}
)
public class ContrastPersistentStateComponent implements PersistentStateComponent<ContrastPersistentStateComponent> {

    public String teamServerUrl = Constants.TEAM_SERVER_URL_VALUE;
    public String username = "";
    public String serviceKey = "";
    public String selectedOrganizationName = "";
//    Key = organization name, Value = Organization (API key, UUID) represented as a String
    public Map<String, String> organizations;

    @Nullable
    @Override
    public ContrastPersistentStateComponent getState() {
        return this;
    }

    @Override
    public void loadState(ContrastPersistentStateComponent contrastPersistentStateComponent) {
        XmlSerializerUtil.copyBean(contrastPersistentStateComponent, this);
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

    public Map<String, String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Map<String, String> organizations) {
        this.organizations = organizations;
    }

    public String getSelectedOrganizationName() {
        return selectedOrganizationName;
    }

    public void setSelectedOrganizationName(String selectedOrganizationName) {
        this.selectedOrganizationName = selectedOrganizationName;
    }

    @Nullable
    public static ContrastPersistentStateComponent getInstance() {
        return ServiceManager.getService(ContrastPersistentStateComponent.class);
    }
}
