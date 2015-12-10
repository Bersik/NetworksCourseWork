package view;

import files.FileWorks;
import files.NetworkSerialization;
import files.NetworkFileFilter;
import network.*;
import network.algorithm.AlgorithmType;
import network.exception.BufferLengthException;
import network.generator.Generator;
import network.Network;
import network.packet.Packet;
import util.ErrorDialog;
import view.form.CreateNetworkDialog2;
import view.form.MainWindow;
import view.form.information.InformationFrame;
import view.form.information.LinkInformation;
import view.form.information.NodeInformation;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static settings.Settings.*;
import static settings.Settings.DELAY_MAX;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created on 1:29 27.11.2015
 *
 * @author Bersik
 */
public class Realization extends MainWindow {
    //Ексземляр обробника натисення клавіш миші в базовому режимі (Курсор + "Вузол")
    private MouseListener basicMouseListener = new BasicMouseListener();
    //Ексземляр обробника натиснення клавіш миші в режим "Вузол"
    private MouseListener addNodeMouseListener = new NodeMouseListener();
    //Ексземляр обробника натиснення клавіш миші в режим "Канал"
    private LinkMouseListener linkMouseListener = new LinkMouseListener();
    //Ексземляр обробника натиснення клавіш миші в режим "Видалення"
    private RemoveElementListener removeElementListener = new RemoveElementListener();
    //Ексземляр обробника подвійного натиснення кнопки не елементи
    private DoubleClickImageListener doubleClickImageListener = new DoubleClickImageListener();

    //Вибраний вузол
    private Node selectedNode;
    //Вибраний канал
    private Link selectedLink;

    //Вибір файлу
    private JFileChooser fileChooser;

    //стан системи
    private State state;

    //Інформаційне вікно
    private InformationFrame informationFrame;

    //Список вузлів
    private ArrayList<Node> nodes;
    //Список каналів
    private ArrayList<Link> links;

    private Network network;

    public Realization() {
        setTitle("Курсовий проект: Комп'ютерні мережі powered by Кісільчук С. В. КВ-21");
        //Мінімальний розмір форми
        setMinimumSize(new Dimension(1010, 600));

        //Дія при зміні розміру форми
        addComponentListener(new FrameResize());

        //Скривавамо параметри каналів
        communicationParameters.setVisible(false);

        links = new ArrayList<>();
        nodes = new ArrayList<>();
        network = new Network(this, nodes, links, calculateDelay());

        //Обробка натиску кнопки "Курсор"
        cursorButton.addActionListener(new CursorButtonListener());
        cursorButton.setSelected(true);
        //Обробка натиску кнопки "Вузол"
        nodeButton.addActionListener(new NodeButtonListener());
        //Обробка натиску кнопки "Канал"
        channelButton.addActionListener(new ChannelButtonListener());
        //Обробка натиску кнопки "Видалити"
        removeButton.addActionListener(new RemoveButtonListener());
        //Обробка натиску кнопки "Очистити"
        clearButton.addActionListener(new ClearButtonListener());

        //Обробка вибору автоматичної ваги каналу
        autoWeightCheckBox.addActionListener(new AutoWeightCheckBoxListener());
        //Обробка вибору автоматичної довжини вузла
        autoLengthCheckBox.addActionListener(new AutoLengthCheckBoxListener());

        //Обробка вибору ваги каналу
        comboBoxWeight.addActionListener(new SelectWeightActionListener());

        //Обробка подій при зміні типу комунікаційного з'єднання
        buttonDuplex.addActionListener(new LinkMethodListener());
        buttonHalfDuplex.addActionListener(new LinkMethodListener());

        //Обробка подій при зміні типу комунікаційного з'єднання
        satelliteRadioButton.addActionListener(new CommunicationMethodListener());
        groundRadioButton.addActionListener(new CommunicationMethodListener());

        //Створення мережі
        createNetworkButton.addActionListener(new CreateNetworkButtonListener());

        //Активація/деактивація елементу
        activeCheckBox.addActionListener(new ActiveElementCheckBox());

        //Відкриття і збереження мережі
        saveButton.addActionListener(new SaveButtonListener());
        openButton.addActionListener(new OpenButtonListener());

        //Моделювання
        startStopButton.addActionListener(new StartStopActionListener());
        pauseButton.addActionListener(new PauseButtonActionListener());
        stepButton.addActionListener(new StepButtonActionListener());

        //відправка повідомлень
        sendMessageButton.addActionListener(new SendMessageActionListener());

        //Заповнення даних ваг каналів
        for (int w : Link.WEIGHTS)
            comboBoxWeight.addItem(w);

        //Заповнення даних довжин буферу вузла
        for (int w : Link.BUFFER_LENGTHS)
            lengthComboBox.addItem(w);

        //Фокус на кнопці створення мережі
        createNetworkButton.requestFocus();

        //ініціалізація вибору файлу
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new NetworkFileFilter());

