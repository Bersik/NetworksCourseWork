package network.model.packet;

import network.Link;
import network.Node;

/**
 * Created on 3:25 01.12.2015
 *
 * @author Bersik
 */

public class HelloPacket extends Packet{
    private static final int SIZE = 100;

    public HelloPacket(Node from, Node to, Link link) {
        super(from, to, link,SIZE,PacketPriority.HIGH);
    }
}
