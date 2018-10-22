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
package com.contrastsecurity.core;

import com.contrastsecurity.core.internal.preferences.OrganizationConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilTest {

    private final static String TEAMSERVER_URL = "TeamServer URL";
    private final static String USERNAME = "username";
    private final static String SERVICE_KEY = "service key";
    private final static String API_KEY = "api key";
    private final static String UUID = "uuid";
    private final static String DELIMITER = Constants.DELIMITER;

    @Test
    public void getStringFromOrganizationConfigTest() {
        OrganizationConfig organizationConfig = new OrganizationConfig(TEAMSERVER_URL, USERNAME, SERVICE_KEY, API_KEY, UUID);
        String organizationConfigString = Util.getStringFromOrganizationConfig(organizationConfig, DELIMITER);
        assertEquals(organizationConfigString, API_KEY + DELIMITER + UUID);
    }

    @Test
    public void getOrganizationConfigFromStringTest() {
        String organizationConfigString = TEAMSERVER_URL + DELIMITER + USERNAME + DELIMITER + SERVICE_KEY + DELIMITER + API_KEY + DELIMITER + UUID;
        OrganizationConfig organizationConfig = Util.getOrganizationConfigFromString(organizationConfigString, DELIMITER);
        assertEquals(organizationConfig.getApiKey(), API_KEY);
        assertEquals(organizationConfig.getUuid(), UUID);
    }
}
