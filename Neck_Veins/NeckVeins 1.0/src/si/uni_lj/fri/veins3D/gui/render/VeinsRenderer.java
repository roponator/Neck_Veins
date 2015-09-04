package si.uni_lj.fri.veins3D.gui.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glValidateProgram;
import static org.lwjgl.util.glu.Project.*;
import static si.uni_lj.fri.veins3D.utils.Tools.allocFloats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;

import si.uni_lj.fri.veins3D.exceptions.ShaderLoadException;
import si.uni_lj.fri.veins3D.gui.render.models.VeinsModel;
import si.uni_lj.fri.veins3D.gui.render.models.VeinsModelMesh;
import si.uni_lj.fri.veins3D.gui.render.models.VeinsModelRaycastVolume;
import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.veins3D.math.Quaternion;

public class VeinsRenderer
{
	public static final int FIXED_PIPELINE = -1;
	public static final int SIMPLE_SHADER = 0;
	public static final int SIMPLE_SHADER_NORM_INTERP = 1;
	public static final int SIMPLE_SHADER_NORM_INTERP_AMBIENT_L = 2;
	public static final int SIMPLE_SHADER_NORM_INTERP_AMBIENT_L_SPEC_BLINN_PHONG = 3;
	public static final int SIMPLE_SHADER_NORM_INTERP_AMBIENT_L_SPEC_PHONG = 4;
	public static final int SHADER_6 = 5;
	public static final int SHADER_7 = 6;
	public static final int SHADER_8 = 7;
	public static final int NUMBER_OF_SHADER_PROGRAMS = 8;
	public static final float FOV_Y = 90;
	public static final float Z_NEAR = 10;
	public static final float Z_FAR = 10000;

	private int activeShaderProgram;
	private boolean isWireframeOn;
	private boolean isAAEnabled;

	public boolean WasLastModelLoadedFromObj = false;
	public ModelType LastLoadedModelType = ModelType.VOLUME_RENDER;

	private Camera cam;

	public VeinsModel veinsModel; // TODO: CHANGE TO PRIVATE
	public boolean isOpen = false;
	public double[] screenPlaneInitialUpperLeft = new double[3];
	public double[] screenPlaneInitialUpperRight = new double[3];
	public double[] screenPlaneInitialLowerLeft = new double[3];
	public double[] screenPlaneInitialLowerRight = new double[3];

	private int[] shaderPrograms;
	private int[] vertexShaders;
	private int[] fragmentShaders;

	public enum ModelType
	{
		MARCHING_CUBES, MPUI, VOLUME_RENDER
	}

	public VeinsRenderer() throws LWJGLException
	{

		cam = new Camera();
		activeShaderProgram = 4;
		isWireframeOn = false;
		isAAEnabled = true;
	}

	public void SetNewResolution(int width, int height)
	{
		if (veinsModel != null)
			veinsModel.SetNewResolution(width, height); // needed for volume renderer
	}

	/**
	 * @since 0.1
	 * @version 0.2
	 */
	public void setupView()
	{
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		glViewport(0, 0, VeinsWindow.settings.resWidth, VeinsWindow.settings.resHeight);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(FOV_Y, VeinsWindow.settings.resWidth / (float) VeinsWindow.settings.resHeight, Z_NEAR, Z_FAR);
		glShadeModel(GL_SMOOTH);
		setCameraAndLight(0);

	}

