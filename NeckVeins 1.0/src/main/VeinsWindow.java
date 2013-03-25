package main;

import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.theme.ThemeManager;

public class VeinsWindow {
	
	private MainFrameRefactored frame;
	private RendererPanel renderer;
	private HUD hud;
	private GUI gui;
	private boolean isRunning = true;
	private String title = "VeinsRefactored";
	
	public VeinsWindow() {
		createDisplay();
		initWindowElements();
		setupWindow();
	}
	private void createDisplay() {
        try {
        	// TODO Change
			Display.setDisplayMode(Display.getDesktopDisplayMode());
			// TODO Change title
			Display.setTitle(title);
			Display.setVSyncEnabled(true);
			Display.create(new PixelFormat().withStencilBits(1));
			//Display.create();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initWindowElements() {
		try {
			frame = new MainFrameRefactored();
			renderer = new RendererPanel();
			hud = new HUD();
			gui = new GUI(frame, renderer);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}
	
	private void setupWindow() {
		try {
			GL11.glClearStencil(0);
			renderer.setupView();
			
			ThemeManager themeManager = ThemeManager.createThemeManager(
			        MainFrameRefactored.class.getResource("simple.xml"), renderer);
			gui.applyTheme(themeManager);
			
			renderer.syncViewportSize();
			frame.invalidateLayout();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @since 0.1
	 * @version 0.4
	 */
	public void mainLoop(){
		int fps = 0;
		long timePastFrame = (Sys.getTime()*1000)/Sys.getTimerResolution();
		long timePastFps = timePastFrame;
		int fpsToDisplay = 0;
		renderer.setupView();
		while(!Display.isCloseRequested() && isRunning){
			renderer.resetView();
			// TODO
			//pollInput();
			renderer.render();
			// TODO change updating
			gui.update();
			Display.update();
			// TODO
			//logic();
			Display.sync(MainFrameRefactored.settings.frequency);
		}
		hud.drawHUD();
		if(MainFrameRefactored.settings.isFpsShown)Display.setTitle(title +" - FPS: "+fpsToDisplay);else	Display.setTitle(title);
	}
	
}
