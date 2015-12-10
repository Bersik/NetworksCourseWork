package view.form.information.table;

import network.Node;
import network.Network;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 22:55 03.12.2015
 *
 * @author Bersik
 */
public class NeighborTable extends AbstractTableModel {

    private final String[] columnNames = {"Вузол","Пройшло часу з останнього оновлення"};

    private ArrayList<Map.Entry<Node,Long>> nodes;

    public NeighborTable(HashMap<Node, Long> neighbors) {
        nodes = new ArrayList<>(neighbors.entrySet());
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex < columnNames.length)
            return columnNames[columnIndex];
        return "";
    }

    @Override
    public int getRowCount() {
        return nodes.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Map.Entry<Node,Long> entry = nodes.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return Integer.toString(entry.getKey().getId());
            case 1:
                return Long.toString(Network.getTime() - entry.getValue());
        }
        return "";
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {

    }

    public static void setColumnsWidth(JTable table) {
        int widths[] = {100,table.getWidth()-100};
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

    }

}
