package si.uni_lj.fri.MPU_Implicits;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.lwjgl.LWJGLException;
import org.lwjgl.util.vector.Vector3f;

import si.uni_lj.fri.Bloomenthal.Bloomenthal;
import si.uni_lj.fri.Bloomenthal.BloomenthalCallable;
import si.uni_lj.fri.Bloomenthal.BloomenthalCallableMC;
import si.uni_lj.fri.Bloomenthal.BloomenthalGPU;
import si.uni_lj.fri.Bloomenthal.BloomenthalMC;
import si.uni_lj.fri.MPUI_Utils.ImplicitInterface;
import si.uni_lj.fri.MPUI_Utils.KDtree;
import si.uni_lj.fri.MPUI_Utils.Point3f;

public class MPUI {
	
	private float[] points;
	private float[] normals;
	private Point3f[] pointuples;
	private Vector3f[] normaltuples;
	private float[] outputVertices;
	private float[] outputNormals;
	private ImplicitInterface globalImplicit;
	
	public MPUI(float alpha, float lambda, float error, boolean cubes, float res, float[] points, float[] normals, int xDim, int yDim, int zDim){
		this.points = points;
		this.normals = normals;

		
		boundPoints(xDim, yDim, zDim);
		
		long startTime = System.nanoTime();
		
		
		
		initializeOctree(alpha, lambda, error, 10);
		
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("Implicits calculated in: "+(float) duration / 1000000000f);
		


		startTime = System.nanoTime(); 
		//	Serial bloomenthal
		if(cubes) 		initializePoligonizationCube(res, xDim, yDim, zDim);
		else			initializePoligonization(res,  xDim,  yDim,  zDim);

		
		
	/*	globalImplicit.primitiveTreeConstruction();
		System.out.println(globalImplicit.getFnBuff().length);
		try {
			Object[] out = BloomenthalGPU.createModel(0.0025f, globalImplicit);
			IntBuffer nTrianglesBuff = (IntBuffer) out[0];
			FloatBuffer trianglesBuff = (FloatBuffer) out[1];
			FloatBuffer normalsBuf = (FloatBuffer) out[2];
			outputVertices = new float[nTrianglesBuff.get(0)*9];
		//	outputNormals = new float[nTrianglesBuff.get(0)*9];
			for(int i = 0; i<nTrianglesBuff.get(0)*9; i++){
				outputVertices[i] = trianglesBuff.get(i);
				//outputNormals[i] = null;				
			}		
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		endTime = System.nanoTime();
		duration = endTime - startTime;
		System.out.println("Poligonization calculated in: "+(float) duration / 1000000000f);
	}
	
	public void boundPoints(float xDim, float yDim, float zDim){
		
		pointuples = new Point3f[points.length/3];
		normaltuples = new Vector3f[normals.length/3];
		float max = xDim;
		if(yDim > xDim && zDim < yDim ) max = yDim;
		else if(zDim > xDim) max = zDim;
		
		
		float kxDim = 1/max;
		float kyDim = 1/max;
		float kzDim = 1/max;
		float hx = xDim * 0.5f;
		float hy = yDim * 0.5f;
		float hz = zDim * 0.5f;
		int counter = 0;
		
		for(int i = 0; i<points.length; i+=3){;
			
			pointuples[counter] = new Point3f((points[i]-hx)*kxDim, (points[i+1]-hy)*kyDim, (points[i+2] - hz)*kzDim);
			normaltuples[counter] = new Vector3f(normals[i], normals[i+1], normals[i+2]); 
			counter++;
		}
	}
	
	public void initializeOctree(float alpha, float lambda, float maxError, int maxDepth){
	        
		    float r = (float) (Math.sqrt(3)*alpha*0.5f);
		    Cell root = new Cell(new Point3f(0, 0, 0), 1f, r*2f);
		    root.setRadius(root.getRadiusOriginal());
		    
		    Cell c1 = new Cell(new Point3f(0.25f, 0.25f, 0.25f),   .5f, r);
			Cell c2 = new Cell(new Point3f(0.25f, 0.25f, -0.25f),  .5f, r);
			Cell c3 = new Cell(new Point3f(0.25f, -0.25f, 0.25f),   .5f, r);
			Cell c4 = new Cell(new Point3f(0.25f, -0.25f, -0.25f),  .5f, r);
			Cell c5 = new Cell(new Point3f(-0.25f, 0.25f, 0.25f),  .5f, r);
			Cell c6 = new Cell(new Point3f(-0.25f, 0.25f, -0.25f),  .5f, r);
			Cell c7 = new Cell(new Point3f(-0.25f, -0.25f, 0.25f),  .5f, r);
			Cell c8 = new Cell(new Point3f(-0.25f, -0.25f, -0.25f), .5f, r);
			Cell[] cells = new Cell[]{c1, c2, c3, c4, c5, c6, c7, c8};
			
	        LinkedList< Future > futuresList = new LinkedList< Future >();
	        KDtree kd = new KDtree(normaltuples, pointuples);
	        int nrOfProcessors = Runtime.getRuntime().availableProcessors();
	        ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);

	        for(int index = 0; index < 8; index++){
	        	 futuresList.add(eservice.submit(new MPUcallable(alpha, lambda, maxError, maxDepth, pointuples, normaltuples, cells[index], new KDtree(kd.getRoot()))));
	        }
	          
	           
	           for(Future future: futuresList) {
	              try {
	   	           Object taskResult;
	                 taskResult = future.get();
	                 LinkedList<Cell> extracted = (LinkedList<Cell>) taskResult;
	                 root.setChild(extracted.getFirst());
	           }
	           catch (InterruptedException e) {
	        	   e.printStackTrace();
	           }
	           catch (ExecutionException e) {
	        	   e.printStackTrace();
	           }
	        }
		    eservice.shutdown();
	        globalImplicit = new ImplicitInterface(root);
		
	}
	
	public void initializePoligonization(float cubeSize,int xDim, int yDim, int zDim){
        LinkedList< Future > futuresList = new LinkedList< Future >();
        int nrOfProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
        
        int share = (int) (1/(cubeSize*nrOfProcessors));

        int current = 0;
	    for(int index = 0; index < nrOfProcessors; index++){
	        ImplicitInterface ii = new ImplicitInterface(globalImplicit.getRoot());
	       futuresList.add(eservice.submit(new BloomenthalCallable(cubeSize, ii, current, current + share, xDim, yDim, zDim)));
	       current = current + share;
	    }
         
      LinkedList<Float> vertices = new LinkedList<Float>();
      LinkedList<Float> normals = new LinkedList<Float>();
      Bloomenthal[] b = new Bloomenthal[nrOfProcessors];
      int index = 0;
      for(Future future: futuresList) {

             try {
  	           Object taskResult;
                taskResult = future.get();
                Bloomenthal extracted = (Bloomenthal) taskResult;
                b[index] = extracted;

	          }
	          catch (InterruptedException e) {
	       	   e.printStackTrace();
	          }
	          catch (ExecutionException e) {
	       	   e.printStackTrace();
	          }
       	  index++;
       }
      for(Bloomenthal extracted : b){
          for(float f : extracted.getVertices())
          	vertices.add(f);
          for(float n : extracted.getNormals())
          	normals.add(n);
      }
      outputVertices = new float[vertices.size()];
      outputNormals = new float[normals.size()];
      	for(int i = 0; i< outputVertices.length; i++){
      		outputVertices[i] = vertices.getFirst();
      		outputNormals[i] = normals.getFirst();
      		normals.removeFirst();
      		vertices.removeFirst();

      	}
      	
     eservice.shutdown();
	//	outputVertices = polygonized.getVertices();
	//	outputNormals = polygonized.getNormals();
		
	}
	public void initializePoligonizationCube(float cubeSize,int xDim, int yDim, int zDim){
        LinkedList< Future > futuresList = new LinkedList< Future >();
        int nrOfProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
        
        int share = (int) (1/(cubeSize*nrOfProcessors));

        int current = 0;
	    for(int index = 0; index < nrOfProcessors; index++){
	        ImplicitInterface ii = new ImplicitInterface(globalImplicit.getRoot());
	       futuresList.add(eservice.submit(new BloomenthalCallableMC(cubeSize, ii, current, current + share, xDim, yDim, zDim)));
	       current = current + share;
	    }
         
      LinkedList<Float> vertices = new LinkedList<Float>();
      LinkedList<Float> normals = new LinkedList<Float>();
      BloomenthalMC[] b = new BloomenthalMC[nrOfProcessors];
      int index = 0;
      for(Future future: futuresList) {

             try {
  	           Object taskResult;
                taskResult = future.get();
                BloomenthalMC extracted = (BloomenthalMC) taskResult;
                b[index] = extracted;

	          }
	          catch (InterruptedException e) {
	       	   e.printStackTrace();
	          }
	          catch (ExecutionException e) {
	       	   e.printStackTrace();
	          }
       	  index++;
       }
      for(BloomenthalMC extracted : b){
          for(float f : extracted.getVertices())
          	vertices.add(f);
          for(float n : extracted.getNormals())
          	normals.add(n);
      }
      outputVertices = new float[vertices.size()];
      outputNormals = new float[normals.size()];
      	for(int i = 0; i< outputVertices.length; i++){
      		outputVertices[i] = vertices.getFirst();
      		outputNormals[i] = normals.getFirst();
      		normals.removeFirst();
      		vertices.removeFirst();

      	}
      	
     eservice.shutdown();
	//	outputVertices = polygonized.getVertices();
	//	outputNormals = polygonized.getNormals();
		
	}
	public float[] getOutputVertices() {
		return outputVertices;
	}

	public void setOutputVertices(float[] outputVertices) {
		this.outputVertices = outputVertices;
	}

	public float[] getOutputNormals() {
		return outputNormals;
	}

	public void setOutputNormals(float[] outputNormals) {
		this.outputNormals = outputNormals;
	}
	
	
}
