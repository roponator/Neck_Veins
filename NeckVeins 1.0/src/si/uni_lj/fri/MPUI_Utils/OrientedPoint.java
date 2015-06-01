package si.uni_lj.fri.MPUI_Utils;

import org.lwjgl.util.vector.Vector3f;

public class OrientedPoint {
		private Point3f point;
		private Vector3f vector;
		private int index;
		
		public OrientedPoint(Point3f point, Vector3f vector, int index){
			this.point = point;
			this.vector = vector;
			this.index = index;
		}
		
		
		public Point3f getPoint() {
			return point;
		}
		public void setPoint(Point3f point) {
			this.point = point;
		}
		public Vector3f getVector() {
			return vector;
		}
		public void setVector(Vector3f vector) {
			this.vector = vector;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		
		
		
		
	
}
