package si.uni_lj.fri.segmentation;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import si.uni_lj.fri.segmentation.utils.FileUtils;
import si.uni_lj.fri.segmentation.utils.Gauss3D;
import si.uni_lj.fri.segmentation.utils.Graytresh;
import si.uni_lj.fri.segmentation.utils.MarchingCubes;

public class ModelCreatorJava {

	public static Object[] createModel(String fileName, double sigma, double threshold) {
		float[][][] ctMatrix = FileUtils.readFile3D(fileName);
		if (sigma > 0)
			Gauss3D.gauss3D(ctMatrix, sigma);

		double max = 0;
		if (threshold < 0) {
			double[] t = Graytresh.graytresh(ctMatrix);
			threshold = t[0];
			max = t[1];
		} else {
			for (float[][] i : ctMatrix) {
				for (float[] j : i) {
					for (float f : j) {
						if (f > max)
							max = f;
					}
				}
			}
		}
		ArrayList<Float> vertices = MarchingCubes.marchingCubes(ctMatrix, (float) threshold, (float) (threshold * max));
		int nTriangles = vertices.size() / 9;
		float[] v = new float[vertices.size()];
		for (int i = 0; i < vertices.size(); i++)
			v[i] = vertices.get(i);

		return new Object[] { IntBuffer.wrap(new int[] { nTriangles }), FloatBuffer.wrap(v), 0,
				(float) (threshold * max) };
	}
}
