package view;

import files.FileWorks;
import files.Network;
import files.NetworkFileFilter;
import javafx.stage.FileChooser;
import network.*;
import view.form.CreateNetworkDialog2;
import view.form.MainWindow;

import javax.swing.*;

import static settings.Settings.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created on 1:29 27.11.2015
 *
 * @author Bersik
 */
public class Realization extends MainWindow {
    //Список вузлів
    private ArrayList<Node> nodes;

    //Список каналів
    private ArrayList<Link> links;

    //Ексземляр обробника натисення клавіш миші в базовому режимі (Курсор + "Вузол")
    private MouseListener basicMouseListener = new BasicMouseListener();
    //Ексземляр обробника натиснення клавіш миші в режим "Вузол"
    private MouseListener addNodeMouseListener = new NodeMouseListener();
    //Ексземляр обробника натиснення клавіш миші в режим "Канал"
    private LinkMouseListener linkMouseListener = new LinkMouseListener();
    //Ексземляр обробника натиснення клавіш миші в режим "Видалення"
    private RemoveElementListener removeElementListener = new RemoveElementListener();

    //Вибраний вузол
    private Node selectedNode;
    //Вибраний канал
    private Link selectedLink;

    //Вибір файлу
    private JFileChooser fileChooser;

    //стан системи
    private State state;

    public Realization() {
        setTitle("Курсовий проект: Комп'ютерні мережі powered by Кісільчук С. В. КВ-21");
        //Мінімальний розмір форми
        setMinimumSize(new Dimension(1010, 600));

        //Дія при зміні розміру форми
        addComponentListener(new FrameResize());

        //Скривавамо параметри каналів
        communicationParameters.setVisible(false);

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

        saveButton.addActionListener(new SaveButtonListener());
        openButton.addActionListener(new OpenButtonListener());

        //Заповнення даних ваг каналів
        for (int w : Link.WEIGHTS)
            comboBoxWeight.addItem(w);

        //Ініціалізація списку вузлів і каналів
        nodes = new ArrayList<>();
        links = new ArrayList<>();

        //Фокус на кнопці створення мережі
        createNetworkButton.requestFocus();

        //ініціалізація вибору файлу
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new NetworkFileFilter());

