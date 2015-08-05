package si.uni_lj.fri.veins3D.main;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.util.ResourceLoader;

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
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.gui.settings.NeckVeinsSettings;
import si.uni_lj.fri.veins3D.math.Quaternion;
import si.uni_lj.fri.veins3D.utils.Mouse3D;
import si.uni_lj.fri.veins3D.utils.RayUtil;
import si.uni_lj.fri.veins3D.utils.SettingsUtil;
import static org.lwjgl.opengl.GL11.*;

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

	
	private VeinsRenderer renderer;
	LwjglInputSystem inputSystem;
	private HUD hud;
	private boolean isRunning;
	private String title;
	private int fps;
	private long timePastFrame;
	private long timePastFps;
	private int fpsToDisplay;
	private int clickedOn;
	private DisplayMode[] displayModes;
	private DisplayMode currentDisplayMode;
	public static Mouse3D joystick;
	
	public static Nifty nifty = null; // So it can be accessed from outside
	public static GUIMain gui = null; // So it can be accessed from outside
	
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

	void init(String Title, String filename)
	{
		isRunning = true;
		this.title = Title;
		loadSettings(filename);
		createDisplay();
		// GL gl = new LwjglGL();
		initNiftyAndGUI();
		initWindowElements();
		setupWindow();

	}

	private void initNiftyAndGUI()
	{
		// Init nifty
		try
		{
			//Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE); // spams console a lot otherwise
			inputSystem = new LwjglInputSystem();
			inputSystem.startup();
			//BatchRenderConfiguration.DEFAULT_USE_HIGH_QUALITY_TEXTURES=true;
			BatchRenderBackendCoreProfileInternal niftyRenderFactory = (BatchRenderBackendCoreProfileInternal)LwjglBatchRenderBackendCoreProfileFactory.create();
			
			niftyRenderFactory.useHighQualityTextures(true); 
			
			BatchRenderDevice niftyRenderer = new BatchRenderDevice(niftyRenderFactory);
			niftyRenderFactory.useHighQualityTextures(true); 
			niftyRenderer.resetTextureAtlases();
			
			nifty = new Nifty(niftyRenderer, new NullSoundDevice(), inputSystem, new AccurateTimeProvider());
			niftyRenderFactory.useHighQualityTextures(true); 
			
			System.out.println("**NIFTY FROM XML*********************");
			nifty.fromXml("0", ResourceLoader.getResourceAsStream("xml/nifty_gui.xml"), "GScreen0");
			System.out.println("**NIFTY FROM XML DONE*********************");
			
			
			
		}
		catch (Exception e)
		{
			System.out.println("Nifty init error: " + e.getMessage());
			e.printStackTrace();
		}

	}
	

	private void loadSettings(String fileName)
	{
		try
		{
			displayModes = Display.getAvailableDisplayModes();
			// displayModeStrings = new String[displayModes.length];
			currentDisplayMode = Display.getDesktopDisplayMode();
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
				// setMouseSettings(settings);
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
			
			currentDisplayMode = displayModes[21];
			//settings.fullscreen = true;
			settings.resWidth = currentDisplayMode.getWidth();
			settings.resHeight = currentDisplayMode.getHeight();
			settings.bitsPerPixel = currentDisplayMode.getBitsPerPixel();
			settings.frequency = currentDisplayMode.getFrequency();
			
			if(settings.fullscreen)
			{
				Display.setFullscreen(true);
				Display.setVSyncEnabled(true);
			}
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	private void createDisplay()
	{
		try
		{
			//DisplayMode dm=new DisplayMode(800, 800);
			//currentDisplayMode = dm;
			Display.setDisplayMode(currentDisplayMode);
			Display.setTitle(title);
			Display.setVSyncEnabled(true);
			//Display.setDisplayMode(new DisplayMode(512, 800));
			// Display.create(new PixelFormat().withStencilBits(1));
			Display.create(new PixelFormat(), new ContextAttribs(2, 0));

			// Display.create();
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
		//GL14.GL_TEXTURE_LOD_BIAS
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

	public void mainLoop()
	{
		fps = 0;
		timePastFrame = (Sys.getTime() * 1000) / Sys.getTimerResolution();
		timePastFps = timePastFrame;
		fpsToDisplay = 0;

		while (!Display.isCloseRequested() && isRunning)
		{
			// Strange render loop
			boolean done = false;

			// renderer.setupView(); // raycast volume renderer changes some
			// states, theys must be reset
			// renderer.clearView();

			// pollInput();
			// hud.setClickedOn(clickedOn);

			// renderer.render();

			// hud.drawHUD();
			setTitle();

			// TODO: PRESENT ORDER: BEFORE OR AFTER NIFTY.RENDER?
			// Display.update();
			// logic();

			glDisable(GL_CULL_FACE);
			glDisable(GL_DEPTH_TEST);

			nifty.update();
			nifty.render(true); // TODO: THROWS ERROR ON FIRST FRAME??
			Display.update();

			Display.sync(settings.frequency); // TODO NIFTY

			int error = GL11.glGetError();
			if (error != GL11.GL_NO_ERROR)
			{
				String glerrmsg = GLU.gluErrorString(error);
				System.err.println(glerrmsg);
			}
		}
	}

	/**
	 * 
	 */
	private void logic()
	{
		// update framerate and calculate time that passed since last frame
		long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
		fps++;
		if (time - timePastFps >= 1000)
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
		timePastFrame = time;
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

	/**
	 * 
	 */
	public void pollInput()
	{
		// pollKeyboardInput();
		// pollMouseInput();
		// poll3DMouseInput(); TODO NIFTY
	}

	/**
	 * 
	 */
	private void pollKeyboardInput()
	{
		while (Keyboard.next())
		{
			// if a key was pressed (vs.// released)
			if (Keyboard.getEventKeyState())
			{
				if (Keyboard.getEventKey() == Keyboard.KEY_TAB)
				{
					settings.isFpsShown = !settings.isFpsShown;
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_1)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SIMPLE_SHADER);
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_2)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP);
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_3)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP_AMBIENT_L);
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_4)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP_AMBIENT_L_SPEC_BLINN_PHONG);
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_5)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SIMPLE_SHADER_NORM_INTERP_AMBIENT_L_SPEC_PHONG);
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_6)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SHADER_6);
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_7)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SHADER_7);
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_8)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.SHADER_8);
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_0)
				{
					renderer.setActiveShaderProgram(VeinsRenderer.FIXED_PIPELINE);
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_9)
				{
					renderer.switchWireframe();
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_ADD && renderer.getVeinsModel() != null)
				{
					renderer.getVeinsModel().increaseSubdivisionDepth();
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_SUBTRACT && renderer.getVeinsModel() != null)
				{
					renderer.getVeinsModel().decreaseSubdivisionDepth();
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_9)
				{
					renderer.switchAA();
				}
				else if (Keyboard.getEventKey() == Keyboard.KEY_L)
				{
					if (settings.locale.getLanguage().equals("sl"))
						settings.locale = new Locale("en", "US");
					else
						settings.locale = new Locale("sl", "SI");
					// frame.setLanguageSpecific(); TODO NIFTY
				}
			}
		}

		// if (!frame.isDialogOpened()) return; TODO NIFTY

		// moving the camera
		if (Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			renderer.getCamera().lookUp();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S))
		{
			renderer.getCamera().lookDown();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			renderer.getCamera().lookRight();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			renderer.getCamera().lookLeft();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Q))
		{
			renderer.getCamera().rotateCounterClockwise();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_E))
		{
			renderer.getCamera().rotateClockwise();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_UP))
		{
			renderer.getCamera().moveForward();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
		{
			renderer.getCamera().moveBackwards();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
		{
			renderer.getCamera().moveRight();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
		{
			renderer.getCamera().moveLeft();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_R))
		{
			renderer.getCamera().moveUp();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_F))
		{
			renderer.getCamera().moveDown();
		}

	}

	// TODO NIFTY
	/*
	 * private void pollMouseInput() { if (!frame.isDialogOpened() || renderer.getVeinsModel() == null) return;
	 * 
	 * int z = Mouse.getDWheel(); if (z > 0) { renderer.getCamera().zoomIn(); } else if (z < 0) { renderer.getCamera().zoomOut(); }
	 * 
	 * if (Mouse.isButtonDown(0)) { calculateClickedOn();
	 * 
	 * if (clickedOn == CLICKED_ON_VEINS_MODEL) { renderer.getVeinsModel().changeAddedOrientation(renderer); }
	 * 
	 * if (clickedOn == CLICKED_ON_ROTATION_CIRCLE || clickedOn == CLICKED_ON_MOVE_CIRCLE) { float x = (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_CIRCLE) ? hud.x1 : hud.x2; float y = (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_CIRCLE) ? hud.y1 : hud.y2;
	 * 
	 * float clickToCircleDistance = (float) Math.sqrt((x - Mouse .getX()) (x - Mouse.getX()) + (y - Mouse.getY()) (y - Mouse.getY())); float upRotation = (Mouse.getY() - y) / ((clickToCircleDistance > hud.r) ? clickToCircleDistance : hud.r); float rightRotation = (Mouse.getX() - x) /
	 * ((clickToCircleDistance > hud.r) ? clickToCircleDistance : hud.r);
	 * 
	 * hud.clickToCircleDistance = Math.min(clickToCircleDistance, hud.r) / hud.r; hud.clickOnCircleAngle = (float) Math.atan2(Mouse.getY() - y, Mouse.getX() - x);
	 * 
	 * if (clickedOn == CLICKED_ON_ROTATION_CIRCLE) { renderer.getCamera().rotate(upRotation, rightRotation); } else { renderer.getCamera().move(upRotation, rightRotation); }
	 * 
	 * }
	 * 
	 * if (clickedOn == CLICKED_ON_ROTATION_ELLIPSE) { if (hud.x1 - Mouse.getX() <= 0) { hud.ellipseSide = 0; renderer.getCamera().rotateClockwise(); } else { hud.ellipseSide = 1; renderer.getCamera().rotateCounterClockwise(); } }
	 * 
	 * if (clickedOn == CLICKED_ON_MOVE_ELLIPSE) { if (hud.x2 - Mouse.getX() <= 0) { hud.ellipseSide = 0; renderer.getCamera().moveDown(); } else { hud.ellipseSide = 1; renderer.getCamera().moveUp(); } }
	 * 
	 * } else { clickedOn = CLICKED_ON_NOTHING; // renderer.getVeinsModel().veinsGrabbedAt = null; // TODO: WHAT, IS // THIS EVEN NEEDED? renderer.getVeinsModel().saveCurrentOrientation(); renderer.getVeinsModel().setAddedOrientation(new Quaternion()); }
	 * 
	 * }
	 */

	private void poll3DMouseInput()
	{

		if (joystick.connected() && renderer.getVeinsModel() != null)
		{
			joystick.pollMouse();
			if (settings.mSelected)
				renderer.getCamera().moveCamera3D(joystick.getAxis(), joystick.getRot());
			else
			{
				renderer.getCamera().moveCamera3D(new double[]
				{ -joystick.getAxisX(), -joystick.getAxisY(), -joystick.getAxisZ() }, new double[]
				{ 0, 0, 0 });
				renderer.getVeinsModel().rotateModel3D(joystick.getRot(), renderer);
			}
		}
	}

	/**
	 * Calculates on which element mouse click was performed - on HUD element or on veins model
	 */
	private void calculateClickedOn()
	{
		float distanceToRotationCircle = (hud.x1 - Mouse.getX()) * (hud.x1 - Mouse.getX()) + (hud.y1 - Mouse.getY()) * (hud.y1 - Mouse.getY());

		float distanceToMoveCircle = (hud.x2 - Mouse.getX()) * (hud.x2 - Mouse.getX()) + (hud.y2 - Mouse.getY()) * (hud.y2 - Mouse.getY());

		float distanceToRotationFoci = (float) (Math.sqrt((hud.x1 - hud.f - Mouse.getX()) * (hud.x1 - hud.f - Mouse.getX()) + (hud.y1 - Mouse.getY()) * (hud.y1 - Mouse.getY())) + Math.sqrt((hud.x1 + hud.f - Mouse.getX()) * (hud.x1 + hud.f - Mouse.getX()) + (hud.y1 - Mouse.getY())
				* (hud.y1 - Mouse.getY())));

		float distanceToMoveFoci = (float) (Math.sqrt((hud.x2 - hud.f - Mouse.getX()) * (hud.x2 - hud.f - Mouse.getX()) + (hud.y2 - Mouse.getY()) * (hud.y2 - Mouse.getY())) + Math.sqrt((hud.x2 + hud.f - Mouse.getX()) * (hud.x2 + hud.f - Mouse.getX()) + (hud.y2 - Mouse.getY())
				* (hud.y2 - Mouse.getY())));

		if (clickedOn == CLICKED_ON_NOTHING)
		{
			if (settings.resHeight - Mouse.getY() < settings.resHeight / 18)
			{
				clickedOn = CLICKED_ON_BUTTONS;

			}
			else if (distanceToRotationCircle <= hud.r * hud.r)
			{
				clickedOn = CLICKED_ON_ROTATION_CIRCLE;

			}
			else if (distanceToMoveCircle <= hud.r * hud.r)
			{
				clickedOn = CLICKED_ON_MOVE_CIRCLE;

			}
			else if (distanceToRotationFoci <= hud.r * 3f)
			{
				clickedOn = CLICKED_ON_ROTATION_ELLIPSE;

			}
			else if (distanceToMoveFoci <= hud.r * 3f)
			{
				clickedOn = CLICKED_ON_MOVE_ELLIPSE;

			}
			else
			{
				renderer.getVeinsModel().SetVeinsGrabbedAt(RayUtil.getRaySphereIntersection(Mouse.getX(), Mouse.getY(), renderer));
				renderer.getVeinsModel().setAddedOrientation(new Quaternion());
				if (renderer.getVeinsModel().GetVeinsGrabbedAt() != null)
					clickedOn = CLICKED_ON_VEINS_MODEL;
			}
		}
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
