package si.uni_lj.fri.segmentation;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import si.uni_lj.fri.segmentation.utils.FileUtils;
import si.uni_lj.fri.segmentation.utils.Gauss3D;
import si.uni_lj.fri.segmentation.utils.Graytresh;
import si.uni_lj.fri.segmentation.utils.Kockanje;
import si.uni_lj.fri.segmentation.utils.MarchingCubes;

public class ModelCreatorJava {

	public static Object[] createModel(String fileName, double sigma, double threshold, int recursion, int funkcija) {
		float[][][] ctMatrix = FileUtils.readFile3D(fileName);		
		double isolevel = execFindTreshold(ctMatrix, threshold);
		execNormalization(ctMatrix, execFindMax(ctMatrix));
		float[] vertices;
		
		long startTime = System.nanoTime();    
		// ... the code being measured ...
		
		if(funkcija == 1){
			execGauss(ctMatrix, sigma);
			vertices = execMarchingCubes(ctMatrix, isolevel);
		}else if(funkcija == 2){
			vertices = execKocke(ctMatrix, isolevel, recursion);
		}else if(funkcija == 3){
			execGauss(ctMatrix, sigma);
			vertices = execMarchingCubesRecursion(ctMatrix, isolevel, recursion);
		}else{
			if(recursion == -1){
				execGauss(ctMatrix, sigma);
				vertices = execMarchingCubes(ctMatrix, isolevel);
			}else{
				vertices = execKocke(ctMatrix, isolevel, recursion);
			}
		}
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println(estimatedTime + " nanoseconds.");
		//long seconds = TimeUnit.SECONDS.convert(estimatedTime, TimeUnit.NANOSECONDS);
		//System.out.println(seconds + " seconds.");
		
		//zamenjaj vse caca z vertices
		//float[] caca = Arrays.copyOfRange(vertices, 0, 90);
		System.exit(0);
		
		int[] nTriangles = new int[] { vertices.length / 9 };		
		return new Object[] { IntBuffer.wrap(nTriangles), FloatBuffer.wrap(vertices), 0, (float) isolevel };
	}

	private static void execNormalization(float[][][] ctMatrix, double max) {
		System.out.println("Values normalization...");
		for (int i = 0; i < ctMatrix.length; i++) {
			for (int j = 0; j < ctMatrix[0].length; j++) {
				for (int k = 0; k < ctMatrix[0][0].length; k++) {
					ctMatrix[i][j][k] /= max;
				}
			}
		}
	}

	private static void execGauss(float[][][] ctMatrix, double sigma) {
		System.out.println("Gauss Java implementation...");
		if (sigma > 0)
			Gauss3D.gauss3D(ctMatrix, sigma);
	}

	private static double execFindMax(float[][][] ctMatrix) {
		double max = 0;
		for (int i = 0; i < ctMatrix.length; i++) {
			for (int j = 0; j < ctMatrix[0].length; j++) {
				for (int k = 0; k < ctMatrix[0][0].length; k++) {
					if (ctMatrix[i][j][k] > max)
						max = ctMatrix[i][j][k];
				}
			}
		}
		return max;
	}

	private static double execFindTreshold(float[][][] ctMatrix, double threshold) {
		System.out.println("Thresholding...");
		if (threshold < 0) {
			return Graytresh.graytresh(ctMatrix)[0];
		} else {
			return threshold;
		}
	}

	private static float[] execMarchingCubes(float[][][] ctMatrix, double isolevel) {
		System.out.println("Marching cubes...");
		ArrayList<Float> vertices = MarchingCubes.marchingCubes(ctMatrix, (float) isolevel);
		float[] v = new float[vertices.size()];
		for (int i = 0; i < vertices.size(); i++)
			v[i] = vertices.get(i);
		return v;
	}
	
	private static float[] execMarchingCubesRecursion(float[][][] ctMatrix, double isolevel, int recursion) {
		System.out.println("Marching Cubes with recursion...");
		//ArrayList<Float> vertices = Kockanje.vseKocke(ctMatrix, (float) isolevel);
		ArrayList<Float> vertices = Kockanje.delneKockeMarching(ctMatrix, (float) isolevel, 60, recursion);
		float[] v = new float[vertices.size()];
		for (int i = 0; i < vertices.size(); i++)
			v[i] = vertices.get(i);
		return v;
	}
	
	private static float[] execKocke(float[][][] ctMatrix, double isolevel, int recursion) {
		System.out.println("Recursion...");
		//ArrayList<Float> vertices = Kockanje.vseKocke(ctMatrix, (float) isolevel);
		ArrayList<Float> vertices = Kockanje.delneKocke(ctMatrix, (float) isolevel, 80, recursion);
		float[] v = new float[vertices.size()];
		for (int i = 0; i < vertices.size(); i++)
			v[i] = vertices.get(i);
		return v;
	}
}
