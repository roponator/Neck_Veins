package si.uni_lj.fri.MPU_Implicits;

import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.lwjgl.util.vector.Vector3f;

import si.uni_lj.fri.MPUI_Utils.KDtree;
import si.uni_lj.fri.MPUI_Utils.Point3f;

public class MPUcallable implements Callable<LinkedList<Cell>> {
	Octree o;
    
    public MPUcallable(float alpha, float lambda, float maxError, int maxDepth, Point3f[] points, Vector3f[] normals, Cell c, KDtree kd) {
    	o = new Octree(alpha, lambda, maxError, maxDepth, points, normals, c, kd);

    
}
	@Override
	public LinkedList<Cell> call() throws Exception {
		o.initializeSubdivision(o.getRoot(), 1, new Cell());
		return o.getOctreeCells();
	}
}