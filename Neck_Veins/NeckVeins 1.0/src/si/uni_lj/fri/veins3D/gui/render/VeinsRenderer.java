package si.uni_lj.fri.veins3D.gui.render;

import static org.lwjgl.opengl.GL11.GL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_EQUAL;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_POSITION;
import static org.lwjgl.opengl.GL11.GL_SHININESS;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SPECULAR;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLight;
import static org.lwjgl.opengl.GL11.glMaterial;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glStencilFunc;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glViewport;
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
import static org.lwjgl.util.glu.Project.gluLookAt;
import static si.uni_lj.fri.veins3D.utils.Tools.allocFloats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class VeinsRenderer  {
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

	public VeinsRenderer() throws LWJGLException {
		
		cam = new Camera();
		activeShaderProgram = 4;
		isWireframeOn = false;
		isAAEnabled = true;
	}

	/**
	 * @since 0.1
	 * @version 0.2
	 */
	public void setupView() {
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
	public void clearView() {
		glClearColor(200.0f/255.0f, 199.0f/255.0f,199.0f/255.0f, 0.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	/**
	 * @since 0.1
	 * @version 0.4
	 */
	public void render()
	{
		if (VeinsWindow.settings.stereoEnabled) {
			float offset = VeinsWindow.settings.stereoValue / 10f;
			StencilMask.initStencil();
			glStencilFunc(GL_EQUAL, 0, 0x01);
			setCameraAndLight(offset);
			renderVeins();
			glStencilFunc(GL_EQUAL, 1, 0x01);
			setCameraAndLight(-offset);
			renderVeins();
			glDisable(GL_STENCIL_TEST);
		} else {
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

			if (activeShaderProgram == -1) {
				GL20.glUseProgram(0);
			} else {
				GL20.glUseProgram(shaderPrograms[activeShaderProgram]);
				int myUniformLocation = glGetUniformLocation(shaderPrograms[activeShaderProgram], "bloodColor");
				glUniform4f(myUniformLocation, 0.8f, 0.06667f, 0.0f, 1);
			}

			glEnable(GL_LIGHTING);
			glColor4f(0.8f, 0.06667f, 0.0f, 1);
			glMaterial(GL_FRONT, GL_AMBIENT, allocFloats(new float[] { 0.8f, 0.06667f, 0.0f, 1 }));
			glMaterial(GL_FRONT, GL_DIFFUSE, allocFloats(new float[] { 0.8f, 0.06667f, 0.0f, 1 }));
			glMaterial(GL_FRONT, GL_SPECULAR, allocFloats(new float[] { 0.66f, 0.66f, 0.66f, 1f }));
			glMaterial(GL_FRONT, GL_SHININESS, allocFloats(new float[] { 100f, 256.0f, 256.0f, 256.0f }));

			veinsModel.render();

			GL20.glUseProgram(0);
			glPopMatrix();
			
		
		}
		
		

	}

	/**
	 * @since 0.1
	 * @version 0.1
	 */
	private void setCameraAndLight(float offset) {
		double v[] = new double[] { offset, 0, 0 };
	
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
		glLight(GL_LIGHT0, GL_POSITION, allocFloats(new float[] {-(centerX-posX)*1000f, -(centerY-posY)*1000f, -(centerZ-posZ)*1000f, 1.0f }));
		glLight(GL_LIGHT0, GL_DIFFUSE, allocFloats(new float[] { 1f, 1f, 1f, 1 }));
		glLight(GL_LIGHT0, GL_AMBIENT, allocFloats(new float[] { 0.15f, 0.15f, 0.15f, 1 }));
		glLight(GL_LIGHT0, GL_SPECULAR, allocFloats(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }));
		gluLookAt(posX, posY, posZ, centerX, centerY, centerZ, upX, upY, upZ);

	}

	/**
	 * 
	 * @throws ShaderLoadException
	 */
	public void prepareShaders() throws ShaderLoadException {
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

		try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						VeinsWindow.class.getResourceAsStream(path + "shader" + i + ".vert")));
				String line;
				while ((line = reader.readLine()) != null) {
					vertexShaderSource.append(line).append("\n");
				}
				reader.close();
			} catch (IOException e) {
				System.err.println("Vertex shader" + i + " wasn't loaded properly.");
				throw new ShaderLoadException("Vertex shader" + i + " wasn't loaded properly.");
			}

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						VeinsRenderer.class.getResourceAsStream(path + "shader" + i + ".frag")));
				String line;
				while ((line = reader.readLine()) != null) {
					fragmentShaderSource.append(line).append("\n");
				}
				reader.close();
			} catch (IOException e) {
				System.err.println("Fragment shader" + i + " wasn't loaded properly.");
				throw new ShaderLoadException("Fragment shader" + i + " wasn't loaded properly.");
			}

			glShaderSource(vertexShaders[i], vertexShaderSource);
			glCompileShader(vertexShaders[i]);
			if (glGetShader(vertexShaders[i], GL_COMPILE_STATUS) == GL_FALSE) {
				System.err.println("Vertex shader" + i + " not compiled correctly");
			}

			glShaderSource(fragmentShaders[i], fragmentShaderSource);
			glCompileShader(fragmentShaders[i]);
			if (glGetShader(fragmentShaders[i], GL_COMPILE_STATUS) == GL_FALSE) {
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
			//System.out.println("Vertex shader" + i + " info: " + glGetShaderInfoLog(vertexShaders[i], 999));
			//System.out.println("Fragment shader" + i + " info: " + glGetShaderInfoLog(fragmentShaders[i], 999));
			//System.out.println("Shader program" + i + " info: " + glGetShaderInfoLog(shaderPrograms[i], 999));
		}
	}
	

	public void cleanShaders() {
		for (int i = 0; i < NUMBER_OF_SHADER_PROGRAMS; i++) {
			glDetachShader(shaderPrograms[i], vertexShaders[i]);
			glDeleteShader(vertexShaders[i]);
			glDetachShader(shaderPrograms[i], fragmentShaders[i]);
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
	public void loadModelObj(String fileName) {
		if (veinsModel != null)
			veinsModel.cleanup();
		veinsModel = new VeinsModelMesh();
		VeinsModelMesh veinsModelMesh = (VeinsModelMesh)veinsModel;
		veinsModelMesh.constructVBOFromObjFile(fileName);
		setDefaultViewOptions();
		isOpen = true;
	}

	/**
	 * Loads Mhd with raw file using OpenCL
	 * 
	 * @param fileName
	 * @param sigma
	 * @param threshold
	 * @throws LWJGLException
	 */
	public void loadModelRaw(String fileName, double sigma, double threshold) throws LWJGLException {
		if (veinsModel != null)
			veinsModel.cleanup();
		
		veinsModel = new VeinsModelMesh();
		VeinsModelMesh veinsModelMesh = (VeinsModelMesh)veinsModel;
		veinsModelMesh.constructVBOFromRawFile(fileName, sigma, threshold);
		//veinsModel = new VeinsModelRaycastVolume(VeinsWindow.settings.resWidth,VeinsWindow.settings.resHeight);

		setDefaultViewOptions();
		isOpen = true;
	}

	/**
	 * Loads Mhd with raw file using Java implemenentation
	 * 
	 * @param fileName
	 * @param sigma
	 * @param threshold
	 */
	public void loadModelRawSafeMode(String fileName, double sigma, double threshold) {
		if (veinsModel != null)
			veinsModel.cleanup();
		veinsModel = new VeinsModelMesh();
		VeinsModelMesh veinsModelMesh = (VeinsModelMesh)veinsModel;
		veinsModelMesh.constructVBOFromRawFileSafeMode(fileName, sigma, threshold);
		setDefaultViewOptions();
		isOpen = true;
	}

	public void changeModel(double threshold) throws LWJGLException 
	{
		veinsModel.cleanup();
		veinsModel = new VeinsModelMesh(threshold, veinsModel.GetCurrentOrientation());
		VeinsModelMesh veinsModelMesh = (VeinsModelMesh)veinsModel;
		double d = veinsModel.calculateCameraDistance();
		 veinsModelMesh.SetVeinsGrabRadius(d / Math.sqrt(2));
	}

	private void setDefaultViewOptions()
	{
		double fovMin = (VeinsWindow.settings.resWidth < VeinsWindow.settings.resHeight) ? FOV_Y
				* VeinsWindow.settings.resWidth / (double) VeinsWindow.settings.resHeight : FOV_Y;
		fovMin = Math.toRadians(fovMin); // Math.PI * fovMin / 180
		
		double d = veinsModel.calculateCameraDistance();
		veinsModel.SetVeinsGrabRadius(d / Math.sqrt(2));
		setCameraPositionAndOrientation(d, fovMin);
		setScreenPlanes(d, fovMin);
	}

	/**
	 * Calculate the appropriate camera distance: The following code takes the
	 * most extreme values on each coordinate of all the specified vertices in
	 * the VeinsModel (from .obj file). It uses the bigger distance (of two)
	 * from the average location on each axis to calculate the radius of a
	 * circle that would surely enclose every vertex, although allowing the
	 * radius to be slightly bigger than necessary.
	 * 
	 * @param veinsModel
	 * @return
	 */
	/*private double calculateCameraDistance(VeinsModel veinsModel) {
		double d1 = veinsModel.minX - veinsModel.centerx;
		double d2 = veinsModel.maxX - veinsModel.centerx;
		double d3 = veinsModel.minY - veinsModel.centery;
		double d4 = veinsModel.maxY - veinsModel.centery;
		double d5 = veinsModel.minZ - veinsModel.centerz;
		double d6 = veinsModel.maxZ - veinsModel.centerz;
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
	}*/

	private void setCameraPositionAndOrientation(double d, double fovMin) {
		cam.cameraZ = 0;
		cam.cameraX = 0;
		cam.cameraY = 0;
		cam.cameraOrientation = new Quaternion();
	}

	private void setScreenPlanes(double d1, double fovMin) {
		double yAngle = Math.PI * FOV_Y / 360d;
		double xAngle = yAngle * (double) VeinsWindow.settings.resWidth / (double) VeinsWindow.settings.resHeight;
		double screenPlaneZ = -d1 / Math.tan(fovMin / 2);
		double screenPlaneY = Math.tan(yAngle) * (-screenPlaneZ);
		double screenPlaneX = Math.tan(xAngle) * (-screenPlaneZ);

		screenPlaneInitialUpperLeft = new double[] { -screenPlaneX, screenPlaneY, screenPlaneZ };
		screenPlaneInitialUpperRight = new double[] { screenPlaneX, screenPlaneY, screenPlaneZ };
		screenPlaneInitialLowerLeft = new double[] { -screenPlaneX, -screenPlaneY, screenPlaneZ };
		screenPlaneInitialLowerRight = new double[] { screenPlaneX, -screenPlaneY, screenPlaneZ };

	}

	public void switchWireframe() {
		if (isWireframeOn)
			GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
		else
			GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);

		isWireframeOn = !isWireframeOn;
	}

	public void switchAA() {
		isAAEnabled = !isAAEnabled;
	}

	public Camera getCamera() {
		return cam;
	}

	public VeinsModel getVeinsModel() {
		return veinsModel;
	}

	public void setActiveShaderProgram(int shaderProgram) {
		activeShaderProgram = shaderProgram;
	}

}
