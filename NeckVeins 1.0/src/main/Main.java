package main;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// TODO Change
            Display.setDisplayMode(Display.getDesktopDisplayMode());
			// TODO Change title
            Display.setTitle("lala");
            Display.create(new PixelFormat().withStencilBits(1));
            //Display.create();
            Display.setVSyncEnabled(true);
            
            GL11.glClearStencil(0);
			MainFrameRefactored frame = new MainFrameRefactored();
			GUI gui = null;
			RendererPanel renderer = new RendererPanel();
			gui = new GUI(frame, renderer);
			renderer.setGUI(gui);
			renderer.setHUD(new HUD());
            ThemeManager themeManager = ThemeManager.createThemeManager(
                    MainFrameRefactored.class.getResource("simple.xml"), renderer);
            gui.applyTheme(themeManager);
            renderer.syncViewportSize();
            frame.invalidateLayout();
            ((RendererPanel) renderer).mainLoop();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	

}
