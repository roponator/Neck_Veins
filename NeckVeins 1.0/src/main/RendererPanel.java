package main;

import static org.lwjgl.opengl.GL11.GL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_EQUAL;
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
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static tools.Tools.allocFloats;

import java.nio.FloatBuffer;

import models.VeinsModel;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;

import tools.Quaternion;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;

public class RendererPanel extends LWJGLRenderer {
	public static final int FIXED_PIPLINE = 0;
	public static final int SIMPLE_SHADER = 1;
	public static final int SIMPLE_SHADER_INTERPOLATED = 2;
	public static final int SIMPLE_SHADER_BLINN_PHONG = 3;
	public static final int SIMPLE_SHADER_PHONG = 4;
	public static final int WIREFRAME = 0;

	private int activeShaderProgram;
	private boolean isWireframeOn;
	private boolean isAAEnabled;

	private Camera cam;
	private VeinsModel veinsModel;
	private Quaternion currentModelOrientation;
	private Quaternion addedModelOrientation;

	// Projection parameters
	public float fovy = 45;
	public float zNear = 10;
	public float zFar = 10000;
	public double veinsRadius;
	public double[] screenPlaneInitialUpperLeft;
	public double[] screenPlaneInitialUpperRight;
	public double[] screenPlaneInitialLowerLeft;
	public double[] screenPlaneInitialLowerRight;
	private int[] shaderPrograms;

	public RendererPanel() throws LWJGLException {
		super();
		cam = new Camera();
		activeShaderProgram = 0;
		isWireframeOn = false;
	}

	/**
	 * @since 0.1
	 * @version 0.2
	 */
	public void setupView() {
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		glViewport(0, 0, MainFrameRefactored.settings.resWidth, MainFrameRefactored.settings.resHeight);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(fovy,
				MainFrameRefactored.settings.resWidth / (float) MainFrameRefactored.settings.resHeight, zNear, zFar);
		glShadeModel(GL_SMOOTH);
		setCameraAndLight(0);
	}

	/**
	 * @since 0.1
	 * @version 0.1
	 */
	public void resetView() {
		glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	/**
	 * @since 0.1
	 * @version 0.4
	 */
	public void render() {
		if (MainFrameRefactored.settings.stereoEnabled) {
			float offset = MainFrameRefactored.settings.stereoValue / 10f;
			StencilMask.initStencil();
			glStencilFunc(GL_EQUAL, 0, 0x01);
			setCameraAndLight(offset);
			// TODO
			// renderVeins();
			glStencilFunc(GL_EQUAL, 1, 0x01);
			setCameraAndLight(-offset);
			// TODO
			// renderVeins();
			glDisable(GL_STENCIL_TEST);
		} else {
			setCameraAndLight(0);
			// TODO
			// renderVeins();
		}
	}

	/**
	 * TODO load shaders
	 */
	private void renderVeins() {
		if (veinsModel != null) {
			glMatrixMode(GL_MODELVIEW);
			glPushMatrix();
			Quaternion compositeOrientation = Quaternion.quaternionMultiplication(currentModelOrientation,
					addedModelOrientation);
			FloatBuffer fb = compositeOrientation.getRotationMatrix(false);
			GL11.glMultMatrix(fb);

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

	public Camera getCamera() {
		return cam;
	}

	public VeinsModel getVeinsModel() {
		return veinsModel;
	}

	public void setActiveShaderProgram(int shaderProgram) {
		activeShaderProgram = shaderProgram;
	}

	public int getActiveShaderProgram() {
		return activeShaderProgram;
	}

	public boolean isWireframeOn() {
		return isWireframeOn;
	}

	public void switchWireframe() {
		isWireframeOn = !isWireframeOn;
	}

	public void switchAA() {
		isAAEnabled = !isAAEnabled;
	}

	public boolean isAAEnables() {
		return isAAEnabled;
	}

	public Quaternion getCurrentModelOrientation() {
		return currentModelOrientation;
	}

	public Quaternion getAddedModelOrientation() {
		return addedModelOrientation;
	}

	public void setCurrentModelOrientation(Quaternion q) {
		currentModelOrientation = q;
	}

	public void setAddedModelOrientation(Quaternion q) {
		addedModelOrientation = q;
	}

	public void setVeinsModel(VeinsModel veinsModel) {
		this.veinsModel = veinsModel;
	}

}
