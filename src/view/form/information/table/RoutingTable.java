package view.form.information.table;

import network.Node;
import network.algorithm.Path;
import network.model.packet.table.TopologyBase;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.util.*;

/**
 * Created on 23:30 05.12.2015
 *
 * @author Bersik
 */

public class RoutingTable extends AbstractTableModel {
    private final String[] columnNames = {"Куди","Відстань","Шлях"};

    private HashMap<Node,Path> routingTable;

    ArrayList<Node> nodesTo;

    public RoutingTable(HashMap<Node,Path> routingTable) {
        this.routingTable = routingTable;

        nodesTo = new ArrayList<>(routingTable.keySet());
        Collections.sort(nodesTo);
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
        return nodesTo.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
//        {"Куди","Відстань","Шлях"}

        Node nodeTo = nodesTo.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return Integer.toString(nodeTo.getId());
            case 1:
                return routingTable.get(nodeTo).weight;
            case 2:
                return routingTable.get(nodeTo).pathToString();
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
        final int widths[] = {100,100,table.getWidth()-200};
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

}
