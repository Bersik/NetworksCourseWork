package view.form.information.table;

import network.Node;
import network.TopologyBase;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * Created on 23:13 03.12.2015
 *
 * @author Bersik
 */

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
        for(int i=0;i<table.length;i++)
            for(int j=0;j<table.length;j++)
                table[i][j] = -1;
        for(Node nodeFrom:topologyBase.keySet()){
            HashMap<Node,Integer> map = topologyBase.get(nodeFrom);
            //TODO Можна видалити, якщо в середині null, але десь в глобальнішому місці
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
        return columnNames.size() + 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex > 0 && columnIndex <= columnNames.size())
            return "В " + Integer.toString(columnNames.get(columnIndex-1));
        return "";
    }

    @Override
    public int getRowCount() {
        return rowNames.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0){
            return "З " + Integer.toString(rowNames.get(rowIndex));
        }else if (columnIndex <= columnNames.size()){
            int val = table[rowIndex][columnIndex-1];
            if (rowIndex == columnIndex-1)
                return "0";
            if (val == -1)
                return "-";
            return Integer.toString(val);
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
        final int widths = 35;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths);

    }

}
