package com.contrastsecurity.core.internal.preferences;

public class OrganizationConfig {
	
	private String apiKey;
	private String uuid;

    public OrganizationConfig(String apiKey, String uuid) {
        this.apiKey = apiKey;
        this.uuid = uuid;
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
}
