package network.generator;

import network.Link;
import network.Node;

import java.util.ArrayList;

/**
 * Created on 2:46 07.12.2015
 *
 * @author Bersik
 */

public interface Generator {
    ArrayList<Node> getNodes();

    ArrayList<Link> getLinks();
}
