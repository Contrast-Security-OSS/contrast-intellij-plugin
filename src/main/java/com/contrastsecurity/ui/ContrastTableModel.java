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
package com.contrastsecurity.ui;

import com.contrastsecurity.core.Constants;
import com.contrastsecurity.models.Trace;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class ContrastTableModel extends AbstractTableModel {
    private String[] columnNames = {"Severity", "Vulnerability", "Actions", "Open in Teamserver"};
    private Trace[] data = new Trace[0];

    private final ImageIcon severityIconCritical = new ImageIcon(getClass().getResource("/contrastToolWindow/critical.png"));
    private final ImageIcon severityIconHigh = new ImageIcon(getClass().getResource("/contrastToolWindow/high.png"));
    private final ImageIcon severityIconMedium = new ImageIcon(getClass().getResource("/contrastToolWindow/medium.png"));
    private final ImageIcon severityIconLow = new ImageIcon(getClass().getResource("/contrastToolWindow/low.png"));
    private final ImageIcon severityIconNote = new ImageIcon(getClass().getResource("/contrastToolWindow/note.png"));
    private final ImageIcon externalLinkIcon = new ImageIcon(getClass().getResource("/contrastToolWindow/externalLink.png"));


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
        Trace trace = data[rowIndex];
        Object obj;
        switch(columnIndex){
            case 0:
                String severity = trace.getSeverity();
                if (severity.equals("Note")) {
                    obj = severityIconNote;
                } else if (severity.equals("Low")) {
                    obj = severityIconLow;
                } else if (severity.equals("Medium")) {
                    obj = severityIconMedium;
                } else if (severity.equals("High")) {
                    obj = severityIconHigh;
                } else if (severity.equals("Critical")) {
                    obj = severityIconCritical;
                } else {
                    obj = "";
                }
                break;
            case 1:
                String title = trace.getTitle();
                int indexOfUnlicensed = title.indexOf(Constants.UNLICENSED);
                if (indexOfUnlicensed != -1){
                    title = "UNLICENSED - " + title.substring(0, indexOfUnlicensed);
                }
                obj = title;
                break;
            case 2:
                obj = "View Details";
                break;
            case 3:
                obj = externalLinkIcon;
                break;
            default:
                obj = null;
                break;
        }
        return obj;
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public void setData(Trace[] data) {
        this.data = data;
    }


}
