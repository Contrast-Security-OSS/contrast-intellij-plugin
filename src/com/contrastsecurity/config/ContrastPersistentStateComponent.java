package com.contrastsecurity.config;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@State(
        name="ContrastPersistentStateComponent",
        storages = {
                @Storage(StoragePathMacros.WORKSPACE_FILE)}
)
public class ContrastPersistentStateComponent implements PersistentStateComponent<ContrastPersistentStateComponent> {

    public String teamServerUrl;
    public String username;
    public String serviceKey;
    public String selectedOrganizationUuid;
//    Key = organization UUID, Value = Organization (API key, UUID, organization name) represented as a String
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

    @Nullable
    public static ContrastPersistentStateComponent getInstance() {
        return ServiceManager.getService(ContrastPersistentStateComponent.class);
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

    public String getSelectedOrganizationUuid() {
        return selectedOrganizationUuid;
    }

    public void setSelectedOrganizationUuid(String selectedOrganizationUuid) {
        this.selectedOrganizationUuid = selectedOrganizationUuid;
    }

    public Map<String, String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Map<String, String> organizations) {
        this.organizations = organizations;
    }
}
