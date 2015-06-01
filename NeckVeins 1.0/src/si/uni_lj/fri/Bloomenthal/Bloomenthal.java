package si.uni_lj.fri.Bloomenthal;


import java.util.LinkedList;

import org.lwjgl.util.vector.Vector3f;

import si.uni_lj.fri.MPUI_Utils.ImplicitInterface;
import si.uni_lj.fri.MPUI_Utils.Point3f;

public class Bloomenthal {
	private LinkedList<Point3f> points = new LinkedList<Point3f>();
	private LinkedList<Vector3f> normals = new LinkedList<Vector3f>();
	private float xDim;
	private float yDim;
	private float zDim;
	private float max;
	private float diff;
	private float cellDim = 0.66f;
	
	public Bloomenthal(ImplicitInterface ii, float cubeSize, float minBound, float maxBound, float xDim, float yDim, float zDim){
		diff = cubeSize*3f;
		this.xDim = xDim;
		this.yDim = yDim;
		this.zDim = zDim;
		float xResolution = 1/cubeSize;
		float yResolution = yDim/xDim/cubeSize;
		float zResolution = zDim / xDim/cubeSize;
		max = xDim;
		
		if(xDim < yDim && yDim > zDim){
			xResolution = xDim/yDim/cubeSize;
			yResolution = 1/cubeSize;
			zResolution = zDim / yDim/cubeSize;
			max = yDim;
		}
		else if(xDim < zDim){ 
			xResolution = xDim/zDim/cubeSize;
			yResolution = yDim/zDim/cubeSize;
			zResolution = 1/cubeSize;
			max = zDim;
		}
		
		
		float dx = -0.5f + 0.5f * (1 - xDim/max)- cubeSize;
		float dy = -0.5f + 0.5f * (1 - yDim/max)- cubeSize;
		float dz = -0.5f + 0.5f * (1 - zDim/max)- cubeSize;
		
		minBound = (float) Math.ceil(minBound / xResolution / cubeSize);
		maxBound = (float) Math.ceil(maxBound / xResolution / cubeSize);
		int xRes = (int) Math.ceil(maxBound - minBound)+2;
		int yRes = (int) yResolution+2;
		int zRes = (int) zResolution + 2;
		float[][][] cornerMatrix = new float[xRes+1][yRes+1][zRes+1];
		
		int iterator = 0;
		for(int i = (int) minBound; i<maxBound+1; i++){
			for(int j = 0; j<yRes+1; j++){
				for(int k = 0; k<zRes+1; k++){
					cornerMatrix[iterator][j][k] = ii.globalFunctionValue(new Point3f(dx + i*cubeSize, dy + j*cubeSize, dz + k*cubeSize));	
				}					
			}
			iterator++;
		}


		iterator = 0;
		for(int i = (int)  minBound; i<maxBound; i++){
			for(int j = 0; j<yRes; j++){
				for(int k = 0; k<zRes; k++){
					Cube c = new Cube(new Point3f(dx + i*cubeSize, dy + j*cubeSize, dz + k*cubeSize), cubeSize, ii, diff);
					c.cornerValues[0] = cornerMatrix[iterator]	[j]		[k];
					c.cornerValues[1] = cornerMatrix[iterator]	[j]		[k+1];
					c.cornerValues[2] = cornerMatrix[iterator]	[j+1]	[k];
					c.cornerValues[3] = cornerMatrix[iterator]	[j+1]	[k+1];
					c.cornerValues[4] = cornerMatrix[iterator+1][j]		[k];
					c.cornerValues[5] = cornerMatrix[iterator+1][j]		[k+1];
					c.cornerValues[6] = cornerMatrix[iterator+1][j+1]	[k];
					c.cornerValues[7] = cornerMatrix[iterator+1][j+1]	[k+1];

					c.tetrahedralDecomposition();
					points.addAll(c.getPoints());
					normals.addAll(c.getNormals());
				}
				
				
			}
			iterator++;
			if(minBound == 0)System.out.println("Progress: :"+ (float)i/xResolution);
			
		}
	}
	
	
	public float[] getVertices(){
		float[] vertices = new float[points.size() * 3];
		int i = 0;
		float hxDim = xDim * 0.5f*cellDim;
		float hyDim = yDim * 0.5f*cellDim;
		float hzDim = zDim * 0.5f*cellDim;
		for( Point3f p : points) {
			vertices[i] = p.x*max * cellDim + hxDim;
			vertices[i + 1] = p.y*max * cellDim + hyDim;
			vertices[i + 2] = p.z*max * cellDim + hzDim;
			i = i+3;
			
			
		}
		return vertices;
	}
	public float[] getNormals(){
		float[] outputNormals = new float[normals.size() * 3];
		int i = 0;
		for( Vector3f v : normals) {
			outputNormals[i] = v.x;
			outputNormals[i + 1] = v.y;
			outputNormals[i + 2] = v.z;
			i = i+3;
			
			
		}
		return outputNormals;
	}
}


