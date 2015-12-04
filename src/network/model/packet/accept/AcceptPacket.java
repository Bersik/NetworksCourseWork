package network.model.packet.accept;

import network.Link;
import network.Node;
import network.model.packet.Packet;
import network.model.packet.PacketPriority;

/**
 * Created on 3:47 01.12.2015
 *
 * @author Bersik
 */

public class AcceptPacket extends Packet {
    private static final int SIZE = 100;

    private int idOriginalPacket;

    /**
     *
     * @param from
     * @param to
     * @param link
     * @param id номер пакета, який підтверджуємо
     */
    public AcceptPacket(Node from, Node to, Link link, int id) {
        super(from, to, link,SIZE, PacketPriority.HIGH);
        idOriginalPacket = id;

    }

    public int getIdOriginalPacket() {
        return idOriginalPacket;
    }
}
