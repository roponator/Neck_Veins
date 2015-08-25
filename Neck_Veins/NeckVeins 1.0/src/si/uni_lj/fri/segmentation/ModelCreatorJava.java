package si.uni_lj.fri.segmentation;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import si.uni_lj.fri.segmentation.utils.FileUtils;
import si.uni_lj.fri.segmentation.utils.Gauss3D;
import si.uni_lj.fri.segmentation.utils.Graytresh;
import si.uni_lj.fri.segmentation.utils.MarchingCubes;
import si.uni_lj.fri.veins3D.gui.NiftyScreenController;
import si.uni_lj.fri.veins3D.gui.render.models.MeshCreationInfo;
import si.uni_lj.fri.veins3D.main.VeinsWindow;

public class ModelCreatorJava {

	public static Object[] createModel(String fileName) 
	{
		// Create mesh info
		String fileNameOnly = MeshCreationInfo.GetFileNameOnlyFromPath(fileName);
		MeshCreationInfo.InfoMarchingCubes meshCreationInfo= new MeshCreationInfo.InfoMarchingCubes(fileNameOnly, VeinsWindow.settings.gaussSigma, VeinsWindow.settings.threshold);
			
		// check if the obj  file exists for this model params: if it does, return one output.
		File existingObjFile = new File(meshCreationInfo.GetObjFilePath());
		if(existingObjFile.exists())
		{
			System.out.println("createModel (CPU MARCHING CUBES): obj file exists, using obj file..");
			return new Object[]{meshCreationInfo.GetObjFilePath()};
		}
		NiftyScreenController.UpdateLoadingBarDialog("Creating model ( this may take a few minutes )...", 30.0f);
		VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();
				
		float[][][] ctMatrix = FileUtils.readFile3D(fileName);
		execGauss(ctMatrix, VeinsWindow.settings.gaussSigma);
		float threshold = 0.0019760127f;
		
		NiftyScreenController.UpdateLoadingBarDialog("Creating model ( this may take a few minutes )...", 40.0f);
		VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();
		
		double isolevel = execFindTreshold(ctMatrix, threshold);
		execNormalization(ctMatrix, 65536.0);
		
		NiftyScreenController.UpdateLoadingBarDialog("Creating model ( this may take a few minutes )...", 50.0f);
		VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();
		
		float[] vertices = execMarchingCubes(ctMatrix, isolevel);
		int[] nTriangles = new int[] { vertices.length / 9 };
		
		return new Object[] { IntBuffer.wrap(nTriangles), FloatBuffer.wrap(vertices), null, (float) isolevel, meshCreationInfo };
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
}
