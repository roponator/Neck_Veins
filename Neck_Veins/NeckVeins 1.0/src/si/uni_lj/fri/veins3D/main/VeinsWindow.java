package si.uni_lj.fri.veins3D.main;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import static org.lwjgl.opengl.ARBBufferObject.glBindBufferARB;
import static org.lwjgl.opengl.ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB;
import static org.lwjgl.opengl.ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glViewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.util.ResourceLoader;

import com.apple.eawt.Application;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.builder.EffectBuilder;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.nulldevice.NullSoundDevice;
import de.lessvoid.nifty.render.batch.BatchRenderConfiguration;
import de.lessvoid.nifty.render.batch.BatchRenderDevice;
import de.lessvoid.nifty.render.batch.core.BatchRenderBackendCoreProfileInternal;
import de.lessvoid.nifty.render.batch.spi.BatchRenderBackend;
import de.lessvoid.nifty.render.batch.spi.GL;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglBatchRenderBackendCoreProfileFactory;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglBatchRenderBackendFactory;
import de.lessvoid.nifty.screen.DefaultScreenController;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;
import de.lessvoid.nifty.tools.Color;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.resourceloader.NiftyResourceLoader;
import si.uni_lj.fri.veins3D.exceptions.ShaderLoadException;
import si.uni_lj.fri.veins3D.gui.HUD;
import si.uni_lj.fri.veins3D.gui.GUIMain;
import si.uni_lj.fri.veins3D.gui.NiftyScreenController;
import si.uni_lj.fri.veins3D.gui.NiftyScreenController.GUI_STATE;
import si.uni_lj.fri.veins3D.gui.render.StencilMask;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.gui.settings.NeckVeinsSettings;
import si.uni_lj.fri.veins3D.math.Quaternion;
import si.uni_lj.fri.veins3D.utils.Mouse3D;
import si.uni_lj.fri.veins3D.utils.RayUtil;
import si.uni_lj.fri.veins3D.utils.SettingsUtil;
import static org.lwjgl.opengl.GL11.*;

// TODO: CLICK ON UI WHILE LOADING VOLUME MODEL CAUSES MULTIPLE LOAD

public class VeinsWindow
{
	public final static int CLICKED_ON_NOTHING = 0;
	public final static int CLICKED_ON_VEINS_MODEL = 1;
	public final static int CLICKED_ON_ROTATION_CIRCLE = 2;
	public final static int CLICKED_ON_MOVE_CIRCLE = 3;
	public final static int CLICKED_ON_ROTATION_ELLIPSE = 4;
	public final static int CLICKED_ON_MOVE_ELLIPSE = 5;
	public final static int CLICKED_ON_BUTTONS = 6;

	public static NeckVeinsSettings settings;

	public static VeinsRenderer renderer;
	LwjglInputSystem inputSystem;
	private HUD hud;
	private boolean isRunning;
	private String title;
	private int fps;
	private long timePastFrame;
	private long timePastFps;
	private int fpsToDisplay;
	private int clickedOn;
	public static DisplayMode[] displayModes;
	public static DisplayMode currentDisplayMode;
	public static DisplayMode m_lastWindowedResoltuon = null;
	public static Mouse3D joystick;

	public static boolean canModelBeRotatedByMouse = true; // false when click on some widget

	public static Nifty nifty = null; // So it can be accessed from outside
	public static GUIMain gui = null; // So it can be accessed from outside
	public static NiftyScreenController screenController = null;
	public static Frame frame = null; // Window frame
	public static VeinsWindow veinsWindow = null;// itself

	public static boolean increaseSubdivLevel = false;
	public static boolean decreaseSubdivLevel = false;

	public final static String defaultGradientFile = "gradient/defaultGrad.grad";

