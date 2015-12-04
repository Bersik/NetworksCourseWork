package view.form.information.table;

import network.Link;
import network.Node;
import network.model.packet.table.TopologyBase;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.util.*;

/**
 * Created on 23:13 03.12.2015
 *
 * @author Bersik
 */

class TableEntry {
    public int from;
    public int to;
    public int weight;

    public TableEntry(int from, int to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }
}

public class TopologyBaseTable extends AbstractTableModel {

    private TopologyBase topologyBase;

    private ArrayList<Integer> rowNames;
    private ArrayList<Integer> columnNames;

    private int[][] table;

    public TopologyBaseTable(Node node) {
        topologyBase = node.getTopologyBase();

        //назва рядків
        rowNames = new ArrayList<>(topologyBase.size());

        //множина назв стовпчиків
        Set<Integer> columnNames$ = new HashSet<>();

        for(Node nodeFrom:topologyBase.keySet()){
            rowNames.add(nodeFrom.getId());
            HashMap<Node,Integer> map = topologyBase.get(nodeFrom);
            if (map != null)
                for(Node nodeTo:map.keySet()){
                    columnNames$.add(nodeTo.getId());
                }
        }
        columnNames = new ArrayList<>(columnNames$);


        Collections.sort(rowNames);
        Collections.sort(columnNames);


        table = new int[rowNames.size()][columnNames.size()];
        for(Node nodeFrom:topologyBase.keySet()){
            HashMap<Node,Integer> map = topologyBase.get(nodeFrom);
            //Можна видалити, якщо в середині null, але десь в глобальнішому місці
            if (map != null)
                for(Node nodeTo:map.keySet()){
                    table[rowNames.indexOf(nodeFrom.getId())][columnNames.indexOf(nodeTo.getId())] = map.get(nodeTo);
                }
        }

    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex > 0 && columnIndex < columnNames.size())
            return "To " + Integer.toString(columnNames.get(columnIndex-1));
        return "";
    }

    @Override
    public int getRowCount() {
        return rowNames.size() + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex == 0)
            return "";
        if (columnIndex == 0){
            return "From " + Integer.toString(rowNames.get(rowIndex-1));
        }else if (columnIndex < rowNames.size()){
            return Integer.toString(table[rowIndex-1][columnIndex-1]);
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
        final int widths = 50;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths);

    }

}
