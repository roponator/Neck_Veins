package si.uni_lj.fri.veins3D.gui.render.models;

import org.lwjgl.input.Mouse;


import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.math.Quaternion;
import si.uni_lj.fri.veins3D.math.Vector;
import si.uni_lj.fri.veins3D.utils.RayUtil;

/*
 * Base class for models. You don't need to 
 * implement all methods. At least 'render' needs
 * to be implemented.
 */
public abstract class VeinsModel 
{
	// You must implement at least 'render'. 
	// Also implementing 'cleanup' would be nice :)
	public abstract void render();
	public abstract void cleanup();
	
	public abstract void changeMinTriangles(int minTriangels);
	
	public abstract int GetMaxTriangles();
	public abstract float GetThreshold();
	public abstract double GetVeinsGrabRadius();
	public abstract double[] GetVeinsGrabbedAt();
	
	public abstract void SetVeinsGrabRadius(double r);
	public abstract void SetVeinsGrabbedAt(double[] v);
	
	public abstract void normalizeCurrentOrientation();
	public abstract void normalizeAddedOrientation() ;
	
	public abstract void increaseSubdivisionDepth();
	public abstract void decreaseSubdivisionDepth();
	
	public abstract void changeAddedOrientation(VeinsRenderer renderer);
	
	public abstract void saveCurrentOrientation();
	public abstract void setAddedOrientation(Quaternion q);
	
	public abstract void rotateModel3D(double[] rot, VeinsRenderer renderer);
	
	public abstract double calculateCameraDistance();
	
	public abstract void SetNewResolution(int width, int height); // needed only for volumeRenderer
	
	public Quaternion GetCurrentOrientation()
	{
		return computeDefaultOrientation();
	}
	
	public static Quaternion computeDefaultOrientation() {
		return Quaternion.quaternionFromAngleAndRotationAxis(0, new double[] { 1, 0, 0 });
	}
	
	
}
