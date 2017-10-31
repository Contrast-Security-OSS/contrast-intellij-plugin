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

import com.contrastsecurity.models.Application;

public class ApplicationComboBoxItem {
    private Application application;
    private String allApplicationsName;

    public ApplicationComboBoxItem(Application application) {
        this.application = application;
    }

    public ApplicationComboBoxItem(String allApplicationsName) {
        this.allApplicationsName = allApplicationsName;
    }

    @Override
    public String toString() {
        if (application == null) {
            return allApplicationsName;
        } else {
            return application.getName();
        }
    }
}
