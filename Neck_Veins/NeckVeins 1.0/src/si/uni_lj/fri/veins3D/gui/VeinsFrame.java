package si.uni_lj.fri.veins3D.gui;

import java.io.File;
import java.util.ResourceBundle;

import org.lwjgl.LWJGLException;
import org.lwjgl.opencl.OpenCLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import si.uni_lj.fri.segmentation.ModelCreatorSettings;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.gui.render.models.VeinsModel;
import si.uni_lj.fri.veins3D.gui.render.models.VeinsModelMesh;

public class VeinsFrame
{
	private enum Message
	{
		FALLBACK, IMPORT, LOADING;
	}

	private enum MeshGenerationMethod
	{
		MARCHING_CUBES_OR_OBJ, MPUI
	}


	private boolean isDialogOpened;
	private int selectedResolution;
	private String[] displayModeStrings;
	private DisplayMode[] displayModes;
	private DisplayMode currentDisplayMode;


	private MeshGenerationMethod meshGenerationMethod = MeshGenerationMethod.MARCHING_CUBES_OR_OBJ;

	// NEW UI

	// NEW UI END

	public VeinsFrame() throws LWJGLException
	{
		getDisplayModes();
		initGUI();

	
	}

	private void getDisplayModes() throws LWJGLException
	{
		displayModes = Display.getAvailableDisplayModes();
		currentDisplayMode = Display.getDisplayMode();
	}

	private void initGUI()
	{
		
	}




}
