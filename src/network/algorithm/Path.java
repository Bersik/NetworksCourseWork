package network.algorithm;

import network.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 19:53 05.12.2015
 *
 * @author Bersik
 */
public class Path {
    public List<Node> path;
    public int weight;

    public Path(int weight,ArrayList<Node> path){
        this.weight = weight;
        this.path = path;
    }


    public String pathToString() {
        StringBuilder sb = new StringBuilder();
        for(Node node: path){
            sb.append(node.getId());
            sb.append(">");
        }

        return sb.toString().substring(0,sb.length()-1);
    }
}
