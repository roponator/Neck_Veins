package si.uni_lj.fri.veins3D.gui.settings;

import java.io.Serializable;

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
}
