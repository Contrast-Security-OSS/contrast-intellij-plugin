package com.contrastsecurity.core.internal.preferences;

public class OrganizationConfig {
	
	private String apiKey;
	private String uuid;
	private String name;

    public OrganizationConfig(String apiKey, String uuid, String name) {
        this.apiKey = apiKey;
        this.uuid = uuid;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
