package network;

import java.io.Serializable;

/**
 * Created on 4:50 27.11.2015
 *
 * @author Bersik
 */
public enum LinkType implements Serializable{
    DUPLEX("Дуплекс"), HALF_DUPLEX("Напів-дуплекс");

    private String str;
    LinkType(String s){
        str =s;
    }

    @Override
    public String toString() {
        return str;
    }
}
