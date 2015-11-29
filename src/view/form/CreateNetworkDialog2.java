package view.form;

import network.GeneratorNetwork;
import network.Link;
import view.Realization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.NumericShaper;
import java.text.NumberFormat;

import static settings.Settings.*;

public class CreateNetworkDialog2 extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JFormattedTextField countCommutationNodes;
    private JFormattedTextField countSatelliteLinks;
    private JFormattedTextField degreeNetwork;
    private JRadioButton listRadioButton;
    private JRadioButton rangeRadioButton;
    private JTextField rangeTextField;
    private JTextField fromTextField;
    private JTextField toTextField;
    private JPanel listPanel;
    private JPanel rangePanel;
    private Realization frame;
    private GeneratorNetwork generatorNetwork;

    public CreateNetworkDialog2(Realization frame) {
        this.frame = frame;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        setTitle("Створення мережі");
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        listRadioButton.addActionListener(new ChangeActionListener());
        rangeRadioButton.addActionListener(new ChangeActionListener());

        buttonOK.requestFocus();
    }

    private void onOK() {
        int countCommutationNodesValue = Integer.parseInt(countCommutationNodes.getText());
        int countSatelliteLinksValue = Integer.parseInt(countSatelliteLinks.getText());
        int degreeNetworkValue = Integer.parseInt(degreeNetwork.getText());

        Dimension dimension = frame.getImageSize();
        int[] weights;

        //якщо список значень
        if (listRadioButton.isSelected()){
            String[] strWeights = rangeTextField.getText().trim().split(" ");
            weights = new int[strWeights.length];
            for(int i=0;i<strWeights.length;i++)
                weights[i] = Integer.parseInt(strWeights[i]);
        }else{
            int from = Integer.parseInt(fromTextField.getText());
            int to = Integer.parseInt(toTextField.getText());

            weights = new int[to-from+1];
            for(int i=0;i<to-from+1;i++)
                weights[i] = from+i;
        }

        generatorNetwork = new GeneratorNetwork(
                dimension, countCommutationNodesValue, countSatelliteLinksValue, degreeNetworkValue,weights);

        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public void launch() {
        StringBuilder sb = new StringBuilder();
        for (int weight: Link.WEIGHTS)
            sb.append(weight).append(" ");

        rangeTextField.setText(sb.toString());
        fromTextField.setText(Integer.toString(MIN_VALUE_WEIGHT_OF_RANGE));
        toTextField.setText(Integer.toString(MAX_VALUE_WEIGHT_OF_RANGE));


        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    private void createUIComponents() {
        NumberFormat amountFormat = NumberFormat.getNumberInstance();

        countCommutationNodes = new JFormattedTextField(amountFormat);
        countCommutationNodes.setText(Integer.toString(COUNT_COMMUTATION_NODES));

        countSatelliteLinks = new JFormattedTextField(amountFormat);
        countSatelliteLinks.setText(Integer.toString(COUNT_SATELLITE_LINKS));

        degreeNetwork = new JFormattedTextField(amountFormat);
        degreeNetwork.setText(Integer.toString(DEGREE_NETWORKS));

    }

    public GeneratorNetwork getGeneratorNetwork() {
        return generatorNetwork;
    }

    private class ChangeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (rangeRadioButton.isSelected()){
                rangePanel.setVisible(true);
                listPanel.setVisible(false);
            }else {
                rangePanel.setVisible(false);
                listPanel.setVisible(true);
            }
            pack();
        }
    }
}