        //ініціалізацію управління швидкістю
        sliderSpeed.addChangeListener(new SliderSpeedChangeListener());

        timeLabel.setText("0");

        frequencyTextField.setText(Double.toString(FREQUENCY));

        generatorCheckBox.addActionListener(new GeneratorCheckBoxActionListener());

        enableComponents(messagesPanel,false);
        tabbedPane1.addChangeListener(new ChangeTablePaneListener());

        //По замовчуванню, спочатку просто оброблюємо натиски на різні вузли і канали
        changeState(State.CURSOR);
    }

    /**
     * Знайти вузол в заданій точці
     *
     * @param point    точка
     * @param accuracy точність
     * @return знайдений вузол
     */
    private Node intersectNode(Point point, int accuracy) {
        for (Node n : nodes) {
            if (n.intersect(point, accuracy))
                return n;
        }
        return null;
    }

    /**
     * Перевірка накладання точки на якийсь вузол
     *
     * @param point    точка натиску
     * @param accuracy точність
     * @return якщо накладається, повертає true
     */
    private boolean intersectNodes(Point point, int accuracy) {
        return Node.intersectNodes(nodes, point, accuracy);
    }

    /**
     * Перевірка накладання точки на якийсь канал
     *
     * @param point точка
     * @return знайдений канал
     */
    private Link intersectLink(Point point) {
        for (Link n : links)
            if (n.intersect(point))
                return n;
        return null;
    }

    /**
     * Перевірка накладання точки на якийсь канал
     *
     * @param point точка натиску
     * @return якщо накладається, повертає true
     */
    private boolean intersectLinks(Point point) {
        return Link.intersectLinks(links, point);
    }

    /**
     * Вибрати вузол в точці p
     *
     * @param point точка
     */
    private void selectNode(Point point) {
        for (Node n : nodes) {
            if (n.intersect(point, RADIUS_ACCURACY)) {
                selectNode(n);
            }
        }
    }

    /**
     * Вибрати вузол
     *
     * @param node вузол
     */
    public void selectNode(Node node) {
        //якщо вибрали інший вузол, але вже був вибраний інший вузол
        unselectNodeAndLink();
        selectedNode = node;

        //вибрати довжину буферу
        for (int i = 0; i < Link.BUFFER_LENGTHS.length; i++)
            if (Link.BUFFER_LENGTHS[i] == node.getBufferLength()) {
                lengthComboBox.setSelectedIndex(i);
            }

        image1.drawSelectedNode(node);
        selectElementState();
    }

    private void selectElementState() {
        Boolean value = null;
        boolean edit = true;
        if (selectedNode != null) {
            value = selectedNode.isActive();
        } else if (selectedLink != null) {
            value = selectedLink.isActive();
            //якщо один із вузлів каналу неактивний, то і канал не активний
            if (!selectedLink.getNode1().isActive() || !selectedLink.getNode2().isActive())
                edit = false;
        }
        if (value != null) {
            activeCheckBox.setSelected(value);
            activeCheckBox.setEnabled(edit);
        }
    }

    /**
     * Вибрати канал в точці p
     *
     * @param point точка
     */
    private void selectLink(Point point) {
        for (Link l : links) {
            if (l.intersect(point)) {
                selectLink(l);
            }
        }
    }

    /**
     * Вибрати канал
     *
     * @param link канал
     */
    public void selectLink(Link link) {
        //TODO також вибрати вагу в полі і інші парамети
        unselectNodeAndLink();
        selectedLink = link;
        image1.drawSelectedLink(link);

        if (state == State.CHANNEL) {
            switch (link.getLinkType()) {
                case DUPLEX:
                    buttonDuplex.setSelected(true);
                    break;
                case HALF_DUPLEX:
                    buttonHalfDuplex.setSelected(true);
                    break;
            }
            switch (link.getConnectionType()) {
                case SATELLITE:
                    satelliteRadioButton.setSelected(true);
                    break;
                case GROUND:
                    groundRadioButton.setSelected(true);
                    break;
            }
            for (int i = 0; i < Link.WEIGHTS.length; i++)
                if (Link.WEIGHTS[i] == link.getWeight()) {
                    comboBoxWeight.setSelectedIndex(i);
                }
        }
        selectElementState();
    }

    /**
     * Чи вибраний якийсь елемент
     *
     * @return true, false
     */
    private boolean isSelectedNode() {
        return selectedNode != null;
    }

    private boolean isSelectedLink() {
        return selectedLink != null;
    }

    /**
     * Зняти вибір вузла
     */
    private void unselectNode() {
        if (isSelectedNode()) {
            image1.drawNode(selectedNode);
            selectedNode = null;
            unselectElement();
        }
    }

    private void unselectElement() {
        activeCheckBox.setEnabled(false);
    }


    /**
     * Зняти вибір вузла і каналу
     */
    private void unselectNodeAndLink() {
        if (isSelectedLink() || isSelectedNode()) {
            selectedNode = null;
            selectedLink = null;
            unselectElement();
            redrawAll();
        }
    }

    private void updateNodesCheckBox() {
        nodeFromComboBox.removeAllItems();
        nodeToComboBox.removeAllItems();
        for (Node node : nodes) {
            nodeFromComboBox.addItem(node.getId());
            nodeToComboBox.addItem(node.getId());
        }
    }

    /**
     * Визначає довжину буфера вузла:
     * якщо {@code autoLengthCheckBox} вибраний, поверне рандомне значення із заданих за умовою
     *
     * @return довжина буферу
     */
    private int getLengthOfBuffer() {
        if (autoLengthCheckBox.isSelected()) {
            Random random = new Random();
            return Link.BUFFER_LENGTHS[random.nextInt(Link.BUFFER_LENGTHS.length)];
        }
        return lengthComboBox.getItemAt(lengthComboBox.getSelectedIndex());
    }

    /**
     * Додати новий вузол
     *
     * @param p точка знаходженн вузла на полотні
     * @return створений вузол
     */
    private Node addNode(Point p) {
        Node node = new Node(p, getLengthOfBuffer());
        nodes.add(node);
        image1.drawNode(node);
        return node;
    }

    /**
     * Видаляє заданий вузол
     *
     * @param node вузол
     */
    private void removeNode(Node node) {
        for (int i = 0; i < links.size(); i++) {
            if ((links.get(i).getNode1() == node) || (links.get(i).getNode2() == node)) {
                links.remove(i);
                i--;
            }
        }
        nodes.remove(node);
        updateNodesCheckBox();
        redrawAll();
    }

    /**
     * Видаляє заданий канал
     *
     * @param link вузол
     */
    private void removeLink(Link link) {
        link.getNode1().removeLink(link);
        link.getNode2().removeLink(link);
        links.remove(link);
        redrawAll();
    }

    /**
     * Перемалювати все
     */
    public void redrawAll() {
        image1.clear();
        image1.drawLinks(links);
        image1.drawNodes(nodes);
        if (selectedLink != null)
            image1.drawSelectedLink(selectedLink);
        else if (selectedNode != null)
            image1.drawSelectedNode(selectedNode);
        if (state == State.MODELING) {
            image1.drawPackets(links);
        }
    }

    /**
     * Змінити стан системи
     *
     * @param state вказаний стан
     */
    private void changeState(State state) {
        //Очистити всі оброблювачі подій з image
        for (MouseListener mouseListener : image1.getMouseListeners()) {
            image1.removeMouseListener(mouseListener);
        }
        for (MouseMotionListener mouseMotionListener : image1.getMouseMotionListeners()) {
            image1.removeMouseMotionListener(mouseMotionListener);
        }

        this.state = state;
        //Зняти вибір всіх елементів
        unselectNodeAndLink();
        //Перемалювати все
        redrawAll();

        //Деактивувати все
        cursorButton.setSelected(false);
        channelButton.setSelected(false);
        nodeButton.setSelected(false);
        removeButton.setSelected(false);
        communicationParameters.setVisible(false);
        nodeParameters.setVisible(false);

        image1.addMouseListener(doubleClickImageListener);
        switch (state) {
            case CURSOR:
                image1.addMouseListener(basicMouseListener);
                image1.addMouseMotionListener(new MoveListener());
                cursorButton.setSelected(true);
                break;
            case NODE:
                image1.addMouseListener(basicMouseListener);
                image1.addMouseListener(addNodeMouseListener);
                image1.addMouseMotionListener(new MoveListener());
                nodeParameters.setVisible(true);
                nodeButton.setSelected(true);
                break;
            case CHANNEL:
                channelButton.setSelected(true);
                communicationParameters.setVisible(true);
                image1.addMouseListener(linkMouseListener);
                break;
            case REMOVE:
                removeButton.setSelected(true);
                image1.addMouseListener(removeElementListener);
        }
    }

    /**
     * Перевірка співпадіння каналу між заданими вузлами
     *
     * @param link канал
     * @param node кінцевий вузол
     * @return при співпадінні true
     */
    public static boolean isIncluded(Link link, Node node) {
        return (node == link.getNode1()) || (node == link.getNode2());
    }

    /**
     * Визначає вагу каналу: якщо {@code autoWeightCheckBox} вибраний, поверне рандомне значення із заданих за умовою
     *
     * @return вагу каналу
     */
    private int getWeight() {
        if (autoWeightCheckBox.isSelected()) {
            Random random = new Random();
            return Link.WEIGHTS[random.nextInt(Link.WEIGHTS.length)];
        }
        return comboBoxWeight.getItemAt(comboBoxWeight.getSelectedIndex());
    }

    /**
     * Створює новий канал між заданими вузлами
     *
     * @param node1 вузол 1
     * @param node2 вузол 2
     * @return ствоворений канал
     */
    private Link createNewLink(Node node1, Node node2) {
        //якщо вибрано автоматичне генерування ваги
        return new Link(node1, node2, getWeight(), getLinkType(), getConnectionType());
    }

    /**
     * Duplex vs Half-duplex
     *
     * @return тип мережі
     */
    private LinkType getLinkType() {
        if (buttonDuplex.isSelected())
            return LinkType.DUPLEX;
        return LinkType.HALF_DUPLEX;
    }

    private ConnectionType getConnectionType() {
        if (satelliteRadioButton.isSelected())
            return ConnectionType.SATELLITE;
        return ConnectionType.GROUND;
    }


    /**
     * Вираховує затримку при моделюванні
     *
     * @return затримку
     */
    public int calculateDelay() {
        int pos = sliderSpeed.getValue() - sliderSpeed.getMinimum();
        double delta = (double) (DELAY_MAX - DELAY_MIN) / (double) (sliderSpeed.getMaximum() - sliderSpeed.getMinimum());
        return (int) (DELAY_MAX - pos * delta);
    }

    //
    public void update() {
        redrawAll();
        if (informationFrame != null)
            informationFrame.update();
        timeLabel.setText(Long.toString(Network.getTime()));
        countInformationPacket.setText(Long.toString(Network.getCountInformationPacket()));
        countSpecialPacket.setText(Long.toString(Network.getCountSpecialPacket()));
        waitTimeLabel.setText(Integer.toString(Packet.calculateWaitTime()));
    }

    private Node findNodeByID(int id) {
        if (nodes != null) {
            for (Node node : nodes)
                if (node.getId() == id)
                    return node;
        }
        return null;
    }

    /**
     * Все очищає
     */
    private void clearAll() {
        unselectNodeAndLink();
        image1.clear();
        links.clear();
        nodes.clear();
    }

    public Dimension getImageSize() {
        return image1.getSize();
    }

    public void closeInformationWindow() {
        informationFrame = null;
    }


    //Обробка натиснень клавіш на формі

    /**
     * Обробка натиску кнопки "Курсор"
     */
    class CursorButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            changeState(State.CURSOR);
        }
    }

    /**
     * Обробка натиску кнопки "Видалити"
     */
    class RemoveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            changeState(State.REMOVE);
        }
    }

    /**
     * Обробка натиску кнопки додавання вузлів
     */
    class NodeButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //переключення між режимами
            changeState(State.NODE);
        }
    }

    /**
     * Запуск/зупинка моделювання
     */
    private class StartStopActionListener implements ActionListener {
        final String startStr = "Увімкнути";
        final String stopStr = "Вимкнути";
        boolean stateStart = true;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (stateStart) {
                startStopButton.setText(stopStr);
                pauseButton.setEnabled(true);
                stepButton.setEnabled(true);
                stateStart = false;
                network.setPacketSize(((Number) packetSizeTextField1.getValue()).intValue());
                if (shortestDistanceRadioButton.isSelected())
                    network.setAlgorithmType(AlgorithmType.SHORTEST_DISTANCE);
                else
                    network.setAlgorithmType(AlgorithmType.SHOTEST_TRANSIT);
                enableComponents(typeRouting,false);
                tabbedPane1.setEnabled(false);
                enableComponents(messagesPanel,true);
                enableComponents(settingsPanel,false);
                network.datagramAccept(UDPAcceptCheckBox.isSelected());
                network.startTimer();
            } else {
                startStopButton.setText(startStr);
                pauseButton.setEnabled(false);
                stepButton.setEnabled(false);
                stateStart = true;
                network.stopTimer();
                enableComponents(typeRouting,true);
                tabbedPane1.setEnabled(true);
                enableComponents(messagesPanel,false);
                enableComponents(settingsPanel,true);


            }
        }
    }

    /**
     * Пауза моделювання
     */
    private class PauseButtonActionListener implements ActionListener {
        final String pauseStr = "Пауза";
        final String continueStr = "Продовжити";
        boolean statePause = true;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!statePause) {
                //пауза
                pauseButton.setText(continueStr);
                statePause = true;
                network.pauseTimer();
            } else {
                //продовжити
                pauseButton.setText(pauseStr);
                statePause = false;
                network.continueTimer();
            }
        }
    }

    /**
     * Крок моделювання
     */
    private class StepButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            network.stepTimer();
        }
    }


    /**
     * Обробка натиску кнопки додавання каналів
     */
    class ChannelButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            changeState(State.CHANNEL);
        }
    }

    /**
     * Обробка натиску кнопки очищення всього
     */
    class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearAll();
            Node.reset();
        }
    }

    //Обробка натиснень клавіш миші

    /**
     * Обробка натиснень миші, якщо вибраний режим "встановлення точок" + пересув
     */
    class NodeMouseListener extends MouseAdapter {
        /**
         * При натисненні
         *
         * @param e інформація про подію
         */
        @Override
        public void mousePressed(MouseEvent e) {

            //Якщо натиснута ліва клавіша миші
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();

                //якщо немає накладання на канали
                if (!intersectLinks(p)) {
                    //якщо немає накладання на інші вузли
                    if (!intersectNodes(p, RADIUS_INTERSECT)) {
                        //Знімаємо вибір вузла
                        unselectNode();
                        //Додаємо і вибираємо створену точку
                        selectNode(addNode(p));
                        updateNodesCheckBox();
                    }
                }

            }
        }
    }

    /**
     * Обробка натиснень миші, якщо вибраний режим "встановлення зв'язків"
     */
    class LinkMouseListener extends MouseAdapter {

        /**
         * При натисненні
         *
         * @param e інформація про подію
         */
        @Override
        public void mousePressed(MouseEvent e) {
            //Якщо натиснута ліва клавіша миші
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();
                //знімаємо вибір вершини і вузла
                unselectNodeAndLink();
                //Якщо є накладання на якийсь вузол, починаємо звідти наш канал
                if (intersectNodes(p, RADIUS_ACCURACY)) {
                    selectNode(p);
                    image1.drawNodeLink1(selectedNode);
                } else if (intersectLinks(p)) {
                    //Якщо попали на вузол
                    selectLink(p);
                }
            }
        }

        /**
         * При відтисненні
         *
         * @param e подія
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            //Якщо натиснута ліва клавіша миші
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();

                //якщо один вузол вже вибраний і кінцева точка співпала з іншим вузлом
                if (isSelectedNode() && intersectNodes(p, RADIUS_ACCURACY)) {
                    Node node1 = selectedNode;
                    Node node2 = intersectNode(p, RADIUS_ACCURACY);
                    if (node1 == node2 || node2 == null)
                        return;
                    image1.drawNodeLink2(node2);

                    for (Link link : node1.getLinks())
                        if (isIncluded(link, node2)) {
                            return;
                        }

                    //якщо немає співпадінь, то додаємо даний канал
                    Link link = createNewLink(node1, node2);
                    links.add(link);
                    node1.addLink(link);
                    node2.addLink(link);
                    selectLink(link);
                }

            }
        }

    }

    /**
     * Базова обробка натиснень миші. Можна вибирати вузли і перетягувати їх
     */
    class BasicMouseListener extends MouseAdapter {
        /**
         * При натисненні
         *
         * @param e інформація про подію
         */
        @Override
        public void mousePressed(MouseEvent e) {
            //Якщо натиснута ліва клавіша миші
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();

                //якщо вибрали канал
                if (intersectNodes(p, CIRCLE_RADIUS)) {
                    //якщо попали на вузол
                    //вибираємо новий
                    selectNode(p);
                } else if (intersectLinks(p) && (state == State.CURSOR)) {
                    selectLink(p);
                } else {
                    //якщо нікуди не попали
                    unselectNodeAndLink();
                }
            }
        }

    }

    private class MoveListener extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();
            if (isSelectedNode() /*&& !selectedNode.intersect(p, RADIUS_ACCURACY) *//*&&
                    !intersectNodes(p, RADIUS_INTERSECT)*/ /*&& !intersectLinks(p)*/) {
                selectedNode.setPosition(p);
                redrawAll();
                selectNode(selectedNode);
            }
        }

    }

    private class RemoveElementListener extends MouseAdapter {
        /**
         * При натисненні
         *
         * @param e інформація про подію
         */
        @Override
        public void mousePressed(MouseEvent e) {
            //Якщо натиснута ліва клавіша миші
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();

                //якщо вибрали канал
                Node node = intersectNode(p, CIRCLE_RADIUS);
                if (node != null) {
                    removeNode(node);
                    return;
                }
                Link link = intersectLink(p);
                if (link != null) {
                    removeLink(link);
                    return;
                }
                //якщо нікуди не попали
                unselectNodeAndLink();
            }
        }
    }

    private class DoubleClickImageListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
                Point p = e.getPoint();

                if (informationFrame != null)
                    informationFrame.dispose();
                //Якщо це вузол
                Node node = intersectNode(p, RADIUS_ACCURACY);
                if (node != null) {
                    informationFrame = new NodeInformation(Realization.this, node);
                    informationFrame.loadForm();
                } else {
                    //якщо це канал
                    Link link = intersectLink(p);
                    if (link != null) {
                        informationFrame = new LinkInformation(Realization.this, link);
                        informationFrame.loadForm();
                    }
                }
            }
        }
    }

    //Інші оброблювачі подій

    /**
     * Обробка вибору автоматичної ваги каналу
     */
    private class AutoWeightCheckBoxListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (autoWeightCheckBox.isSelected()) {
                labelWeight.setEnabled(false);
                comboBoxWeight.setEnabled(false);
            } else {
                labelWeight.setEnabled(true);
                comboBoxWeight.setEnabled(true);
            }
        }
    }

    /**
     * Обробка вибору автоматичної довжини буфера вузла
     */
    private class AutoLengthCheckBoxListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (autoLengthCheckBox.isSelected()) {
                labelLength.setEnabled(false);
                lengthComboBox.setEnabled(false);
            } else {
                labelLength.setEnabled(true);
                lengthComboBox.setEnabled(true);
            }
        }
    }

    /**
     * Зміна розмірів вікна
     */
    class FrameResize extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            image1.resize_();
            redrawAll();
        }
    }

    /**
     * Вибір ваги вибраного каналу
     */
    private class SelectWeightActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedLink != null) {
                selectedLink.setWeight(comboBoxWeight.getItemAt(comboBoxWeight.getSelectedIndex()));
                redrawAll();
            }
        }
    }

    /**
     * Вибір типу комутатції
     */
    private class CommunicationMethodListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedLink != null) {
                selectedLink.setConnectionType(getConnectionType());
                redrawAll();
            }
        }
    }

    /**
     * Вибір типу комутатції
     */
    private class LinkMethodListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedLink != null) {
                selectedLink.setLinkType(getLinkType());
                redrawAll();
            }
        }
    }

    /**
     * Кнопка створення нової мережі
     */
    private class CreateNetworkButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Generator generatorNetwork = CreateNetworkDialog2.showDialog(Realization.this);
            if (generatorNetwork != null) {
                clearAll();
                links.clear();
                links = generatorNetwork.getLinks();
                nodes = generatorNetwork.getNodes();
                updateNodesCheckBox();
                network.cleanAll();
                network = new Network(Realization.this, nodes, links, calculateDelay());

                redrawAll();
            }

        }
    }

    /**
     * Активація/деактивація елементу
     */
    private class ActiveElementCheckBox implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (activeCheckBox.isSelected()) {
                if (selectedNode != null) {
                    selectedNode.activate();
                } else if (selectedLink != null)
                    selectedLink.activate();
            } else {
                if (selectedNode != null) {
                    selectedNode.deActivate();
                } else if (selectedLink != null)
                    selectedLink.deActivate();
            }
            redrawAll();
        }
    }

    private class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = fileChooser.showSaveDialog(Realization.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    FileWorks.writeToFile(file, new NetworkSerialization(links, nodes));
                } catch (IOException e1) {
                    ErrorDialog.showErrorDialog(Realization.this, "Не вдалося зберегти файл.");
                }
            }
        }
    }

    private class OpenButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int returnVal = fileChooser.showOpenDialog(Realization.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    NetworkSerialization networkSerialization = FileWorks.loadOfFile(file);
                    clearAll();
                    links = networkSerialization.links;
                    nodes = networkSerialization.nodes;
                    updateNodesCheckBox();
                    network.cleanAll();
                    network = new Network(Realization.this, nodes, links, calculateDelay());
                    Node.reset(nodes.size());
                    redrawAll();
                } catch (IOException | ClassNotFoundException e1) {
                    ErrorDialog.showErrorDialog(Realization.this, "Не вдалося завантажити мережу з файлу.");
                }
            }
        }
    }


    /**
     * Обробка змінення швидкості
     */
    private class SliderSpeedChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            int delay = calculateDelay();
            if (network != null)
                network.setDelay(delay);
        }
    }


    /**
     * Додає повідомлення в чергу на відсилання
     */
    private class SendMessageActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (network != null) {
                Node from = findNodeByID(nodeFromComboBox.getItemAt(nodeFromComboBox.getSelectedIndex()));
                Node to = findNodeByID(nodeFromComboBox.getItemAt(nodeToComboBox.getSelectedIndex()));
                int size = ((Number) messageSizeTextField.getValue()).intValue();
                MessageType messageType = MessageType.UDP;
                if (TCPRadioButton.isSelected())
                    messageType = MessageType.TCP;
                if (from != null && to != null && from != to) {
                    try {
                        network.sendMessage(from, to, size, messageType);
                    } catch (BufferLengthException be) {
                        ErrorDialog.showErrorDialog(Realization.this, "Буфер переповнений.");
                    }
                } else
                    ErrorDialog.showErrorDialog(Realization.this, "Вибрані невірні параметри відправки повідомлення.");

            }
        }
    }

    protected void createUIComponents() {
        super.createUIComponents();
        NumberFormat numericFormat = NumberFormat.getNumberInstance();
        numericFormat.setMaximumIntegerDigits(6);
        numericFormat.setMinimumIntegerDigits(1);
        messageSizeTextField = new JFormattedTextField(numericFormat);
        messageSizeTextField.setValue(DEFAULT_MESSAGE_SIZE);

        packetSizeTextField1 = new JFormattedTextField(numericFormat);
        packetSizeTextField1.setValue(DEFAULT_MAX_PACKET_SIZE);

        messagesSizeFrom = new JFormattedTextField(numericFormat);
        messagesSizeFrom.setValue(MESSAGE_SIZE_FROM);
        messagesSizeTo = new JFormattedTextField(numericFormat);
        messagesSizeTo.setValue(MESSAGE_SIZE_TO);
    }


    /**
     * Включити/виключити елементи в компоненті
     *
     * @param container компонент
     * @param enable    статус
     */
    public void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container) component, enable);
            }
        }
    }

    private class GeneratorCheckBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (network != null) {
                if (generatorCheckBox.isSelected()) {
                    double frequency;
                    int minSize;
                    int maxSize;
                    try {
                        frequency = Double.parseDouble(frequencyTextField.getText());
                        minSize = ((Number) messagesSizeFrom.getValue()).intValue();
                        maxSize = ((Number) messagesSizeTo.getValue()).intValue();
                        if (minSize > maxSize)
                            throw new InvalidParameterException();
                    } catch (Exception nfe) {
                        ErrorDialog.showErrorDialog(Realization.this, "Вказані дані невірні");
                        generatorCheckBox.setSelected(false);
                        return;
                    }
                    network.startGeneration(frequency, minSize, maxSize);
                    enableComponents(generatorPanel,false);
                } else {
                    enableComponents(generatorPanel,true);
                    network.stopGeneration();
                }
            }

        }
    }

    private class ChangeTablePaneListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (tabbedPane1.getSelectedIndex()==1){
                changeState(State.CURSOR);
            }
        }
    }
}
