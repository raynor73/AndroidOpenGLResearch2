package ilapin.collada_parser.collada_loader;

import java.io.InputStream;

import ilapin.collada_parser.data_structures.AnimatedModelData;
import ilapin.collada_parser.data_structures.AnimationData;
import ilapin.collada_parser.data_structures.MeshData;
import ilapin.collada_parser.data_structures.SkeletonData;
import ilapin.collada_parser.data_structures.SkinningData;
import ilapin.collada_parser.xml_parser.XmlNode;
import ilapin.collada_parser.xml_parser.XmlParser;

public class ColladaLoader {

	public static AnimatedModelData loadColladaModel(final InputStream inputStream, int maxWeights) {
		XmlNode node = XmlParser.loadXmlFile(inputStream);

		SkinLoader skinLoader = new SkinLoader(node.getChild("library_controllers"), maxWeights);
		SkinningData skinningData = skinLoader.extractSkinData();

		SkeletonLoader jointsLoader = new SkeletonLoader(node.getChild("library_visual_scenes"), skinningData.jointOrder);
		SkeletonData jointsData = jointsLoader.extractBoneData();

		GeometryLoader g = new GeometryLoader(node.getChild("library_geometries"), skinningData.verticesSkinData);
		MeshData meshData = g.extractModelData();

		return new AnimatedModelData(meshData, jointsData);
	}

	public static AnimationData loadColladaAnimation(final InputStream inputStream) {
		XmlNode node = XmlParser.loadXmlFile(inputStream);
		XmlNode animNode = node.getChild("library_animations");
		XmlNode jointsNode = node.getChild("library_visual_scenes");
		AnimationLoader loader = new AnimationLoader(animNode, jointsNode);
		AnimationData animData = loader.extractAnimation();
		return animData;
	}

}