        //По замовчуванню, спочатку просто оброблюємо натиски на різні вузли і канали
        changeState(State.CURSOR);
    }

    /**
     * Знайти вузол в заданій точці
     * @param point точка
     * @param accuracy точність
     * @return знайдений вузол
     */
    private Node intersectNode(Point point,int accuracy){
        for (Node n : nodes) {
            if (n.intersect(point, accuracy))
                return n;
        }
        return null;
    }

    /**
     * Перевірка накладання точки на якийсь вузол
     * @param point точка натиску
       @param accuracy точність
     * @return якщо накладається, повертає true
     */
    private boolean intersectNodes(Point point,int accuracy) {
        return Node.intersectNodes(nodes,point,accuracy);
    }

    /**
     * Перевірка накладання точки на якийсь канал
     * @param point точка
     * @return знайдений канал
     */
    private Link intersectLink(Point point){
        for (Link n : links)
            if (n.intersect(point))
                return n;
        return null;
    }

    /**
     * Перевірка накладання точки на якийсь канал
     * @param point точка натиску
     * @return якщо накладається, повертає true
     */
    private boolean intersectLinks(Point point) {
        return Link.intersectLinks(links,point);
    }

    /**
     * Вибрати вузол в точці p
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
     * @param node вузол
     */
    private void selectNode(Node node) {
        //якщо вибрали інший вузол, але вже був вибраний інший вузол
        unselectNodeAndLink();
        selectedNode = node;
        image1.drawSelectedNode(node);
        selectElementState();
    }

    private void selectElementState(){
        Boolean value = null;
        boolean edit = true;
        if (selectedNode!=null){
            value = selectedNode.isActive();
        }else if (selectedLink != null) {
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
     * @param link канал
     */
    private void selectLink(Link link) {
        //TODO також вибрати вагу в полі і інші парамети
        unselectNodeAndLink();
        selectedLink = link;
        image1.drawSelectedLink(link);

        if (state == State.CHANNEL){
            switch (link.getLinkType()){
                case DUPLEX:
                    buttonDuplex.setSelected(true);
                    break;
                case HALF_DUPLEX:
                    buttonHalfDuplex.setSelected(true);
                    break;
            }
            switch (link.getConnectionType()){
                case SATELLITE:
                    satelliteRadioButton.setSelected(true);
                    break;
                case GROUND:
                    groundRadioButton.setSelected(true);
                    break;
            }
            for(int i=0;i<Link.WEIGHTS.length;i++)
                if (Link.WEIGHTS[i] == link.getWeight()){
                    comboBoxWeight.setSelectedIndex(i);
            }
        }
        selectElementState();
    }

    /**
     * Чи вибраний якийсь елемент
     * @return true, false
     */
    private boolean isSelectedItem(){
        return isSelectedLink() || isSelectedNode();
    }

    private boolean isSelectedNode(){
        return selectedNode != null;
    }

    private boolean isSelectedLink(){
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

    private void unselectElement(){
        activeCheckBox.setEnabled(false);
    }

    /**
     * Зняти вибір каналу
     */
    private void unselectLink() {
        if (isSelectedLink()) {
            selectedLink = null;
            unselectElement();
            redrawAll();
        }
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

    /**
     * Додати новий вузол
     * @param p точка знаходженн вузла на полотні
     * @return створений вузол
     */
    private Node addNode(Point p) {
        Node node = new Node(p);
        nodes.add(node);
        image1.drawNode(node);
        return node;
    }

    /**
     * Видаляє заданий вузол
     * @param node вузол
     */
    private void removeNode(Node node){
        for(int i=0;i<links.size();i++){
            if ((links.get(i).getNode1() == node) || (links.get(i).getNode2() == node)){
                links.remove(i);
                i--;
            }
        }
        nodes.remove(node);
        redrawAll();
    }

    /**
     * Видаляє заданий канал
     * @param link вузол
     */
    private void removeLink(Link link){
        links.remove(link);
        redrawAll();
    }

    /**
     * Перемалювати все
     */
    private void redrawAll() {
        image1.clear();
        image1.drawLinks(links);
        image1.drawNodes(nodes);
        if (selectedLink != null)
            image1.drawSelectedLink(selectedLink);
        else if (selectedNode != null)
            image1.drawSelectedNode(selectedNode);
    }

    /**
     * Змінити стан системи
     * @param state вказаний стан
     */
    private void changeState(State state){
        //Очистити всі оброблювачі подій з image
        for (MouseListener mouseListener:image1.getMouseListeners()) {
            image1.removeMouseListener(mouseListener);
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
        switch (state){
            case CURSOR:
                image1.addMouseListener(basicMouseListener);
                cursorButton.setSelected(true);
                break;
            case NODE:
                image1.addMouseListener(basicMouseListener);
                image1.addMouseListener(addNodeMouseListener);
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
     * @param link канал
     * @param node кінцевий вузол
     * @return при співпадінні true
     */
    public static boolean isIncluded(Link link, Node node) {
        return (node == link.getNode1()) || (node == link.getNode2());
    }

    /**
     * Визначає вагу каналу: якщо {@code autoWeightCheckBox} вибраний, поверне рандомне значення із заданих за умовою
     * @return вагу каналу
     */
    private int getWeight(){
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

    //Обробка натиснень клавіш на формі

    /**
     * Обробка натиску кнопки "Курсор"
     */
    class CursorButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            changeState(State.CURSOR);
        }
    }

    /**
     * Обробка натиску кнопки "Видалити"
     */
    class RemoveButtonListener implements ActionListener{
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
            /*TODO також треба вивести контекстне меню при правій кнопці типу видалити вузол канал,
            TODO хоча це можна зробити в окремому лісенері, який буде працювати весь час
            */

            //Якщо натиснута ліва клавіша миші
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();

                //якщо немає накладання на канали
                if (!intersectLinks(p)) {
                    //якщо немає накладання на інші вузли
                    if (!intersectNodes(p,RADIUS_INTERSECT)) {
                        //Знімаємо вибір вузла
                        unselectNode();
                        //Додаємо і вибираємо створену точку
                        selectNode(addNode(p));
                    }
                    //TODO якщо попали на вузол, то треба щось зробити...
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
            /*TODO також треба вивести контекстне меню при правій кнопці типу видалити вузол канал,
            TODO хоча це можна зробити в окремому лісенері, який буде працювати весь час
            */

            //Якщо натиснута ліва клавіша миші
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();
                //знімаємо вибір вершини і вузла
                unselectNodeAndLink();
                //Якщо є накладання на якийсь вузол, починаємо звідти наш канал
                if (intersectNodes(p,RADIUS_ACCURACY)){
                    selectNode(p);
                    image1.drawNodeLink1(selectedNode);
                }else if (intersectLinks(p)){
                    //Якщо попали на вузол
                    selectLink(p);
                }
            }
        }

        /**
         * При відтисненні
         * @param e подія
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            //Якщо натиснута ліва клавіша миші
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();

                //якщо один вузол вже вибраний і кінцева точка співпала з іншим вузлом
                if (isSelectedNode() && intersectNodes(p,RADIUS_ACCURACY)){
                    Node node1 = selectedNode;
                    Node node2 = intersectNode(p,RADIUS_ACCURACY);
                    if (node1 == node2 || node2 == null)
                        return;
                    image1.drawNodeLink2(node2);

                    for(Link link:node1.getLinks())
                        if (isIncluded(link,node2)) {
                            //TODO вибрати цей зв'язок
                            return;
                        }

                    //якщо немає співпадінь, то додаємо даний канал
                    Link link = createNewLink(node1,node2);
                    links.add(link);
                    node1.addLink(link);
                    node2.addLink(link);
                    selectLink(link);
                }
                /*
                //знімаємо вибір вершини і вузла
                unselectNodeAndLink();
                //Якщо є накладання на якийсь вузол, починаємо звідти наш канал
                if (intersectNodes(p,RADIUS_ACCURACY)){
                    selectNode(p);
                    image1.drawNodeLink1(selectedNode);
                }else if (intersectLinks(p)){
                    //Якщо попали на вузол
                    selectLink(p);
                }*/
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
            /*TODO також треба вивести контекстне меню при правій кнопці типу видалити вузол канал,
            */

            //Якщо натиснута ліва клавіша миші
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();

                //якщо вибрали канал
                if (intersectNodes(p,CIRCLE_RADIUS)) {
                    //якщо попали на вузол
                    //вибираємо новий
                    selectNode(p);
                }else if (intersectLinks(p) && (state == State.CURSOR)) {
                    selectLink(p);
                } else{
                    //якщо нікуди не попали
                    unselectNodeAndLink();
                }
            }
        }

        /**
         * При відтисненні (перетягування вузла)
         * @param e інформація про подію
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            Point p = e.getPoint();
            //Якщо натиснута ліва клавіша миші
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (isSelectedNode() && !selectedNode.intersect(p,RADIUS_ACCURACY) &&
                        !intersectNodes(p,RADIUS_INTERSECT) /*&& !intersectLinks(p)*/){
                    selectedNode.setPosition(p);
                    redrawAll();
                    selectNode(selectedNode);
                }
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
                Node node = intersectNode(p,CIRCLE_RADIUS);
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
            if (selectedLink != null){
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
            if (selectedLink != null){
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
            if (selectedLink != null){
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
            CreateNetworkDialog2 dialog = new CreateNetworkDialog2(Realization.this);
            dialog.launch();
            GeneratorNetwork generatorNetwork = dialog.getGeneratorNetwork();
            if (generatorNetwork !=null){
                clearAll();
                Node.reset();
                links = generatorNetwork.getLinks();
                nodes = generatorNetwork.getNodes();
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
            if (activeCheckBox.isSelected()){
                if (selectedNode != null) {
                    selectedNode.activate();
                    for(Link link:selectedNode.getLinks()){
                        link.activate();
                    }
                }
                else if (selectedLink != null)
                    selectedLink.activate();
            }else{
                if (selectedNode != null) {
                    selectedNode.deActivate();
                    //якщо деактивуємо вузол, то також деактивуємо канали, які з'єднані з цим вузлом
                    selectedNode.getLinks().forEach(Link::deActivate);
                }
                else if (selectedLink != null )
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
                    FileWorks.writeToFile(file,new Network(links,nodes));
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(Realization.this,
                            "Не вдалося зберегти файл.","Помилка",JOptionPane.ERROR_MESSAGE);
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
                    Network network = FileWorks.loadOfFile(file);
                    clearAll();
                    links = network.links;
                    nodes = network.nodes;
                    redrawAll();
                } catch (IOException | ClassNotFoundException e1) {
                    JOptionPane.showMessageDialog(Realization.this,
                            "Не вдалося завантажити мережу з файлу.","Помилка",JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
