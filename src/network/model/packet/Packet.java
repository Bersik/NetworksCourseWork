package network.model.packet;

import network.Link;
import network.Node;
import network.model.Network;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created on 3:21 01.12.2015
 *
 * @author Bersik
 */
public abstract class Packet implements Comparable<Packet>, Serializable {

    protected static int nextId = 0;
    protected Node from;
    protected Node to;
    //Розмір
    protected int size;
    //Пріорітет
    protected PacketPriority priority;

    //Час створення пакету
    protected long timeBorn;

    //Канал, по якому іде пакет
    protected Link link;

    //ID пакету
    protected int packetId;

    protected int position;
    //номер пакету
    private int number;
    //загальна кількість пакетів у повідомленні
    private int totalNumber;

    public Packet(Node from, Node to, Link link, int size, PacketPriority priority, int ID) {
        this.from = from;
        this.to = to;
        this.link = link;
        this.size = size;
        //TCP,UDP
        this.priority = priority;
        this.timeBorn = Network.getTime();
        this.packetId = ID;


        this.number = 1;
        this.totalNumber = 1;

        position = 0;
    }

    public Packet(Node from, Node to, Link link, int size) {
        this(from, to, link, size, PacketPriority.LOW);
    }


    public Packet(Node from, Node to, Link link, int size, PacketPriority priority) {
        this(from, to, link, size, priority, nextId++);
    }

    //Пакет робить крок
    public boolean increment() {
        position++;
        return position >= link.getWeight();
    }

    public int getId() {
        return packetId;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public Link getLink() {
        return link;
    }


    public int getSize() {
        return size;
    }

    public PacketPriority getPriority() {
        return priority;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int compareTo(Packet o) {
        if (priority.getNum() > o.getPriority().getNum())
            return 1;
        if (priority.getNum() < o.getPriority().getNum())
            return -1;
        return 0;
    }

    public static int getNewPacketId() {
        return nextId++;
    }

    public int getNumber() {
        return number;
    }

    public int getTotalNumber() {
        return totalNumber;
    }
}
