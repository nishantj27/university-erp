package edu.univ.erp.ui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/** Helpers for building the read-only, sortable tables used across the dashboards. */
public final class Tables {

    private Tables() {
    }

    /** A table model whose cells can't be edited directly (all changes go through the services). */
    public static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JTable sortable(TableModel model) {
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);   // click a header to sort
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }
}
