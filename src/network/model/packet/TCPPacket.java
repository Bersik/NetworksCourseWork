package network.model.packet;

import network.Link;
import network.Node;

/**
 * Created on 21:44 01.12.2015
 *
 * @author Bersik
 */

public class TCPPacket extends Packet{
    public TCPPacket(Node from, Node to, Link link, int size) {
        super(from, to, link, size);
    }
}