	/**
	 * @since 0.1
	 * @version 0.1
	 */
	public void clearView()
	{
		glClearColor(200.0f / 255.0f, 199.0f / 255.0f, 199.0f / 255.0f, 0.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	/**
	 * @since 0.1
	 * @version 0.4
	 */
	public void render()
	{

		if (VeinsWindow.settings.stereoEnabled)
		{
			float offset = VeinsWindow.settings.stereoValue / 10f;
			StencilMask.initStencil();
			glStencilFunc(GL_EQUAL, 0, 0x01);
			setCameraAndLight(offset);
			renderVeins();
			glStencilFunc(GL_EQUAL, 1, 0x01);
			setCameraAndLight(-offset);
			renderVeins();
			glDisable(GL_STENCIL_TEST);
		}
		else
		{
			setCameraAndLight(0);
			renderVeins();
		}
	}

	private void renderVeins()
	{
		if (veinsModel != null)
		{
			glMatrixMode(GL_MODELVIEW);
			glPushMatrix();

			if (activeShaderProgram == -1)
			{
				GL20.glUseProgram(0);
			}
			else
			{
				GL20.glUseProgram(shaderPrograms[activeShaderProgram]);
				int myUniformLocation = glGetUniformLocation(shaderPrograms[activeShaderProgram], "bloodColor");
				glUniform4f(myUniformLocation, 0.8f, 0.06667f, 0.0f, 1);
			}

			glEnable(GL_LIGHTING);
			glColor4f(0.8f, 0.06667f, 0.0f, 1);
			glMaterial(GL_FRONT, GL_AMBIENT, allocFloats(new float[]
			{ 0.8f, 0.06667f, 0.0f, 1 }));
			glMaterial(GL_FRONT, GL_DIFFUSE, allocFloats(new float[]
			{ 0.8f, 0.06667f, 0.0f, 1 }));
			glMaterial(GL_FRONT, GL_SPECULAR, allocFloats(new float[]
			{ 0.66f, 0.66f, 0.66f, 1f }));
			glMaterial(GL_FRONT, GL_SHININESS, allocFloats(new float[]
			{ 100f, 256.0f, 256.0f, 256.0f }));

			veinsModel.render(cam);

			GL20.glUseProgram(0);
			glPopMatrix();

		}

	}

	/**
	 * @since 0.1
	 * @version 0.1
	 */
	private void setCameraAndLight(float offset)
	{
	/*	double v[] = new double[]
		{ offset, 0, 0 };

		v = cam.cameraOrientation.rotateVector3d(v);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		float posX = 53.220627f;
		float posY = 142.01552f;
		float posZ = 16.375349f;
		float centerX = posX + 0.43148643f;
		float centerY = posY - 0.6955627f;
		float centerZ = posZ + 0.5744667f;
		float upX = 0.41773185f;
		float upY = 0.7184654f;
		float upZ = 0.5561543f;

		Quaternion worldOrientation = Quaternion.quaternionReciprocal(cam.cameraOrientation);
		glMultMatrix(worldOrientation.getRotationMatrix(false));

		glTranslatef(-cam.cameraX + (float) v[0], -cam.cameraY + (float) v[1], -cam.cameraZ + (float) v[2]);

		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glLight(GL_LIGHT0, GL_POSITION, allocFloats(new float[]
		{ -(centerX - posX) * 1000f, -(centerY - posY) * 1000f, -(centerZ - posZ) * 1000f, 1.0f }));
		glLight(GL_LIGHT0, GL_DIFFUSE, allocFloats(new float[]
		{ 1f, 1f, 1f, 1 }));
		glLight(GL_LIGHT0, GL_AMBIENT, allocFloats(new float[]
		{ 0.15f, 0.15f, 0.15f, 1 }));
		glLight(GL_LIGHT0, GL_SPECULAR, allocFloats(new float[]
		{ 1.0f, 1.0f, 1.0f, 1.0f }));
		gluLookAt(posX, posY, posZ, centerX, centerY, centerZ, upX, upY, upZ);*/
		
		double v[] = new double[] { offset, 0, 0 };
		v = cam.cameraOrientation.rotateVector3d(v);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		Quaternion worldOrientation = Quaternion.quaternionReciprocal(cam.cameraOrientation);
		glMultMatrix(worldOrientation.getRotationMatrix(false));
		glTranslatef(-cam.cameraX + (float) v[0], -cam.cameraY + (float) v[1], -cam.cameraZ + (float) v[2]);

		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glLight(GL_LIGHT0, GL_POSITION, allocFloats(new float[] { 0.0f, 1000.0f, 0.0f, 0.0f }));
		glLight(GL_LIGHT0, GL_DIFFUSE, allocFloats(new float[] { 1f, 1f, 1f, 1 }));
		glLight(GL_LIGHT0, GL_AMBIENT, allocFloats(new float[] { 0.3f, 0.3f, 0.3f, 1 }));
		glLight(GL_LIGHT0, GL_SPECULAR, allocFloats(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }));

	}

	/**
	 * 
	 * @throws ShaderLoadException
	 */
	public void prepareShaders() throws ShaderLoadException
	{
		shaderPrograms = new int[NUMBER_OF_SHADER_PROGRAMS];
		vertexShaders = new int[NUMBER_OF_SHADER_PROGRAMS];
		fragmentShaders = new int[NUMBER_OF_SHADER_PROGRAMS];

		String path = "/shaders/";
		for (int i = 0; i < NUMBER_OF_SHADER_PROGRAMS; i++)
		{
			shaderPrograms[i] = GL20.glCreateProgram();
			vertexShaders[i] = glCreateShader(GL_VERTEX_SHADER);
			fragmentShaders[i] = glCreateShader(GL_FRAGMENT_SHADER);
			StringBuilder vertexShaderSource = new StringBuilder();
			StringBuilder fragmentShaderSource = new StringBuilder();

			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(VeinsWindow.class.getResourceAsStream(path + "shader" + i + ".vert")));
				String line;
				while ((line = reader.readLine()) != null)
				{
					vertexShaderSource.append(line).append("\n");
				}
				reader.close();
			}
			catch (IOException e)
			{
				System.err.println("Vertex shader" + i + " wasn't loaded properly.");
				throw new ShaderLoadException("Vertex shader" + i + " wasn't loaded properly.");
			}

			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(VeinsRenderer.class.getResourceAsStream(path + "shader" + i + ".frag")));
				String line;
				while ((line = reader.readLine()) != null)
				{
					fragmentShaderSource.append(line).append("\n");
				}
				reader.close();
			}
			catch (IOException e)
			{
				System.err.println("Fragment shader" + i + " wasn't loaded properly.");
				throw new ShaderLoadException("Fragment shader" + i + " wasn't loaded properly.");
			}

			glShaderSource(vertexShaders[i], vertexShaderSource);
			glCompileShader(vertexShaders[i]);
			if (glGetShader(vertexShaders[i], GL_COMPILE_STATUS) == GL_FALSE)
			{
				System.err.println("Vertex shader" + i + " not compiled correctly");
			}

			glShaderSource(fragmentShaders[i], fragmentShaderSource);
			glCompileShader(fragmentShaders[i]);
			if (glGetShader(fragmentShaders[i], GL_COMPILE_STATUS) == GL_FALSE)
			{
				System.err.println("Fragment shader" + i + " not compiled correctly");
			}

			glAttachShader(shaderPrograms[i], vertexShaders[i]);
			glAttachShader(shaderPrograms[i], fragmentShaders[i]);
			glLinkProgram(shaderPrograms[i]);
			glValidateProgram(shaderPrograms[i]);
			glDetachShader(shaderPrograms[i], vertexShaders[i]); // must unbind all, otherwise raycaster tries to use it and renders nothing
			glDetachShader(shaderPrograms[i], fragmentShaders[i]);

			// DONT PRINT CAUSES CRASH, NOT SURE WHY, STARTED HAPPENING WHEN SWITCHED TO NIFTY,
			// MAYBE BECAUSE SHADERS ARE DETACHED
			// System.out.println("Vertex shader" + i + " info: " + glGetShaderInfoLog(vertexShaders[i], 999));
			// System.out.println("Fragment shader" + i + " info: " + glGetShaderInfoLog(fragmentShaders[i], 999));
			// System.out.println("Shader program" + i + " info: " + glGetShaderInfoLog(shaderPrograms[i], 999));
		}
	}

	public void cleanShaders()
	{
		for (int i = 0; i < NUMBER_OF_SHADER_PROGRAMS; i++)
		{
			//glDetachShader(shaderPrograms[i], vertexShaders[i]);
			glDeleteShader(vertexShaders[i]);
			//glDetachShader(shaderPrograms[i], fragmentShaders[i]);
			glDeleteShader(fragmentShaders[i]);
			glDeleteProgram(shaderPrograms[i]);
		}
	}

	/**
	 * Loads obj file
	 * 
	 * @param fileName
	 * @throws LWJGLException
	 */
	public void loadModelObj(String fileName)
	{
		if (veinsModel != null)
			veinsModel.cleanup();
		veinsModel = new VeinsModelMesh();
		VeinsModelMesh veinsModelMesh = (VeinsModelMesh) veinsModel;
		veinsModelMesh.constructVBOFromObjFile(fileName);
		setDefaultViewOptions();
		isOpen = true;
		WasLastModelLoadedFromObj = true;
	}

	/**
	 * Loads Mhd with raw file using OpenCL
	 * 
	 * @param fileName
	 * @param sigma
	 * @param threshold
	 * @throws LWJGLException
	 */
	public void loadModelRaw(String fileName, ModelType modelType, boolean useSafeMode) throws LWJGLException
	{
		if (veinsModel != null)
			veinsModel.cleanup();

		LastLoadedModelType = modelType;
		WasLastModelLoadedFromObj = false;

		// create either the volume model or the mesh model (mpui, marching cubes)
		if (modelType == ModelType.VOLUME_RENDER)
		{
			veinsModel = new VeinsModelRaycastVolume(VeinsWindow.settings.resWidth, VeinsWindow.settings.resHeight,fileName);
		}
		else
		{
			veinsModel = new VeinsModelMesh();
			VeinsModelMesh veinsModelMesh = (VeinsModelMesh) veinsModel;
			WasLastModelLoadedFromObj = veinsModelMesh.constructVBOFromRawFile(fileName, modelType, useSafeMode);
		}

		setDefaultViewOptions();
		isOpen = true;
	}

	/*
	 * public void ChangeMinTriangles(int min) throws LWJGLException { if(veinsModel instanceof VeinsModelMesh) { veinsModel.cleanup(); veinsModel = new VeinsModelMesh(threshold, veinsModel.GetCurrentOrientation()); VeinsModelMesh veinsModelMesh = (VeinsModelMesh)veinsModel; double d =
	 * veinsModel.calculateCameraDistance(); veinsModelMesh.SetVeinsGrabRadius(d / Math.sqrt(2)); } }
	 */

	private void setDefaultViewOptions()
	{
		double fovMin = (VeinsWindow.settings.resWidth < VeinsWindow.settings.resHeight) ? FOV_Y * VeinsWindow.settings.resWidth / (double) VeinsWindow.settings.resHeight : FOV_Y;
		fovMin = Math.toRadians(fovMin); // Math.PI * fovMin / 180

		double d = veinsModel.calculateCameraDistance();
		veinsModel.SetVeinsGrabRadius(d / Math.sqrt(2));
		resetCameraPositionAndOrientation();
		setScreenPlanes(d, fovMin);
	}

	/**
	 * Calculate the appropriate camera distance: The following code takes the most extreme values on each coordinate of all the specified vertices in the VeinsModel (from .obj file). It uses the bigger distance (of two) from the average location on each axis to calculate the radius of a circle that
	 * would surely enclose every vertex, although allowing the radius to be slightly bigger than necessary.
	 * 
	 * @param veinsModel
	 * @return
	 */
	/*
	 * private double calculateCameraDistance(VeinsModel veinsModel) { double d1 = veinsModel.minX - veinsModel.centerx; double d2 = veinsModel.maxX - veinsModel.centerx; double d3 = veinsModel.minY - veinsModel.centery; double d4 = veinsModel.maxY - veinsModel.centery; double d5 = veinsModel.minZ -
	 * veinsModel.centerz; double d6 = veinsModel.maxZ - veinsModel.centerz; d1 *= d1; d2 *= d2; d3 *= d3; d4 *= d4; d5 *= d5; d6 *= d6; d1 = Math.max(d1, d2); d2 = Math.max(d3, d4); d3 = Math.max(d5, d6); d1 = Math.sqrt(Math.max(Math.max(d1 + d2, d2 + d3), d1 + d3));
	 * 
	 * return d1; }
	 */

	public void resetCameraPositionAndOrientation()
	{
		cam.cameraZ = 0;
		cam.cameraX = 0;
		cam.cameraY = 0;
		cam.cameraOrientation = new Quaternion();
	}

	private void setScreenPlanes(double d1, double fovMin)
	{
		double yAngle = Math.PI * FOV_Y / 360d;
		double xAngle = yAngle * (double) VeinsWindow.settings.resWidth / (double) VeinsWindow.settings.resHeight;
		double screenPlaneZ = -d1 / Math.tan(fovMin / 2);
		double screenPlaneY = Math.tan(yAngle) * (-screenPlaneZ);
		double screenPlaneX = Math.tan(xAngle) * (-screenPlaneZ);

		screenPlaneInitialUpperLeft = new double[]
		{ -screenPlaneX, screenPlaneY, screenPlaneZ };
		screenPlaneInitialUpperRight = new double[]
		{ screenPlaneX, screenPlaneY, screenPlaneZ };
		screenPlaneInitialLowerLeft = new double[]
		{ -screenPlaneX, -screenPlaneY, screenPlaneZ };
		screenPlaneInitialLowerRight = new double[]
		{ screenPlaneX, -screenPlaneY, screenPlaneZ };

	}

	public void switchWireframe()
	{
		if (isWireframeOn == false)
			GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
		else
			GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);

		isWireframeOn = !isWireframeOn;
	}

	public void switchWireframe(boolean useWireframe)
	{
		isWireframeOn = useWireframe;

		if (isWireframeOn == false)
			GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
		else
			GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
	}

	public void switchAA()
	{
		isAAEnabled = !isAAEnabled;
	}

	public Camera getCamera()
	{
		return cam;
	}

	public VeinsModel getVeinsModel()
	{
		return veinsModel;
	}

	public void setActiveShaderProgram(int shaderProgram)
	{
		activeShaderProgram = shaderProgram;
	}

	public static String GetOpenCLErrorString(int error)
	{
		switch (error)
		{
		// run-time and JIT compiler errors
		case 0:
			return "CL_SUCCESS";
		case -1:
			return "CL_DEVICE_NOT_FOUND";
		case -2:
			return "CL_DEVICE_NOT_AVAILABLE";
		case -3:
			return "CL_COMPILER_NOT_AVAILABLE";
		case -4:
			return "CL_MEM_OBJECT_ALLOCATION_FAILURE";
		case -5:
			return "CL_OUT_OF_RESOURCES";
		case -6:
			return "CL_OUT_OF_HOST_MEMORY";
		case -7:
			return "CL_PROFILING_INFO_NOT_AVAILABLE";
		case -8:
			return "CL_MEM_COPY_OVERLAP";
		case -9:
			return "CL_IMAGE_FORMAT_MISMATCH";
		case -10:
			return "CL_IMAGE_FORMAT_NOT_SUPPORTED";
		case -11:
			return "CL_BUILD_PROGRAM_FAILURE";
		case -12:
			return "CL_MAP_FAILURE";
		case -13:
			return "CL_MISALIGNED_SUB_BUFFER_OFFSET";
		case -14:
			return "CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST";
		case -15:
			return "CL_COMPILE_PROGRAM_FAILURE";
		case -16:
			return "CL_LINKER_NOT_AVAILABLE";
		case -17:
			return "CL_LINK_PROGRAM_FAILURE";
		case -18:
			return "CL_DEVICE_PARTITION_FAILED";
		case -19:
			return "CL_KERNEL_ARG_INFO_NOT_AVAILABLE";

			// compile-time errors
		case -30:
			return "CL_INVALID_VALUE";
		case -31:
			return "CL_INVALID_DEVICE_TYPE";
		case -32:
			return "CL_INVALID_PLATFORM";
		case -33:
			return "CL_INVALID_DEVICE";
		case -34:
			return "CL_INVALID_CONTEXT";
		case -35:
			return "CL_INVALID_QUEUE_PROPERTIES";
		case -36:
			return "CL_INVALID_COMMAND_QUEUE";
		case -37:
			return "CL_INVALID_HOST_PTR";
		case -38:
			return "CL_INVALID_MEM_OBJECT";
		case -39:
			return "CL_INVALID_IMAGE_FORMAT_DESCRIPTOR";
		case -40:
			return "CL_INVALID_IMAGE_SIZE";
		case -41:
			return "CL_INVALID_SAMPLER";
		case -42:
			return "CL_INVALID_BINARY";
		case -43:
			return "CL_INVALID_BUILD_OPTIONS";
		case -44:
			return "CL_INVALID_PROGRAM";
		case -45:
			return "CL_INVALID_PROGRAM_EXECUTABLE";
		case -46:
			return "CL_INVALID_KERNEL_NAME";
		case -47:
			return "CL_INVALID_KERNEL_DEFINITION";
		case -48:
			return "CL_INVALID_KERNEL";
		case -49:
			return "CL_INVALID_ARG_INDEX";
		case -50:
			return "CL_INVALID_ARG_VALUE";
		case -51:
			return "CL_INVALID_ARG_SIZE";
		case -52:
			return "CL_INVALID_KERNEL_ARGS";
		case -53:
			return "CL_INVALID_WORK_DIMENSION";
		case -54:
			return "CL_INVALID_WORK_GROUP_SIZE";
		case -55:
			return "CL_INVALID_WORK_ITEM_SIZE";
		case -56:
			return "CL_INVALID_GLOBAL_OFFSET";
		case -57:
			return "CL_INVALID_EVENT_WAIT_LIST";
		case -58:
			return "CL_INVALID_EVENT";
		case -59:
			return "CL_INVALID_OPERATION";
		case -60:
			return "CL_INVALID_GL_OBJECT";
		case -61:
			return "CL_INVALID_BUFFER_SIZE";
		case -62:
			return "CL_INVALID_MIP_LEVEL";
		case -63:
			return "CL_INVALID_GLOBAL_WORK_SIZE";
		case -64:
			return "CL_INVALID_PROPERTY";
		case -65:
			return "CL_INVALID_IMAGE_DESCRIPTOR";
		case -66:
			return "CL_INVALID_COMPILER_OPTIONS";
		case -67:
			return "CL_INVALID_LINKER_OPTIONS";
		case -68:
			return "CL_INVALID_DEVICE_PARTITION_COUNT";

			// extension errors
		case -1000:
			return "CL_INVALID_GL_SHAREGROUP_REFERENCE_KHR";
		case -1001:
			return "CL_PLATFORM_NOT_FOUND_KHR";
		case -1002:
			return "CL_INVALID_D3D10_DEVICE_KHR";
		case -1003:
			return "CL_INVALID_D3D10_RESOURCE_KHR";
		case -1004:
			return "CL_D3D10_RESOURCE_ALREADY_ACQUIRED_KHR";
		case -1005:
			return "CL_D3D10_RESOURCE_NOT_ACQUIRED_KHR";
		default:
			return "Unknown OpenCL error";
		}
	}
}
