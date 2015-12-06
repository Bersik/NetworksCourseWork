package network.model;

import network.MessageType;
import network.algorithm.AlgorithmType;
import network.exception.BufferLengthException;
import network.Link;
import network.Node;
import network.model.packet.*;
import network.model.packet.accept.AcceptPacket;

import static settings.Settings.*;
import static network.algorithm.AlgorithmType.*;

import network.model.packet.tcp.AcceptConnectionTCPPacket;
import network.model.packet.tcp.ConnectionTCPPacket;
import network.model.packet.tcp.TCPPacket;
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
    //Розмір пакету
    private int packetSize;

    //Поточний такт часу (по 1 мілісекунді ідемо)
    private static long time;

    //Спосіб розрахунку відстані
    private AlgorithmType algorithmType;

    //затримка
    private int delay;

    Timer timer;

    private boolean acceptUDP = true;


    public Network(Realization frame, ArrayList<Node> nodes, ArrayList<Link> links, int delay) {
        //Ініціалізація списку вузлів і каналів
        this.frame = frame;
        this.nodes = nodes;
        this.links = links;
        this.delay = delay;

        packetSize = DEFAULT_MAX_PACKET_SIZE;
        time = 0;
        algorithmType = SHORTEST_DISTANCE;
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
        for (Link link : links) {
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
            if (timeDelta > UPDATE_NEIGHBOR) {
                sendHello(node);
            }
        }

        /*
        Перевіряємо наявність змін у таблиці сусідів
        Якщо є зміни, розсилаємо таблицю сусідів сусідам
         */
        for (Node node : nodes) {
            //якщо є зміни, розсилаємо нову таблицю
            if (node.checkNeighborTable()) {


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

        time++;
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
            nodeTo.updateRoutingTable(algorithmType);

            nodeTo.addPerformedPacket(packet);
        }

        //якщо прийшла інформація з таблицею сусіда
        // повинні змінити свою таблицю і відіслати цю інфу всім іншим
        if (packet instanceof LSAPacket) {
            Packet departurePacket = new AcceptPacket(nodeTo, nodeFrom, link, packet.getId());
            //Додаємо пакет
            try {
                //в той самий канал відправляємо підтвердження
                nodeTo.addPacket(departurePacket, packet.getLink());
            } catch (BufferLengthException e) {
                System.out.println("Переповнення вихідного буферу");
            }
            LSAPacket lsaPacket = (LSAPacket) packet;
            //оновлюємо таблицю. Якщо є зміни, відсилаємо наш пакет всім іншим (окрім вузла відправача)

            //тепер працюємо з пакетом

            //Потрібно внести зміни в свою таблицю

            // Також треба перевірити, чи не приймали ми вже цей пакет. Якщо не приймали, то пересилаємо далі
            if (nodeTo.updateTable(lsaPacket.getId(), lsaPacket.getBaseNode(), lsaPacket.getNeighbor())) {
                //Оновлюємо таблицю маршрутизіції
                nodeTo.updateRoutingTable(algorithmType);
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
            nodeTo.addPerformedPacket(packet);
        }

        /**
         * Якщо прийшов UDP пакет, перевіряємо чи це його кінцева точка
         */
        if (packet instanceof UDPPacket) {
            //якщо кінцева - додаємо до виконаних
            if (((UDPPacket) packet).getBaseTo() == nodeTo) {
                nodeTo.addPerformedPacket(packet);
            } else {
                //інакше пересилаємо далі
                Link nextLink = nodeTo.getLinkForSend(((UDPPacket) packet).getBaseTo());
                if (nextLink != null) {
                    packet.setFrom(nodeTo);
                    packet.setTo(nextLink.getAnotherNode(nodeTo));
                    packet.setLink(nextLink);
                    packet.setPosition(0);
                    try {
                        nodeTo.addPacket(packet, nextLink);
                        nodeTo.addConfirmPacket(packet);
                    } catch (BufferLengthException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (acceptUDP) {
                //посилаємо підтвердження
                Packet departurePacket = new AcceptPacket(nodeTo, nodeFrom, link, packet.getId());
                //Додаємо пакет
                try {
                    //в той самий канал відправляємо підтвердження
                    nodeTo.addPacket(departurePacket, link);
                } catch (BufferLengthException e) {
                    System.out.println("Переповнення вихідного буферу");
                }
            }

        }

        /**
         * якщо прийшов пакет підтвердження
         */
        if (packet instanceof AcceptPacket) {
            //дістаємо оригінальний пакет
            Packet originalPacket = nodeTo.confirmPacket(((AcceptPacket) packet).getIdOriginalPacket());
            if (originalPacket != null) {
                //Якщо прийшло підтвердження на хеллоу пакет
                if (originalPacket instanceof HelloPacket) {
                    //від кого прийшло
                    Node node = originalPacket.getTo();
                }
                if (originalPacket instanceof LSAPacket) {
                    //від кого прийшло
                    Node node = originalPacket.getTo();
                }
            }
        }

        /**
         * якщо прийшов пакет встановлення з'єднання через віртуальний канал
         * (під час подорожі пакет створює цей віртуальний канал на кожному вузлі)
         */
        if (packet instanceof ConnectionTCPPacket) {
            //якщо кінцева - додаємо до виконаних
            ConnectionTCPPacket conPacket = (ConnectionTCPPacket) packet;
            if (conPacket.getBaseTo() == nodeTo) {
                nodeTo.addPerformedPacket(packet);

                int vtID = conPacket.getVirtualConnectionID();
                VirtualConnection vt = new VirtualConnection(vtID, link, null);
                nodeTo.addVirtualConnection(vt);

                //посилаємо підтвердження

                Link departureLink = vt.getPrevLink();
                Packet departurePacket = new AcceptConnectionTCPPacket(nodeTo,
                        conPacket.getBaseFrom(), departureLink, packet.getId(), vtID);
                //Додаємо пакет
                try {
                    //в той самий канал відправляємо підтвердження
                    nodeTo.addPacket(departurePacket, departureLink);
                } catch (BufferLengthException e) {
                    System.out.println("Переповнення вихідного буферу");
                }
            } else {
                //інакше пересилаємо далі
                Link nextLink = nodeTo.getLinkForSend(conPacket.getBaseTo());
                if (nextLink != null) {
                    packet.setFrom(nodeTo);
                    packet.setTo(nextLink.getAnotherNode(nodeTo));
                    packet.setLink(nextLink);
                    packet.setPosition(0);
                    int vtID = conPacket.getVirtualConnectionID();
                    VirtualConnection vt = new VirtualConnection(vtID, link, nextLink);
                    nodeTo.addVirtualConnection(vt);

                    try {
                        nodeTo.addPacket(packet, nextLink);
                        nodeTo.addConfirmPacket(packet);
                    } catch (BufferLengthException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        /**
         * Якщо прийшло підтвердження встановлення з'єднання
         */
        if (packet instanceof AcceptConnectionTCPPacket) {

            AcceptConnectionTCPPacket acceptConnectionTCPPacket = (AcceptConnectionTCPPacket) packet;

            //Підтверджуємо пакет
            nodeTo.confirmPacket(((AcceptPacket) packet).getIdOriginalPacket());

            //якщо кінцева - додаємо до виконаних
            if (acceptConnectionTCPPacket.getBaseTo() == nodeTo) {
                nodeTo.addPerformedPacket(packet);

                int vtID = acceptConnectionTCPPacket.getVirtualConnectionID();
                VirtualConnection vt = nodeTo.getVirtualConnection(vtID);
                for (Packet packetVT : nodeTo.getVirtualConnectionsQueue(vtID)) {
                    try {
                        nodeTo.addPacket(packetVT, vt.getNextLink());
                        //очікуємо підтвердження
                        nodeTo.addConfirmPacket(packetVT);
                    } catch (BufferLengthException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                //інакше пересилаємо далі
                VirtualConnection vt = nodeTo.getVirtualConnection(acceptConnectionTCPPacket.getVirtualConnectionID());
                Link nextLink = vt.getPrevLink();
                if (nextLink != null) {
                    packet.setFrom(nodeTo);
                    packet.setTo(nextLink.getAnotherNode(nodeTo));
                    packet.setLink(nextLink);
                    packet.setPosition(0);
                    try {
                        nodeTo.addPacket(packet, nextLink);

                    } catch (BufferLengthException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //Якщо прийшло підтвердження встановлення з'єднання
        if (packet instanceof TCPPacket) {

            TCPPacket TCPPacket = (TCPPacket) packet;


            //якщо кінцева - додаємо до виконаних
            if (TCPPacket.getBaseTo() == nodeTo) {
                nodeTo.addPerformedPacket(packet);


            } else {
                //інакше пересилаємо далі
                VirtualConnection vt = nodeTo.getVirtualConnection(TCPPacket.getVirtualConnectionID());
                Link nextLink = vt.getNextLink();
                if (nextLink != null) {
                    packet.setFrom(nodeTo);
                    packet.setTo(nextLink.getAnotherNode(nodeTo));
                    packet.setLink(nextLink);
                    packet.setPosition(0);
                    try {
                        nodeTo.addPacket(packet, nextLink);
                    } catch (BufferLengthException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

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
     *
     * @param node вузол, з якого відсилаємо
     */
    private void sendLSA(Node node) {
        for (Link link : node.getLinks()) {
            Node neighbor = link.getAnotherNode(node);
            try {
                Packet packet = new LSAPacket(node, neighbor, link);
                node.addPacket(packet, link);
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

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    /**
     * Відправляє повідомлення з одного вузла в інший
     *
     * @param from        вузол, який відправляє
     * @param to          приймач
     * @param size        розмір повідомлення
     * @param messageType тип повідомлення(протокол) (TCP, UDP)
     */
    public void sendMessage(Node from, Node to, int size, MessageType messageType) throws BufferLengthException {
        if (messageType == MessageType.UDP) {
            sendUDPMessage(from, to, size);
        } else if (messageType == MessageType.TCP)
            sendTCPMessage(from, to, size);
    }

    /**
     * Відправляє UDP повідомлення з одного вузла в інший
     *
     * @param from вузол, який відправляє
     * @param to   приймач
     * @param size розмір повідомлення
     */
    public void sendUDPMessage(Node from, Node to, int size) throws BufferLengthException {
        ArrayList<Packet> packets = new ArrayList<>();
        Link link = detectBestLink(from, to);

        if (size <= packetSize) {
            packets.add(new UDPPacket(from, to, link, size));
        } else {

            int count = size / packetSize;
            if (size % packetSize == 0) {
                for (int i = 1; i <= size / packetSize; i++)
                    packets.add(new UDPPacket(from, to, link, packetSize, i, count));
            } else {
                count++;
                for (int i = 1; i < count; i++)
                    packets.add(new UDPPacket(from, to, link, packetSize, i, count));
                int size$ = count * packetSize;
                packets.add(new UDPPacket(from, to, link, size - size$, count, count));
            }
        }

        for (Packet packet : packets) {
            from.addPacket(packet, link);
            if (acceptUDP)
                from.addConfirmPacket(packet);
        }
    }

    /**
     * Відправляє TCP повідомлення з одного вузла в інший
     *
     * @param from вузол, який відправляє
     * @param to   приймач
     * @param size розмір повідомлення
     */
    public void sendTCPMessage(Node from, Node to, int size) throws BufferLengthException {
        /*  1. Створюємо новий віртуальний канал, прив'язуємо до нього повідомлення
            2. Відправляємо запит на встановлення з'єднання. (під час подорожі пакет створює цей віртуальний канал на кожному вузлі)
            3. Чекаємо поки прийде підвердження. Якщо іде відміна, видаляємо по черзі віртуальний канал
            4. Після цього ставимо в чергу повідомлення з virtualConnectionsQueue і також додаємо їх в чергу підтвердження
        */
        ArrayList<Packet> packets = new ArrayList<>();
        Link link = detectBestLink(from, to);

        //створене віртуальне з'єднання і вказуємо наступний канал, в якйі потрібно передавати повідомлення
        VirtualConnection vt = from.createVirtualConnection(link);
        int virtualConnectionID = vt.getID();

        if (size <= packetSize) {
            packets.add(new TCPPacket(from, to, link, size, virtualConnectionID));
        } else {

            int count = size / packetSize;
            if (size % packetSize == 0) {
                for (int i = 1; i <= size / packetSize; i++)
                    packets.add(new TCPPacket(from, to, link, packetSize, i, count, virtualConnectionID));
            } else {
                count++;
                for (int i = 1; i < count - 1; i++)
                    packets.add(new TCPPacket(from, to, link, packetSize, i, count, virtualConnectionID));
                int size$ = count * packetSize;
                packets.add(new TCPPacket(from, to, link, size - size$, count, count, virtualConnectionID));
            }
        }

        from.addTCPMessages(virtualConnectionID, packets);

        //2. Відправляємо запит на встановлення з'єднання. (під час подорожі пакет створює цей віртуальний канал на кожному вузлі)
        Packet connPacket = new ConnectionTCPPacket(from, to, link, virtualConnectionID);
        from.addPacket(connPacket, link);
        //додаємо в такі, які чекають підтвердження
        from.addConfirmPacket(connPacket);
    }

    /**
     * Визначає канал, в який буде відправлено повідомлення
     *
     * @param from вузол, який відправляє
     * @param to   вузол, який приймає
     */
    public Link detectBestLink(Node from, Node to) {
        return from.getLinkForSend(to);
    }

    public void setAlgorithmType(AlgorithmType algorithmType) {
        this.algorithmType = algorithmType;
    }
}
