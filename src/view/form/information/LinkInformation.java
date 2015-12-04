package view.form.information;

import network.Link;
import view.Realization;
import view.form.information.table.PacketsInLinkTable;

import javax.swing.*;
import java.awt.event.*;

public class LinkInformation extends InformationFrame {
    private JPanel contentPane;
    private JButton buttonClose;
    private JCheckBox activeCheckBox;
    private JTextField textFieldWeight;
    private JLabel node1Label;
    private JLabel node2Label;
    private JLabel connectionTypeLabel;
    private JLabel linkTypeLabel;
    private JTabbedPane tabbedPane;
    private JTable packetsTable;

    private Realization frame;
    private Link link;

    public LinkInformation(Realization frame, Link link) {
        this.frame = frame;
        this.link = link;
        setContentPane(contentPane);


        setTitle("Канал " + Integer.toString(link.getNode1().getId()) + " <> " +
                Integer.toString(link.getNode2().getId()));
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
                    link.activate();
                } else {
                    link.deActivate();
                }
                initialize();
                frame.redrawAll();
                frame.selectLink(link);
            }
        });
    }

    private void onClose() {
// add your code here if necessary
        dispose();
    }

    @Override
    public void loadForm() {
        initialize();
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    @Override
    public void update() {
        packetsTable.repaint();
    }

    private void initialize() {
        activeCheckBox.setSelected(link.isActive());
        textFieldWeight.setText(Integer.toString(link.getWeight()));
        node1Label.setText(Integer.toString(link.getNode1().getId()));
        node2Label.setText(Integer.toString(link.getNode2().getId()));
        connectionTypeLabel.setText(link.getConnectionType().toString());
        linkTypeLabel.setText(link.getLinkType().toString());

        packetsTable.setModel(new PacketsInLinkTable(link));
        PacketsInLinkTable.setColumnsWidth(packetsTable);
        packetsTable.setFillsViewportHeight(true);
    }
}
