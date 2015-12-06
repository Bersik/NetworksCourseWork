package util;

import network.Node;

import java.util.HashMap;

/**
 * Created on 21:16 05.12.2015
 *
 * @author Bersik
 */

public class HashMapFindNode {
    public static <T> Node hashMapFindNode(int nodeID, HashMap<Node,T> map){
        for(Node node:map.keySet()){
            if (node.getId() == nodeID)
                return node;
        }
        return null;
    }
}
