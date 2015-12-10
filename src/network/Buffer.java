package network;

import network.packet.Packet;

import java.io.Serializable;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Created on 3:22 01.12.2015
 *
 * @author Bersik
 */

public class Buffer extends PriorityQueue<Packet> implements Serializable {
    //Розмір буферу ( в байтах)
    private int bufferLength;
    //канал, з яким зв'язаний буфер
    private Link link;

    private int delay;

    public Buffer(Link link, int bufferLength, Comparator<Packet> comparator) {
        super(comparator);
        this.link = link;
        this.bufferLength = bufferLength;
        this.delay = 0;
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


    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int calculateDelay(int weight,int lastWeightLink) {
        //кількість пакетів в каналі
        int newDelay = this.size() + weight;
        int append = 0;

        double pers = 0.50;

        //Тепер змінюємо вагу каналу у випадку, якщо використання каналу > 50%
        if (newDelay > bufferLength / 2) {
            if (lastWeightLink != bufferLength / 2)
                return newDelay;
        }else {
            if ((lastWeightLink == bufferLength / 2) && (newDelay < bufferLength / 4))
                return weight;
        }
        return -1;
    }
}
