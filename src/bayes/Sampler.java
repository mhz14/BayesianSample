package bayes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Sampler {

	private static final String BASE_PATH = "./net/";

	private static final String NET_FORMAT = ".bif";

	private static final String BURGLAR_NET = BASE_PATH + "burglar";

	private static final String ALARM_NET = BASE_PATH + "alarm";

	private static final String CARPO_NET = BASE_PATH + "carpo";

	private static final String SACHS_NET = BASE_PATH + "sachs";

	public static void main(String[] args) {
		String curNet = ALARM_NET;

		BayesianNet bn = BIFUtil.readBif(curNet + NET_FORMAT);

		List<Node> order = getTopologicalOrder(bn);

		forwardSample(bn, order, curNet);
	}

	public static void forwardSample(BayesianNet bn, List<Node> order,
			String fileName) {
		// Map<String, Integer> sampleStatistic = new HashMap<>();

		File sampleFile = new File(fileName + ".sample");
		try {
			PrintWriter printer = new PrintWriter(new FileWriter(sampleFile));
			int nodesNum = bn.nodes.size();
			int samplesNum = 10000;
			int i, j, k;
			int[] oneSample = new int[nodesNum];
			for (i = 0; i < samplesNum; i++) {
				for (j = 0; j < nodesNum; j++) {
					Node curNode = order.get(j);
					String parentSetValue = "";
					for (k = 0; k < curNode.parents.size(); k++) {
						int parentId = bn.nodeNameIdMap.get(curNode.parents
								.get(k));
						Node parentNode = bn.nodes.get(parentId);
						parentSetValue += parentNode.values
								.get(oneSample[parentId] - 1);
					}
					oneSample[bn.nodeNameIdMap.get(curNode.name)] = discreteSample(curNode.prob
							.get(parentSetValue));
				}
				String line = "";
				for (j = 0; j < nodesNum - 1; j++) {
					line += oneSample[j] + " ";
				}
				line += oneSample[j];
				printer.println(line);

				// String sampleKey = "";
				// for (j = 0; j < nodesNum; j++) {
				// sampleKey += oneSample[j];
				// }
				// printer.println();
				// if (sampleStatistic.containsKey(sampleKey)) {
				// sampleStatistic.put(sampleKey,
				// sampleStatistic.get(sampleKey) + 1);
				// } else {
				// sampleStatistic.put(sampleKey, 1);
				// }
			}
			printer.flush();
			printer.close();

			// sampleStatistic
			// .entrySet()
			// .stream()
			// .sorted(Map.Entry.<String, Integer> comparingByKey())
			// .forEach(
			// (e) -> {
			// double result = calcProb(bn, e.getKey());
			// System.out.println(e.getKey() + " "
			// + Double.valueOf(e.getValue())
			// / samplesNum + " " + result);
			// });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File infoFile = new File(fileName + ".info");
		try {
			PrintWriter printer = new PrintWriter(new FileWriter(infoFile));
			int i;
			for (i = 0; i < bn.nodes.size() - 1; i++) {
				printer.println(bn.nodes.get(i).values.size());
			}
			printer.print(bn.nodes.get(i).values.size());
			printer.flush();
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<Node> getTopologicalOrder(BayesianNet bn) {

		int nodesNum = bn.nodes.size();
		int[] inDegree = new int[nodesNum];
		int i, j;
		for (i = 0; i < nodesNum; i++) {
			inDegree[i] = 0;
			for (j = 0; j < nodesNum; j++) {
				if (bn.net[j][i] == 1) {
					inDegree[i]++;
				}
			}
		}

		Stack<Integer> s = new Stack<>();
		for (i = 0; i < nodesNum; i++) {
			if (inDegree[i] == 0) {
				s.push(i);
			}
		}

		List<Node> order = new ArrayList<>();
		while (!s.isEmpty()) {
			int cur = s.pop();
			order.add(bn.nodes.get(cur));
			inDegree[cur] = -1;
			for (i = 0; i < nodesNum; i++) {
				if (bn.net[cur][i] == 1 && inDegree[i] != -1) {
					inDegree[i]--;
					if (inDegree[i] == 0) {
						s.push(i);
					}
				}
			}
		}

		if (order.size() != nodesNum) {
			System.out.println("net must be DAG!");
			return null;
		}
		return order;
	}

	public static double calcProb(BayesianNet bn, String value) {
		int[] values = new int[value.length()];
		int i, j;
		for (i = 0; i < value.length(); i++) {
			values[i] = value.charAt(i) - '0';
		}

		double result = 1.0;
		for (i = 0; i < bn.nodes.size(); i++) {
			Node curNode = bn.nodes.get(i);
			String parentSetValue = "";
			for (j = 0; j < curNode.parents.size(); j++) {
				int parentId = bn.nodeNameIdMap.get(curNode.parents.get(j));
				parentSetValue += bn.nodes.get(parentId).values
						.get(values[parentId] - 1);
			}
			List<Double> doubles = curNode.prob.get(parentSetValue);
			result *= doubles
					.get(values[bn.nodeNameIdMap.get(curNode.name)] - 1);
		}
		return result;
	}

	public static int discreteSample(List<Double> prob) {
		int i;
		double[] distribution = new double[prob.size()];
		double sum = 0.0;
		for (i = 0; i < prob.size(); i++) {
			sum += prob.get(i);
			distribution[i] = sum;
		}
		int pos = -1;

		double p = Math.random();
		for (i = 0; i < prob.size(); i++) {
			if (p < distribution[i]) {
				pos = i + 1;
				break;
			}
		}
		return pos;
	}
}
