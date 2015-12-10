package network.packet.vt;

import network.Link;
import network.Node;
import network.packet.Packet;
import network.packet.PacketPriority;

/**
 * Created on 20:21 06.12.2015
 *
 * @author Bersik
 */

public class ConnectionVirtualConnPacket extends Packet{
    private static final int SIZE = 100;
    private int virtualConnectionID;

    public ConnectionVirtualConnPacket(Node from, Node to, Link link, int virtualConnectionID) {
        super(from, to, link,SIZE,PacketPriority.HIGH);
        this.virtualConnectionID = virtualConnectionID;

        this.to = link.getAnotherNode(from);
    }

    public int getVirtualConnectionID() {
        return virtualConnectionID;
    }

}
