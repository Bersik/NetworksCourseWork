package network.packet;

import network.Link;
import network.Node;
import network.Network;

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

    protected Node baseFrom;
    protected Node baseTo;

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
    protected int currentNumber;
    //загальна кількість пакетів у повідомленні
    protected int count;
    private int currentId;

    //час простою пакета в вузлах
    private long waitTime;
    protected static ArrayList<Packet> packets = new ArrayList<>();


    public Packet(Node from, Node to, Link link, int size, PacketPriority priority, int ID, int currentNumber, int count) {
        this.from = from;
        this.to = to;

        this.baseFrom = from;
        this.baseTo = to;

        this.link = link;
        this.size = size;
        //TCP,UDP
        this.priority = priority;
        this.timeBorn = Network.getTime();
        this.packetId = ID;

        this.currentNumber = currentNumber;
        this.count = count;

        position = 0;
        waitTime = 0;
        packets.add(this);
    }

    public Packet(Node from, Node to, Link link, int size, PacketPriority priority, int ID) {
        this(from, to, link, size, priority, ID, 1, 1);
    }

    public Packet(Node from, Node to, Link link, int size, PacketPriority priority) {
        this(from, to, link, size, priority, nextId++);
    }

    public Packet(Node from, Node to, Link link, int size, int currentNumber, int count) {
        this(from, to, link, size, PacketPriority.LOW, nextId++, currentNumber, count);
    }

    public Packet(Node from, Node to, Link link, int size) {
        this(from, to, link, size, PacketPriority.LOW);
    }

    public static int getCurrentId() {
        return nextId;
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
        return new PacketComparator().compare(this, o);
    }

    public static int getNewPacketId() {
        return nextId++;
    }

    public int getNumber() {
        return count;
    }

    public int getTotalNumber() {
        return currentNumber;
    }


    public void setFrom(Node from) {
        this.from = from;
    }

    public void setTo(Node to) {
        this.to = to;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Node getBaseFrom() {
        return baseFrom;
    }

    public Node getBaseTo() {
        return baseTo;
    }

    public static void clean() {
        nextId = 0;
        clearPackets();
    }

    public static void clearPackets(){
        packets = new ArrayList<>();
    }

    public void waitTact() {
        waitTime++;
    }

    public static int calculateWaitTime() {
        long waitTimes = 0;

        for (Packet packet : packets) {
            waitTimes += packet.waitTime;
        }
        if (packets.size() > 0)
            return (int) (waitTimes / packets.size());
        else
            return 0;
    }

    public static int packetsCount() {
        return packets.size();
    }
}

