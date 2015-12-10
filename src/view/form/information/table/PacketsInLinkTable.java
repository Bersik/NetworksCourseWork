package view.form.information.table;

import network.Link;
import network.packet.Packet;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * Created on 15:27 03.12.2015
 *
 * @author Bersik
 */

public class PacketsInLinkTable extends AbstractTableModel {

    private final String[] columnNames = {"ID","Звідки","Куди","Тип","Позиція","№ пакету","Всього"};

    private ArrayList<Packet> packets;


    public PacketsInLinkTable(Link link) {
        packets = link.getPackets();
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
        return packets.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Packet packet = packets.get(rowIndex);

        switch (columnIndex) {
            case 0:
                //"ID"
                return Integer.toString(packet.getId());
            case 1:
                //"Звідки"
                return Integer.toString(packet.getBaseFrom().getId());
            case 2:
                //"Куди"
                return Integer.toString(packet.getBaseTo().getId());
            case 3:
                //"Тип"
                return packet.getClass().getSimpleName();
            case 4:
                //"Позиція"
                return Integer.toString(packet.getPosition());
            case 5:
                //"# пакету"
                return Integer.toString(packet.getNumber());
            case 6:
                return Integer.toString(packet.getTotalNumber());
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
        final int widths[] = {55,50,50,120,60,70,60};
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }
}
