package view.form.information.table;

import network.Link;

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

    private final String[] columnNames = {"№","Куди","Вага","Тип","Тип","Статус"};

    private List<Link> links;

    private int row = 0;

    public LinksTableModel(List<Link> links) {
        this.links = links;
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
                return Integer.toString(rowIndex);
            case 1:
                return Integer.toString(link.getNode2().getId());
            case 2:
                return Integer.toString(link.getWeight());
            case 3:
                return link.getConnectionType().toString();
            case 4:
                return link.getLinkType().toString();
            case 5:
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
        final int widths[] = {30,40,40,80,80,70};
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableHeader th = table.getTableHeader();
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

    }

}
