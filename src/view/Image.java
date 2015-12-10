package view;

import network.ConnectionType;
import network.Link;
import network.LinkType;
import network.Node;
import network.packet.HelloPacket;
import network.packet.LSAPacket;
import network.packet.Packet;
import network.packet.DatagramPacket;
import network.packet.AcceptPacket;
import network.packet.vt.ConnectionVirtualConnPacket;
import network.packet.vt.DisconnectionVirtualConnPacket;
import network.packet.vt.VirtualConnPacket;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static settings.Settings.*;

/**
 * Created on 20:58 26.11.2015
 *
 * @author Bersik
 */
public class Image extends JPanel {
    private BufferedImage imag;
    private Graphics2D graphics;

    public Image(int width, int height) {
        super();

        setNewSize(width, height);
    }

    /**
     * Встановити новий розмір
     *
     * @param width  ширина
     * @param height висота
     */
    public void setNewSize(int width, int height) {
        setSize(width, height);

        imag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        graphics = imag.createGraphics();
        clear();
        this.repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(imag, 0, 0, this);
    }

    /**
     * Очистити полотно
     */
    public void clear() {
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setColor(Color.black);
        repaint();
    }

    /**
     * Змінює розмір на новий
     */
    public void resize_() {
        setNewSize(getWidth(), getHeight());
    }

    /**
     * Малює вузол на полотні
     *
     * @param node вузол
     */
    public void drawNode(Node node) {
        if (node.isActive())
            drawNode(node, NODE_COLOR);
        else
            drawNode(node, ELEMENT_DISABLE_COLOR);
    }

    /**
     * Малює вузол заданми кольором
     *
     * @param node  вузол
     * @param color колір
     */
    public void drawNode(Node node, Color color) {
        Point p = node.getPosition();
        graphics.setColor(color);
        graphics.fillOval(p.x - CIRCLE_RADIUS, p.y - CIRCLE_RADIUS, CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2);
        graphics.setColor(DEFAULT_COLOR);
        graphics.drawOval(p.x - CIRCLE_RADIUS, p.y - CIRCLE_RADIUS, CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2);
        int textOffset = CIRCLE_RADIUS / 2;
        graphics.drawString(Integer.toString(node.getId()), p.x - textOffset, p.y + textOffset);
        repaint();
    }

    /**
     * Малює вибраний вузол
     *
     * @param node вузол
     */
    public void drawSelectedNode(Node node) {
        drawNode(node, NODE_SELECTED_COLOR);
    }

    /**
     * Малює вузол, з якого починається канал
     *
     * @param node вузол
     */
    public void drawNodeLink1(Node node) {
        drawNode(node, NODE_LINK1_COLOR);
    }

    /**
     * Малює вузол, в якому закінчується канал
     *
     * @param node вузол
     */
    public void drawNodeLink2(Node node) {
        drawNode(node, NODE_LINK2_COLOR);
    }

    /**
     * Малює всі вузли, які знаходяться в {@code nodes}
     *
     * @param nodes список вузлів
     */
    public void drawNodes(ArrayList<Node> nodes) {
        for (Node node : nodes)
            drawNode(node);
    }

    /**
     * Малює канал на полотні
     *
     * @param link вузол
     */
    public void drawLink(Link link) {
        if (link.isActive()) {
            if (link.getLinkType() == LinkType.DUPLEX)
                drawLink(link, LINK_DUPLEX);
            else
                drawLink(link, LINK_HALF_DUPLEX);
        } else
            drawLink(link, ELEMENT_DISABLE_COLOR);
    }

