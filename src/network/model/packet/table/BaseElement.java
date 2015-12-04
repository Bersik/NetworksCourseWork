package network.model.packet.table;

import network.Node;

/**
 * Created on 4:46 03.12.2015
 *
 * @author Bersik
 */

public class BaseElement {
    public final Node nodeFrom;
    public final Node nodeTo;
    public final int weight;

    public BaseElement(Node nodeFrom, Node nodeTo, int weight) {
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.weight = weight;
    }
}
