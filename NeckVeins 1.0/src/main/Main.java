package main;

import org.lwjgl.LWJGLException;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainFrameRefactored frame = new MainFrameRefactored();
			LWJGLRenderer renderer = new LWJGLRenderer();
			GUI gui = new GUI(frame, renderer);
            ThemeManager themeManager = ThemeManager.createThemeManager(
                    MainFrameRefactored.class.getResource("simple.xml"), renderer);
            gui.applyTheme(themeManager);
            renderer.syncViewportSize();
            frame.invalidateLayout();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
