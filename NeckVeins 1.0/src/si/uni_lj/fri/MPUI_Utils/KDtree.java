package si.uni_lj.fri.MPUI_Utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import org.lwjgl.util.vector.Vector3f;

public class KDtree {
	
	OrientedPoint[] ops;
	Node root;
	
	public KDtree(Node n){
		root = n;
		
	}
	
	public KDtree(Vector3f[] normals, Point3f[] points){
		ops = new OrientedPoint[points.length];
		for(int i = 0; i< points.length; i++){
			ops[i] = new OrientedPoint(points[i], normals[i], i);
			
			
		}
		OrientedPoint[] sortedX;
		OrientedPoint[] sortedY;
		OrientedPoint[] sortedZ;
		sortedX = ops.clone();
		sortedY = ops.clone();
		sortedZ = ops.clone();
		OrientedPoint[][] sortedPoints = new OrientedPoint[3][];
		sortedPoints[0] = sortedX;
		sortedPoints[1] = sortedX;		
		sortedPoints[2] = sortedX;	
		
		Arrays.sort(sortedX, compareX() );
		Arrays.sort(sortedY, compareY() );
		Arrays.sort(sortedZ, compareZ() );
		
		root = buildKDtree(0, sortedPoints);
		
	}
	

	private Node buildKDtree(int depth, OrientedPoint[][] sortedPoints){
		int axis = depth % 3;
		
		if(sortedPoints[axis].length == 1) return new Node(sortedPoints[axis][0]); 
		else if( sortedPoints[axis].length == 0 ) return null;
		
		int indexMedian = (int)(sortedPoints[axis].length*0.5);
		OrientedPoint median = sortedPoints[axis][indexMedian];
		
		while(indexMedian > 0 && median.getPoint().getAxis(axis) == sortedPoints[axis][indexMedian-1].getPoint().getAxis(axis)){
			median =  sortedPoints[axis][indexMedian-1];
			indexMedian = indexMedian - 1;
		}
		float value = median.getPoint().getAxis(axis);
		Node n = new Node(median);
		OrientedPoint[][] subsetsLeft = new OrientedPoint[3][];
		OrientedPoint[][] subsetsRight = new OrientedPoint[3][];

		for(int i = 0; i<3; i++){
			CompositeList cl = new CompositeList();
			LinkedList<OrientedPoint> left = cl.getList(axis, true);
			LinkedList<OrientedPoint> right = cl.getList(axis, false);
			for(int j = 0; j<sortedPoints[i].length; j++){
				if(sortedPoints[i][j].getIndex() == median.getIndex()){
					continue;
				}
				else if(sortedPoints[i][j].getPoint().getAxis(axis) > value){
					right.add(sortedPoints[i][j]);
				}else{
					left.add(sortedPoints[i][j]);
					
				}
			}
			subsetsRight[i] = right.toArray(new OrientedPoint[right.size()]);
			subsetsLeft[i] = left.toArray(new OrientedPoint[left.size()]);
		}
		
		
		n.setChildLeft(buildKDtree(depth+1, subsetsLeft));
		n.setChildRight(buildKDtree(depth+1, subsetsRight));	

		return n;
	}
	
	public int[] findNeighbours(Point3f center, float radius){
		LinkedList<Integer> indexes = checkNode(root, center, radius, 0);
		int[] array = new int[indexes.size()];
		int index = 0;
		for(int i : indexes){
			array[index] = i;
			index++;
		}
		return array;
		
	}
	
	private LinkedList<Integer> checkNode(Node n, Point3f c, float r, int axis){

		LinkedList<Integer> indexes = new LinkedList<Integer>();
		if(n == null) return indexes;
		axis = axis % 3;
		
		
		float dim = n.getOp().getPoint().getAxis(axis);

		
		if(dim - c.getAxis(axis) > r){
			indexes.addAll(checkNode(n.getChildLeft(), c, r, axis+1));
			
			
		}else if( dim - c.getAxis(axis) < -r ){
			indexes.addAll(checkNode(n.getChildRight(), c, r, axis+1));
			
		}else{


			indexes.addAll(checkNode(n.getChildRight(), c, r, axis+1));
			indexes.addAll(checkNode(n.getChildLeft(), c, r, axis+1));
			
			if(n.getOp().getPoint().getUnsquaredDistance(c) < r*r) indexes.add(n.getOp().getIndex()) ;
			
		}
		return indexes;
		
	}
	
	
    static Comparator<OrientedPoint> compareX() {
        return new Comparator<OrientedPoint>() {
            @Override
            public int compare(OrientedPoint o1, OrientedPoint o2) {
                return ((Float)o1.getPoint().x).compareTo(o2.getPoint().x);
            }
        };
    }

    static Comparator<OrientedPoint> compareY() {
        return new Comparator<OrientedPoint>() {
            @Override
            public int compare(OrientedPoint o1, OrientedPoint o2) {
                return ((Float)o1.getPoint().y).compareTo(o2.getPoint().y);
            }
        };
    }
    static Comparator<OrientedPoint> compareZ() {
        return new Comparator<OrientedPoint>() {
            @Override
            public int compare(OrientedPoint o1, OrientedPoint o2) {
                return ((Float)o1.getPoint().z).compareTo(o2.getPoint().z);
            }
        };
    }


	public Node getRoot() {
		return root;
	}


	public void setRoot(Node root) {
		this.root = root;
	}
}

class CompositeList{
	LinkedList<OrientedPoint> listXL = new LinkedList<OrientedPoint>();
	LinkedList<OrientedPoint> listYL = new LinkedList<OrientedPoint>();
	LinkedList<OrientedPoint> listZL = new LinkedList<OrientedPoint>();
	
	LinkedList<OrientedPoint> listXR = new LinkedList<OrientedPoint>();
	LinkedList<OrientedPoint> listYR = new LinkedList<OrientedPoint>();
	LinkedList<OrientedPoint> listZR = new LinkedList<OrientedPoint>();	
	
	
	 public LinkedList<OrientedPoint> getList(int axis, boolean left){
		 if(left){
			 if(axis == 0) return listXL;
			 else if(axis == 1)return listYL;
			 else return listZL;
		 }
		 if(axis == 0) return listXR;
		 else if(axis == 1)return listYR;
		 else return listZR;
	 }
	
}


class Node{
	private Node childLeft;
	private Node childRight;
	private OrientedPoint op;
	
	public Node(OrientedPoint op){
		this.op = op;
	}
	
	public Node getChildLeft() {
		return childLeft;
	}
	public void setChildLeft(Node childLeft) {
		this.childLeft = childLeft;
	}
	public Node getChildRight() {
		return childRight;
	}
	public void setChildRight(Node childRight) {
		this.childRight = childRight;
	}
	public OrientedPoint getOp() {
		return op;
	}
	public void setOp(OrientedPoint op) {
		this.op = op;
	}
	
	
}

