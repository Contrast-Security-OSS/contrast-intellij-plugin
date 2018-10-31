/*******************************************************************************
 * Copyright (c) 2018 Contrast Security.
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
package com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow;

import javax.swing.table.AbstractTableModel;

public class OrganizationTableModel extends AbstractTableModel {

    private String[] columnNames = {"Organizations"};
    private String[] data = new String[0];

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object obj = null;
        if (getRowCount() > rowIndex) {
            switch (columnIndex) {
                case 0:
                    obj = data[rowIndex];
                    break;
            }

        }
        return obj;
    }

    public Class getColumnClass(int c) {
        if (getValueAt(0, c) != null) {
            return getValueAt(0, c).getClass();
        } else {
            return "".getClass();
        }

    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public void setData(String[] data) {
        this.data = data;
    }

    public String[] getData() {
        return data;
    }
}
