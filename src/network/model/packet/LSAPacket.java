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
    private HashMap<Node,Integer> neighbor;

    //Транзитне створення пакету
    public LSAPacket(Node from, Node to, Link link,Node baseNode,int ID) {
        super(from, to, link, SIZE, PacketPriority.HIGH,ID);
        this.baseFrom = baseNode;
        this.neighbor = new HashMap<>(baseNode.getNeighborTable());
    }

    //Початкове створення пакету
    public LSAPacket(Node from, Node to, Link link) {
        this(from,to,link,from,nextId++);
    }

    public HashMap<Node, Integer> getNeighbor() {
        return neighbor;
    }

    public Node getBaseNode() {
        return baseFrom;
    }

}
