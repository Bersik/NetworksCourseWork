package network;

import network.exception.BufferLengthException;
import network.model.Buffer;
import network.model.Network;
import network.model.packet.Packet;
import network.model.packet.table.TopologyBase;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

import static settings.Settings.*;

/**
 * Created on 1:09 27.11.2015
 *
 * @author Bersik
 */
public class Node implements Serializable {
    //номер наступного вузла
    private static int nextId = 0;

    //Позиція вузла на полотні
    private Point position;

    //Номер вузла
    private int id;

    //Стан вузла (включений, виключений)
    private boolean active;

    //----------Комунікація

    //Довжина буферів
    private int bufferLength;

    //список пакетів, які чекають підтвердження
    private HashMap<Integer, Packet> waitingToConfirm;

    //Всі канали, які зв'язані з цим вузлом
    //список вихідних буферів
    private HashMap<Link, Buffer> outBuffers;

    //Таблиця сусідів (час останнього їхнього оновлення)
    private HashMap<Node, Long> neighbors;
    //час останньої розсилки HELLO пакетів
    private long lastUpdateNeighbor = 0;
    //індикатор змін в таблиці сусідів
    private boolean changesNeighborTable = false;

    //прийняті пакети
    private HashMap<Integer,Packet> performed;

    //топологічна база
    private TopologyBase topologyBase;


    public Node(Point point, int bufferLength) {
        this.position = point;
        this.id = nextId++;
        this.bufferLength = bufferLength;
        outBuffers = new HashMap<>();
        active = true;

        waitingToConfirm = new HashMap<>();
        neighbors = new HashMap<>();
        performed = new HashMap<>();
        topologyBase = new TopologyBase();
    }

    public static void reset() {
        reset(0);
    }

    public static void reset(int val) {
        nextId = val;
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
            if (link.getNode1().isActive() && link.getNode2().isActive()) {
                link.activate();
            }
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
        outBuffers.put(link, new Buffer(link, bufferLength));
    }

