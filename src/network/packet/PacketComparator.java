package network.packet;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created on 1:56 06.12.2015
 *
 * @author Bersik
 */

public class PacketComparator implements Comparator<Packet>,Serializable {
    @Override
    public int compare(Packet o1, Packet o2) {
        int p1 = o1.getPriority().getNum();
        int p2 = o1.getPriority().getNum();

        if (p1 > p2)
            return 1;
        if (p1 < p2)
            return -1;

        if (o1.getId() > o2.getId())
            return 1;
        if (o1.getId() < o2.getId())
            return -1;

        return 0;
    }

}
