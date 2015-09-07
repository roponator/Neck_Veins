package si.uni_lj.fri.veins3D.gui.settings;

import java.io.Serializable;
import java.util.Locale;

public class NeckVeinsSettings implements Serializable {
	private static final long serialVersionUID = 1L;
	public int resWidth;
	public int resHeight;
	public int bitsPerPixel;
	public int frequency;
	public boolean isFpsShown;
	public boolean fullscreen;
	public boolean stereoEnabled;
	public int stereoValue;
	public Locale locale;
	public boolean mTrans;
	public boolean mRot;
	public boolean mSelected;
	public boolean mStrong;
	public int sensitivity = 50;
	public String workingDirectory;
	public int leapSensitivity = 75; 
	
	public float gaussSigma = 0.5f;
	public float threshold = 0.5f;
	public int selectedModelMethodIndex = 0;
	public int volumeRenderMethod = 0; // iso, alpha, max projection
	
	public boolean useModelMoveMode = false; // if true the model is moved instead of camera on input
	
	/* Approximation radius - bigger radius smoother objects - slower algorithm */
	public static float MPUI__APLHA = 0.7f;
		
	/* Approximation error, dont set it too low. */
	public static float MPUI__ERROR = 0.005f;
	
	/* Polygonization resolution, 0.01 means 100x100 */
	public static float MPUI__RESOLUTION = 0.01f;
	
	public static int MPUI__SAMPLE_SIZE = 200;
	
	/* Draw point cloud instead of MPUI or MC */
	public static boolean MPUI__POINT_CLOUD = false;
	
	
	public static float VOLUME_RENDER_GAUSS_SIGMA = 0.5f;
	
	
	
}
