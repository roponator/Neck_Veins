package si.uni_lj.fri.Bloomenthal;

import java.util.LinkedList;
import java.util.concurrent.Callable;

import si.uni_lj.fri.MPUI_Utils.ImplicitInterface;
import si.uni_lj.fri.MPU_Implicits.Cell;



public class BloomenthalCallableMC implements Callable<BloomenthalMC>{
	
	private ImplicitInterface gi;
	private float cubeSize;
	private int minBound;
	private int maxBound;
	private int xDim;
	private int yDim;
	private int zDim;
	
	public BloomenthalCallableMC(float cubeSize, ImplicitInterface globalImplicit, int minBound, int maxBound, int xDim, int yDim, int zDim){
		gi = globalImplicit;
		this.cubeSize = cubeSize;
		this.minBound = minBound;
		this.maxBound = maxBound;
		this.xDim = xDim;
		this.yDim = yDim;
		this.zDim = zDim;
		
	}
	
	@Override
	public BloomenthalMC call() throws Exception {
		BloomenthalMC polygonized = new BloomenthalMC(gi, cubeSize, minBound, maxBound, xDim, yDim, zDim);
		return polygonized;
	}
}
