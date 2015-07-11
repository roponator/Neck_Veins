package si.uni_lj.fri.MPU_Implicits;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.lwjgl.util.vector.Vector3f;

import si.uni_lj.fri.MPUI_Utils.KDtree;
import si.uni_lj.fri.MPUI_Utils.MathUtils;
import si.uni_lj.fri.MPUI_Utils.Point3f;


public class Octree {
	private float lambda;
	private Point3f[] points;
	private Vector3f[] normals;
	private float error;
	private int maxDepth;
	private Cell root;
	private KDtree kd;
	private LinkedList<Cell> octreeCells = new LinkedList<Cell>();
	



	public Octree(float alpha, float lambda, float maxError, int maxDepth, Point3f[] points, Vector3f[] normals, Cell c, KDtree kd){ 
		this.lambda = lambda;
		this.points = points;
		this.kd = kd;
		this.normals = normals;
		this.error = maxError;
		this.maxDepth = maxDepth;
		root = c;
	//	initializeSubdivision(root, 1, null);
	}
	
	
	public void initializeSubdivision(Cell c, int depth, Cell parent){

		octreeCells.add(c);
		Point3f center = c.getCenter();
		float dh = c.getWidth()*0.5f;

		float squareR = c.getRadiusOriginal()*c.getRadiusOriginal();
		c.setRadius(c.getRadiusOriginal());
		Point3f[] nearPoints;
		Vector3f[] nearNormals;
		
		int count = 0;
		boolean empty = false;
		int[] nearIndices;
		int minN = 15;
		
		while(true ){
			nearIndices = kd.findNeighbours(center, c.getRadius());

			count = nearIndices.length;
			if(count < minN){
				squareR = (c.getRadius() + c.getRadiusOriginal()*lambda);
				c.setRadius(squareR);

			}else{
				break;
				
			}
		}
		
		nearPoints = new Point3f[nearIndices.length];
		nearNormals = new Vector3f[nearIndices.length];
		for(int i = 0; i<nearIndices.length; i++){
			nearPoints[i] = points[nearIndices[i]];
			nearNormals[i] = normals[nearIndices[i]];			
			
		}
		
		parent.setChild(c);
		
		Point3f averageP = new Point3f(0f, 0f, 0f);
		Vector3f averageN = new Vector3f(0f, 0f, 0f);
		
		float[] weights = new float[count];
		int iterator = 0;
		float weightSum = 0;

		for( Point3f p : nearPoints){
			float w = MathUtils.weight(p, center, c.getRadius() );
			averageP.sum(p.x*w, p.y*w, p.z*w);
			weights[iterator] = w;
			iterator++;
			weightSum = weightSum + w;
		}
		weightSum = 1/weightSum;
		averageP.scale(weightSum);
		
		iterator = 0;
		for( Vector3f n : nearNormals){
			Vector3f scaled = new Vector3f(n.x * weights[iterator], n.y * weights[iterator], n.z * weights[iterator]);
			Vector3f.add(scaled, averageN, averageN);
			iterator++;
		}	
		averageN.x += 0.0000000000001f;
		averageN.y += 0.0000000000001f;
		averageN.z += 0.0000000000001f;
		averageN.normalise();
		
	    boolean general = false;
	    for(Vector3f n : nearNormals){
	      if(averageN.x*n.x + averageN.y*n.y + averageN.z*n.z < 0){
	    	general = true;
	        break;
	      }
	    }
		if(general){
			c.generalQuadratic(averageN, averageP, nearPoints, (Vector3f[]) nearNormals );
		}
		else c.bivariatePolynomial(averageN, averageP, nearPoints, (Vector3f[]) nearNormals );
		if(empty){
			c.setError(1);
			c.setLeaf(true);
			return;
		}
		
		float error = 0;
		float r2 = c.getRadiusOriginal() * c.getRadiusOriginal();
		for(Point3f p : nearPoints){
			Point3f dp = p.sub(c.getCenter());
			if(r2 < dp.x*dp.x + dp.y * dp.y + dp.z*dp.z){
				continue;
			}
			
		    float f = (float)Math.abs(c.localFunctionValue(p));
		    Vector3f g = c.gradient(p);
		    float e = f/(float)Math.sqrt(g.x * g.x + g.y * g.y + g.z * g.z);
		    if(e > error)
		      error = e;
			
			
		}
		c.setError(error);
		if(error > this.error && depth < maxDepth){

			float dhh = dh * 0.5f;
			initializeSubdivision(new Cell(center.getSum(-dhh, -dhh, -dhh), dh, c.getRadiusOriginal()*0.5f), depth+1, c);
			initializeSubdivision(new Cell(center.getSum(-dhh, -dhh,  dhh), dh, c.getRadiusOriginal()*0.5f), depth+1, c);
			initializeSubdivision(new Cell(center.getSum(-dhh,  dhh, -dhh), dh, c.getRadiusOriginal()*0.5f), depth+1, c);
			initializeSubdivision(new Cell(center.getSum(-dhh,  dhh,  dhh), dh, c.getRadiusOriginal()*0.5f), depth+1, c);
			initializeSubdivision(new Cell(center.getSum( dhh, -dhh, -dhh), dh, c.getRadiusOriginal()*0.5f), depth+1, c);
			initializeSubdivision(new Cell(center.getSum( dhh, -dhh,  dhh), dh, c.getRadiusOriginal()*0.5f), depth+1, c);
			initializeSubdivision(new Cell(center.getSum( dhh,  dhh, -dhh), dh, c.getRadiusOriginal()*0.5f), depth+1, c);
			initializeSubdivision(new Cell(center.getSum( dhh,  dhh,  dhh), dh, c.getRadiusOriginal()*0.5f), depth+1, c);
			
		}else {		
			c.setLeaf(true);
		}
	}


	public Cell getRoot() {
		return root;
	}


	public void setRoot(Cell root) {
		this.root = root;
	}
	
	public LinkedList<Cell> getOctreeCells() {
		return octreeCells;
	}


	public void setOctreeCells(LinkedList<Cell> octreeCells) {
		this.octreeCells = octreeCells;
	}
}
