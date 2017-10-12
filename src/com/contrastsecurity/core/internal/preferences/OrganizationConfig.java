package com.contrastsecurity.core.internal.preferences;

public class OrganizationConfig {
	
	private String apiKey;
	private String organizationUUID;
	
	public OrganizationConfig(final String apiKey, final String serviceKey) {
		this.apiKey = apiKey;
		this.organizationUUID = serviceKey;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getOrganizationUUIDKey() {
		return organizationUUID;
	}

	public void setOrganizationUUIDKey(String organizationUUIDKey) {
		this.organizationUUID = organizationUUIDKey;
	}

}
