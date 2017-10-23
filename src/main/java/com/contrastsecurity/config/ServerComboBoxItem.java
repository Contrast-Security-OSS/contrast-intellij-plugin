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

import com.contrastsecurity.models.Server;

public class ServerComboBoxItem {
    private Server server;
    private String allServersName;

    public ServerComboBoxItem(Server server) {
        this.server = server;
    }

    public ServerComboBoxItem(String name) {
        this.allServersName = name;
    }

    @Override
    public String toString() {
        if (server == null) {
            return allServersName;
        } else {
            return server.getName();
        }
    }

    public Server getServer() {
        return server;
    }
}
