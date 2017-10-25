package com.contrastsecurity.core;

import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilTest {

    private final static String API_KEY = "api key";
    private final static String UUID = "uuid";
    private final static String DELIMITER = Constants.DELIMITER;

    @Test
    public void getStringFromOrganizationConfigTest(){
        OrganizationConfig organizationConfig = new OrganizationConfig(API_KEY, UUID);
        String organizationConfigString = Util.getStringFromOrganizationConfig(organizationConfig, DELIMITER);
        assertEquals(organizationConfigString, API_KEY + DELIMITER + UUID);
    }

    @Test
    public void getOrganizationConfigFromStringTest(){
        String organizationConfigString = API_KEY + DELIMITER + UUID;
        OrganizationConfig organizationConfig = Util.getOrganizationConfigFromString(organizationConfigString, DELIMITER);
        assertEquals(organizationConfig.getApiKey(), API_KEY);
        assertEquals(organizationConfig.getUuid(), UUID);
    }
}
