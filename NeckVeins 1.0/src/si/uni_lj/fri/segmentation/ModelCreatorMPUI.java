package si.uni_lj.fri.segmentation;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

import si.uni_lj.fri.MPU_Implicits.MPUI;
import si.uni_lj.fri.segmentation.utils.FileUtils;
import si.uni_lj.fri.segmentation.utils.Gauss3D;
import si.uni_lj.fri.segmentation.utils.Graytresh;
import si.uni_lj.fri.segmentation.utils.MarchingCubes;
import si.uni_lj.fri.segmentation.utils.PointCloud;

public class ModelCreatorMPUI {

	public static Object[] createModel(String fileName, double sigma, double threshold) {
		float[][][] origMatrix = FileUtils.readFile3D(fileName);
		float[][][] ctMatrix = origMatrix;
		System.out.println(ctMatrix.length + " "+ctMatrix[0].length + " "+ctMatrix[0][0].length);
		float alpha = 1.4f;
		float lambda = 0.1f;
		float error = 0.005f;
		float res = 0.0025f;
		boolean cubes = true;
		boolean pointCloud = false;
		String s = "";

		

		double isolevel = execFindTreshold(ctMatrix, 0.0019760127);
		execNormalization(ctMatrix, 65536.0);
		execGauss(ctMatrix, sigma);
		
		//POINT CLOUD
		long startTime = System.nanoTime(); 
		
		PointCloud cloud = new PointCloud();
		cloud.createVertices(ctMatrix, (float)isolevel);
		
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Point Cloud calculated in: "+(float) duration / 1000000000f);
		
		float[] vertices = cloud.getVertices();
		float[] normals = cloud.getNormals();
	
		//MPUI + POLYGONIZATION
		if(!pointCloud && vertices.length > 10){
			s+="MPU alpha_"+Math.round(alpha * 100.0)+" error_"+Math.round(error*10000)+ " tresh_"+Math.round(threshold*10000)+ " res_"+Math.round(res*10000);
			if(cubes) s += " CUBES";
			else s+= "TETRA";

			MPUI mpu = new MPUI(alpha, lambda, error, cubes, res, vertices, normals, ctMatrix.length, ctMatrix[0].length, ctMatrix[0][0].length );
			vertices = mpu.getOutputVertices();
			normals = mpu.getOutputNormals();
		
		}else{
			
			s+="Point Cloud tresh_"+Math.round(threshold*10000);
			
		}
		int[] nTriangles = new int[] { vertices.length / 9 };
		return new Object[] { IntBuffer.wrap(nTriangles), FloatBuffer.wrap(vertices), normals, (float) isolevel, s };
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

	private static void execGauss(float[][][] ctMatrix, double sigma) {
		System.out.println("Gauss Java implementation...");
		if (sigma > 0)
			Gauss3D.gauss3D(ctMatrix, sigma);
	}
}
