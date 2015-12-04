package network.model;

import network.Link;
import network.model.packet.Packet;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Created on 3:22 01.12.2015
 *
 * @author Bersik
 */

public class Buffer extends PriorityQueue<Packet>  implements Serializable {
    //Розмір буферу ( в байтах)
    private int bufferLength;
    //канал, з яким зв'язаний буфер
    private Link link;

    public Buffer(Link link,int bufferLength){
        this.link = link;
        this.bufferLength = bufferLength;
    }

    public int getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(int bufferLength) {
        this.bufferLength = bufferLength;
    }

    public Link getLink() {
        return link;
    }


}
