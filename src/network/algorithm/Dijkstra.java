package network.algorithm;

import network.Link;
import network.Node;
import static network.algorithm.AlgorithmType.*;

import java.util.*;


public class Dijkstra {

    public static HashMap<Node,Path> leastWeight(Node from,HashMap<Node,HashMap<Node,Integer>> g,AlgorithmType type) {
        //використані вершини
        Set<Node> used = new HashSet<>();

        //відстані
        HashMap<Node, Integer> d = new HashMap<>();
        //предкі
        HashMap<Node, Node> p = new HashMap<>();
        d.put(from, 0);


        for (Node ignored : g.keySet()) {
            Node v = null;
            for (Node nodeTo : d.keySet()) {
                if (!used.contains(nodeTo)) {
                    if (v == null || (d.get(nodeTo) < d.get(v)))
                        v = nodeTo;
                }
            }
            if (d.get(v) == null)
                break;
            used.add(v);

            if (g.get(v) == null)
                continue;
            for (Node nodeTo : g.get(v).keySet()) {
                int len;
                if (type == SHORTEST_DISTANCE)
                    len = g.get(v).get(nodeTo);
                else
                    len = 1;
                if ((d.get(nodeTo) == null) || (d.get(v) + len < d.get(nodeTo))) {
                    d.put(nodeTo, d.get(v) + len);
                    p.put(nodeTo, v);
                }
            }
        }

        HashMap<Node,Path> nodePathHashMap = new HashMap<>();
        for(Node node:d.keySet()){
            ArrayList<Node> path = new ArrayList<>();

            for (Node v=node; v!=from; v=p.get(v))
                path.add(v);
            path.add(from);
            Collections.reverse(path);
            nodePathHashMap.put(node,new Path(d.get(node),path));
        }
        return nodePathHashMap;
    }

}