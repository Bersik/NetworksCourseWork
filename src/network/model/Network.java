package network.model;

import network.LinkType;
import network.exception.BufferLengthException;
import network.Link;
import network.Node;
import network.model.packet.LSAPacket;
import network.model.packet.accept.AcceptPacket;
import network.model.packet.HelloPacket;
import network.model.packet.Packet;
import settings.Settings;
import view.Realization;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 15:15 30.11.2015
 *
 * @author Bersik
 */

public class Network implements ActionListener {
    private Realization frame;
    //Список вузлів
    private ArrayList<Node> nodes;
    //Список каналів
    private ArrayList<Link> links;

    //Поточний такт часу (по 1 мілісекунді ідемо)
    private static long time;

    //затримка
    private int delay;

    Timer timer;


    public Network(Realization frame, ArrayList<Node> nodes, ArrayList<Link> links, int delay) {
        //Ініціалізація списку вузлів і каналів
        this.frame = frame;
        this.nodes = nodes;
        this.links = links;
        this.delay = delay;

        time = 0;
    }


    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    /**
     * Типу перед видаленням почистити щось там
     */
    public void destroy() {

    }


    public void startTimer() {
        cleanAll();
        if (timer != null)
            timer.stop();
        timer = new Timer(delay, this);
    }

    public void continueTimer() {
        if (timer != null)
            timer.start();
    }

