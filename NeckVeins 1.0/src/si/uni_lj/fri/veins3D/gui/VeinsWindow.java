package si.uni_lj.fri.veins3D.gui;

import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import si.uni_lj.fri.veins3D.exceptions.ShaderLoadException;
import si.uni_lj.fri.veins3D.gui.render.Camera;
import si.uni_lj.fri.veins3D.gui.render.VeinsRenderer;
import si.uni_lj.fri.veins3D.gui.settings.NeckVeinsSettings;
import si.uni_lj.fri.veins3D.math.Quaternion;
import si.uni_lj.fri.veins3D.math.Vector;
import si.uni_lj.fri.veins3D.utils.RayUtil;
import si.uni_lj.fri.veins3D.utils.SettingsUtil;
import de.matthiasmann.twl.Container;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.theme.ThemeManager;

/**
 * This is the root pane of the program.
 * 
 */
public class VeinsWindow extends Container {
	public final static int CLICKED_ON_NOTHING = 0;
	public final static int CLICKED_ON_VEINS_MODEL = 1;
	public final static int CLICKED_ON_ROTATION_CIRCLE = 2;
	public final static int CLICKED_ON_MOVE_CIRCLE = 3;
	public final static int CLICKED_ON_ROTATION_ELLIPSE = 4;
	public final static int CLICKED_ON_MOVE_ELLIPSE = 5;
	public final static int CLICKED_ON_BUTTONS = 6;
	private final float ELLIPSEF = 1.1180339887498948482045868343656f;

	public static NeckVeinsSettings settings;

	private VeinsFrame frame;
	private VeinsRenderer renderer;
	private HUD hud;
	private boolean isRunning;
	private String title;
	private int fps;
	private long timePastFrame;
	private long timePastFps;
	private int fpsToDisplay;
	private int clickedOn;
	private GUI gui;
	private ThemeManager themeManager;
	private DisplayMode[] displayModes;
	private DisplayMode currentDisplayMode;

	public VeinsWindow() {
		isRunning = true;
		title = "Veins3D";
		loadSettings();
		createDisplay();
		initWindowElements();
		setupWindow();
	}

	public VeinsWindow(String title) {
		isRunning = true;
		this.title = title;
		// loadSettings();
		createDisplay();
		initWindowElements();
		setupWindow();
	}

