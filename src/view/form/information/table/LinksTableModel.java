package view.form.information.table;

import network.Link;
import network.Node;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.util.List;

/**
 * Created on 0:05 30.11.2015
 *
 * @author Bersik
 */
public class LinksTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Куди","Вага","Тип","Тип","Статус"};

    private Node node;
    private List<Link> links;


    public LinksTableModel(Node node) {
        this.node = node;
        this.links = node.getLinks();

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
        return links.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Link link = links.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return Integer.toString(link.getAnotherNode(node).getId());
            case 1:
                return Integer.toString(link.getWeight());
            case 2:
                return link.getConnectionType().toString();
            case 3:
                return link.getLinkType().toString();
            case 4:
                return (link.isActive())? "Активний":"Відключений";
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
        final int widths[] = {50,50,90,80,table.getWidth()-270};
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

    }

}
