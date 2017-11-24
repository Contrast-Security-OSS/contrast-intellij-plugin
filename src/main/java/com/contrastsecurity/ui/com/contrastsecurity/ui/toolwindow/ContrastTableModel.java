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
package com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow;

import com.contrastsecurity.config.ContrastUtil;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.models.Trace;
import icons.ContrastPluginIcons;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Date;

public class ContrastTableModel extends AbstractTableModel {
    private String[] columnNames = {"Severity", "Vulnerability", "Application", "View Details", "Open in Teamserver", "Last Detected", "Status"};
    private Trace[] data = new Trace[0];
    private ContrastUtil contrastUtil = new ContrastUtil();

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
        Trace trace;
        Object obj = null;
        if (getRowCount() > rowIndex) {
            trace = data[rowIndex];
            switch (columnIndex) {
                case 0:
                    String severity = trace.getSeverity();
                    if (severity.equals(Constants.SEVERITY_LEVEL_NOTE)) {
                        obj = ContrastPluginIcons.SEVERITY_ICON_NOTE;
                    } else if (severity.equals(Constants.SEVERITY_LEVEL_LOW)) {
                        obj = ContrastPluginIcons.SEVERITY_ICON_LOW;
                    } else if (severity.equals(Constants.SEVERITY_LEVEL_MEDIUM)) {
                        obj = ContrastPluginIcons.SEVERITY_ICON_MEDIUM;
                    } else if (severity.equals(Constants.SEVERITY_LEVEL_HIGH)) {
                        obj = ContrastPluginIcons.SEVERITY_ICON_HIGH;
                    } else if (severity.equals(Constants.SEVERITY_LEVEL_CRITICAL)) {
                        obj = ContrastPluginIcons.SEVERITY_ICON_CRITICAL;
                    } else {
                        obj = "";
                    }
                    break;
                case 1:
                    String title = trace.getTitle();
                    int indexOfUnlicensed = title.indexOf(Constants.UNLICENSED);
                    if (indexOfUnlicensed != -1) {
                        title = "UNLICENSED - " + title.substring(0, indexOfUnlicensed);
                    }
                    obj = title;
                    break;
                case 2:
                    obj = trace.getApplication().getName();
                    break;
                case 3:
                    if (contrastUtil.isTraceLicensed(trace)) {
                        obj = ContrastPluginIcons.DETAILS_ICON;
                    } else {
                        obj = ContrastPluginIcons.UNLICENSED_ICON;
                    }
                    break;
                case 4:
                    obj = ContrastPluginIcons.EXTERNAL_LINK_ICON;
                    break;
                case 5:
                    obj = new Date(trace.getLastTimeSeen());
                    break;
                case 6:
                    obj = trace.getStatus();
                    break;
                default:
                    obj = null;
                    break;
            }
        }
        return obj;
    }

    public Class getColumnClass(int c) {
        if (getValueAt(0, c) != null) {
            if (getValueAt(0, c) instanceof Icon) {
                return ImageIcon.class;
            }
            return getValueAt(0, c).getClass();
        } else {
            return "".getClass();
        }

    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public void setData(Trace[] data) {
        this.data = data;
    }

    public Trace getTraceAtRow(int row) {
        return data[row];
    }


}
