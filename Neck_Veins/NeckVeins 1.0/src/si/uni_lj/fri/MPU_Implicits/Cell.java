package si.uni_lj.fri.MPU_Implicits;

import java.util.LinkedList;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.SingularOps;
import org.lwjgl.util.vector.Vector3f;

import si.uni_lj.fri.MPUI_Utils.MathUtils;
import si.uni_lj.fri.MPUI_Utils.Point3f;

public class Cell {
	
	private LinkedList<Cell> children = new LinkedList<Cell>();
	private Cell parent;
	
	private float[] localFunction;
	private boolean empty;
	private Point3f center;
	private float radius, radiusOriginal, width;
	private boolean isLeaf = false;
	private float error;
	
	public Cell(Point3f center, float width, float radiusOriginal){
		this.center = center;
		this.width = width;
		this.radiusOriginal = radiusOriginal;
		
	}
	
	public Cell() {
	}

	public void generalQuadratic(Vector3f averageN, Point3f averageP,
			Point3f[] nearPoints, Vector3f[] nearNormals) {
		
		float[][] A = new float[10][10];
		float[] b = new float[10];
		Point3f[] ad_point = new Point3f[9];
		
		int n = 9;
		float dh = width*0.5f;
		float x = center.x;
		float y = center.y;
		float z = center.z;

		ad_point[0] = new Point3f(x+dh, y+dh, z+dh);
		ad_point[1] = new Point3f(x-dh, y+dh, z+dh);
		ad_point[2] = new Point3f(x+dh, y-dh, z+dh);
		ad_point[3] = new Point3f(x-dh, y-dh, z+dh);
		ad_point[4] = new Point3f(x+dh, y+dh, z-dh);
		ad_point[5] = new Point3f(x-dh, y+dh, z-dh);
		ad_point[6] = new Point3f(x+dh, y-dh, z-dh);
		ad_point[7] = new Point3f(x-dh, y-dh, z-dh);
		ad_point[8] = new Point3f(x, y, z);
		
		int[][] nearIndices = new int[n][6];
		float[][] nearValues = new float[n][6];
		
		for(int i=0; i<n; i++){
		    for(int j=0; j<6; j++){
		      nearIndices[i][j] = -1;
		      nearValues[i][j] = 10000000000f;
		    }
		}
		
		float totalW = 0;
		int counter = 0;
		for(Point3f p : nearPoints){
			
			float w = MathUtils.weight(p, center, radius);
			totalW += w;
			
			Point3f v = p.sub(averageP);
			float[] values = {w, w*v.x, w*v.y, w*v.z, w*v.x*v.x, w*v.y*v.y, w*v.z*v.z, w*v.x*v.y, w*v.y*v.z, w*v.z*v.x};
		    for(int j=0; j<10; j++){
		        for(int k=j; k<10; k++)
		          A[j][k] += values[j]*values[k];
		    }
			
		    //near points
		    for(int j = 0; j<n; j++){
		    	
		      Point3f q = ad_point[j];
		      float d = p.getUnsquaredDistance(q);
		      
		      int insert = -1;
		      
		      for(int k = 0; k < 6; k++){
		        if(nearValues[j][k] > d)
		          insert = k;
		        else
		          break;
		      }
		      if(insert < 0)
		        continue;
		      
		      for(int k=0; k<insert; k++){
		        nearValues[j][k] = nearValues[j][k+1];
		        nearIndices[j][k] = nearIndices[j][k+1];
		      }
		      nearValues[j][insert] = d;
		      nearIndices[j][insert] = counter;
		    }
		    counter++;
		}
		
		totalW = 1/totalW;
		

		for(int i=0; i<10; i++){
			for(int j=i; j<10; j++)
				A[i][j] *= totalW;
		}
			  
		//Extra points
		int notAdd = 0;
		counter = 0;
		for(int i = 0; i < n; i++){
			Point3f p = ad_point[i];
		    
		    //distance (inner product with normal)
			double v = 0;
			for(int j=0; j<6; j++){
				int in = nearIndices[i][j];
				Point3f q = nearPoints[in];
				Vector3f normal = nearNormals[in];
			//	System.out.println(q + " n: "+normal);
				nearValues[i][j]= normal.x*(p.x-q.x) + normal.y*(p.y-q.y) + normal.z*(p.z-q.z);
				v += nearValues[i][j];
			}
		    v /= 6;
		    
		    //sign check
		    boolean flag = true;
		    for(int j=1; j<6; j++){
		      if(nearValues[i][0]*nearValues[i][j] < 0){
		        flag = false;
		        notAdd++;
		        break;
		      }
		    }
		    if(!flag){
		    //	System.out.println("add_P"+i);
			      continue;
		    }

			counter++;
		    
		    float w = 1.0f/n;
		    Point3f dv = p.sub(averageP);
		    
			float[] values = {w, w*dv.x, w*dv.y, w*dv.z, w*dv.x*dv.x, w*dv.y*dv.y, w*dv.z*dv.z, w*dv.x*dv.y, w*dv.y*dv.z, w*dv.z*dv.x};
		    
		    for(int j=0; j<10; j++){
		    	for(int k=j; k<10; k++)
		    		A[j][k] += values[j]*values[k];
		    	b[j] += (float)(values[j]*v*w);
		    }
		}

		  
		for(int i=1; i<10; i++)
			for(int j=0; j<i; j++)
				A[i][j] = A[j][i];
			  
		double[][] dmat = new double[10][10];
		  
		for(int i=0; i<10; i++){
			
			for(int j=0; j<10; j++){
				dmat[i][j] = A[i][j];

			}

		}
		  
		DenseMatrix64F M = new DenseMatrix64F(dmat);
		SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(10,10,true,true,true);
			
		if( !svd.decompose(M) )
			throw new RuntimeException("Decomposition failed");
			
		DenseMatrix64F U = svd.getU(null,false);
		DenseMatrix64F W = svd.getW(null);
		DenseMatrix64F V = svd.getV(null,false);
		
		SingularOps.descendingOrder(U, false, W, V, false);
		
		
        double[] ud = U.data;
        double[] wd = W.data;
        double[] vd = V.data;
        float[][] u = new float[10][10];
        float[] w = new float[10];
        float[][] v = new float[10][10];
        int index = 0;
        for(int i = 0; i<10; i++){
        	for(int j = 0; j<10; j++){
        		index = i*10 + j;

        		u[i][j] = (float) ud[index];
        		v[i][j] = (float) vd[index];
        	}

        	w[i] = (float) wd[i + i*10];
        }
	
        float wmax=0.0f;
        for (int k=0;k<10;k++)
          if (Math.abs(w[k]) > wmax) wmax=(float)Math.abs(w[k]);
        
        float cxx = 0, cyy = 0, czz = 0, cxy = 0, cyz = 0, czx = 0, cx = 0, cz = 0, cy = 0, c0 = 0;
        if(wmax < 0.000000000001f || counter == 0){
            localFunction = new float[]{cxx, cyy, czz, cxy, cyz, czx, cx, cy, cz, c0};
        	return;
        }
        
        float wmin=wmax*0.0000001f;
        for(int k=1; k<10;k++){
          if(Math.abs(w[k]) < wmin) 
            w[k] = .0f;
        }
        
        
        float[] r = MathUtils.backSubstitution(u, w, v, 10, 10, b);
        
        cxx = r[4];
        cyy = r[5];
        czz = r[6];
        
        cxy = r[7];
        cyz = r[8];
        czx = r[9];
        
        cx = r[1] - cxy*averageP.y - czx*averageP.z - 2.0f*cxx*averageP.x;
        cy = r[2] - cyz*averageP.z - cxy*averageP.x - 2.0f*cyy*averageP.y;
        cz = r[3] - czx*averageP.x - cyz*averageP.y - 2.0f*czz*averageP.z;
        
        c0 = r[0] - r[1]*averageP.x - r[2]*averageP.y - r[3]*averageP.z 
              + cxy*averageP.x*averageP.y + cyz*averageP.y*averageP.z + czx*averageP.z*averageP.x
              + cxx*averageP.x*averageP.x + cyy*averageP.y*averageP.y + czz*averageP.z*averageP.z;
        
        localFunction = new float[]{cxx, cyy, czz, cxy, cyz, czx, cx, cy, cz, c0};
	}

	
	