class Cube{	
	
	Point3f center;
	private ImplicitInterface ii;
	private LinkedList<Point3f> points = new LinkedList<Point3f>();
	private LinkedList<Vector3f> normals = new LinkedList<Vector3f>();
	private float diff;
	float[] cornerValues = new float[8];
	Point3f[] corners = new Point3f[8];
	
	public LinkedList<Point3f> getPoints() {
		return points;
	}


	public void setPoints(LinkedList<Point3f> points) {
		this.points = points;
	}

	public LinkedList<Vector3f> getNormals() {
		return normals;
	}


	public void setNormals(LinkedList<Vector3f> normals) {
		this.normals = normals;
	}

	
	public Cube(Point3f start, float cubeSize, ImplicitInterface ii, float diff ){
		this.diff = diff;
		float half = cubeSize*0.5f;
		this.ii = ii;
		center = start;
		corners[0] = new Point3f(start.x - half, start.y - half, start.z - half);
		corners[1] = new Point3f(start.x - half, start.y - half, start.z + half);
		corners[2] = new Point3f(start.x - half, start.y + half, start.z - half);
		corners[3] = new Point3f(start.x - half, start.y + half, start.z + half);
		corners[4] = new Point3f(start.x + half, start.y - half, start.z - half);
		corners[5] = new Point3f(start.x + half, start.y - half, start.z + half);
		corners[6] = new Point3f(start.x + half, start.y + half, start.z - half);
		corners[7] = new Point3f(start.x + half, start.y + half, start.z + half);
		
		
		

	}
	// 0 LBN 1 LBF 2 LTN 3 LTF 4 RBN 5 RBF 6 RTN 7 RTF
	public void tetrahedralDecomposition(){		
		tetrahedra(0, 2, 4, 1) ;
		tetrahedra(6, 2, 1, 4) ;
		tetrahedra( 6, 2, 3, 1) ;
		tetrahedra(6, 4, 1, 5) ;
		tetrahedra( 6, 1, 3, 5) ;
		tetrahedra(6, 3, 7, 5 ) ;				
	}
	
	
	private void tetrahedra(int a, int b, int c, int d){
		
		int index = 0;
		boolean apos = false,  bpos =  false, cpos = false, dpos = false;
		if (cornerValues[a] >= 0){
			apos = true;
			index += 8;
		}
	    if (cornerValues[b] >= 0){
	    	bpos = true;
	    	index += 4;
	    }
	    if (cornerValues[c] >= 0){
	    	cpos = true;
	    	index += 2;
	    }
	    if (cornerValues[d] >= 0){
	    	dpos = true;
	    	index += 1;
	    }
	    
	    Point3f e1 = null, e2 = null, e3= null, e4= null, e5= null, e6= null;
	    Vector3f n1 = null, n2 = null, n3= null, n4= null, n5= null, n6= null;
	    if (apos != bpos){
	    	
	    	e1 = vertid(a, b);
	    	n1 = normalid(e1);
	    }
	    if (apos != cpos){
	    	e2 = vertid(a, c);
	    	n2 = normalid(e2);
	    }
	    if (apos != dpos){
	    	e3 = vertid(a, d);
	    	n3 = normalid(e3);
	    }
	    if (bpos != cpos){
	    	e4 = vertid(b, c);
	    	n4 = normalid(e4);
	    }
	    if (bpos != dpos){
	    	e5 = vertid(b, d);
	    	n5 = normalid(e5);
	    }
	    if (cpos != dpos){
	    	e6 = vertid(c, d);
	    	n6 = normalid(e6);
	    }
	    
	    
	    switch(index){
	    case 1:
	    	points.add(e5);	    	normals.add(n5);
	    	points.add(e6);	    	normals.add(n6);
	    	points.add(e3);	    	normals.add(n3);

	    	break;
	    case 2:
	    	points.add(e2);	    	normals.add(n2);
	    	points.add(e6);	    	normals.add(n6);
	    	points.add(e4);	  		normals.add(n4);

	    	break;
	    case 3:
	    	points.add(e3);	   		normals.add(n3);
	    	points.add(e5);	    	normals.add(n5);
	    	points.add(e4);	    	normals.add(n4);
	    	points.add(e3);	    	normals.add(n3);
	    	points.add(e4);	    	normals.add(n4);
	    	points.add(e2);	    	normals.add(n2);

	    	break;
	    case 4:
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e4);	    	normals.add(n4);
	    	points.add(e5);	    	normals.add(n5);

	    	break;
	    case 5:
	    	points.add(e3);	    	normals.add(n3);
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e4);	    	normals.add(n4);
	    	points.add(e3);	    	normals.add(n3);
	    	points.add(e4);	    	normals.add(n4);
	    	points.add(e6);	    	normals.add(n6);
	    	
