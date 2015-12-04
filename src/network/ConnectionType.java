package network;

import java.io.Serializable;

/**
 * Created on 4:51 27.11.2015
 *
 * @author Bersik
 */
public enum ConnectionType  implements Serializable {
    GROUND("Наземний"), SATELLITE("Супутниковий");

    private String str;
    ConnectionType(String s){
        this.str = s;
    }

    @Override
    public String toString() {
        return str;
    }
}
