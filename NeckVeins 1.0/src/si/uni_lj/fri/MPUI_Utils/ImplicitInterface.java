package si.uni_lj.fri.MPUI_Utils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import si.uni_lj.fri.MPU_Implicits.Cell;

public class ImplicitInterface {

	public Cell root;
	private float value;
	private float weightSum;
	public  LinkedList<Cell[]> cellList;
	private float[] crBuff;
	private float[] fnBuff;
	private int[] otBuff;
	private int[] lnBuff;
	
	
	public ImplicitInterface(Cell c){
	    root = new Cell(new Point3f(0, 0, 0), 1f, (float) (Math.sqrt(3)*0.75));
		root.setChild(c);
	}

	public float globalFunctionValue(Point3f p){
		value = 0;
		float temp;
		octreeSearch(root.getChildren().getFirst().getChildren().getFirst(), p); 
		

		temp = value/weightSum;
		return temp;

	}
	
	private void octreeSearch(Cell c, Point3f p){
		if(p.getUnsquaredDistance(c.getCenter()) < c.getRadiusOriginal() * c.getRadiusOriginal()){
			if(c.isLeaf()){
				float w = MathUtils.weight(p, c.getCenter(), c.getRadius());
				
					value += c.localFunctionValue(p)*w;
					weightSum += w;
			}
			else{
				for(Cell child : c.getChildren()) octreeSearch(child, p);
			}
			
		}
		
	}
	
	public void primitiveTreeConstruction(){
		LinkedList<Cell> list = new LinkedList<Cell>();
		LinkedList<Integer> output = new LinkedList<Integer>();
		LinkedList<Integer> linksList = new LinkedList<Integer>();		
		LinkedList<Float> functionsList = new LinkedList<Float>();	
		LinkedList<Float> centeradius = new LinkedList<Float>();
		
		list.add(root.getChildren().getFirst());
		int count = 1;
		int iter = 0;
		int functionIter = 0;
		while(!list.isEmpty()){
			Cell c = list.getFirst();
			centeradius.add(c.getCenter().x);
			centeradius.add(c.getCenter().y);
			centeradius.add(c.getCenter().z);
			centeradius.add(c.getRadiusOriginal());
			if(c.isLeaf()){
				output.add(0);
				linksList.add(functionIter);
				functionIter += 10;
				functionsList.addAll(c.getLocalFunction());
			}else{
				list.addAll(c.getChildren());
				output.add(count);
				count = count + 8;
				linksList.add(-1);		
			}
			list.removeFirst();
			iter++;
		}
		System.out.println(functionIter +" fsize "+functionsList.size()+"centeradius "+centeradius.size()+" output s "+output.size() +"haha"+linksList.size());
		lnBuff = new int[linksList.size()];
		fnBuff = new float[functionsList.size()];
		otBuff = new int[output.size()];
		crBuff = new float[centeradius.size()];
		

		
		
		for(int i = 0; i<lnBuff.length; i++){
			lnBuff[i] = linksList.getFirst();
			otBuff[i] = output.getFirst();
			crBuff[i*4] = centeradius.getFirst();
			linksList.removeFirst();
			output.removeFirst();
			centeradius.removeFirst();
			crBuff[i*4+1] = centeradius.getFirst();
			centeradius.removeFirst();
			crBuff[i*4+2] = centeradius.getFirst();
			centeradius.removeFirst();
			crBuff[i*4+3] = centeradius.getFirst();
			centeradius.removeFirst();			
		}
		int i = 0;
		for(Float f : functionsList){
			fnBuff[i] = f;
			i++;
		}

	}

	public float[] getCrBuff() {
		return crBuff;
	}

	public void setCrBuff(float[] crBuff) {
		this.crBuff = crBuff;
	}

	public float[] getFnBuff() {
		return fnBuff;
	}

	public void setFnBuff(float[] fnBuff) {
		this.fnBuff = fnBuff;
	}

	public int[] getOtBuff() {
		return otBuff;
	}

	public void setOtBuff(int[] otBuff) {
		this.otBuff = otBuff;
	}

	public int[] getLnBuff() {
		return lnBuff;
	}

	public void setLnBuff(int[] lnBuff) {
		this.lnBuff = lnBuff;
	}

	public Cell getRoot() {
		return root;
	}

	public void setRoot(Cell root) {
		this.root = root;
	}


	
	
	
}