	private void loadSettings() {
		try {
			displayModes = Display.getAvailableDisplayModes();
			// displayModeStrings = new String[displayModes.length];
			currentDisplayMode = Display.getDesktopDisplayMode();
			settings = SettingsUtil.loadSettings(title);
			if (settings != null) {
				for (DisplayMode mode : displayModes) {
					if (mode.getWidth() == settings.resWidth && mode.getHeight() == settings.resHeight
							&& mode.getBitsPerPixel() == settings.bitsPerPixel
							&& mode.getFrequency() == settings.frequency) {
						currentDisplayMode = mode;
					}
				}
			} else {
				settings = new NeckVeinsSettings();
				settings.isFpsShown = false;
				settings.fullscreen = true;
				settings.stereoEnabled = false;
				settings.stereoValue = 0;
			}
			settings.resWidth = currentDisplayMode.getWidth();
			settings.resHeight = currentDisplayMode.getHeight();
			settings.bitsPerPixel = currentDisplayMode.getBitsPerPixel();
			settings.frequency = currentDisplayMode.getFrequency();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	private void createDisplay() {
		try {
			Display.setDisplayMode(currentDisplayMode);
			Display.setTitle(title);
			Display.setVSyncEnabled(true);
			Display.create(new PixelFormat().withStencilBits(1));
			// Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			exitProgram(1);
		}
	}

	private void initWindowElements() {
		try {
			hud = new HUD();
			renderer = new VeinsRenderer();
			frame = new VeinsFrame();
			gui = new GUI(frame, renderer);
			add(gui);
			setTheme("mainframe");
		} catch (LWJGLException e) {
			e.printStackTrace();
			exitProgram(1);
		}
	}

	private void setupWindow() {
		try {
			// Theme setup
			themeManager = ThemeManager.createThemeManager(VeinsFrame.class.getResource("/xml/simple.xml"), renderer);
			gui.applyTheme(themeManager);

			// OpenGL setup
			GL11.glClearStencil(0);
			renderer.setupView();
			renderer.prepareShaders();

			// Sync
			renderer.syncViewportSize();
			frame.invalidateLayout();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ShaderLoadException e) {
			e.printStackTrace();
			exitProgram(1);
		}
	}

	/**
	 * @since 0.1
	 * @version 0.4
	 */
	public void mainLoop() {
		fps = 0;
		timePastFrame = (Sys.getTime() * 1000) / Sys.getTimerResolution();
		timePastFps = timePastFrame;
		fpsToDisplay = 0;
		renderer.setupView();
		while (!Display.isCloseRequested() && isRunning) {
			/* Reset view */
			renderer.resetView();

			/* Handle input and inform HUD about it */
			pollInput();
			hud.setClickedOn(clickedOn);

			/* Render */
			renderer.render();
			hud.drawHUD();
			setTitle();

			/* Update */
			gui.update();
			Display.update();
			logic();
			Display.sync(settings.frequency);
		}
	}

	/**
	 * @since 0.1
	 * @version 0.1
	 */
	private void logic() {
		// update framerate and calculate time that passed since last frame
		long time = (Sys.getTime() * 1000) / Sys.getTimerResolution();
		fps++;
		if (time - timePastFps >= 1000) {
			fpsToDisplay = fps;
			fps = 0;
			timePastFps = time;
			renderer.getCamera().cameraOrientation = Quaternion
					.quaternionNormalization(renderer.getCamera().cameraOrientation);
			if (renderer.getVeinsModel() != null) {
				renderer.setAddedModelOrientation(Quaternion.quaternionNormalization(renderer
						.getAddedModelOrientation()));
				renderer.setCurrentModelOrientation(Quaternion.quaternionNormalization(renderer
						.getCurrentModelOrientation()));
			}
		}
		timePastFrame = time;

	}

	/**
	 * Updates title if FPS are shown
	 */
	private void setTitle() {
		if (settings.isFpsShown)
			Display.setTitle(title + " - FPS: " + fpsToDisplay);
		else
			Display.setTitle(title);
	}

	/**
	 * @since 0.1
	 * @version 0.4
	 */
	public void pollInput() {
		pollKeyboardInput();
		pollMouseInput();
	}

	private void pollKeyboardInput() {
		while (Keyboard.next()) {
			// if a key was pressed (vs.// released)
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_TAB) {
					if (settings.isFpsShown)
						settings.isFpsShown = false;
					else
						settings.isFpsShown = true;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_1) {
					renderer.setActiveShaderProgram(0);
				} else if (Keyboard.getEventKey() == Keyboard.KEY_2) {
					renderer.setActiveShaderProgram(1);
				} else if (Keyboard.getEventKey() == Keyboard.KEY_3) {
					renderer.setActiveShaderProgram(2);
				} else if (Keyboard.getEventKey() == Keyboard.KEY_4) {
					renderer.setActiveShaderProgram(3);
				} else if (Keyboard.getEventKey() == Keyboard.KEY_5) {
					renderer.setActiveShaderProgram(4);
				} else if (Keyboard.getEventKey() == Keyboard.KEY_6) {
					renderer.setActiveShaderProgram(5);
				} else if (Keyboard.getEventKey() == Keyboard.KEY_7) {
					renderer.setActiveShaderProgram(6);
				} else if (Keyboard.getEventKey() == Keyboard.KEY_8) {
					renderer.setActiveShaderProgram(7);
				} else if (Keyboard.getEventKey() == Keyboard.KEY_0) {
					renderer.setActiveShaderProgram(-1);
				} else if (Keyboard.getEventKey() == Keyboard.KEY_9) {
					renderer.switchWireframe();
					if (renderer.isWireframeOn())
						GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
					else
						GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
				} else if (Keyboard.getEventKey() == Keyboard.KEY_ADD) {
					renderer.getVeinsModel().increaseSubdivisionDepth();
				} else if (Keyboard.getEventKey() == Keyboard.KEY_SUBTRACT) {
					renderer.getVeinsModel().decreaseSubdivisionDepth();
				} else if (Keyboard.getEventKey() == Keyboard.KEY_9) {
					renderer.switchAA();
				}
			}
		}

		if (!frame.isDialogOpened())
			return;

		// moving the camera
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			renderer.getCamera().lookUp();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			renderer.getCamera().lookDown();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			renderer.getCamera().lookRight();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			renderer.getCamera().lookLeft();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			renderer.getCamera().rotateCounterClockwise();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			renderer.getCamera().rotateClockwise();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			renderer.getCamera().moveForward();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			renderer.getCamera().moveBackwards();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			renderer.getCamera().moveRight();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			renderer.getCamera().moveLeft();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			renderer.getCamera().moveUp();
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
			renderer.getCamera().moveDown();
		}

	}

	/**
	 * TODO re-think clickOn implementation
	 */
	private void pollMouseInput() {
		if (!frame.isDialogOpened())
			return;

		int z = Mouse.getDWheel();
		if (z > 0) {
			renderer.getCamera().zoomIn();
		} else if (z < 0) {
			renderer.getCamera().zoomOut();
		}

		if (renderer.getVeinsModel() != null) {
			if (Mouse.isButtonDown(0)) {
				float w = settings.resWidth;
				float h = settings.resHeight;
				float r = w / 18;
				float offset = r * 2 / 3;
				float x = w - offset - r;
				float y = h - h / 18 - offset - r;
				float x2 = w - offset - r;
				float y2 = h - h / 18 - 2 * offset - 3 * r;
				float f = ELLIPSEF * r;

				calcClickedOn(x, y, x2, y2, f, r);

				if (clickedOn == CLICKED_ON_VEINS_MODEL) {
					double[] veinsHeldAt = RayUtil.getRaySphereIntersection(Mouse.getX(), Mouse.getY(), renderer);
					if (veinsHeldAt != null) {
						double[] rotationAxis = Vector.crossProduct(renderer.veinsGrabbedAt, veinsHeldAt);
						if (Vector.length(rotationAxis) > 0) {
							rotationAxis = Vector.normalize(rotationAxis);
							rotationAxis = Quaternion.quaternionReciprocal(renderer.getCurrentModelOrientation())
									.rotateVector3d(rotationAxis);
							double angle = Math.acos(Vector.dotProduct(renderer.veinsGrabbedAt, veinsHeldAt)
									/ (Vector.length(renderer.veinsGrabbedAt) * Vector.length(veinsHeldAt)));
							renderer.setAddedModelOrientation(Quaternion.quaternionFromAngleAndRotationAxis(angle,
									rotationAxis));
						}
					}
				}
				if (clickedOn == CLICKED_ON_ROTATION_CIRCLE || clickedOn == CLICKED_ON_MOVE_CIRCLE) {
					if (clickedOn == CLICKED_ON_MOVE_CIRCLE) {
						x = x2;
						y = y2;
					}
					hud.rotationCircleDistance = (x - Mouse.getX()) * (x - Mouse.getX()) + (y - Mouse.getY())
							* (y - Mouse.getY());
					hud.rotationCircleAngle = (float) Math.atan2(Mouse.getY() - y, Mouse.getX() - x);
					hud.rotationCircleDistance = (float) Math.sqrt(hud.rotationCircleDistance);
					float upRotation = (Mouse.getY() - y);
					float rightRotation = (Mouse.getX() - x);
					if (hud.rotationCircleDistance > r) {
						upRotation /= hud.rotationCircleDistance;
						rightRotation /= hud.rotationCircleDistance;
					} else {
						upRotation /= r;
						rightRotation /= r;
					}
					hud.rotationCircleDistance = Math.min(hud.rotationCircleDistance, r) / r;
					if (clickedOn == CLICKED_ON_ROTATION_CIRCLE) {
						Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(
								Camera.CAMERA_ROTATION_SPEED * upRotation, new double[] { 1, 0, 0 });
						renderer.getCamera().cameraOrientation = Quaternion.quaternionMultiplication(
								renderer.getCamera().cameraOrientation, addRotation);
						addRotation = Quaternion.quaternionFromAngleAndRotationAxis(Camera.CAMERA_ROTATION_SPEED
								* rightRotation, new double[] { 0, -1, 0 });
						renderer.getCamera().cameraOrientation = Quaternion.quaternionMultiplication(
								renderer.getCamera().cameraOrientation, addRotation);
					} else {
						double up[] = new double[] { 0, 0, -upRotation };
						up = renderer.getCamera().cameraOrientation.rotateVector3d(up);
						double right[] = new double[] { rightRotation, 0, 0 };
						right = renderer.getCamera().cameraOrientation.rotateVector3d(right);
						renderer.getCamera().cameraX += (float) (up[0] + right[0]);
						renderer.getCamera().cameraY += (float) (up[1] + right[1]);
						renderer.getCamera().cameraZ += (float) (up[2] + right[2]);
					}
				}
				if (clickedOn == CLICKED_ON_ROTATION_ELLIPSE) {
					if (x - Mouse.getX() <= 0) {
						hud.ellipseSide = 0;
						Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(
								Camera.CAMERA_ROTATION_SPEED, new double[] { 0, 0, -1 });
						renderer.getCamera().cameraOrientation = Quaternion.quaternionMultiplication(
								renderer.getCamera().cameraOrientation, addRotation);
					} else {
						hud.ellipseSide = 1;
						Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(
								Camera.CAMERA_ROTATION_SPEED, new double[] { 0, 0, 1 });
						renderer.getCamera().cameraOrientation = Quaternion.quaternionMultiplication(
								renderer.getCamera().cameraOrientation, addRotation);
					}
				}
				if (clickedOn == CLICKED_ON_MOVE_ELLIPSE) {
					double v[];
					if (x2 - Mouse.getX() <= 0) {
						hud.ellipseSide = 0;
						v = new double[] { 0, 1, 0 };
					} else {
						hud.ellipseSide = 1;
						v = new double[] { 0, -1, 0 };
					}
					v = renderer.getCamera().cameraOrientation.rotateVector3d(v);
					renderer.getCamera().cameraX += (float) v[0];
					renderer.getCamera().cameraY += (float) v[1];
					renderer.getCamera().cameraZ += (float) v[2];
				}
			} else {
				clickedOn = CLICKED_ON_NOTHING;
				renderer.veinsGrabbedAt = null;
				Quaternion currentModelOrientation = Quaternion.quaternionMultiplication(
						renderer.getCurrentModelOrientation(), renderer.getAddedModelOrientation());
				renderer.setCurrentModelOrientation(currentModelOrientation);
				renderer.setAddedModelOrientation(new Quaternion());
			}
		}
	}

	private int calcClickedOn(float x, float y, float x2, float y2, float f, float r) {

		if (clickedOn == CLICKED_ON_NOTHING) {
			float distanceToRotationCircle = (x - Mouse.getX()) * (x - Mouse.getX()) + (y - Mouse.getY())
					* (y - Mouse.getY());
			float distanceToMoveCircle = (x2 - Mouse.getX()) * (x2 - Mouse.getX()) + (y2 - Mouse.getY())
					* (y2 - Mouse.getY());
			float distanceToRotationFoci = (float) (Math.sqrt((x - f - Mouse.getX()) * (x - f - Mouse.getX())
					+ (y - Mouse.getY()) * (y - Mouse.getY())) + Math.sqrt((x + f - Mouse.getX())
					* (x + f - Mouse.getX()) + (y - Mouse.getY()) * (y - Mouse.getY())));
			float distanceToMoveFoci = (float) (Math.sqrt((x2 - f - Mouse.getX()) * (x2 - f - Mouse.getX())
					+ (y2 - Mouse.getY()) * (y2 - Mouse.getY())) + Math.sqrt((x2 + f - Mouse.getX())
					* (x2 + f - Mouse.getX()) + (y2 - Mouse.getY()) * (y2 - Mouse.getY())));

			if (settings.resHeight - Mouse.getY() < settings.resHeight / 18) {
				clickedOn = CLICKED_ON_BUTTONS;

			} else if (distanceToRotationCircle <= r * r) {
				clickedOn = CLICKED_ON_ROTATION_CIRCLE;

			} else if (distanceToMoveCircle <= r * r) {
				clickedOn = CLICKED_ON_MOVE_CIRCLE;

			} else if (distanceToRotationFoci <= r * 3f) {
				clickedOn = CLICKED_ON_ROTATION_ELLIPSE;

			} else if (distanceToMoveFoci <= r * 3f) {
				clickedOn = CLICKED_ON_MOVE_ELLIPSE;

			} else {
				renderer.veinsGrabbedAt = RayUtil.getRaySphereIntersection(Mouse.getX(), Mouse.getY(), renderer);
				renderer.setAddedModelOrientation(new Quaternion());

				if (renderer.veinsGrabbedAt != null)
					clickedOn = CLICKED_ON_VEINS_MODEL;
			}
		}

		return clickedOn;
	}

	public void exitProgram(int n) {
		SettingsUtil.saveSettings(settings, title);
		renderer.cleanShaders();
		gui.destroy();
		if (themeManager != null)
			themeManager.destroy();
		Display.destroy();
		System.exit(n);

	}

	public DisplayMode[] getDisplayModes() {
		return displayModes;
	}

	public DisplayMode getCurrentDisplayMode() {
		return currentDisplayMode;
	}

	public HUD getHUD() {
		return hud;
	}

}
