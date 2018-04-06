package com.contrastsecurity.ui.com.contrastsecurity.ui.toolwindow;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class ContrastTableRowSorter extends TableRowSorter<TableModel> {

    private String columnToSort;

    ContrastTableRowSorter(AbstractTableModel model) {
        super(model);
    }

    @Override
    public void sort() {
    }

    @Override
    protected void fireSortOrderChanged() {
        final SortKey k = getSortKeys().get(0);

        final String colName = getModel().getColumnName(k.getColumn());

        if (colName != null && !colName.isEmpty()) {
            columnToSort = (k.getSortOrder() == SortOrder.ASCENDING ? '+' : '-') + colName;
        } else {
            columnToSort = null;
        }

        super.fireSortOrderChanged();
    }

    public String getColumnToSort() {
        return columnToSort;
    }

}