    public ArrayList<Link> getLinks() {
        return new ArrayList<>(outBuffers.keySet());
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
        return outBuffers.size();
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


    public void addPacket(Packet packet, Link link) throws BufferLengthException {
        //Записуємо пакет в вихідний буфер
        if (getFreeBufferPlace(link) >= bufferLength)
            throw new BufferLengthException();

        Buffer buffer = outBuffers.get(link);
        for(Packet packet1:buffer) {
            if (packet1.getId() == packet.getId())
                return;
        }
        outBuffers.get(link).add(packet);
    }

    public int getFreeBufferPlace(Link link) {
        Buffer buffer = outBuffers.get(link);
        if (buffer == null)
            outBuffers.put(link, new Buffer(link, bufferLength));
        return outBuffers.get(link).size();
    }

    public long getLastUpdateNeighbor() {
        return lastUpdateNeighbor;
    }

    public void setLastUpdateNeighbor(long lastUpdateNeighborhood) {
        this.lastUpdateNeighbor = lastUpdateNeighborhood;
    }

    /**
     * Видаляємо канал
     *
     * @param link канал
     */
    public void removeLink(Link link) {
        //видаляємо відповідний буфер
        outBuffers.remove(link);
    }

    /**
     * Прийшло підтвердження пакету
     *
     * @param id номер пакету
     * @return оригінальний пакет
     */
    public Packet confirmPacket(int id) {
        if (waitingToConfirm.get(id) != null)
            return waitingToConfirm.remove(id);

        return null;
    }

    /**
     * Додати пакет, який очікує підтвердження
     *
     * @param packet пакет
     */
    public void addConfirmPacket(Packet packet) {
        waitingToConfirm.put(packet.getId(), packet);
    }

    /**
     * Пробуємо відправити повідомлення із буферів вузла
     */
    public void trySendPackets() {
        for (Link link : outBuffers.keySet()) {
            //якщо канал включений
            if (link.isActive()) {
                Buffer buffer = outBuffers.get(link);

                /*Пробуємо відправити пакет.
                Дивимся, наскільки завантажений канал
                */
                if ((buffer.size() > 0) && (link.countPackets() < MAX_PACKETS_IN_LINK)) {
                    /*якщо це напівдуплекс
                    В нас кожен такт актоматично звільняється

                    */
                    if (link.getLinkType() == LinkType.HALF_DUPLEX) {

                        //1. в канал передано достатьно пакетів, потрібно змінити напрям
                        if ((link.getOverallCountPackets() >= COUNT_CHANGE_DIRECTION_HALF_DUPLEX)
                                && (link.countPackets() == 0)) {
                            link.setOverallCountPackets(0);
                            //Змінюємо напрям
                            link.setTransmitter(link.getAnotherNode(link.getTransmitter()));
                        }


                        Node transmitter = buffer.peek().getFrom();
                        //Якщо напрям передачі такий, який зараз треба
                        //визначаємо напрям пакету
                        //якщо напрям співпав, або ж канал вільний - додаємо пакет
                        if ((link.getTransmitter() == null) || (link.getTransmitter() == transmitter)) {

                            link.setTransmitter(transmitter);
                            link.setFree(false);
                            link.addPacket(buffer.poll());
                        }
                    }
                    //якщо дуплекс
                    else {
                        link.addPacket(buffer.poll());
                    }
                }
                /*
                Якщо:
                немає пакетів на відправку і канал налаштований на нашу сторону,
                позначаємо його як вільним
                 */
                if ((buffer.size() == 0) && (link.getLinkType() == LinkType.HALF_DUPLEX) &&
                        (link.getTransmitter() == this) && (link.countPackets() == 0)) {
                    link.setTransmitter(null);
                    link.setOverallCountPackets(0);
                }
            }
        }
    }

    /**
     * Вносить дані в таблицю про сусіда(час оновлення)
     * @param node вузол-сусід
     */
    public void updateNeighbor(Node node) {
        //якщо інформації про вузол не було - додався новий канал
        if (neighbors.get(node) == null)
            changesNeighborTable = true;
        //додаємо сусіда до даного вузла
        neighbors.put(node, Network.getTime());
        //топологія даного вузла
        topologyBase.put(this,getNeighborTable());
    }

    /**
     * Перевіряє зміни в таблиці сусідів і дивиться, чи немає "мертвих" каналів
     * @return
     */
    public boolean checkNeighborTable() {
        //якщо були зміни в таблиці - відсилаємо
        if (changesNeighborTable) {
            changesNeighborTable = false;
            return true;
        }
        boolean flag = false;

        for (Iterator<Map.Entry<Node,Long>> it = neighbors.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Node,Long> entry = it.next();
            long value = entry.getValue();
            if (Network.getTime() - value > NEIGHBOR_LIVE) {
                flag = true;
                it.remove();

            }
        }

        return flag;
    }

    /**
     * Генерує таблицю сусідів вузла для передачі в LSA
     * @return таблиця сусідів
     */
    public HashMap<Node,Integer> getNeighborTable(){
        if (neighbors.size() == 0)
            return null;
        HashMap<Node,Integer> neighborTable = new HashMap<>();
        for(Node node:neighbors.keySet()){
            int len = getLength(node);
            neighborTable.put(node, len);
        }
        return neighborTable;
    }

    public int getLength(Node node){
        for(Link link:outBuffers.keySet()){
            if (link.getAnotherNode(this) == node)
                return link.getWeight();
        }
        throw new RuntimeException();
    }


    public void clean() {
        setLastUpdateNeighbor(-100000);
        waitingToConfirm = new HashMap<>();
        neighbors = new HashMap<>();
    }

    public void addPerformedPacket(Packet packet) {
        performed.put(packet.getId(),packet);
    }


    public boolean updateTable(int ID, Node baseNode, HashMap<Node, Integer> neighbor) {
        //якщо прийшов пакет тому, хто відправляв, або вузол вже цей пакет приймав
        if ((baseNode == this) || (performed.get(ID) != null))
            return false;

        topologyBase.put(baseNode,neighbor);
        //TODO перероблюємо дерево тут

        return true;
    }

    public boolean isPacketOfBuffer(int packetID,Link link){
        for(Packet packet:outBuffers.get(link)){
            if (packet.getId() == packetID)
                return true;
        }
        return false;
    }

    public int getBufferSize(Link link) {
        Buffer buffer = outBuffers.get(link);
        if (buffer == null)
            return -1;
        return buffer.size();
    }

    public HashMap<Node, Long> getNeighbors() {
        return neighbors;
    }
/*
    public int[][] topologyBaseIntegers(){
        Set<Integer> fromNodes =
        ArrayList<Integer> fromNodes = new ArrayList<>(topologyBase.keySet());
        Collections.sort(fromNodes);

        Set<Integer> toNodes

        ArrayList<Integer> toNodes = new ArrayList<>(topologyBase.keySet());


        int[][] base = new int[topologyBase.size()+1][];

        for(int[] arr:base)
            arr = new int[topologyBase.size()];

        for(Node firstNode:topologyBase.keySet()){
            HashMap<Node,Integer> map = topologyBase.get(firstNode);
            int firstNodeID = firstNode.getId();
            for(Node secondNode:topologyBase.get(firstNode).keySet()){
                int secondNodeID = secondNode.getId();
                base[firstNodeID][secondNodeID] = map.get(secondNode);
            }
        }




        return base;
    }
*/

    public TopologyBase getTopologyBase() {
        return topologyBase;
    }


}
