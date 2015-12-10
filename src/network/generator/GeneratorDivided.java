package network.generator;

import network.ConnectionType;
import network.Link;
import network.LinkType;
import network.Node;
import view.Realization;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import static settings.Settings.*;

/**
 * Created on 4:04 28.11.2015
 *
 * @author Bersik
 */
public class GeneratorDivided implements Generator {
    protected ArrayList<Node> nodes1;
    protected ArrayList<Node> nodes2;
    protected ArrayList<Link> links;
    protected int[] weights;
    protected int[] bufferLengths;
    protected Random random;
    protected int degreeNetworkValue;

    //розмір площини
    protected Dimension dimension;

    /**
     * Створює мережу
     *
     * @param dimension                  розмір полотна
     * @param countCommutationNodesValue кількість вузлів
     * @param countSatelliteLinksValue   кількість супутникових ліній зв'язку
     * @param degreeNetworkValue         ступінь мережі
     */
    public GeneratorDivided(Dimension dimension, int countCommutationNodesValue,
                            int countSatelliteLinksValue, int degreeNetworkValue, int[] weights, int[] bufferLengths) {
        links = new ArrayList<>();
        random = new Random();
        this.weights = weights;
        this.degreeNetworkValue = degreeNetworkValue;
        this.dimension = dimension;
        this.bufferLengths = bufferLengths;

        generateNodes(countCommutationNodesValue);

        generateLink(countSatelliteLinksValue);
    }

    /**
     * Генерує рандомну точку, яка не входить в центр екрану
     *
     * @return точка
     */
    protected Point generateRandomPoint(int group) {
        final int OFFSET = CIRCLE_RADIUS * 2;
        final int MIDDLE_OFFSET_X = 200;

        int width = (int) dimension.getWidth();
        int height = (int) dimension.getHeight();

        int middleX = width / 2;

        int x;
        int y;
        while (true) {
            x = OFFSET + random.nextInt(width - OFFSET * 2);
            y = OFFSET + random.nextInt(height - OFFSET * 2);
            if (group == 1 && x < middleX - MIDDLE_OFFSET_X)
                break;
            else if (group == 2 && x > middleX + MIDDLE_OFFSET_X)
                break;
        }

        return new Point(x, y);
    }

    /**
     * Генерує задану кількість вузлів на площині
     *
     * @param countCommutationNodesValue кількість вузлів
     */
    protected void generateNodes(int countCommutationNodesValue) {

        int countNodesInGroup1 = countCommutationNodesValue/2;
        int countNodesInGroup2 = countCommutationNodesValue - countNodesInGroup1;

        nodes1 = generateGroupNodes(1,countNodesInGroup1);
        nodes2 = generateGroupNodes(2,countNodesInGroup2);

    }

    /**
     * Генерує групу вузлів
     * @param group номер групи
     * @param count кількість
     * @return список вузлів
     */
    private ArrayList<Node> generateGroupNodes(int group,int count){
        ArrayList<Node> nodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            while (true) {
                Point p = generateRandomPoint(group);
                if (!Node.intersectNodes(nodes, p, RADIUS_INTERSECT * 2)) {
                    int lenBuffer = bufferLengths[random.nextInt(bufferLengths.length)];
                    nodes.add(new Node(p, lenBuffer));
                    break;
                }
            }
        }
        return nodes;
    }

    /**
     * Генерує канали
     */
    protected void generateLinkInGroup(ArrayList<Node> nodes) {
        for (Node node1 : nodes) {
            for (int i = node1.countLinks(); i < degreeNetworkValue; i++) {
                Node node2 = getSecondNode(node1, nodes);
                addNewLink(node1, node2, weights[random.nextInt(weights.length)], getRandomLinkType(), ConnectionType.GROUND);
            }
        }
    }

    /**
     * Генерує канали
     *
     * @param countSatelliteLinksValue кількість супутникових каналів
     */
    protected void generateLink(int countSatelliteLinksValue) {

        for (int i = 0; i < countSatelliteLinksValue; i++) {
            //Шукаємо перший вузол
            Node node1 = getFirstNode(nodes1);
            //Шукаємо другий вузол
            Node node2 = getSecondNode(node1,nodes2);

            int minWeightID = 3 * weights.length / 4;
            int weightID = minWeightID + random.nextInt(weights.length - minWeightID);

            addNewLink(node1, node2, weights[weightID], getRandomLinkType(), ConnectionType.SATELLITE);
        }

        generateLinkInGroup(nodes1);
        generateLinkInGroup(nodes2);



    }


    /**
     * Додає новий канал
     *
     * @param node1          перший вузол
     * @param node2          другий вузол
     * @param weight         вага
     * @param linkType       тип з'єднання
     * @param connectionType тип з'єднання
     */
    protected void addNewLink(Node node1, Node node2, int weight, LinkType linkType, ConnectionType connectionType) {
        Link link = new Link(node1, node2,
                weight, linkType, connectionType);
        node1.addLink(link);
        node2.addLink(link);
        links.add(link);
    }

    /**
     * Вибирає перший вузол для каналу
     *
     * @return перший вузол
     */
    protected Node getFirstNode(ArrayList<Node> nodes) {
        Node node1;
        while (true) {
            node1 = nodes.get(random.nextInt(nodes.size()));
            if (node1.countLinks() < degreeNetworkValue)
                return node1;
        }
    }

    /**
     * Вибирає другий вузол для каналу з урахуванням накладання каналів
     *
     * @param firstNode перший вузол
     * @return другий вузол
     */
    protected Node getSecondNode(Node firstNode,ArrayList<Node> nodes) {
        Node node2;
        node2Circle:
        while (true) {
            node2 = nodes.get(random.nextInt(nodes.size()));
            if ((firstNode != node2)/* && (node2.countLinks() < degreeNetworkValue)*/) {
                //перевіряємо, чи немає такого вузла
                for (Link link : firstNode.getLinks())
                    if (Realization.isIncluded(link, node2))
                        continue node2Circle;
                return node2;
            }
        }
    }

    /**
     * Генерує випадковий тип з'єднання
     *
     * @return тип з'єднання
     */
    protected LinkType getRandomLinkType() {
        if (random.nextDouble() < 0.5)
            return LinkType.DUPLEX;
        return LinkType.HALF_DUPLEX;
    }

    public ArrayList<Node> getNodes() {
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.addAll(nodes1);
        nodes.addAll(nodes2);
        return nodes;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

}