	public enum OSType {
	    Windows, MacOS, Linux, Other
	  };

	
	  public static OSType getOperatingSystemType()
	  {
		  OSType type = null;
	    if (type == null) {
	      String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
	      if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
	    	  type = OSType.MacOS;
	      } else if (OS.indexOf("win") >= 0) {
	    	  type = OSType.Windows;
	      } else if (OS.indexOf("nux") >= 0) {
	    	  type = OSType.Linux;
	      } else {
	    	  type = OSType.Other;
	      }
	    }
	    return type;
	  }
	

	boolean handleDeminimization = false;
	
	/**
	 * 
	 */
	public VeinsWindow()
	{
		init("Veins3D", "Veins3D");
	}

	/**
	 * @param title
	 */
	public VeinsWindow(String title, String fileName)
	{
		init(title, fileName);

	}

	public static int GetMouseX()
	{
		return Mouse.getX();
	}

	public static int GetMouseY()
	{
		return Mouse.getY();
	}

	void init(String Title, String filename)
	{
		veinsWindow = this;
		isRunning = true;
		this.title = Title;
		loadSettings(filename); // MUST BE BEFORE NIFTY GUI INIT, BECAUSE NIFTY CONTROLS RESTORE STATE FROM THIS!
		createDisplay();

		initNiftyAndGUI();
		initWindowElements();
		setupWindow();

	}

