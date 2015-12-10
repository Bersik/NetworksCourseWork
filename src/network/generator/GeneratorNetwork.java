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
public class GeneratorNetwork implements Generator {
    protected ArrayList<Node> nodes;
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
    public GeneratorNetwork(Dimension dimension, int countCommutationNodesValue,
                            int countSatelliteLinksValue, int degreeNetworkValue, int[] weights, int[] bufferLengths) {
        nodes = new ArrayList<>();
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
    protected Point generateRandomPoint() {
        final int OFFSET = CIRCLE_RADIUS * 2;
        final int MIDDLE_OFFSET_X = 200;
        final int MIDDLE_OFFSET_Y = 200;

        int width = (int) dimension.getWidth();
        int height = (int) dimension.getHeight();

        int middleX = width / 2;
        int middleY = height / 2;

        int x;
        int y;
        while (true) {
            x = OFFSET + random.nextInt(width - OFFSET * 2);
            y = OFFSET + random.nextInt(height - OFFSET * 2);
            if (x > middleX - MIDDLE_OFFSET_X && x < middleX + MIDDLE_OFFSET_X &&
                    y > middleY - MIDDLE_OFFSET_Y && y < middleY + MIDDLE_OFFSET_Y)
                continue;
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

        for (int i = 0; i < countCommutationNodesValue; i++) {
            while (true) {
                Point p = generateRandomPoint();
                if (!Node.intersectNodes(nodes, p, RADIUS_INTERSECT * 2)) {
                    int lenBuffer = bufferLengths[random.nextInt(bufferLengths.length)];
                    nodes.add(new Node(p, lenBuffer));
                    break;
                }
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
            Node node1 = getFirstNode();
            //Шукаємо другий вузол
            Node node2 = getSecondNode(node1);

            int minWeightID = 3 * weights.length / 4;
            int weightID = minWeightID + random.nextInt(weights.length - minWeightID);

            addNewLink(node1, node2, weights[weightID], getRandomLinkType(), ConnectionType.SATELLITE);
        }

        for (Node node1 : nodes) {
            for (int i = node1.countLinks(); i < degreeNetworkValue; i++) {
                Node node2 = getSecondNode(node1);
                addNewLink(node1, node2, weights[random.nextInt(weights.length)], getRandomLinkType(), ConnectionType.GROUND);
            }
        }
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
    protected Node getFirstNode() {
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
    protected Node getSecondNode(Node firstNode) {
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
        return nodes;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }
}
