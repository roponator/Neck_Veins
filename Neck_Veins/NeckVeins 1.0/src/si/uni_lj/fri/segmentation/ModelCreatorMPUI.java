package si.uni_lj.fri.segmentation;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

import si.uni_lj.fri.MPU_Implicits.MPUI;
import si.uni_lj.fri.segmentation.utils.FileUtils;
import si.uni_lj.fri.segmentation.utils.Gauss3D;
import si.uni_lj.fri.segmentation.utils.Graytresh;
import si.uni_lj.fri.segmentation.utils.MarchingCubes;
import si.uni_lj.fri.segmentation.utils.PointCloud;
import si.uni_lj.fri.veins3D.gui.NiftyScreenController;
import si.uni_lj.fri.veins3D.gui.render.models.MeshCreationInfo;
import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.MPU_Implicits.Configuration;

public class ModelCreatorMPUI
{

	// TODO: REMOVE
	public static float[][][] testMatrix()
	{
		int dim = 20;
		float[][][] m = new float[20][20][20];

		for (int z = 0; z < dim; ++z)
			for (int y = 0; y < dim; ++y)
				for (int x = 0; x < dim; ++x)
				{
					float dx = (float) x - 10.0f;
					float dy = (float) y - 10.0f;
					float dz = (float) z - 10.0f;
					float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

					if (dist > 5.0f)
						m[z][y][x] = 0.0f;
					else
						m[z][y][x] = 100.0f;
				}

		return m;
	}

	// Returns only one String in output if the obj file with these presets already exists.
	public static Object[] createModel(String fileName)
	{
		// Create mesh info
		String fileNameOnly = MeshCreationInfo.GetFileNameOnlyFromPath(fileName);
		MeshCreationInfo.InfoMPUI meshCreationInfo = new MeshCreationInfo.InfoMPUI(fileNameOnly, VeinsWindow.settings.gaussSigma, VeinsWindow.settings.threshold, VeinsWindow.settings.MPUI__APLHA, Configuration.__LAMBDA, VeinsWindow.settings.MPUI__ERROR, VeinsWindow.settings.MPUI__RESOLUTION, VeinsWindow.settings.MPUI__POINT_CLOUD);

		// check if the obj file exists for this model params: if it does, return one output.
		File existingObjFile = new File(meshCreationInfo.GetObjFilePath());
		if (existingObjFile.exists())
		{
			System.out.println("createModel (MPUI): obj file exists, using obj file..");
			return new Object[]
			{ meshCreationInfo.GetObjFilePath() };
		}

		System.out.println("createModel (MPUI)...");
		float[][][] ctMatrix = FileUtils.readFile3D(fileName);
		 //float[][][] ctMatrix = testMatrix();
		System.out.println(ctMatrix.length + " " + ctMatrix[0].length + " " + ctMatrix[0][0].length);
		float alpha = VeinsWindow.settings.MPUI__APLHA;
		float lambda = Configuration.__LAMBDA;
		float error = VeinsWindow.settings.MPUI__ERROR;
		float res = VeinsWindow.settings.MPUI__RESOLUTION;
		boolean cubes = true;
		boolean pointCloud = VeinsWindow.settings.MPUI__POINT_CLOUD;
		// String s = "";

		// TODO: UNCOMMENT
		/*
		 * double isolevel = execFindTreshold(ctMatrix, 0.0019760127); execNormalization(ctMatrix, 65536.0); execGauss(ctMatrix, sigma);
		 */
		double isolevel = 10.0;

		// POINT CLOUD
		long startTime = System.nanoTime();

		PointCloud cloud = new PointCloud();
		cloud.createVertices(ctMatrix, (float) isolevel);

		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Point Cloud calculated in: " + (float) duration / 1000000000f);

		float[] voxelVertices = cloud.getVertices();
		float[] voxelNormals = cloud.getNormals();

		float[] mpuiMeshVertices = null;
		float[] mpuiMeshNormals = null;

		// MPUI + POLYGONIZATION
		if (!pointCloud && voxelVertices.length > 10)
		{
			// s+="MPU alpha_"+Math.round(alpha * 100.0)+" error_"+Math.round(error*10000)+ " tresh_"+Math.round(threshold*10000)+ " res_"+Math.round(res*10000);
			// if(cubes) s += " CUBES";
			// else s+= "TETRA";

			MPUI mpu = new MPUI(alpha, lambda, error, cubes, res, voxelVertices, voxelNormals, ctMatrix.length, ctMatrix[0].length, ctMatrix[0][0].length);
			mpuiMeshVertices = mpu.getOutputVertices();
			mpuiMeshNormals = mpu.getOutputNormals();

		}
		else
		{

			// s+="Point Cloud tresh_"+Math.round(threshold*10000);
		}

		int[] nTriangles = new int[]
		{ mpuiMeshVertices.length / 9 };
		return new Object[]
		{ IntBuffer.wrap(nTriangles), FloatBuffer.wrap(mpuiMeshVertices), mpuiMeshNormals, (float) isolevel, meshCreationInfo, voxelVertices, voxelNormals };
	}

	private static void execNormalization(float[][][] ctMatrix, double max)
	{
		System.out.println("Values normalization...");
		for (int i = 0; i < ctMatrix.length; i++)
		{
			for (int j = 0; j < ctMatrix[0].length; j++)
			{
				for (int k = 0; k < ctMatrix[0][0].length; k++)
				{
					ctMatrix[i][j][k] /= max;
				}
			}
		}
	}

	private static double execFindMax(float[][][] ctMatrix)
	{
		double max = 0;
		for (int i = 0; i < ctMatrix.length; i++)
		{
			for (int j = 0; j < ctMatrix[0].length; j++)
			{
				for (int k = 0; k < ctMatrix[0][0].length; k++)
				{
					if (ctMatrix[i][j][k] > max)
						max = ctMatrix[i][j][k];
				}
			}
		}
		return max;
	}

	private static double execFindTreshold(float[][][] ctMatrix, double threshold)
	{
		System.out.println("Thresholding...");
		if (threshold < 0)
		{
			return Graytresh.graytresh(ctMatrix)[0];
		}
		else
		{
			return threshold;
		}
	}

	private static void execGauss(float[][][] ctMatrix, double sigma)
	{
		System.out.println("Gauss Java implementation...");
		if (sigma > 0)
			Gauss3D.gauss3D(ctMatrix, sigma);
	}
}
