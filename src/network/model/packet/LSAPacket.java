package network.model.packet;

import network.Link;
import network.Node;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 3:25 01.12.2015
 *
 * @author Bersik
 */

public class LSAPacket extends Packet{
    private static final int SIZE = 100;
    //Сусіди (сусід, відстань до сусіда)
    private Node baseNode;
    private HashMap<Node,Integer> neighbor;

    //Транзитне створення пакету
    public LSAPacket(Node from, Node to, Link link,Node baseNode,int ID) {
        super(from, to, link, SIZE, PacketPriority.HIGH,ID);
        this.baseNode = baseNode;
        neighbor = baseNode.getNeighborTable();
    }

    //Початкове створення пакету
    public LSAPacket(Node from, Node to, Link link) {
        super(from,to,link,SIZE,PacketPriority.HIGH);
        baseNode = from;
        neighbor = baseNode.getNeighborTable();
    }

    //Початкове створення пакету
    public LSAPacket(Node from, Node to, Link link,int ID) {
        this(from,to,link,from,ID);
    }

    public HashMap<Node, Integer> getNeighbor() {
        return neighbor;
    }

    public Node getBaseNode() {
        return baseNode;
    }

}
