package network.model.packet.tcp;

import network.Link;
import network.Node;
import network.model.packet.Packet;
import network.model.packet.PacketPriority;

/**
 * Created on 22:42 06.12.2015
 *
 * @author Bersik
 */

public class DisconnectionTCPPacket extends Packet{
    private static final int SIZE = 100;
    private int virtualConnectionID;

    public DisconnectionTCPPacket (Node from, Node to, Link link, int virtualConnectionID) {
        super(from, to, link,SIZE,PacketPriority.HIGH);
        this.virtualConnectionID = virtualConnectionID;

        this.to = link.getAnotherNode(from);
    }

    public int getVirtualConnectionID() {
        return virtualConnectionID;
    }
}
