package network.model;

import network.Link;
import network.Node;

/**
 * Created on 19:04 06.12.2015
 *
 * @author Bersik
 */

public class VirtualConnection {
    private static int nextID;

    public VirtualConnection(int ID,Link prevLink,Link nextLink){
        this.prevLink = prevLink;
        this.nextLink = nextLink;
        this.ID = ID;
    }

    public VirtualConnection(Link nextLink){
        this(nextID++,null,nextLink);
    }

    //номер віртуального каналу
    private int ID;

    //наступний канал
    private Link nextLink;
    //попередній канал
    private Link prevLink;

    public int getID() {
        return ID;
    }

    public Link getNextLink() {
        return nextLink;
    }

    public Link getPrevLink() {
        return prevLink;
    }
}
