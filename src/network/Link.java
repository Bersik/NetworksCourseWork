package network;

import network.packet.Packet;
import settings.Settings;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created on 1:19 27.11.2015
 *
 * @author Bersik
 */
public class Link implements Serializable {
    public static final int[] WEIGHTS = {1, 2, 4, 5, 6, 7, 8, 10, 15, 21, 25, 30};
    public static final int[] BUFFER_LENGTHS = {10, 15, 21, 25, 30};

    private Node node1, node2;

    private int weight;
    private LinkType linkType;
    private ConnectionType connectionType;


    private boolean active;

    //-------Communication
    //список пакетів, які знаходять в даний момент в каналі
    private ArrayList<Packet> packets;

    //Скільки пакетів було віддано в канал
    private int overallCountPackets;

    //Half duplex
    //Вільний
    private boolean free;
    //Напрям передачі (передатчик)
    private Node transmitter;

    public Link(Node node1, Node node2, int weight, LinkType linkType, ConnectionType connectionType) {
        this.node1 = node1;
        this.node2 = node2;
        this.weight = weight;
        this.linkType = linkType;
        this.connectionType = connectionType;
        active = true;

        packets = new ArrayList<>();
        overallCountPackets = 0;
        free = true;
    }

    public boolean intersect(Point point) {
        int radius = Settings.RADIUS_INTERSECT / 2;
        int step = radius / 2;

        int length = (int) (Math.sqrt(Math.pow(node1.getPosition().x - node2.getPosition().x, 2) +
                Math.pow(node1.getPosition().y - node2.getPosition().y, 2)));
        int count = length / step + 1;
        double xstep = (double) (node1.getPosition().x - node2.getPosition().x) / (double) count;
        double ystep = (double) (node1.getPosition().y - node2.getPosition().y) / (double) count;

        for (int i = 0; i < count; i++)
            if ((Math.pow(point.x - node1.getPosition().x + i * xstep, 2) +
                    Math.pow(point.y - node1.getPosition().y + i * ystep, 2)) < Math.pow(radius, 2)) {
                return true;
            }


        return false;
    }

    public int getWeight() {
        return weight;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public Node getNode1() {
        return node1;
    }

    public Node getNode2() {
        return node2;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public void activate() {
        this.active = true;
    }

    public void deActivate() {
        this.active = false;
        packets.clear();
    }

    /**
     * Перевірка накладання точки на якийсь канал
     *
     * @param point точка натиску
     * @return якщо накладається, повертає true
     */
    public static boolean intersectLinks(ArrayList<Link> links, Point point) {
        //TODO Перевірити потім
        for (Link n : links)
            if (n.intersect(point))
                return true;
        return false;
    }

    public boolean isActive() {
        return active;
    }

    public ArrayList<Packet> getPackets() {
        return packets;
    }

    public Node getAnotherNode(Node node){
        return ((node == node2) || (node == null)) ? node1 : node2;
    }

    public int countPackets(Node nodeFrom) {
        if (getLinkType() == LinkType.HALF_DUPLEX){
            return packets.size();
        }
        else{
            int count = 0;
            for (Packet packet:packets)
                if (packet.getFrom() == nodeFrom)
                    count++;
            return count;
        }

    }

    public int getOverallCountPackets() {
        return overallCountPackets;
    }

    public void addPacket(Packet packet){
        packets.add(packet);
        overallCountPackets++;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public void setOverallCountPackets(int overallCountPackets) {
        this.overallCountPackets = overallCountPackets;
    }


    public Node getTransmitter() {
        return transmitter;
    }

    public void setTransmitter(Node transmitter) {
        this.transmitter = transmitter;
    }

    public void removePacket(Packet packet) {
        packets.remove(packet);
    }

    public void clean() {
        packets = new ArrayList<>();
        overallCountPackets = 0;
        free = true;
    }
}
