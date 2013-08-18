package si.uni_lj.fri.mhdreader;

import org.lwjgl.LWJGLException;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ModelCreator.createModel("Pat2_3D-DSA");
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

	}

}
