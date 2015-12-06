package network.model.packet.tcp;

import network.Link;
import network.Node;
import network.model.packet.Packet;

/**
 * Created on 21:44 01.12.2015
 *
 * @author Bersik
 */

public class TCPPacket extends Packet {
    private int virtualConnectionID;

    public TCPPacket(Node from, Node to, Link link, int size,int currentNum,int count,int virtualConnectionID) {
        super(from, to, link, size, currentNum, count);
        baseFrom = from;
        baseTo = to;
        this.to = link.getAnotherNode(from);
        this.virtualConnectionID = virtualConnectionID;
    }

    public TCPPacket(Node from, Node to, Link link, int size,int virtualConnectionID) {
        this(from, to, link, size, 1, 1,virtualConnectionID);
    }

    public int getVirtualConnectionID() {
        return virtualConnectionID;
    }
}
