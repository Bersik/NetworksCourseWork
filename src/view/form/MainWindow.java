package view.form;

import view.*;

import javax.swing.*;
import java.awt.*;
import java.awt.Image;
import java.awt.event.*;

/**
 * Created on 20:18 26.11.2015
 *
 * @author Bersik
 */
public class MainWindow extends JFrame{
    protected JPanel mainPanel;
    protected JButton clearButton;
    protected JToggleButton nodeButton;
    protected JToggleButton channelButton;
    protected JToggleButton cursorButton;
    protected JToggleButton removeButton;

    protected ButtonGroup communicationMethod = new ButtonGroup();
    protected JRadioButton buttonDuplex;
    protected JRadioButton buttonHalfDuplex;

    protected ButtonGroup communicationType = new ButtonGroup();
    protected JRadioButton satelliteRadioButton;
    protected JRadioButton groundRadioButton;
    protected JPanel communicationParameters;
    protected JCheckBox autoWeightCheckBox;
    protected JComboBox<Integer> comboBoxWeight;
    protected JComboBox<Integer> lengthComboBox;
    protected view.Image image1;
    protected JPanel weightPanel;
    protected JLabel labelWeight;
    protected JButton createNetworkButton;
    protected JCheckBox activeCheckBox;
    protected JTabbedPane tabbedPane1;
    protected JButton saveButton;
    protected JButton openButton;
    protected JCheckBox autoLengthCheckBox;
    protected JPanel nodeParameters;
    protected JLabel labelLength;

    protected void groupRadioButtons(){
        communicationMethod.add(buttonDuplex);
        communicationMethod.add(buttonHalfDuplex);
        buttonDuplex.setSelected(true);

        communicationType.add(satelliteRadioButton);
        communicationType.add(groundRadioButton);
        groundRadioButton.setSelected(true);
        labelWeight.setEnabled(false);
        comboBoxWeight.setEnabled(false);
    }

    public MainWindow(){
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        groupRadioButtons();
    }

    public void launchFrame() {
        pack();
        setVisible(true);
        setExtendedState(MAXIMIZED_BOTH);
    }

    private void createUIComponents() {
        image1 = new view.Image(700,600);
    }

}