	    	break;
	    case 6:
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e2);	    	normals.add(n2);
	    	points.add(e6);	    	normals.add(n6);
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e6);	    	normals.add(n6);
	    	points.add(e5);	    	normals.add(n5);

	    	break;
	    case 7:
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e2);	    	normals.add(n2);
	    	points.add(e3);	    	normals.add(n3);
	    	break;
	    case 8:
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e3);	    	normals.add(n3);
	    	points.add(e2);	    	normals.add(n2);

	    	break;
	    case 9:
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e5);	    	normals.add(n5);
	    	points.add(e6);	    	normals.add(n6);
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e6);	    	normals.add(n6);
	    	points.add(e2);	    	normals.add(n2);
	    	break;
	    case 10:
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e3);	    	normals.add(n3);
	    	points.add(e6);	    	normals.add(n6);
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e6);	    	normals.add(n6);
	    	points.add(e4);	    	normals.add(n4);
	    	break;
	    case 11:
	    	points.add(e1);	    	normals.add(n1);
	    	points.add(e5);	    	normals.add(n5);
	    	points.add(e4);	    	normals.add(n4);
	    	break;	    
	    case 12:
	    	points.add(e3);	    	normals.add(n3);
	    	points.add(e2);	    	normals.add(n2);
	    	points.add(e4);	    	normals.add(n4);
	    	points.add(e3);	    	normals.add(n3);
	    	points.add(e4);	    	normals.add(n4);
	    	points.add(e5);	    	normals.add(n5);
	    	break;	 
	    case 13:
	    	points.add(e6);	    	normals.add(n6);
	    	points.add(e2);	    	normals.add(n2);
	    	points.add(e4);	    	normals.add(n4);
	    	break;
	    case 14:
	    	points.add(e5);	    	normals.add(n5);
	    	points.add(e3);	    	normals.add(n3);
	    	points.add(e6);	    	normals.add(n6);
	    	break;	 	 
	    }
	    
	}
	
	private Vector3f normalid(Point3f p){
		double f = ii.globalFunctionValue(p);
		Vector3f v = new Vector3f();
	    v.x = (float) (((double)ii.globalFunctionValue(new Point3f(p.x+diff, p.y, p.z)))-f);
	    v.y = (float) (((double)ii.globalFunctionValue(new Point3f(p.x, p.y+diff, p.z)))-f);
	    v.z = (float) (((double)ii.globalFunctionValue(new Point3f(p.x, p.y, p.z+diff)))-f);
	    f =  Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
	    
	    if (f != 0.0) {
	    	f = 1/f;
	    	v.x *= f;
	    	v.y *= f; 
	    	v.z *= f;
	    }
	    return v;
	}
	
	private Point3f vertid( int a, int b){
		Point3f vertex = new Point3f(0.5f*(corners[a].x + corners[b].x), 0.5f*(corners[a].y + corners[b].y), 0.5f*(corners[a].z + corners[b].z) );
		float v = cornerValues[a];
		
		int i = 0;
	    Point3f pos, neg;
	    if (v < 0) {
	    	pos = new Point3f(corners[b].x, corners[b].y, corners[b].z); 
	    	neg = new Point3f(corners[a].x, corners[a].y, corners[a].z); 
	    }
	    else {
	    	pos = new Point3f(corners[a].x, corners[a].y, corners[a].z); 
	    	neg = new Point3f(corners[b].x, corners[b].y, corners[b].z); 
	    } 
	    while (true) {
			vertex.x = 0.5f*(pos.x + neg.x);
			vertex.y = 0.5f*(pos.y + neg.y);
			vertex.z = 0.5f*(pos.z + neg.z);
			if (i++ == 5) return vertex;
			if ((ii.globalFunctionValue(vertex)) > 0.0)
			     {pos.x = vertex.x; pos.y = vertex.y; pos.z = vertex.z;}
			else {neg.x = vertex.x; neg.y = vertex.y; neg.z = vertex.z;}
	    }
		
	}
	
}


