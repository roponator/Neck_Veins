package si.uni_lj.fri.MPUI_Utils;

public class Point3f {
	public float x;
	public float y;
	public float z;
	
	
	
	public Point3f(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
		
	}
	
	public void setPoints(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
		
		
	}
	
	public float getDistance(Point3f p){
		float d = (x - p.x)*(x - p.x) + (y - p.y)*(y - p.y) + (z - p.z)*(z - p.z);
		d = (float) Math.sqrt(d);
		return d;
		
	}
	
	public float getUnsquaredDistance(Point3f p){
		float d = (x - p.x)*(x - p.x) + (y - p.y)*(y - p.y) + (z - p.z)*(z - p.z);
		return d;
		
	}
	
	 public void sum(float x, float y, float z){
		 this.x += x;
		 this.y += y;
		 this.z += z;
		 
	 }
	 public Point3f getSum(float x, float y, float z){
		return new Point3f(this.x + x, this.y - y, this.z - z);
		 
	 }
	public Point3f sub(Point3f s){
		return new Point3f(this.x - s.x, this.y - s.y, this.z - s.z);
		
	}
	 
	 
	 public void scale(float s){
		 this.x *= s;
		 this.y *= s;
		 this.z *= s;
	 }
	 
	 @Override public String toString() {
		    String s = "point "+x + " " + y + " " + z;
		    return s;
		  }
	 
	 public float getAxis(int axis){
		 if(axis == 0) return x;
		 else if(axis == 1)return y;
		 else return z;
		 
	 }

}
