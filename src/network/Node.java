package network;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created on 1:09 27.11.2015
 *
 * @author Bersik
 */
public class Node implements Serializable {
    private static int nextId = 0;

    //Позиція вузла на полотні
    private Point position;

    //Номер вузла
    private int id;

    //Довжина буферу
    private int bufferLength;

    //Стан вузла (включений, виключений)
    private boolean active;

    private ArrayList<Link> links;

    public Node(Point point, int bufferLength) {
        this.position = point;
        this.id = nextId++;
        this.bufferLength = bufferLength;
        links = new ArrayList<>();
        active = true;
    }

    public static void reset() {
        nextId = 0;
    }

    /**
     * Перевірка накладання точки на вузол
     *
     * @param p точка
     * @return true, якщо накладається
     */
    public boolean intersect(Point p, int radius) {
        return !(Math.abs(position.x - p.x) > radius ||
                Math.abs(position.y - p.y) > radius);
    }

    public Point getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    public void activate() {
        this.active = true;
        for (Link link : getLinks()) {
            if (link.getNode1().isActive() && link.getNode2().isActive())
                link.activate();
        }
    }

    public void deActivate() {
        this.active = false;
        //якщо деактивуємо вузол, то також деактивуємо канали, які з'єднані з цим вузлом
        getLinks().forEach(Link::deActivate);
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public void addLink(Link link) {
        links.add(link);
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    /**
     * Перевірка накладання точки на якийсь вузол
     *
     * @param point    точка натиску
     * @param accuracy точність
     * @return якщо накладається, повертає true
     */
    public static boolean intersectNodes(ArrayList<Node> nodes, Point point, int accuracy) {
        for (Node n : nodes) {
            if (n.intersect(point, accuracy))
                return true;
        }
        return false;
    }

    public int countLinks() {
        return links.size();
    }

    public boolean isActive() {
        return active;
    }

    public int getBufferLength() {
        return bufferLength;
    }

    public void setBufferLength(int bufferLength) {
        this.bufferLength = bufferLength;
    }

}
