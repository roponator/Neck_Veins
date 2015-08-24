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
	public int sensitivity = 100;
	public String workingDirectory;
	public int leapSensitivity = 75; 
	
	public float gaussSigma = 0.5f;
	public float threshold = 0.5f;
	public int selectedModelMethodIndex = 0;
}
