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
    public DisconnectionTCPPacket(Node from, Node to, Link link, int size, PacketPriority priority, int ID, int currentNumber, int count) {
        super(from, to, link, size, priority, ID, currentNumber, count);
    }
}
