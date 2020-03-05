package ilapin.collada_parser.collada_loader;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ilapin.collada_parser.data_structures.SkinningData;
import ilapin.collada_parser.data_structures.VertexSkinData;
import ilapin.collada_parser.xml_parser.XmlNode;

public class SkinLoader {

	//private final XmlNode skinningData;
	private final List<XmlNode> skinsData;
	private final int maxWeights;

	public SkinLoader(final XmlNode controllersNode, final int maxWeights) {
		//this.skinningData = controllersNode.getChild("controller").getChild("skin");
		skinsData = new ArrayList<>();
		for (final XmlNode meshNode : controllersNode.getChildren("controller")) {
			skinsData.add(meshNode.getChild("skin"));
		}
		this.maxWeights = maxWeights;
	}

	public SkinningData extractSkinData() {
		final List<String> jointsList = loadJointsList();
		final float[] weights = loadWeights();
		final XmlNode weightsDataNode = skinningData.getChild("vertex_weights");
		final int[] effectorJointCounts = getEffectiveJointsCounts(weightsDataNode);
		final List<VertexSkinData> vertexWeights = getSkinData(weightsDataNode, effectorJointCounts, weights);
		return new SkinningData(jointsList, vertexWeights);
	}

	private List<String> loadJointsList() {
		final List<String> jointsList = new ArrayList<>();

		for (final XmlNode skinningData : skinsData) {
			final XmlNode inputNode = skinningData.getChild("vertex_weights");
			final String jointDataId = inputNode.getChildWithAttribute("input", "semantic", "JOINT").getAttribute("source")
					.substring(1);
			final XmlNode jointsNode = skinningData.getChildWithAttribute("source", "id", jointDataId).getChild("Name_array");
			final String[] names = jointsNode.getData().split(" ");
			jointsList.addAll(Arrays.asList(names));
		}

		return jointsList;
		/*final XmlNode inputNode = skinningData.getChild("vertex_weights");
		final String jointDataId = inputNode.getChildWithAttribute("input", "semantic", "JOINT").getAttribute("source")
				.substring(1);
		final XmlNode jointsNode = skinningData.getChildWithAttribute("source", "id", jointDataId).getChild("Name_array");
		final String[] names = jointsNode.getData().split(" ");
		final List<String> jointsList = new ArrayList<String>();
		for (final String name : names) {
			jointsList.add(name);
		}
		return jointsList;*/
	}

	private float[] loadWeights() {
		final List<Float> weights = new ArrayList<>();

		for (final XmlNode skinningData : skinsData) {
			final XmlNode inputNode = skinningData.getChild("vertex_weights");
			final String weightsDataId = inputNode.getChildWithAttribute("input", "semantic", "WEIGHT").getAttribute("source")
					.substring(1);
			final XmlNode weightsNode = skinningData.getChildWithAttribute("source", "id", weightsDataId).getChild("float_array");
			final String[] rawData = weightsNode.getData().split(" ");
			for (final String raw : rawData) {
				weights.add(Float.parseFloat(raw));
			}
		}

		final float[] weightsArray = new float[weights.size()];
		for (int i = 0; i < weightsArray.length; i++) {
			weightsArray[i] = weights.get(i);
		}
		return weightsArray;
		/*final XmlNode inputNode = skinningData.getChild("vertex_weights");
		final String weightsDataId = inputNode.getChildWithAttribute("input", "semantic", "WEIGHT").getAttribute("source")
				.substring(1);
		final XmlNode weightsNode = skinningData.getChildWithAttribute("source", "id", weightsDataId).getChild("float_array");
		final String[] rawData = weightsNode.getData().split(" ");
		final float[] weights = new float[rawData.length];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = Float.parseFloat(rawData[i]);
		}
		return weights;*/
	}

	private int[] getEffectiveJointsCounts(final XmlNode weightsDataNode) {
		final String[] rawData = weightsDataNode.getChild("vcount").getData().split(" ");
		final int[] counts = new int[rawData.length];
		for (int i = 0; i < rawData.length; i++) {
			counts[i] = Integer.parseInt(rawData[i]);
		}
		return counts;
	}

	private List<VertexSkinData> getSkinData(final XmlNode weightsDataNode, final int[] counts, final float[] weights) {
		final String[] rawData = weightsDataNode.getChild("v").getData().split(" ");
		final List<VertexSkinData> skinningData = new ArrayList<VertexSkinData>();
		int pointer = 0;
		for (final int count : counts) {
			final VertexSkinData skinData = new VertexSkinData();
			for (int i = 0; i < count; i++) {
				final int jointId = Integer.parseInt(rawData[pointer++]);
				final int weightId = Integer.parseInt(rawData[pointer++]);
				skinData.addJointEffect(jointId, weights[weightId]);
			}
			skinData.limitJointNumber(maxWeights);
			skinningData.add(skinData);
		}
		return skinningData;
	}

}
