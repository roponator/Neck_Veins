package main;

import java.io.Serializable;

public class NeckVeinsSettings implements Serializable {
	private static final long serialVersionUID = 1L;
	int resWidth;
	int resHeight;
	int bitsPerPixel;
	int frequency;
	boolean isFpsShown;
	boolean fullscreen;
	boolean stereoEnabled;
	int stereoValue;
}