	public void bivariatePolynomial(Vector3f averageN, Point3f averageP,
			Point3f[] nearPoints, Vector3f[] nearNormals){
		
		Vector3f t1 = new Vector3f();
		Vector3f t2 = new Vector3f();
		int i, j, k;
		
		if(Math.abs(averageN.x)< Math.abs(averageN.y)){
			float l = (float) Math.sqrt(averageN.y*averageN.y + averageN.z * averageN.z);
			t1.x = 0;
			t1.y = - averageN.z/l;
			t1.z = averageN.y/ l;
		}else{
			float l = (float) Math.sqrt(averageN.x*averageN.x + averageN.z * averageN.z);
			t1.x = averageN.z/l;;
			t1.y = 0;
			t1.z = -averageN.x / l;
		}
		
		
		Vector3f.cross(averageN, t1, t2);
		
		float[][] A = new float[6][6];
		float[] b = new float[6];	

		for(Point3f p : nearPoints){
			
			float w = MathUtils.weight(p, center, radius);
			Point3f dp = p.sub(averageP);
			
			float u = t1.x * dp.x + t1.y * dp.y + t1.z * dp.z;
			float v = t2.x * dp.x + t2.y * dp.y + t2.z * dp.z;
			float g = w*(averageN.x * dp.x + averageN.y * dp.y + averageN.z * dp.z);
		
			float[] tmp = new float[6];
			
			tmp[0] = w;
			tmp[1] = w*u;
			tmp[2] = w*v;
			tmp[3] = w*u*u;
			tmp[4] = w*u*v;
			tmp[5] = w*v*v;
			
		     for( j=0; j<6; j++){
		         for(k=j; k<6; k++)
		           A[j][k] += tmp[j]*tmp[k];
		         b[j] += tmp[j]*g;
		     }
			
		}
			
		   for(i=1; i<6; i++)
			      for(j= 0; j<i; j++)
			        A[i][j] = A[j][i];
		   
			double[][] dmat = new double[6][6];
			  
			for( i=0; i<6; i++){
				for( j=0; j<6; j++){
					dmat[i][j] = A[i][j];
				}
			}
			
			DenseMatrix64F M = new DenseMatrix64F(dmat);
			SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(6,6,true,true,true);
				
			if( !svd.decompose(M) )
				throw new RuntimeException("Decomposition failed");
				
			DenseMatrix64F U = svd.getU(null,false);
			DenseMatrix64F W = svd.getW(null);
			DenseMatrix64F V = svd.getV(null,false);
			
		//	SingularOps.descendingOrder(U, false, W, V, false);
			
			
	        double[] ud = U.data;
	        double[] wd = W.data;
	        double[] vd = V.data;
	        float[][] u = new float[6][6];
	        float[] w = new float[6];
	        float[][] v = new float[6][6];
	        int index = 0;
	        for( i = 0; i<6; i++){
	        	for( j = 0; j<6; j++){
	        		index = i*6 + j;

	        		u[i][j] = (float) ud[index];
	        		v[i][j] = (float) vd[index];
	        	}

	        	w[i] = (float) wd[i + i*6];
	        }
		
	        float wmax=0.0f;
	        for ( k=0;k<6;k++)
	          if (Math.abs(w[k]) > wmax) wmax=(float)Math.abs(w[k]);
	        
	        float cxx = 0, cyy = 0, czz = 0, cxy = 0, cyz = 0, czx = 0, cx = 0, cz = 0, cy = 0, c0 = 0;
	        if(wmax < 0.000000000001f){
	            localFunction = new float[]{cxx, cyy, czz, cxy, cyz, czx, cx, cy, cz, c0};
	        	return;
	        }
		
	        float wmin=wmax*0.0000001f;
	        for( k=0; k<6;k++){
	          if(Math.abs(w[k]) < wmin) 
	            w[k] = .0f;
	        }
	        
	        
	        float[] r = MathUtils.backSubstitution(u, w, v, 6, 6, b);
	        
	        c0 = r[0];
	        float cu = r[1];
	        float cv = r[2];
	        float cuu =r[3];
	        float cuv =r[4];
	        float cvv =r[5];
	        

	        //convert into world coordinates (u, v, w) -> (x, y, z)
	        cxx = -(cuu*t1.x*t1.x + cvv*t2.x*t2.x + cuv*t1.x*t2.x);
	        cyy = -(cuu*t1.y*t1.y + cvv*t2.y*t2.y + cuv*t1.y*t2.y);
	        czz = -(cuu*t1.z*t1.z + cvv*t2.z*t2.z + cuv*t1.z*t2.z);
	        
	        cxy = -(2.0f*(cuu*t1.x*t1.y + cvv*t2.x*t2.y) + cuv*(t1.x*t2.y + t1.y*t2.x));
	        cyz = -(2.0f*(cuu*t1.y*t1.z + cvv*t2.y*t2.z) + cuv*(t1.y*t2.z + t1.z*t2.y));
	        czx = -(2.0f*(cuu*t1.z*t1.x + cvv*t2.z*t2.x) + cuv*(t1.z*t2.x + t1.x*t2.z));
	        
	        float tx = averageN.x - cu*t1.x - cv*t2.x;
	        float ty = averageN.y - cu*t1.y - cv*t2.y;
	        float tz = averageN.z - cu*t1.z - cv*t2.z;
	        
	        cx = tx - cxy*averageP.y - czx*averageP.z - 2.0f*cxx*averageP.x;
	        cy = ty - cyz*averageP.z - cxy*averageP.x - 2.0f*cyy*averageP.y;
	        cz = tz - czx*averageP.x - cyz*averageP.y - 2.0f*czz*averageP.z;
	        
	        c0 = -c0 - tx*averageP.x - ty*averageP.y - tz*averageP.z 
	          + cxy*averageP.x*averageP.y + cyz*averageP.y*averageP.z + czx*averageP.z*averageP.x
	            + cxx*averageP.x*averageP.x + cyy*averageP.y*averageP.y + czz*averageP.z*averageP.z;
	        
	        localFunction = new float[]{cxx, cyy, czz, cxy, cyz, czx, cx, cy, cz, c0};
		
	}
	
	
	public float localFunctionValue(Point3f p){
	    return localFunction[9] + 
	    	      (localFunction[6] + localFunction[0]*p.x + localFunction[3]*p.y)*p.x +
	    	        (localFunction[7] + localFunction[1]*p.y + localFunction[4]*p.z)*p.y +
	    	          (localFunction[8] + localFunction[2]*p.z + localFunction[5]*p.x)*p.z;
		
		
	}

