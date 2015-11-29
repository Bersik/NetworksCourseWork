package view.form.information;

import network.Node;
import view.Realization;

import javax.swing.*;
import java.awt.event.*;
import view.form.information.table.*;

public class NodeInformation extends InformationFrame {
    private JPanel contentPane;
    private JButton buttonClose;
    private JProgressBar outProgressBar;
    private JProgressBar inProgressBar;
    private JFormattedTextField bufferSizeFormattedTextField;
    private JTable LinksTable;
    private JTable CommunicationTable;
    private JCheckBox activeCheckBox;
    private Realization frame;
    private JScrollPane scrollPane;

    private Node node;

    public NodeInformation(Realization frame, Node node) {
        this.frame = frame;
        this.node = node;
        setContentPane(contentPane);
        setTitle("Вузол №" + Integer.toString(node.getId()));
        getRootPane().setDefaultButton(buttonClose);


        buttonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        });


// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        activeCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (activeCheckBox.isSelected()) {
                    node.activate();
                } else {
                    node.deActivate();
                }
                initializeTable();
                frame.redrawAll();
                frame.selectNode(node);
            }
        });
    }

    private void onClose() {
// add your code here
        dispose();
    }


    private void initializeTable(){
        activeCheckBox.setSelected(node.isActive());
        bufferSizeFormattedTextField.setText(Integer.toString(node.getBufferLength()));

        LinksTable.setModel(new LinksTableModel(node.getLinks()));
        LinksTableModel.setColumnsWidth(LinksTable);

        LinksTable.setFillsViewportHeight(true);
    }

    @Override
    public void loadForm() {
        pack();
        setLocationRelativeTo(frame);
        initializeTable();
        setVisible(true);
    }

    private void createUIComponents() {

    }

/*

    public static void main(String[] args) {
        NodeInformation dialog = new NodeInformation();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }*/
}
