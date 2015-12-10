package files;

import network.Link;
import network.Node;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created on 5:20 29.11.2015
 * Об'єкт, який буде серіалізуватись
 *
 * @author Bersik
 */
public class NetworkSerialization implements Serializable {
    public final ArrayList<Link> links;
    public final ArrayList<Node> nodes;

    public NetworkSerialization(ArrayList<Link> links, ArrayList<Node> nodes) {
        this.links = links;
        this.nodes = nodes;
    }
}