    /**
     * Малює канал на полотні
     *
     * @param link  вузол
     * @param color колір
     */
    public void drawLink(Link link, Color color) {

        Point pos1 = link.getNode1().getPosition();
        Point pos2 = link.getNode2().getPosition();

        //Виводимо всі пакети, які є в каналі
        for (Packet packet : link.getPackets()) {

            double pos = packet.getPosition();


            int xl = pos1.x - pos2.x;
            int yl = pos1.y - pos2.y;
            double xc = (double) xl / (double) link.getWeight();
            double yc = (double) yl / (double) link.getWeight();

            Point coord;
            if (packet.getFrom() == link.getNode1())
                coord = new Point((int) ((double) pos1.x - pos * xc), (int) ((double) pos1.y - pos * yc));
            else
                coord = new Point((int) ((double) pos2.x + pos * xc), (int) ((double) pos2.y + pos * yc));

            coord.x += 5;
            coord.y -= 20;

            Point coordText = new Point(coord.x+5,coord.y+15);

            graphics.setColor(PACKET_COLOR);
            if (packet instanceof HelloPacket)
                graphics.setColor(PACKET_COLOR_HELLO);
            else if (packet instanceof LSAPacket)
                graphics.setColor(PACKET_COLOR_LSA);
            else if (packet instanceof AcceptPacket)
                graphics.setColor(PACKET_COLOR_ACCEPT);
            else if (packet instanceof DatagramPacket)
                graphics.setColor(PACKET_COLOR_UDP);
            else if (packet instanceof VirtualConnPacket)
                graphics.setColor(PACKET_COLOR_TCP);
            else if (packet instanceof ConnectionVirtualConnPacket)
                graphics.setColor(PACKET_COLOR_TCP_ASK);
            else if (packet instanceof DisconnectionVirtualConnPacket)
                graphics.setColor(PACKET_COLOR_TCP_CLOSE);
            graphics.fillRect(coord.x, coord.y, 40, 20);
            graphics.setColor(DEFAULT_COLOR);
            graphics.drawRect(coord.x, coord.y, 40, 20);
            graphics.drawString(Integer.toString(packet.getId()),coordText.x,coordText.y);

        }

        Stroke tmp = graphics.getStroke();
        final float width;
        if (link.getLinkType() == LinkType.DUPLEX)
            width = 1.0f;
        else
            width = 1.0f;

        if (link.getConnectionType() == ConnectionType.SATELLITE) {
            float dash1[] = {10.0f, 5.0f};
            BasicStroke dashedSatellite =
                    new BasicStroke(width,
                            BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_ROUND,
                            5.0f, dash1, 0.0f);
            graphics.setStroke(dashedSatellite);
        } else {
            graphics.setStroke(new BasicStroke(width));
        }


        //Якщо канал - напівдуплекс
        if (link.getLinkType() == LinkType.HALF_DUPLEX) {
            graphics.setColor(color);
            graphics.drawLine(pos1.x, pos1.y, pos2.x, pos2.y);
        } else {
            //TODO доробити
            graphics.setColor(color);
            final int d = 1;

            //робимо 2 варіанти: / і \ - перша точка зверху
            if (pos1.y > pos2.y) {
                Point p = pos1;
                pos1 = pos2;
                pos2 = p;
            }

            int dx = d, dy = d;

            if (pos1.x < pos2.x) {
                dx = -dx;
            }

            graphics.drawLine(pos1.x + dx, pos1.y + dy, pos2.x + dx, pos2.y + dy);
            graphics.drawLine(pos1.x - dx, pos1.y - dy, pos2.x - dx, pos2.y - dy);
        }

        graphics.setStroke(tmp);

        drawNodeLink1(link.getNode1());
        drawNodeLink1(link.getNode2());

        int midlex = (pos1.x + pos2.x) / 2 - 8;
        int midley = (pos1.y + pos2.y) / 2 - 8;

        //graphics.setColor(Color.decode("#E0DEE3"));
        //graphics.drawOval(midlex-8,midley-8,16,16);
        graphics.setColor(DEFAULT_COLOR);
        if (Integer.toString(link.getWeight()).length() == 1)
            graphics.drawString(Integer.toString(link.getWeight()), midlex + 4, midley);
        else
            graphics.drawString(Integer.toString(link.getWeight()), midlex, midley);

    }

    /**
     * Малює вибраний канал ка полотні
     *
     * @param link канал
     */
    public void drawSelectedLink(Link link) {
        drawLink(link, LINK_SELECTED_COLOR);
    }

    /**
     * Малює всі канали, які знаходяться в {@code links}
     *
     * @param links список каналів
     */
    public void drawLinks(ArrayList<Link> links) {
        for (Link link : links)
            drawLink(link);
    }

    public void drawPackets(ArrayList<Link> links) {
        /*for(Link link:links){
            ArrayList<Packet> packets =  link.getPackets();
            for(Packet packet:packets){
                int pos = packet.getPosition();

            }
        }*/
    }
}