    public void pauseTimer() {
        if (timer != null)
            timer.stop();
    }

    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }

        //TODO Знищити всі пакети
        cleanAll();

        frame.update();
    }

    public void cleanAll() {
        for (Node node : nodes)
            node.clean();
        for(Link link:links){
            link.clean();
        }

        time = 0;
    }

    public void stepTimer() {
        step();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        step();
        time++;
    }

    public void step() {
        /*0. Проходимо по всім пакетам, і додаємо їхню позицію на 1.
        Якщо позиція = довжині каналу, то говоримо, що пакет дійшов (формуємо підтверження).
        */
        stepPackets();

        /*
        1. Якщо кількість секунд кратна частота_оновлення_сусідів, то запускаємо процес розсилки HELLO пакетів.
        */
        for (Node node : nodes) {
            long timeDelta = time - node.getLastUpdateNeighbor();
            if (timeDelta > Settings.UPDATE_NEIGHBOR) {
                sendHello(node);
            }
        }

        /*
        Перевіряємо наявність змін у таблиці сусідів
        Якщо є зміни, розсилаємо таблицю сусідів сусідам
         */
        for(Node node:nodes){
            //якщо є зміни, розсилаємо нову таблицю
            if (node.checkNeighborTable()){


                //TODO відсилаємо не всім, а лиш тим,
                sendLSA(node);
            }
        }

        /*
        Відправка пакетів
         */
        for (Node node : nodes) {
            node.trySendPackets();
        }

        frame.update();
    }


    /**
     * 0. Проходимо по всім пакетам, і додаємо їхню позицію(час) на 1(пакети в каналах і в вихідних буферах).
     * Якщо позиція >= довжині каналу, то говоримо, що пакет дійшов (обробляємо пакет формуємо підтверження).
     */
    private void stepPackets() {
        //спочатку всі пакети, що в каналах
        for (Link link : links) {
            //Якщо активний
            if (link.isActive()) {
                List<Packet> packets = link.getPackets();
                for (int i = 0; i < packets.size(); i++)
                    //якщо дійшов, то
                    if (packets.get(i).increment()) {
                        if (arrived(packets.get(i), link))
                            i--;
                    }
            }
        }
    }

    /**
     * Пакет прибув до місця призначення
     *
     * @param packet пакет
     */
    private boolean arrived(Packet packet, Link link) {
        //приймач
        Node nodeFrom = packet.getFrom();
        Node nodeTo = packet.getTo();

        //якщо це HELLO пакет
        if (packet instanceof HelloPacket) {
            //Вносимо дані в вузол
            nodeTo.updateNeighbor(nodeFrom);
        }

        //якщо прийшла інформація з таблицею сусіда
            // повинні змінити свою таблицю і відіслати цю інфу всім іншим
        if (packet instanceof LSAPacket){
            Packet departurePacket = new AcceptPacket(nodeTo, nodeFrom, link, packet.getId());
            //Додаємо пакет
            try {
                //в той самий канал відправляємо підтвердження
                nodeTo.addPacket(departurePacket, packet.getLink());
            } catch (BufferLengthException e) {
                //TODO
                System.out.println("Переповнення вихідного буферу");
            }
            LSAPacket lsaPacket = (LSAPacket)packet;
            //оновлюємо таблицю. Якщо є зміни, відсилаємо наш пакет всім іншим (окрім вузла відправача)

            //тепер працюємо з пакетом

            //Потрібно внести зміни в свою таблицю

            // Також треба перевірити, чи не приймали ми вже цей пакет. Якщо не приймали, то пересилаємо далі
            if (nodeTo.updateTable(lsaPacket.getId(),lsaPacket.getBaseNode(),lsaPacket.getNeighbor())) {

                // і переслати цей пакет всім іншим сусідам, окрім того, хто прислав.
                for (Link linkNode : nodeTo.getLinks()) {
                    //Перевірити, чи немає в вихідному буфері цього ж пакету

                    //якщо немає - додаємо пакет в список на відправку
                    if (linkNode != packet.getLink())
                        try {
                            nodeTo.addPacket(new LSAPacket(
                                            //звідки
                                            nodeTo,
                                            //куди
                                            linkNode.getAnotherNode(nodeTo),
                                            //по якому каналу
                                            linkNode,
                                            //той, хто створив пакет
                                            ((LSAPacket) packet).getBaseNode(),
                                            //номер пакету (оскільки транзитний)
                                            packet.getId()),
                                    linkNode);
                        } catch (BufferLengthException e) {
                            e.printStackTrace();
                        }
                }
            }
        }

        //якщо прийшов пакет підтвердження
        if (packet instanceof AcceptPacket) {
            //дістаємо оригінальний пакет
            Packet originalPacket = nodeTo.confirmPacket(((AcceptPacket) packet).getIdOriginalPacket());
            if (originalPacket == null)
                System.out.println("пакет вже підтверджено");
            else{
                //Якщо прийшло підтвердження на хеллоу пакет
                if (originalPacket instanceof HelloPacket){
                    //від кого прийшло
                    Node node = originalPacket.getTo();
                }
                if (originalPacket instanceof LSAPacket){
                    //від кого прийшло
                    Node node = originalPacket.getTo();
                }
            }
        }

        //nodeTo.addConfirmPacket(departurePacket);

        //Прийняли пакет
        nodeTo.addPerformedPacket(packet);

        //видаляємо пакет з каналу
        link.removePacket(packet);
        return true;
    }

    /**
     * Відсилає HELLO запити всім своїм сусідам
     *
     * @param node вузол, з якого відсилаємо
     */
    private void sendHello(Node node) {
        node.setLastUpdateNeighbor(time);
        for (Link link : node.getLinks()) {
            if (link.isActive()) {
                Node neighbor = link.getAnotherNode(node);
                try {
                    Packet packet = new HelloPacket(node, neighbor, link);
                    //без підтвердження
                    node.addPacket(packet, link);
                } catch (BufferLengthException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Відсилаємо таблицю сусідів
     * @param node вузол, з якого відсилаємо
     */
    private void sendLSA(Node node) {
        int packetID = Packet.getNewPacketId();
        for(Link link:node.getLinks()){
            Node neighbor = link.getAnotherNode(node);
            try {
                Packet packet = new LSAPacket(node,neighbor,link,packetID);
                node.addPacket(packet,link);
                node.addConfirmPacket(packet);
            } catch (BufferLengthException e) {
                e.printStackTrace();
            }
        }
    }


    public static long getTime() {
        return time;
    }


    public void setDelay(int delay) {
        this.delay = delay;
        if (timer != null)
            timer.setDelay(delay);
    }
}