package main;

import org.lwjgl.opengl.Display;

import de.matthiasmann.twl.GUI;

public class ExitHandler {

	public static void exitProgram(int n, GUI gui) {
		RendererPanel renderer = (RendererPanel) gui.getRenderer();
		SettingsUtil.saveSettings();
		((RendererPanel) gui.getRenderer()).cleanShaders();
		gui.destroy();
		// TODO
		// if (themeManager != null)
		// themeManager.destroy();
		Display.destroy();
		System.exit(n);
	}

}
