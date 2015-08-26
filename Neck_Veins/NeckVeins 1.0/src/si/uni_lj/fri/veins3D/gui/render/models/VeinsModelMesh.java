/* Author of this file: Simon Žagar, 2012, Ljubljana
 * This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/
 * or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 */
package si.uni_lj.fri.veins3D.gui.render.models;

import si.uni_lj.fri.MPU_Implicits.Configuration;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import si.uni_lj.fri.segmentation.ModelCreator;
import si.uni_lj.fri.segmentation.ModelCreatorJava;
import si.uni_lj.fri.segmentation.ModelCreatorMPUI;
import si.uni_lj.fri.segmentation.utils.LabelUtil;
import si.uni_lj.fri.segmentation.utils.TrianglesLabelHelper;
import si.uni_lj.fri.segmentation.utils.obj.Triangle;
import si.uni_lj.fri.segmentation.utils.obj.Vertex;
import si.uni_lj.fri.veins3D.gui.NiftyScreenController;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer.ModelType;
import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.veins3D.math.Quaternion;
import si.uni_lj.fri.veins3D.math.Vector;
import si.uni_lj.fri.veins3D.utils.RayUtil;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Simon Žagar
 * @since 0.2
 * @version 0.2
 */
public class VeinsModelMesh extends VeinsModel
{
	private final int APLICATION_SUBDIVISION_LIMIT = 3;

	private TrianglesLabelHelper labelHelper;
	protected ArrayList<Float> vertices, normals;
	public ArrayList<Mesh> meshes;
	public double centerx, centery, centerz;
	public float maxX, maxY, maxZ;
	public float minX, minY, minZ;
	private float threshold = 0;
	private int maxTriangels = 0;
	private int numberOfSubdivisions = 0;
	private int maxSubDepth = 0;

	private Quaternion currentOrientation;
	private Quaternion addedOrientation;

	private double[] veinsGrabbedAt;
	private double veinsGrabRadius;

	public VeinsModelMesh()
	{
		System.out.println("STACK TRACE START ****************");
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
		    System.out.println(ste);
		}
		System.out.println("STACK TRACE END ****************");
				
