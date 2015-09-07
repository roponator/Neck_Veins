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
import si.uni_lj.fri.veins3D.gui.render.Camera;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.main.VeinsWindow;
import si.uni_lj.fri.veins3D.math.Quaternion;
import si.uni_lj.fri.veins3D.math.Vector;
import si.uni_lj.fri.veins3D.utils.RayUtil;
import si.uni_lj.fri.volumeRaycast.VolumeRaycast;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Simon Žagar
 * @since 0.2
 * @version 0.2
 */
public class VeinsModelRaycastVolume extends VeinsModel
{
	Quaternion addedOrientation;
	Quaternion modelOnlyRotation;
	Quaternion currentOrientation;
	private double[] veinsGrabbedAt;
	private double veinsGrabRadius;
	
	VolumeRaycast m_raycaster = null;

	public VeinsModelRaycastVolume(int displayWidth, int displayHeight,String filepath)
	{
		System.out.println("STACK TRACE START ****************");
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
		    System.out.println(ste);
		}
		System.out.println("STACK TRACE END ****************");
				
		
		m_raycaster = new VolumeRaycast(displayWidth, displayHeight);
		m_raycaster.MainInit(filepath,VeinsWindow.renderer.getCamera());
		
		currentOrientation = new Quaternion();
		addedOrientation = new Quaternion();
		modelOnlyRotation = new Quaternion();
		
		VeinsWindow.renderer.resetCameraPositionAndOrientation();
	}

	public void SetNewResolution(int width, int height)
	{
		m_raycaster.SetNewResolution(width, height);
	}

	@Override
	public void render(Camera camera, float stereoOffset)
	{
		Quaternion mouseRotation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
		mouseRotation = Quaternion.quaternionMultiplication(Quaternion.quaternionFromAngleAndRotationAxis(Math.PI, new double[]{-1,0,0}), mouseRotation);
		m_raycaster.MainRender(camera,mouseRotation,stereoOffset);
	}
	
	public void reloadVolumeGradient(String gradientFile)
	{
		m_raycaster.LoadGradFromFileAndCreateCLMemory(gradientFile);
	}

	public void cleanup()
	{
		m_raycaster.ReleaseResources();
	}

	public boolean wasLoadedFromObj(){return false;}
	
	public void changeMinTriangles(int minTriangels)
	{
	}
	public void changeThreshold(float threshold){}
	
	public int GetMaxTriangles()
	{
		return 0;
	}

	public float GetThreshold()
	{
		return 0.0f;
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

	public void normalizeCurrentOrientation()
	{
		currentOrientation = Quaternion.quaternionNormalization(currentOrientation);
	}

	public void normalizeAddedOrientation()
	{
		addedOrientation = Quaternion.quaternionNormalization(addedOrientation);
	}

	public void moveModelX(float delta)
	{
		m_raycaster.m_keyboardMoveX += delta;
	}
	public void moveModelY(float delta)
	{
		m_raycaster.m_keyboardMoveY += delta;
	}
	public void moveModelZ(float delta)
	{
		m_raycaster.m_keyboardMoveZ += delta;
	}
	
	public void rotateModelX(float delta)
	{
		m_raycaster.m_keyboardRotation = Quaternion.quaternionMultiplication(m_raycaster.m_keyboardRotation, Quaternion.quaternionFromAngleAndRotationAxis(delta, new double[]{1,0,0}));	
	}
	public void rotateModelY(float delta)
	{
		m_raycaster.m_keyboardRotation = Quaternion.quaternionMultiplication(m_raycaster.m_keyboardRotation, Quaternion.quaternionFromAngleAndRotationAxis(delta, new double[]{0,0,-1}));		
	}
	public void rotateModelZ(float delta)
	{
		m_raycaster.m_keyboardRotation = Quaternion.quaternionMultiplication(m_raycaster.m_keyboardRotation, Quaternion.quaternionFromAngleAndRotationAxis(delta, new double[]{0,-1,0}));	
	}
	
	public void increaseSubdivisionDepth()
	{
	}

	public void decreaseSubdivisionDepth()
	{
	}

	public void changeAddedOrientation(VeinsRenderer renderer)
	{
		double[] veinsHeldAt = RayUtil.getRaySphereIntersection_WITH_ONLY_ONE_INTERSECTION_POINT(Mouse.getX(), Mouse.getY(), renderer);

		if (veinsHeldAt != null && veinsGrabbedAt != null ) {
			double[] rotationAxis = Vector.crossProduct(veinsGrabbedAt, veinsHeldAt);
			if (Vector.length(rotationAxis) > 0) {
				rotationAxis = Vector.normalize(rotationAxis);
				//rotationAxis[0] *= -1.0f;
				//rotationAxis[1] *= -1.0f;
				//rotationAxis[2] *= -1.0f;
				rotationAxis = Quaternion.quaternionReciprocal(currentOrientation).rotateVector3d(rotationAxis);
				double angle = Math.acos(Vector.dotProduct(veinsGrabbedAt, veinsHeldAt)
						/ (Vector.length(veinsGrabbedAt) * Vector.length(veinsHeldAt)));
				addedOrientation = Quaternion.quaternionFromAngleAndRotationAxis(angle, rotationAxis);
			}
		}
	}

	public void saveCurrentOrientation()
	{
		currentOrientation = Quaternion.quaternionMultiplication(currentOrientation, addedOrientation);
	}

	public void setAddedOrientation(Quaternion q)
	{
		addedOrientation = q;
	}

	public void rotateModel3D(double[] rot, VeinsRenderer renderer)
	{
	}

	@Override
	public double calculateCameraDistance()
	{
		return 10;
	}





}
