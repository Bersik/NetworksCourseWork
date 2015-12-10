package network.packet;

import java.io.Serializable;

/**
 * Created on 4:13 01.12.2015
 *
 * @author Bersik
 */

public enum PacketPriority  implements Serializable {
    HIGH(0),MEDIUM(1),LOW(2);

    private int num;

    PacketPriority(int num){
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
