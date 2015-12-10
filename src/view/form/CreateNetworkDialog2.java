package view.form;

import network.generator.Generator;
import network.generator.GeneratorDivided;
import network.generator.GeneratorNetwork;
import network.Link;
import network.Node;
import util.ErrorDialog;
import view.Realization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    private JRadioButton listLengthLinkRadioButton;
    private JRadioButton rangeLengthLinkRadioButton;
    private JTextField rangeLengthLinkTextField;
    private JTextField fromLengthTextField;
    private JTextField toLengthTextField;
    private JPanel listLengthPanel;
    private JPanel rangeLengthPanel;
    private JRadioButton togetherRadioButton;
    private JRadioButton separatelyRadioButton;
    private Realization frame;
    private Generator generatorNetwork;

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

        listLengthLinkRadioButton.addActionListener(new ChangeLengthTypeActionListener());
        rangeLengthLinkRadioButton.addActionListener(new ChangeLengthTypeActionListener());

        buttonOK.requestFocus();
    }

    public static Generator showDialog(Realization frame) {
        CreateNetworkDialog2 dialog = new CreateNetworkDialog2(frame);
        dialog.launch();
        return dialog.getGeneratorNetwork();
    }

    private boolean checkList(String text) {
        String regex = "(\\d+[ ]*)+";
        return text.matches(regex);
    }

    private boolean checkAll(int countCommutationNodesValue, int countSatelliteLinksValue, int degreeNetworkValue) {

        if (degreeNetworkValue >= countCommutationNodesValue - 1)
            return false;
        if (degreeNetworkValue * countCommutationNodesValue < countSatelliteLinksValue - 1)
            return false;
        if (listRadioButton.isSelected()) {
            if (!checkList(rangeTextField.getText()))
                return false;
        } else if (Integer.parseInt(fromTextField.getText()) > Integer.parseInt(toTextField.getText()))
            return false;

        if (listLengthLinkRadioButton.isSelected()) {
            if (!checkList(rangeLengthLinkTextField.getText()))
                return false;
        } else if (Integer.parseInt(fromLengthTextField.getText()) > Integer.parseInt(toLengthTextField.getText()))
            return false;


        return true;
    }

    private void showErrorDialog() {
        JOptionPane.showMessageDialog(this,
                "Введені помилкові дані.", "Помилка", JOptionPane.ERROR_MESSAGE);
    }

    private void onOK() {
        int countCommutationNodesValue;
        int countSatelliteLinksValue;
        int degreeNetworkValue;
        int[] weights;
        int[] bufferLengths;
        try {
            countCommutationNodesValue = Integer.parseInt(countCommutationNodes.getText());
            countSatelliteLinksValue = Integer.parseInt(countSatelliteLinks.getText());
            degreeNetworkValue = Integer.parseInt(degreeNetwork.getText());

            if (!checkAll(countCommutationNodesValue, countSatelliteLinksValue, degreeNetworkValue))
                throw new Exception();

            weights = getWeight();

            bufferLengths = getBufferLengths();

        } catch (Exception e) {
            ErrorDialog.showErrorDialog(this,"Введені помилкові дані.");
            return;
        }

        Node.reset();

        Dimension dimension = frame.getImageSize();

        if(togetherRadioButton.isSelected())
            generatorNetwork = new GeneratorNetwork(dimension, countCommutationNodesValue, countSatelliteLinksValue,
                degreeNetworkValue, weights, bufferLengths);
        else
            generatorNetwork = new GeneratorDivided(dimension, countCommutationNodesValue, countSatelliteLinksValue,
                    degreeNetworkValue, weights, bufferLengths);

        dispose();
    }

    /**
     * Визначити допустимі ваги для каналів
     *
     * @return список ваг
     */
    private int[] getWeight() {
        //якщо список значень для ваги каналів
        if (listRadioButton.isSelected()) {
            return getWeightList(rangeTextField.getText());
        }
        //якщо діапазон значень для ваги каналів
        int from = Integer.parseInt(fromTextField.getText());
        int to = Integer.parseInt(toTextField.getText());
        return getValuesOfRange(from, to);
    }

    /**
     * Визначити допустимі значення для довжини буферів
     *
     * @return список довжин
     */
    private int[] getBufferLengths() {
        //якщо список значень для довжини буфурів
        if (listLengthLinkRadioButton.isSelected()) {
            return getWeightList(rangeLengthLinkTextField.getText());
        }
        //якщо список значень для довжини буфурів
        int from = Integer.parseInt(fromLengthTextField.getText());
        int to = Integer.parseInt(toLengthTextField.getText());
        return getValuesOfRange(from, to);
    }

    /**
     * Знаходить список значень в рядку, розділені ' '
     *
     * @param text рядок
     * @return список значень
     */
    private int[] getWeightList(String text) {
        String[] strWeights = text.trim().split(" ");
        int[] weights = new int[strWeights.length];
        for (int i = 0; i < strWeights.length; i++)
            weights[i] = Integer.parseInt(strWeights[i]);
        return weights;
    }

    /**
     * Заходить всі значення в діапазоні
     *
     * @param from початок
     * @param to   кінець
     * @return список значень
     */
    private int[] getValuesOfRange(int from, int to) {
        int[] weights = new int[to - from + 1];
        for (int i = 0; i < to - from + 1; i++)
            weights[i] = from + i;
        return weights;
    }


    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public void launch() {
        StringBuilder sb = new StringBuilder();

        //Ваги каналів по замовчуванню
        for (int weight : Link.WEIGHTS)
            sb.append(weight).append(" ");
        rangeTextField.setText(sb.toString());
        fromTextField.setText(Integer.toString(MIN_VALUE_WEIGHT_OF_RANGE));
        toTextField.setText(Integer.toString(MAX_VALUE_WEIGHT_OF_RANGE));

        sb = new StringBuilder();
        //Довжини буферів по замовчуванню
        for (int weight : Link.BUFFER_LENGTHS)
            sb.append(weight).append(" ");
        rangeLengthLinkTextField.setText(sb.toString());
        fromLengthTextField.setText(Integer.toString(MIN_VALUE_LENGTH_OF_BUFFER));
        toLengthTextField.setText(Integer.toString(MAX_VALUE_LENGTH_OF_BUFFER));

        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    private void createUIComponents() {
        NumberFormat amountFormat = NumberFormat.getNumberInstance();
        amountFormat.setMinimumIntegerDigits(1);
        amountFormat.setMaximumIntegerDigits(5);

        countCommutationNodes = new JFormattedTextField(amountFormat);
        countCommutationNodes.setText(Integer.toString(COUNT_COMMUTATION_NODES));

        countSatelliteLinks = new JFormattedTextField(amountFormat);
        countSatelliteLinks.setText(Integer.toString(COUNT_SATELLITE_LINKS));

        degreeNetwork = new JFormattedTextField(amountFormat);
        degreeNetwork.setText(Integer.toString(DEGREE_NETWORKS));

    }

    public Generator getGeneratorNetwork() {
        return generatorNetwork;
    }

    private class ChangeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (rangeRadioButton.isSelected()) {
                rangePanel.setVisible(true);
                listPanel.setVisible(false);
            } else {
                rangePanel.setVisible(false);
                listPanel.setVisible(true);
            }
            pack();
        }
    }

    private class ChangeLengthTypeActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (rangeLengthLinkRadioButton.isSelected()) {
                rangeLengthPanel.setVisible(true);
                listLengthPanel.setVisible(false);
            } else {
                rangeLengthPanel.setVisible(false);
                listLengthPanel.setVisible(true);
            }
            pack();
        }
    }
}
