package network.model.packet.tcp;

import network.Link;
import network.Node;
import network.model.packet.Packet;
import network.model.packet.PacketPriority;
import network.model.packet.accept.AcceptPacket;

/**
 * Created on 3:47 01.12.2015
 *
 * @author Bersik
 */

public class AcceptConnectionTCPPacket extends AcceptPacket {
    private static final int SIZE = 100;

    private int virtualConnectionID;

    /**
     *
     * @param from
     * @param to
     * @param link
     * @param idOriginalPacket номер пакета, який підтверджуємо
     */
    public AcceptConnectionTCPPacket(Node from, Node to, Link link, int idOriginalPacket,int virtualConnectionID) {
        super(from, to, link,idOriginalPacket);
        this.to = link.getAnotherNode(from);
        this.virtualConnectionID = virtualConnectionID;
    }

    public int getVirtualConnectionID() {
        return virtualConnectionID;
    }
}
