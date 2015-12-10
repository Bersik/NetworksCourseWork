package view.form.information;

import network.Link;
import network.Node;
import view.Realization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import view.form.information.table.*;

public class NodeInformation extends InformationFrame {
    private JPanel contentPane;
    private JButton buttonClose;
    private JFormattedTextField bufferSizeFormattedTextField;
    private JTable linksTable;
    private JTable communicationTable;
    private JCheckBox activeCheckBox;
    private JPanel buffersBlock;
    private JTabbedPane tabbedPane1;
    private JTable neighborsTable;
    private JTable topologyBaseTable;
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
                initialize();
                frame.redrawAll();
                frame.selectNode(node);
            }
        });
    }

    private void onClose() {
        frame.closeInformationWindow();
        dispose();
    }


    private void initialize(){
        activeCheckBox.setSelected(node.isActive());
        bufferSizeFormattedTextField.setText(Integer.toString(node.getBufferLength()));

        update();
    }

    @Override
    public void loadForm() {
        pack();
        setLocationRelativeTo(frame);
        initialize();
        setVisible(true);
    }

    @Override
    public void update() {
        loadBuffers();

        linksTable.setModel(new LinksTableModel(node));
        LinksTableModel.setColumnsWidth(linksTable);
        neighborsTable.setModel(new NeighborTable(node.getNeighbors()));
        NeighborTable.setColumnsWidth(neighborsTable);
        topologyBaseTable.setModel(new TopologyBaseTable(node));
        TopologyBaseTable.setColumnsWidth(topologyBaseTable);
        communicationTable.setModel(new RoutingTable(node.getRoutingTable()));
        RoutingTable.setColumnsWidth(communicationTable);
    }

    public void loadBuffers(){
        buffersBlock.removeAll();
        buffersBlock.setLayout(new GridLayout(node.getLinks().size(),2));
        int bufferSizes = node.getBufferLength();
        for(Link link:node.getLinks()){
            double percent = (double)node.getBufferSize(link) / (double)bufferSizes;
            if (percent < 0)
                percent = 0;
            else if (percent > 1)
                percent = 1;
            JProgressBar progressBar = new JProgressBar();
            progressBar.setMinimum(0);
            progressBar.setMaximum(100);
            progressBar.setValue((int)(percent*100));

            buffersBlock.add(new JLabel("До " + Integer.toString(link.getAnotherNode(node).getId())));
            buffersBlock.add(progressBar);
            pack();
        }
    }

    private void createUIComponents() {

    }



}