		meshes = new ArrayList<Mesh>();
		setDefaultOrientation();
	}

	public VeinsModelMesh(String filepath)
	{
		constructVBOFromObjFile(filepath);
		setDefaultOrientation();
	}

	public VeinsModelMesh(String filepath, ModelType modelType, boolean useSafeMode) throws LWJGLException
	{
		constructVBOFromRawFile(filepath, modelType, useSafeMode);
		setDefaultOrientation();
	}

	public VeinsModelMesh(double threshold, Quaternion currentQuaternion) throws LWJGLException
	{
		changeThreshold(threshold);
		this.currentOrientation = currentQuaternion;
		this.addedOrientation = new Quaternion();
	}

	public void SetNewResolution(int width, int height)
	{
		// not needed for this one, needed only for volume renderer
	}

	public void changeThreshold(double threshold) throws LWJGLException
	{
		Object[] output = ModelCreator.changeModel(threshold);
		constructVBO(output);
	}

	public void changeMinTriangles(int min)
	{
		for (Mesh mesh : meshes)
		{
			mesh.deleteVBO();
		}
		boolean[] labels = LabelUtil.getValidLabels(maxTriangels, min, labelHelper);
		meshes = new ArrayList<Mesh>();
		ArrayList<Integer> tempFaces = new ArrayList<Integer>();
		int tempFaceCount = 0;
		for (Triangle t : labelHelper.getTriangles())
		{
			if (labels[t.label])
			{
				tempFaces.add(t.v3.index);
				tempFaces.add(t.v2.index);
				tempFaces.add(t.v1.index);
				tempFaceCount++;
			}
		}

		if (tempFaceCount > 0)
		{
			Mesh mesh = new Mesh(new ArrayList<String>(), tempFaces, vertices, null);
			meshes.add(mesh);
			System.out.println("Created a new mesh java object that will have it's own VBO.");
		}

		for (Mesh mesh : meshes)
		{
			mesh.constructVBO(true);
		}
	}


	/**
	 * Normals calculated on GPU are currently not used, instead Normals calculated in constructVBO are used.
	 * 
	 * @param filepath
	 */
	public void constructVBOFromRawFile(String filepath, ModelType modelType, boolean useSafeMode) throws LWJGLException
	{
		Object[] output = null;
		
		switch (modelType)
		{
		case MPUI:
		{
			output = ModelCreatorMPUI.createModel(filepath);
			
			NiftyScreenController.UpdateLoadingBarDialog("Loading model...", 70.0f);
			VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();
			
			if (output.length != 1)
				constructVBOPointCloud(output); // generate mesh from raw file
			else
				constructVBOFromObjFile((String) output[0]); // load existing obj
		}
			break;

		case MARCHING_CUBES:
		{
			if (useSafeMode == false) // use GPU marching cubes if no safe mode, otherwise CPU
				output = ModelCreator.createModel(filepath);
			else
				output = ModelCreatorJava.createModel(filepath);

			NiftyScreenController.UpdateLoadingBarDialog("Loading model...", 70.0f);
			VeinsWindow.veinsWindow.RenderSingleFrameWithoutModel();
			
			if (output.length != 1)
				constructVBO(output); // generate mesh from raw file
			else
				constructVBOFromObjFile((String) output[0]); // load existing obj
		}
			break;

		case VOLUME_RENDER:
		{
			System.out.println("ERROR: constructVBOFromRawFile: VOLUME_RENDER: you must use the VeinsModelRaycaseVolume and not VeinsModelMesh for this");
		}
			break;

		default:
			System.out.println("ERROR: constructVBOFromRawFile: invalid modelType: " + modelType.toString());
		}

	}

	public static float[] g_voxelPositions = null;
	public static float[] g_voxelNormals = null;

	private void constructVBOPointCloud(Object[] output)
	{

		IntBuffer nTrianglesBuff = (IntBuffer) output[0];
		FloatBuffer trianglesBuff = (FloatBuffer) output[1];
		MeshCreationInfo.MeshInfo meshCreationInfo = (MeshCreationInfo.MeshInfo) output[4];

		g_voxelPositions = (float[]) output[5];
		g_voxelNormals = (float[]) output[6];

		float[] normals = null;
		normals = (float[]) output[2];
		labelHelper = new TrianglesLabelHelper(nTrianglesBuff.get(0));
		if (normals != null)
		{
			normals = LabelUtil.createVertexNormalList(nTrianglesBuff.get(0), trianglesBuff, labelHelper, normals);
		}
		else
			LabelUtil.createVertexList(nTrianglesBuff.get(0), trianglesBuff, labelHelper);

		this.threshold = (Float) output[3];

		this.maxTriangels = nTrianglesBuff.get(0);

		vertices = new ArrayList<Float>();

		centerx = 0;
		centery = 0;
		centerz = 0;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxZ = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minZ = Float.MAX_VALUE;

		float x, y, z;

		// meshes variables
		meshes = new ArrayList<Mesh>();
		ArrayList<Integer> tempFaces = new ArrayList<Integer>();
		ArrayList<String> groups = new ArrayList<String>();
		int tempFaceCount = 0;

		for (Vertex v : labelHelper.getVertTriMap().keySet())
		{
			vertices.add(x = v.y);
			vertices.add(y = v.x);
			vertices.add(z = v.z);

			centerx += x;
			centery += y;
			centerz += z;
			if (x < minX)
				minX = x;
			if (y < minY)
				minY = y;
			if (z < minZ)
				minZ = z;
			if (x > maxX)
				maxX = x;
			if (y > maxY)
				maxY = y;
			if (z > maxZ)
				maxZ = z;
		}

		for (Triangle t : labelHelper.getTriangles())
		{

			tempFaces.add(t.v2.index);
			tempFaces.add(t.v3.index);
			tempFaces.add(t.v1.index);
			tempFaceCount++;

		}

		if (tempFaceCount > 0)
		{
			Mesh mesh = new Mesh(groups, tempFaces, vertices, normals);

			meshes.add(mesh);
			System.out.println("Created a new mesh java object that will have it's own VBO.");
		}
		else
		{
			System.out.println("One \"g\" holding 0 elements discarted.");
		}

		centerx /= (vertices.size() / 3);
		centery /= (vertices.size() / 3);
		centerz /= (vertices.size() / 3);

		for (Mesh mesh : meshes)
		{
			mesh.SetMeshCreationInfo(meshCreationInfo);
			mesh.constructVBO(true);
		}

	}

	/*
	 * void drawMeshNormals() { glBegin(GL_LINES); float scale = 0.5f; for(int i=0;i<g_voxelNormals.length/3;++i) { float xOffset = 15.0f; glVertex3f(g_voxelPositions[i*3]+xOffset,g_voxelPositions[i*3+1],g_voxelPositions[i*3+2]);
	 * glVertex3f(g_voxelPositions[i*3]+g_voxelNormals[i*3]*scale+xOffset,g_voxelPositions[i*3+1]+g_voxelNormals[i*3+1]*scale,g_voxelPositions[i*3+2]+g_voxelNormals[i*3+2]*scale); } glEnd(); }
	 */

	private void constructVBO(Object[] output)
	{
		IntBuffer nTrianglesBuff = (IntBuffer) output[0];
		FloatBuffer trianglesBuff = (FloatBuffer) output[1];
		MeshCreationInfo.MeshInfo meshCreationInfo = (MeshCreationInfo.MeshInfo) output[4];

		labelHelper = new TrianglesLabelHelper(nTrianglesBuff.get(0));
		LabelUtil.createVertexList(nTrianglesBuff.get(0), trianglesBuff, labelHelper);
		this.threshold = (Float) output[3];
		this.maxTriangels = nTrianglesBuff.get(0);

		vertices = new ArrayList<Float>();
		centerx = 0;
		centery = 0;
		centerz = 0;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxZ = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minZ = Float.MAX_VALUE;

		float x, y, z;

		// meshes variables
		meshes = new ArrayList<Mesh>();
		ArrayList<Integer> tempFaces = new ArrayList<Integer>();
		ArrayList<String> groups = new ArrayList<String>();
		int tempFaceCount = 0;

		for (Vertex v : labelHelper.getVertTriMap().keySet())
		{
			vertices.add(x = v.x);
			vertices.add(y = v.y);
			vertices.add(z = v.z);
			centerx += x;
			centery += y;
			centerz += z;
			if (x < minX)
				minX = x;
			if (y < minY)
				minY = y;
			if (z < minZ)
				minZ = z;
			if (x > maxX)
				maxX = x;
			if (y > maxY)
				maxY = y;
			if (z > maxZ)
				maxZ = z;
		}
		for (Triangle t : labelHelper.getTriangles())
		{

			tempFaces.add(t.v3.index);
			tempFaces.add(t.v2.index);
			tempFaces.add(t.v1.index);
			tempFaceCount++;

		}

		if (tempFaceCount > 0)
		{
			Mesh mesh = new Mesh(groups, tempFaces, vertices, null);
			meshes.add(mesh);

			System.out.println("Created a new mesh java object that will have it's own VBO.");
		}
		else
		{
			System.out.println("One \"g\" holding 0 elements discarted.");
		}

		centerx /= (vertices.size() / 3);
		centery /= (vertices.size() / 3);
		centerz /= (vertices.size() / 3);

		for (Mesh mesh : meshes)
		{
			mesh.SetMeshCreationInfo(meshCreationInfo);
			mesh.constructVBO(true);
		}
	}

	public void constructVBOFromObjFile(String filepath)
	{
		vertices = new ArrayList<Float>();
		ArrayList<Float> normals = new ArrayList<Float>();
		centerx = 0;
		centery = 0;
		centerz = 0;
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxZ = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minZ = Float.MAX_VALUE;

		float x, y, z;

		// meshes variables
		meshes = new ArrayList<Mesh>();
		ArrayList<Integer> tempFaces = new ArrayList<Integer>();
		ArrayList<String> groups = new ArrayList<String>();
		boolean newG = false;
		int tempFaceCount = 0;

		File file = new File(filepath);
		Scanner scanner;
		try
		{
			scanner = new Scanner(file);
			String type;
			String line;
			while (scanner.hasNext())
			{
				line = scanner.nextLine();
				StringTokenizer strTokenizer = new StringTokenizer(line);
				type = strTokenizer.nextToken();
				if (type.equalsIgnoreCase("v"))
				{
					vertices.add(x = Float.parseFloat(strTokenizer.nextToken()));
					vertices.add(y = Float.parseFloat(strTokenizer.nextToken()));
					vertices.add(z = Float.parseFloat(strTokenizer.nextToken()));
					centerx += x;
					centery += y;
					centerz += z;
					if (x < minX)
						minX = x;
					if (y < minY)
						minY = y;
					if (z < minZ)
						minZ = z;
					if (x > maxX)
						maxX = x;
					if (y > maxY)
						maxY = y;
					if (z > maxZ)
						maxZ = z;
				}
				else if (type.equalsIgnoreCase("vn"))
				{
					normals.add(Float.parseFloat(strTokenizer.nextToken()));
					normals.add(Float.parseFloat(strTokenizer.nextToken()));
					normals.add(Float.parseFloat(strTokenizer.nextToken()));

				}
				else if (type.equalsIgnoreCase("f"))
				{
					int a, b, c;
					StringTokenizer tok = new StringTokenizer(strTokenizer.nextToken(), "//");
					a = Integer.parseInt(tok.nextToken());
					tok = new StringTokenizer(strTokenizer.nextToken(), "//");
					b = Integer.parseInt(tok.nextToken());
					tok = new StringTokenizer(strTokenizer.nextToken(), "//");
					c = Integer.parseInt(tok.nextToken());

					tempFaces.add(b);
					tempFaces.add(c);
					tempFaces.add(a);

					tempFaceCount++;
				}
				else if (type.equalsIgnoreCase("g"))
				{
					if (tempFaceCount > 0)
					{
						// It seems that since last starting a new group, there
						// have been faces stored
						// Here I create a new mesh
						float[] narray = new float[normals.size()];
						int counter = 0;
						for (Float f : normals)
						{
							narray[counter] = f;
							counter++;

						}

						Mesh mesh = new Mesh(groups, tempFaces, vertices, narray);
						meshes.add(mesh);
						// After the whole file will be read, each mesh object's
						// faces will be stored as VBOs (Vertex Buffer Objects).
						System.out.println("Created a new mesh java object that will have it's own VBO.");
					}
					else if (newG)
						System.out.println("One \"g\" holding 0 elements discarted.");
					// start a new group
					newG = true;
					groups = new ArrayList<String>();
					tempFaces = new ArrayList<Integer>();
					tempFaceCount = 0;
					while (strTokenizer.hasMoreTokens())
					{
						groups.add(strTokenizer.nextToken());
					}
				}
			}
			if (tempFaceCount > 0)
			{
				// It seems that since last starting a new group, there have
				// been faces stored
				// Here I create a new mesh
				float[] narray = new float[normals.size()];
				int counter = 0;
				for (Float f : normals)
				{
					narray[counter] = f;
					counter++;

				}
				Mesh mesh = new Mesh(groups, tempFaces, vertices, narray);
				meshes.add(mesh);
				// After the whole file will be read, each mesh object's faces
				// will be stored as VBOs (Vertex Buffer Objects).
				System.out.println("Created a new mesh java object that will have it's own VBO.");
			}
			else
			{
				System.out.println("One \"g\" holding 0 elements discarted.");
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		// The file has been read.
		centerx /= (vertices.size() / 3);
		centery /= (vertices.size() / 3);
		centerz /= (vertices.size() / 3);

		for (Mesh mesh : meshes)
		{
			mesh.constructVBO(false); // don't save obj to file if it's created from obj
		}
	}

	@Override
	public void increaseSubdivisionDepth()
	{
		numberOfSubdivisions = Math.min(APLICATION_SUBDIVISION_LIMIT, numberOfSubdivisions + 1);
		if (maxSubDepth < numberOfSubdivisions)
		{
			for (Mesh mesh : meshes)
				mesh.increaseMaxSubdivision();
			maxSubDepth++;
		}
	}

	@Override
	public void decreaseSubdivisionDepth()
	{
		numberOfSubdivisions = Math.max(0, numberOfSubdivisions - 1);
	}

	@Override
	public float GetThreshold()
	{
		return threshold;
	}

	@Override
	public void render()
	{
		
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();

		/* Apply orientation (add rotations) */
		// Quaternion compositeOrientation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
		// FloatBuffer fb = compositeOrientation.getRotationMatrix(false);
		// GL11.glMultMatrix(fb);

		/* Translate and render */
		// glTranslatef(-(float) centerx, -(float) centery, -(float) centerz);
		for (Mesh vbo : meshes)
		{

			vbo.render(numberOfSubdivisions);
		}

		glPopMatrix();
		// drawMeshNormals();
	}

	/**
	 * Change added orientation (rotations) of the model
	 */
	@Override
	public void changeAddedOrientation(VeinsRenderer renderer)
	{
		double[] veinsHeldAt = RayUtil.getRaySphereIntersection(Mouse.getX(), Mouse.getY(), renderer);

		if (veinsHeldAt != null)
		{
			double[] rotationAxis = Vector.crossProduct(veinsGrabbedAt, veinsHeldAt);
			if (Vector.length(rotationAxis) > 0)
			{
				rotationAxis = Vector.normalize(rotationAxis);
				rotationAxis = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(rotationAxis);
				double angle = Math.acos(Vector.dotProduct(veinsGrabbedAt, veinsHeldAt) / (Vector.length(veinsGrabbedAt) * Vector.length(veinsHeldAt)));
				addedOrientation = Quaternion.quaternionFromAngleAndRotationAxis(angle, rotationAxis);
			}
		}
	}

	@Override
	public Quaternion GetCurrentOrientation()
	{
		return currentOrientation;
	}

	private void setDefaultOrientation()
	{
		/*
		 * double angle1 = Math.toRadians(-90); // Math.PI * -90 / 180; double angle2 = Math.toRadians(180); // Math.PI * 180 / 180; currentOrientation = Quaternion.quaternionFromAngleAndRotationAxis(0, new double[] { 1, 0, 0 }); // double[] v =
		 * Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(new double[] { 0, 1, 0 }); // currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, // Quaternion.quaternionFromAngleAndRotationAxis(angle2, v)); addedOrientation = new Quaternion();
		 */

		currentOrientation = computeDefaultOrientation();
		addedOrientation = new Quaternion();

	}

	@Override
	public int GetMaxTriangles()
	{
		return maxTriangels;
	}

	@Override
	public void normalizeCurrentOrientation()
	{
		currentOrientation = Quaternion.quaternionNormalization(currentOrientation);
	}

	@Override
	public void normalizeAddedOrientation()
	{
		addedOrientation = Quaternion.quaternionNormalization(addedOrientation);
	}

	/**
	 * Saves the current orientation of the model in currenOrientation
	 */
	@Override
	public void saveCurrentOrientation()
	{
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
	}

	@Override
	public void setAddedOrientation(Quaternion q)
	{
		addedOrientation = q;
	}

	private void setCurrentOrientation(Quaternion q)
	{
		currentOrientation = q;
	}

	@Override
	public void cleanup()
	{
		if (meshes != null)
		{
			for (Mesh m : meshes)
			{
				m.deleteVBO();
			}
		}
		meshes.clear();
	}

	@Override
	public void rotateModel3D(double[] rot, VeinsRenderer renderer)
	{

		double[] centerVector = RayUtil.getRayDirection((int) VeinsWindow.settings.resWidth / 2, (int) VeinsWindow.settings.resHeight / 2, renderer);

		Quaternion temp = new Quaternion();
		double[] rotationAxis;

		rotationAxis = Vector.crossProduct(centerVector, new double[]
		{ 0, 1, 0 });
		rotationAxis = Vector.normalize(rotationAxis);
		rotationAxis = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(rotationAxis);

		temp = Quaternion.quaternionFromAngleAndRotationAxis(rot[0], rotationAxis);
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, temp);

		rotationAxis = Vector.crossProduct(centerVector, new double[]
		{ 1, 0, 0 });
		rotationAxis = Vector.normalize(rotationAxis);
		rotationAxis = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(rotationAxis);

		temp = Quaternion.quaternionFromAngleAndRotationAxis(rot[2], rotationAxis);
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, temp);

		rotationAxis = Vector.crossProduct(centerVector, new double[]
		{ 0, 0, 1 });
		rotationAxis = Vector.normalize(centerVector);
		rotationAxis = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(rotationAxis);

		temp = Quaternion.quaternionFromAngleAndRotationAxis(-rot[1], rotationAxis);
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, temp);

	}

	@Override
	public double GetVeinsGrabRadius()
	{
		return veinsGrabRadius;
	}

	@Override
	public void SetVeinsGrabRadius(double r)
	{
		veinsGrabRadius = r;
	}

	@Override
	public double[] GetVeinsGrabbedAt()
	{
		return veinsGrabbedAt;
	}

	@Override
	public void SetVeinsGrabbedAt(double[] v)
	{
		veinsGrabbedAt = v;
	}

	@Override
	public double calculateCameraDistance()
	{
		double d1 = minX - centerx;
		double d2 = maxX - centerx;
		double d3 = minY - centery;
		double d4 = maxY - centery;
		double d5 = minZ - centerz;
		double d6 = maxZ - centerz;
		d1 *= d1;
		d2 *= d2;
		d3 *= d3;
		d4 *= d4;
		d5 *= d5;
		d6 *= d6;
		d1 = Math.max(d1, d2);
		d2 = Math.max(d3, d4);
		d3 = Math.max(d5, d6);
		d1 = Math.sqrt(Math.max(Math.max(d1 + d2, d2 + d3), d1 + d3));

		return d1;
	}

}
