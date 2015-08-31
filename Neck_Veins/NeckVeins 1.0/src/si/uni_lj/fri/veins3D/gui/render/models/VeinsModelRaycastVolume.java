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
	VolumeRaycast m_raycaster = null;

	public VeinsModelRaycastVolume(int displayWidth, int displayHeight)
	{
		System.out.println("STACK TRACE START ****************");
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
		    System.out.println(ste);
		}
		System.out.println("STACK TRACE END ****************");
				
		
		m_raycaster = new VolumeRaycast(displayWidth, displayHeight);
		m_raycaster.MainInit();
	}

	public void SetNewResolution(int width, int height)
	{
		m_raycaster.SetNewResolution(width, height);
	}

	@Override
	public void render(Camera camera)
	{
		m_raycaster.MainRender(camera);
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

	public double GetVeinsGrabRadius()
	{
		return 0.0;
	}

	public double[] GetVeinsGrabbedAt()
	{
		return new double[]
		{ 0, 0, };
	}

	public void SetVeinsGrabRadius(double r)
	{
	}

	public void SetVeinsGrabbedAt(double[] v)
	{
	}

	public void normalizeCurrentOrientation()
	{
	}

	public void normalizeAddedOrientation()
	{
	}

	public void increaseSubdivisionDepth()
	{
	}

	public void decreaseSubdivisionDepth()
	{
	}

	public void changeAddedOrientation(VeinsRenderer renderer)
	{
	}

	public void saveCurrentOrientation()
	{
	}

	public void setAddedOrientation(Quaternion q)
	{
	}

	public void rotateModel3D(double[] rot, VeinsRenderer renderer)
	{
	}

	public double calculateCameraDistance()
	{
		return 0.0;
	}
}