	public Vector3f gradient(Point3f p){
		return new Vector3f(
	    localFunction[6] + 2.0f*localFunction[0]*p.x + localFunction[3]*p.y + localFunction[5]*p.z,
	    localFunction[7] + 2.0f*localFunction[1]*p.y + localFunction[3]*p.x + localFunction[4]*p.z,
	    localFunction[8] + 2.0f*localFunction[2]*p.z + localFunction[4]*p.y + localFunction[5]*p.x);
	}
	
	public float getRadius() {
		return radius;
	}


	public void setRadius(float radius) {
		this.radius = radius;
	}


	public float getRadiusOriginal() {
		return radiusOriginal;
	}


	public Point3f getCenter() {
		return center;
	}


	public float getWidth() {
		return width;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public LinkedList<Cell> getChildren() {
		return children;
	}

	public void setChild(Cell child) {
		this.children.add(child);
	}

	public Cell getParent() {
		return parent;
	}

	public void setParent(Cell parent) {
		this.parent = parent;
	}

	public LinkedList<Float> getLocalFunction() {
		LinkedList<Float> list = new LinkedList<Float>();
		
		for(float f : localFunction)list.add(f);
		
		return list;
	}

	public void setLocalFunction(float[] localFunction) {
		this.localFunction = localFunction;
	}

	public float getError() {
		return error;
	}

	public void setError(float error) {
		this.error = error;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

}