class MyWindowListener implements WindowListener
{

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) 
	{
		// this is other thread, not main so we cannot call stuff here directly
		handleDeminimization = true;		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

	void initNiftyAndGUI()
	{
		// Init nifty
		try
		{
			// MAC osx fix, nifty glortho has viewport 0, it fails, probablz the same on windows but it doesnt fail??
			GL11.glViewport(0, 0, currentDisplayMode.getWidth(), currentDisplayMode.getHeight());
			
			 Logger.getLogger("de.lessvoid.nifty").setLevel(Level.OFF); // spams console a lot otherwise
			inputSystem = new LwjglInputSystem();
			inputSystem.startup();

			// MUST NOT USE CORE PROFILE, FOR COMPATIBILITY
			BatchRenderBackend niftyRenderFactory = LwjglBatchRenderBackendFactory.create();
			//BatchRenderBackend niftyRenderFactory = LwjglBatchRenderBackendCoreProfileFactory.create();
			// niftyRenderFactory.useHighQualityTextures(true);

			BatchRenderDevice niftyRenderer = new BatchRenderDevice(niftyRenderFactory);
			// niftyRenderFactory.useHighQualityTextures(true);
			// niftyRenderer.resetTextureAtlases();

			nifty = new Nifty(niftyRenderer, new NullSoundDevice(), inputSystem, new AccurateTimeProvider());
			// niftyRenderFactory.useHighQualityTextures(true);

			// load our GUI
			//System.out.println("**NIFTY FROM XML*********************");
			nifty.fromXml("0", ResourceLoader.getResourceAsStream("xml/nifty_gui.xml"), "GScreen0");
			//System.out.println("**NIFTY FROM XML DONE*********************");

			// get screen controller
			screenController = (NiftyScreenController) nifty.getScreen("GScreen0").getScreenController();

			// enable auto scale
			// nifty.enableAutoScaling(1024,768);

		}
		catch (Exception e)
		{
			System.out.println("Nifty init error: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public static boolean IsMaximized()
	{
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		return currentDisplayMode.getWidth() == width && currentDisplayMode.getHeight() == height;
	}

	// Returns largest display mode or returns the first one if it fails.
	public static DisplayMode GetLargestDisplayMode()
	{
		// must create a smaller window if going fullscreen on mac because of the top menu bar
		boolean offsetY = getOperatingSystemType()!=OSType.Windows; 
		int titleBarHeight = 60;
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();

		DisplayMode displayModes[];
		try
		{
			displayModes = Display.getAvailableDisplayModes();
			for (int i = 0; i < displayModes.length; ++i)
			{
				if (displayModes[i].getWidth() == width && displayModes[i].getHeight() == height)
				{	
					if(offsetY)
					{
						DisplayMode dm=new DisplayMode(displayModes[i].getWidth(), displayModes[i].getHeight()- titleBarHeight);
						return dm;
					}
					else
						return displayModes[i];
				
				}
			}
			
			if(offsetY)
			{
				DisplayMode dm=new DisplayMode(displayModes[0].getWidth(), displayModes[0].getHeight()- titleBarHeight );
				return dm;
			}
			else
				return displayModes[0];
		}
		catch (LWJGLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return currentDisplayMode; // hacky, but should always return a valid display mode
	}

	// Contains all logic to resize window, renderer and niftyGUI
	public static void ResizeWindow(DisplayMode displayMode, boolean fullscreen)
	{
		// fullscreen causes problem on mac, use it only on windows
		if(getOperatingSystemType()!=OSType.Windows)
			fullscreen = false; 
		
		currentDisplayMode = displayMode;
		//FullScreenUtilities.setWindowCanFullScreen(frame.getWindows()[0],true);
		
		DisplayMode largestDM = GetLargestDisplayMode();

		if (fullscreen == false && displayMode.getWidth() < largestDM.getWidth() && displayMode.getHeight() < largestDM.getHeight())
			m_lastWindowedResoltuon = displayMode;

		try
		{
			Display.setDisplayMode(currentDisplayMode);
			Display.setFullscreen(fullscreen);
			VeinsWindow.settings.resWidth = currentDisplayMode.getWidth();
			VeinsWindow.settings.resHeight = currentDisplayMode.getHeight();
			VeinsWindow.settings.fullscreen = fullscreen;
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}
		renderer.setupView();
		frame.setPreferredSize(new Dimension(currentDisplayMode.getWidth(), currentDisplayMode.getHeight()));
		frame.setSize(currentDisplayMode.getWidth(), currentDisplayMode.getHeight());
		frame.pack();
		nifty.resolutionChanged();
		screenController.OnResize(currentDisplayMode);

		// Do this at the end when openGL has setup itself
		// THIS CAUSES THE WHITE SCREEN FLASH ON RESIZE IF YOU ARE USING VOLUME RENDERER:
		// openCL inits itself again, otherwise you get invalid clContext error.
		veinsWindow.RenderSingleFrameWithoutModel(); // FIX FOR WHITE SCREEN FLASH
		veinsWindow.RenderSingleFrameWithoutModel(); // FIX FOR WHITE SCREEN FLASH
		renderer.SetNewResolution(currentDisplayMode.getWidth(), currentDisplayMode.getHeight());

		try
		{
			Display.setFullscreen(fullscreen);
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}
		
		
		//if(fullscreen)
		//	Application.getApplication().requestToggleFullScreen(frame.getWindows()[0]); 
	}

	void loadSettings(String fileName)
	{
		try
		{
			displayModes = Display.getAvailableDisplayModes();
			// displayModeStrings = new String[displayModes.length];
			currentDisplayMode = GetLargestDisplayMode();
			settings = SettingsUtil.readSettingsFile(fileName);
			if (settings != null)	
			{
				for (DisplayMode mode : displayModes)
				{
					if (mode.getWidth() == settings.resWidth && mode.getHeight() == settings.resHeight && mode.getBitsPerPixel() == settings.bitsPerPixel && mode.getFrequency() == settings.frequency)
					{
						currentDisplayMode = mode;
					}
				}
			}
			else
			{
				settings = new NeckVeinsSettings();
				settings.isFpsShown = false;
				settings.fullscreen = false;
				settings.stereoEnabled = false;
				settings.stereoValue = 0;
				settings.locale = Locale.getDefault();
				settings.workingDirectory = "";
			}

			// ADD TO VM ARGS: -Dorg.lwjgl.opengl.Window.undecorated=true
			// currentDisplayMode = displayModes[6]; // TODO: REMOVE LATER ON
			// settings.fullscreen = true;
			settings.resWidth = currentDisplayMode.getWidth();
			settings.resHeight = currentDisplayMode.getHeight();
			settings.bitsPerPixel = currentDisplayMode.getBitsPerPixel();
			settings.frequency = currentDisplayMode.getFrequency();

			// fullscreen causes problem on mac, use it only on windows
			if(getOperatingSystemType()!=OSType.Windows)
				settings.fullscreen = false; 
			
			Display.setFullscreen(settings.fullscreen);
			Display.setVSyncEnabled(true);

			if (settings.fullscreen == false)
				m_lastWindowedResoltuon = currentDisplayMode;

		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	
	  public void removeMinMaxClose(Component comp)
	  {
	    if(comp instanceof AbstractButton)
	    {
	      comp.getParent().remove(comp);
	    }
	    if (comp instanceof Container)
	    {
	      Component[] comps = ((Container)comp).getComponents();
	      for(int x = 0, y = comps.length; x < y; x++)
	      {
	        removeMinMaxClose(comps[x]);
	      }
	    }
	  }


	private void createDisplay()
	{
		try
		{
			//JFrame.setDefaultLookAndFeelDecorated(true);
			frame = new Frame("Med3D");
			
			frame.setLayout(new BorderLayout());
		
			final Canvas canvas = new Canvas();
			frame.add(canvas, BorderLayout.CENTER);

			frame.setPreferredSize(new Dimension(currentDisplayMode.getWidth(), currentDisplayMode.getHeight()));
			frame.setSize(currentDisplayMode.getWidth(), currentDisplayMode.getHeight());
			 // here
			
			frame.setUndecorated(true);		
			frame.pack();
			frame.setVisible(true);
			
			frame.addWindowListener(new MyWindowListener());
			
			// Set new icon
			setNewIcon(frame);
			
			Display.setParent(canvas);
			// -Dorg.lwjgl.opengl.Window.undecorated=true

			// DisplayMode dm=new DisplayMode(800, 800);
			// currentDisplayMode = dm;
			Display.setDisplayMode(currentDisplayMode);
			Display.setTitle(title);
			Display.setVSyncEnabled(false);
			// Display.setDisplayMode(new DisplayMode(512, 800));
			// Display.create(new PixelFormat().withStencilBits(1));

			Display.create(new PixelFormat().withStencilBits(1), new ContextAttribs(2, 0));
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
			exitProgram(1);
		}
	}

	void setNewIcon(Frame frame)
	{
		try
		{
			ClassLoader classLoader = getClass().getClassLoader();
			URI uri = classLoader.getResource("imgs/pngs720p/Med3D-19.png").toURI();
			URL url = uri.toURL();
			Toolkit kit = Toolkit.getDefaultToolkit();
			Image img = kit.createImage(url);
			frame.setIconImage(img);
		}
		catch (Exception e)
		{
			System.out.println("setNewIcon: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void initWindowElements()
	{
		try
		{
			hud = new HUD();
			renderer = new VeinsRenderer();
			gui = new GUIMain();
			joystick = new Mouse3D(settings);
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
			exitProgram(1);
		}
	}

	/**
	 * 
	 */
	private void setupWindow()
	{
		// GL14.GL_TEXTURE_LOD_BIAS
		try
		{
			// OpenGL setup
			GL11.glClearStencil(0);
			renderer.setupView();
			renderer.prepareShaders();

			// Sync
			// renderer.syncViewportSize(); // TODO: NIFTY
			// frame.invalidateLayout(); TODO NIFTY

		}
		catch (ShaderLoadException e)
		{
			e.printStackTrace();
			exitProgram(1);
		}
	}

	/**
	 * 
	 */

	boolean m_wasMouseLeftUp = true;
	public static boolean wire = false;

	long prevTime = System.nanoTime();
	public static float deltaTime = 1.0f / 60.0f;
	float disableInputTime = 0; // number of frames that the input will be disabled
	boolean disableInput = false;
	boolean wasEscapePressedAlready = false; // hackish but stupid input doesnt detect escape

	public void mainLoop()
	{

		while (isRunning)
		{
			if(handleDeminimization)
			{
				System.out.println("min start");
				
				ResizeWindow(veinsWindow.currentDisplayMode, Display.isFullscreen());
				System.out.println("min end");
				handleDeminimization = false;
			}
			
			UpdateLogicAndRenderSingleFrame();
		}
	}

	public void UpdateLogicAndRenderSingleFrame()
	{
		// Input disable for a couple of frames
		if (screenController.m_dragWindow || screenController.getState() != GUI_STATE.DEFAULT || screenController == null)
			disableInputTime = 0.3f;

		canModelBeRotatedByMouse = true; // reset

		// handle subdiv increase
		if (increaseSubdivLevel && VeinsWindow.renderer.veinsModel != null)
			VeinsWindow.renderer.veinsModel.increaseSubdivisionDepth();

		if (decreaseSubdivLevel && VeinsWindow.renderer.veinsModel != null)
			VeinsWindow.renderer.veinsModel.decreaseSubdivisionDepth();

		increaseSubdivLevel = false;
		decreaseSubdivLevel = false;

		// Detect on down click after it was up (NOT DOWN->UP!)
		boolean wasLeftMouseDownClicked = false;
		if (Mouse.isButtonDown(0) == true && m_wasMouseLeftUp == true)
			wasLeftMouseDownClicked = true;

		m_wasMouseLeftUp = !Mouse.isButtonDown(0);

		// Strange render loop

		// Doesn't detect escape otherwise
		if ((Keyboard.getEventKey() == Keyboard.KEY_ESCAPE && Keyboard.getNumKeyboardEvents() > 0) || (wasEscapePressedAlready == false && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE))
		{
			wasEscapePressedAlready = true;
			screenController.OnEscapeKeyPressed();
		}

		// non model input must be before nifty (BUT MUST BE DISABLED IF SOME DIALOG IS OPENED OTHERWISE NIFTY
		// DOESN'T GET INPUT (EF GOR SAVE DIALOG FILE NAME TEXTBOX)
		if (screenController.getState() == GUI_STATE.DEFAULT)
		{
			pollNonModelControlInput();
		}

		// NIFTY LOGIC MUST BE RAN BEFORE GAME INPUT LOGIC BECAUSE OF INPUT!
		if (wasLeftMouseDownClicked)
		{
			screenController.onMouseLeftDownClicked();
		}

		nifty.update();

		// model input must be after nifty
		disableInput = disableInputTime > 0;
		pollModelInput(wasLeftMouseDownClicked);

		renderer.switchWireframe(wire);

		// hud.setClickedOn(clickedOn);
		renderer.setupView(); // raycast volume renderer changes some states, theys must be reset
		renderer.clearView();

		// glPushAttrib(GL_ALL_ATTRIB_BITS);
		renderer.render();
		// glPopAttrib();

		// hud.drawHUD();
		// setTitle();

		// TODO: PRESENT ORDER: BEFORE OR AFTER NIFTY.RENDER?
		// Display.update();

		renderNiftyGUI();

		Display.update();

		logic();

		Display.sync(settings.frequency); // TODO NIFTY

		int error = GL11.glGetError();
		if (error != GL11.GL_NO_ERROR)
		{
			String glerrmsg = GLU.gluErrorString(error);
			System.err.println(glerrmsg);
		}

		// prepare for next frame
		long currentTime = System.nanoTime();
		deltaTime = ((float) currentTime) - ((float) prevTime);
		deltaTime /= 1000000000.0f;
		prevTime = currentTime;

		disableInputTime -= deltaTime;
		if (disableInputTime < 0)
			disableInputTime = -1.0f;
	}

	public void RenderSingleFrameWithoutModel()
	{
		renderer.setupView(); // raycast volume renderer changes some states, theys must be reset
		renderer.clearView();
		renderNiftyGUI();
		Display.update();
	}

	// saves, sets and restores openGL states
	void renderNiftyGUI()
	{

		glPushMatrix();
		glPushAttrib(GL_ALL_ATTRIB_BITS);

		// ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		// ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		// set up GL state for Nifty rendering
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, this.currentDisplayMode.getWidth(), this.currentDisplayMode.getHeight(), 0, -9999, 9999);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);

		glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_NOTEQUAL, 0);

		GL11.glDisable(GL11.GL_LIGHTING);
		glEnable(GL11.GL_TEXTURE_2D);

		nifty.render(false); // TODO: THROWS ERROR ON FIRST FRAME??

		// restore your OpenGL state
		glPopAttrib();
		glPopMatrix();

	}

	/**
	 * 
	 */
	private void logic()
	{
		// update framerate and calculate time that passed since last frame
		long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
		fps++;
		// if (time - timePastFps >= 1000)
		{
			fpsToDisplay = fps;
			fps = 0;
			timePastFps = time;
			renderer.getCamera().normalizeCameraOrientation();
			if (renderer.getVeinsModel() != null)
			{
				renderer.getVeinsModel().normalizeAddedOrientation();
				renderer.getVeinsModel().normalizeCurrentOrientation();
			}
		}
		// timePastFrame = time;
	}

	/**
	 * 
	 */
	private void setTitle()
	{
		if (settings.isFpsShown)
			Display.setTitle(title + " - FPS: " + fpsToDisplay);
		else
			Display.setTitle(title);
	}

	// this must be called before nifty gui, otherwise nifty eats up events
	public void pollNonModelControlInput()
	{

		while (Keyboard.next())
		{

			// if a key was pressed (vs.// released)
			if (Keyboard.getEventKeyState())
			{
				if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_TAB)
				{
					settings.isFpsShown = !settings.isFpsShown;
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_1)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SIMPLE_SHADER);
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_2)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP);
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_3)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP_AMBIENT_L);
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_4)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP_AMBIENT_L_SPEC_BLINN_PHONG);
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_5)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP_AMBIENT_L_SPEC_PHONG);
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_6)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SHADER_6);
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_7)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SHADER_7);
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_8)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SHADER_8);
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_0)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.FIXED_PIPELINE);
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_9)
				{
					wire = !wire;
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_ADD && renderer.getVeinsModel() != null)
				{
					renderer.getVeinsModel().increaseSubdivisionDepth();
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_SUBTRACT && renderer.getVeinsModel() != null)
				{
					renderer.getVeinsModel().decreaseSubdivisionDepth();
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_9) // no aa implemented at all??
				{
					renderer.switchAA();
				}
				else if (!disableInput && Keyboard.getEventKey() == Keyboard.KEY_L)
				{
					if (settings.locale.getLanguage().equals("sl"))
						settings.locale = new Locale("en", "US");
					else
						settings.locale = new Locale("sl", "SI");
					// frame.setLanguageSpecific(); TODO NIFTY
				}
			}
		}
	}

	// this must be called after nifty gui
	public void pollModelInput(boolean wasLeftMouseDownClicked)
	{
		pollKeyboardInput();
		pollMouseInput(wasLeftMouseDownClicked);
		// poll3DMouseInput();
	}

	public static float m_rotationFactor = 0.0f;
	
	
	/**
	 * 
	 */
	private void pollKeyboardInput()
	{

		// if (!frame.isDialogOpened()) return; TODO NIFTY

		float moveDeltaFactor = deltaTime * (float) settings.sensitivity;
		float maxMoveSpeed = 200.0f;
		if (moveDeltaFactor > maxMoveSpeed)
			moveDeltaFactor = maxMoveSpeed;

		float rotationFactor = deltaTime * 2.0f;
		float maxRotationSpeed = 2.0f;
		if (rotationFactor > maxRotationSpeed)
			rotationFactor = maxRotationSpeed;

		m_rotationFactor = rotationFactor;
		
		// moving the camera
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().rotateModelX(-rotationFactor);
			}
			else
			{
				renderer.getCamera().lookUp();
			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_S))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().rotateModelX(rotationFactor);
			}
			else
			{
				renderer.getCamera().lookDown();
			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().rotateModelZ(-rotationFactor);
			}
			else
			{
				renderer.getCamera().lookRight();
			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().rotateModelZ(rotationFactor);
			}
			else
			{
				renderer.getCamera().lookLeft();
			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_Q))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().rotateModelY(-rotationFactor);
			}
			else
			{
				// different rotations if in volume render model
				if (isRenderingVolumeModel())
				{
					renderer.getCamera().rotateCounterClockwise();
				}
				else
				{
					renderer.getCamera().rotateClockwise();
				}

			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_E))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().rotateModelY(rotationFactor);
			}
			else
			{
				// different rotations if in volume render model
				if (isRenderingVolumeModel())
				{
					renderer.getCamera().rotateClockwise();
				}
				else
				{
					renderer.getCamera().rotateCounterClockwise();
				}
			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_UP))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().moveModelY(-moveDeltaFactor);
			}
			else
			{
				renderer.getCamera().moveForward(moveDeltaFactor);
			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_DOWN))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().moveModelY(moveDeltaFactor);
			}
			else
			{
				renderer.getCamera().moveBackwards(moveDeltaFactor);
			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().moveModelX(-moveDeltaFactor);
			}
			else
			{
				renderer.getCamera().moveRight(moveDeltaFactor);
			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_LEFT))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().moveModelX(moveDeltaFactor);
			}
			else
			{
				renderer.getCamera().moveLeft(moveDeltaFactor);
			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_R))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().moveModelZ(moveDeltaFactor);
			}
			else
			{
				renderer.getCamera().moveUp(moveDeltaFactor);
			}
		}
		if (!disableInput && Keyboard.isKeyDown(Keyboard.KEY_F))
		{
			if (settings.useModelMoveMode)
			{
				if (renderer.getVeinsModel() != null)
					renderer.getVeinsModel().moveModelZ(-moveDeltaFactor);
			}
			else
			{
				renderer.getCamera().moveDown(moveDeltaFactor);
			}
		}

	}

	private void pollMouseInput(boolean wasLeftMouseDownClicked)
	{
		if (screenController.getState() != GUI_STATE.DEFAULT || renderer.getVeinsModel() == null)
			return;

		int z = Mouse.getDWheel();
		if (z > 0)
		{
			renderer.getCamera().zoomIn();
		}
		else if (z < 0)
		{
			renderer.getCamera().zoomOut();
		}

		if (canModelBeRotatedByMouse && Mouse.isButtonDown(0) && wasLeftMouseDownClicked == false)
		{
			renderer.getVeinsModel().changeAddedOrientation(renderer);
		}

		if (canModelBeRotatedByMouse && wasLeftMouseDownClicked)
		{
			renderer.getVeinsModel().SetVeinsGrabbedAt(RayUtil.getRaySphereIntersection_WITH_ONLY_ONE_INTERSECTION_POINT(VeinsWindow.GetMouseX(), VeinsWindow.GetMouseY(), renderer));
			renderer.getVeinsModel().saveCurrentOrientation();
			renderer.getVeinsModel().setAddedOrientation(new Quaternion());
		}
	}

	/*
	 * 
	 * 
	 * private void poll3DMouseInput() {
	 * 
	 * if (joystick.connected() && renderer.getVeinsModel() != null) { joystick.pollMouse(); if (settings.mSelected) renderer.getCamera().moveCamera3D(joystick.getAxis(), joystick.getRot()); else { renderer.getCamera().moveCamera3D(new double[] { -joystick.getAxisX(), -joystick.getAxisY(),
	 * -joystick.getAxisZ() }, new double[] { 0, 0, 0 }); renderer.getVeinsModel().rotateModel3D(joystick.getRot(), renderer); } } }
	 * 
	 * /** Calculates on which element mouse click was performed - on HUD element or on veins model
	 */
	/*
	 * private void calculateClickedOn() { float distanceToRotationCircle = (hud.x1 - VeinsWindow.GetMouseX()) * (hud.x1 - VeinsWindow.GetMouseX()) + (hud.y1 - VeinsWindow.GetMouseY()) * (hud.y1 - VeinsWindow.GetMouseY());
	 * 
	 * float distanceToMoveCircle = (hud.x2 - VeinsWindow.GetMouseX()) * (hud.x2 - VeinsWindow.GetMouseX()) + (hud.y2 - VeinsWindow.GetMouseY()) * (hud.y2 - VeinsWindow.GetMouseY());
	 * 
	 * float distanceToRotationFoci = (float) (Math.sqrt((hud.x1 - hud.f - VeinsWindow.GetMouseX()) * (hud.x1 - hud.f - VeinsWindow.GetMouseX()) + (hud.y1 - VeinsWindow.GetMouseY()) * (hud.y1 - VeinsWindow.GetMouseY())) + Math.sqrt((hud.x1 + hud.f - VeinsWindow.GetMouseX()) * (hud.x1 + hud.f -
	 * VeinsWindow.GetMouseX()) + (hud.y1 - VeinsWindow.GetMouseY()) (hud.y1 - VeinsWindow.GetMouseY())));
	 * 
	 * float distanceToMoveFoci = (float) (Math.sqrt((hud.x2 - hud.f - VeinsWindow.GetMouseX()) * (hud.x2 - hud.f - VeinsWindow.GetMouseX()) + (hud.y2 - VeinsWindow.GetMouseY()) * (hud.y2 - VeinsWindow.GetMouseY())) + Math.sqrt((hud.x2 + hud.f - VeinsWindow.GetMouseX()) * (hud.x2 + hud.f -
	 * VeinsWindow.GetMouseX()) + (hud.y2 - VeinsWindow.GetMouseY()) (hud.y2 - VeinsWindow.GetMouseY())));
	 * 
	 * if (clickedOn == CLICKED_ON_NOTHING) { if (settings.resHeight - VeinsWindow.GetMouseY() < settings.resHeight / 18) { clickedOn = CLICKED_ON_BUTTONS;
	 * 
	 * } else if (distanceToRotationCircle <= hud.r * hud.r) { clickedOn = CLICKED_ON_ROTATION_CIRCLE;
	 * 
	 * } else if (distanceToMoveCircle <= hud.r * hud.r) { clickedOn = CLICKED_ON_MOVE_CIRCLE;
	 * 
	 * } else if (distanceToRotationFoci <= hud.r * 3f) { clickedOn = CLICKED_ON_ROTATION_ELLIPSE;
	 * 
	 * } else if (distanceToMoveFoci <= hud.r * 3f) { clickedOn = CLICKED_ON_MOVE_ELLIPSE;
	 * 
	 * } else { renderer.getVeinsModel().SetVeinsGrabbedAt(RayUtil.getRaySphereIntersection(VeinsWindow.GetMouseX(), VeinsWindow.GetMouseY(), renderer)); renderer.getVeinsModel().setAddedOrientation(new Quaternion()); if (renderer.getVeinsModel().GetVeinsGrabbedAt() != null) clickedOn =
	 * CLICKED_ON_VEINS_MODEL; } } }
	 */

	public static boolean isRenderingVolumeModel()
	{
		return settings.selectedModelMethodIndex == 2;
	}

	/**
	 * @param n
	 */
	public void exitProgram(int n)
	{
		SettingsUtil.writeSettingsFile(settings, title);
		renderer.cleanShaders();
		inputSystem.shutdown();
		Display.destroy();
		System.exit(n);

	}

	/**
	 * @return
	 */
	public DisplayMode[] getDisplayModes()
	{
		return displayModes;
	}

	/**
	 * @return
	 */
	public DisplayMode getCurrentDisplayMode()
	{
		return currentDisplayMode;
	}

	/**
	 * @return
	 */
	public HUD getHUD()
	{
		return hud;
	}
}
