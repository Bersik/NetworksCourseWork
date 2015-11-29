package network;

/**
 * Created on 4:51 27.11.2015
 *
 * @author Bersik
 */
public enum ConnectionType {
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
