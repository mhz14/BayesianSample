package bayes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mahongzhi
 */
public class BayesianNet {

    public List<Node> nodes;

    public int[][] net;
    
    Map<String, Integer> nodeNameIdMap;

    public BayesianNet() {
        this.nodes = new ArrayList<>();
        this.nodeNameIdMap = new HashMap<>();
    }
}

class Node {

    public String name;

    public List<String> values;

    public List<String> parents;

    public Map<String, List<Double>> prob;

    public Node(String name) {
        this.name = name;
        this.values = new ArrayList<>();
        this.parents = new ArrayList<>();
        this.prob = new HashMap<>();
    }
}
