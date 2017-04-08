package bayes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author mahongzhi
 */
public class BIFUtil {

	private static final String GARBAGE_CHAR = ",|;|\\(|\\)";

	private enum State {
		NodePart, WaitNextPart, NodeValuePart, FindNodeValuePart, ProbPart, FindParent, FindDistribution, FindProb, FindParentSetValue
	}

	public static BayesianNet readBif(String path) {
		File file = new File(path);
		if (!file.exists()) {
			System.out.println("File doesn't exist: " + path + ".");
			return null;
		}
		if (!file.getName().endsWith("bif")) {
			System.out.println("We need .bif file!");
			return null;
		}

		BayesianNet bn = new BayesianNet();
		State state = State.WaitNextPart;

		try {
			FileInputStream fis = new FileInputStream(file);
			Scanner sin = new Scanner(fis);
			String term = null;
			Node newNode = null, curNode = null, firstParentNode = null;
			int curNodeId = -1;
			int blanketMeetNum = 0, psvAddedNum = 0;
			List<Double> probList = null;
			String parentSetValue = null;
			while (sin.hasNext()) {
				term = sin.next().replaceAll(GARBAGE_CHAR, "");
				if ("".equals(term)) {
					continue;
				}
				switch (state) {
				case WaitNextPart:
					if ("variable".equals(term)) {
						state = State.NodePart;

					} else if ("probability".equals(term)) {
						state = State.ProbPart;
					}
					break;
				case NodePart:
					newNode = new Node(term);
					state = State.FindNodeValuePart;
					blanketMeetNum = 0;
					break;
				case FindNodeValuePart:
					if ("{".equals(term)) {
						blanketMeetNum++;
						if (blanketMeetNum == 2) {
							state = State.NodeValuePart;
						}
					}
					break;
				case NodeValuePart:
					if ("}".equals(term)) {
						bn.nodeNameIdMap.put(newNode.name, bn.nodes.size());
						bn.nodes.add(newNode);
						state = State.WaitNextPart;
					} else {
						newNode.values.add(term);
					}
					break;
				case ProbPart:
					if ("|".equals(term)) {
						state = State.FindParent;
					} else if ("{".equals(term)) {
						state = State.FindDistribution;
					} else {
						curNode = bn.nodes.get(bn.nodeNameIdMap.get(term));
						curNodeId = bn.nodeNameIdMap.get(term);
					}
					break;
				case FindParent:
					if ("{".equals(term)) {
						firstParentNode = bn.nodes.get(bn.nodeNameIdMap
								.get(curNode.parents.get(0)));
						state = State.FindDistribution;
					} else {
						curNode.parents.add(term);
						if (bn.net == null) {
							int nodesNum = bn.nodes.size();
							bn.net = new int[nodesNum][nodesNum];
						}
						int parentNodeId = bn.nodeNameIdMap.get(term);
						bn.net[parentNodeId][curNodeId] = 1;
					}
					break;
				case FindDistribution:
					if ("table".equals(term)) {
						state = State.FindProb;
						parentSetValue = "";
						probList = new ArrayList<>();
					} else if ("}".equals(term)) {
						state = State.WaitNextPart;
					} else if (firstParentNode.values.contains(term)) {
						parentSetValue = term;
						psvAddedNum = 1;
						probList = new ArrayList<>();
						if(curNode.parents.size() == 1){
							state = State.FindProb;
						}else{
							state = State.FindParentSetValue;
						}
					}
					break;
				case FindParentSetValue:
					parentSetValue += term;
					psvAddedNum++;
					if (psvAddedNum == curNode.parents.size()) {
						state = State.FindProb;
					}
					break;
				case FindProb:
					probList.add(Double.valueOf(term));
					if (probList.size() == curNode.values.size()) {
						curNode.prob.put(parentSetValue, probList);
						state = State.FindDistribution;
					}
					break;
				default:
					System.out.println("wrong state!");
					break;
				}
			}

			fis.close();
			sin.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bn;
	}
}
