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

import com.contrastsecurity.config.ContrastUtil;
import com.contrastsecurity.core.extended.ExtendedContrastSDK;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContrastUtilTest {

    @Test
    public void filterHeadersTest() {
        String authorizationString = "Authorization: Basic Z3Vl...Q6Z3Vlc3Q=";
        String intuitTidString = "intuit_tid: iasjdfjas9023423234lkj24";
        String tokenString = "token : afskjfasdfljljasdfljasdf";

        String goodString1 = "/plugin_extracted/plugin/DBCrossSiteScripting/jsp/EditProfile.jsp";
        String goodString2 = "/plugin_extracted/plugin/DBCrossSiteScripting/jsp/DBCrossSiteScripting.jsp";
        String goodString3 = "/plugin_extracted/plugin/SQLInjection/jsp/ViewProfile.jsp";

        String separator = "\n";
        String data = goodString1 + separator + authorizationString + separator + goodString2 + separator +
                intuitTidString + separator + goodString3 + separator + tokenString;

        String filtered = ContrastUtil.filterHeaders(data, separator);
        assertEquals(goodString1 + separator + goodString2 + separator + goodString3, filtered);

    }
}
