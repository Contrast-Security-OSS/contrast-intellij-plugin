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

import com.contrastsecurity.config.ContrastUtil;
import com.contrastsecurity.core.Constants;
import com.contrastsecurity.models.Trace;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Date;

public class ContrastTableModel extends AbstractTableModel {
    private String[] columnNames = {"Severity", "Vulnerability", "View Details", "Open in Teamserver", "Last Detected", "Status"};
    private Trace[] data = new Trace[0];

    private final ImageIcon severityIconCritical = new ImageIcon(getClass().getResource("/contrastToolWindow/critical.png"));
    private final ImageIcon severityIconHigh = new ImageIcon(getClass().getResource("/contrastToolWindow/high.png"));
    private final ImageIcon severityIconMedium = new ImageIcon(getClass().getResource("/contrastToolWindow/medium.png"));
    private final ImageIcon severityIconLow = new ImageIcon(getClass().getResource("/contrastToolWindow/low.png"));
    private final ImageIcon severityIconNote = new ImageIcon(getClass().getResource("/contrastToolWindow/note.png"));
    private final ImageIcon externalLinkIcon = new ImageIcon(getClass().getResource("/contrastToolWindow/externalLink.png"));
    private final ImageIcon detailsIcon = new ImageIcon(getClass().getResource("/contrastToolWindow/details.png"));
    private final ImageIcon unlicensedIcon = new ImageIcon(getClass().getResource("/contrastToolWindow/unlicensed.png"));
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
                        obj = severityIconNote;
                    } else if (severity.equals(Constants.SEVERITY_LEVEL_LOW)) {
                        obj = severityIconLow;
                    } else if (severity.equals(Constants.SEVERITY_LEVEL_MEDIUM)) {
                        obj = severityIconMedium;
                    } else if (severity.equals(Constants.SEVERITY_LEVEL_HIGH)) {
                        obj = severityIconHigh;
                    } else if (severity.equals(Constants.SEVERITY_LEVEL_CRITICAL)) {
                        obj = severityIconCritical;
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
                    if (contrastUtil.isTraceLicensed(trace)){
                        obj = detailsIcon;
                    } else {
                        obj = unlicensedIcon;
                    }
                    break;
                case 3:
                    obj = externalLinkIcon;
                    break;
                case 4:
                    obj = new Date(trace.getLastTimeSeen());
                    break;
                case 5:
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
        if (getValueAt(0, c)!=null){
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

    public Trace getTraceAtRow(int row){
        return data[row];
    }


}
