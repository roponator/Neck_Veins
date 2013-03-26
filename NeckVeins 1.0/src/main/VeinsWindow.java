package main;

import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import tools.Quaternion;
import tools.Vector;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.theme.ThemeManager;

public class VeinsWindow {
	public final static int CLICKED_ON_NOTHING = 0;
	public final static int CLICKED_ON_VEINS_MODEL = 1;
	public final static int CLICKED_ON_ROTATION_CIRCLE = 2;
	public final static int CLICKED_ON_MOVE_CIRCLE = 3;
	public final static int CLICKED_ON_ROTATION_ELLIPSE = 4;
	public final static int CLICKED_ON_MOVE_ELLIPSE = 5;
	public final static int CLICKED_ON_BUTTONS = 6;
	private final float ELLIPSEF = 1.1180339887498948482045868343656f;

	private MainFrameRefactored frame;
	private RendererPanel renderer;
	private HUD hud;
	private GUI gui;
	private boolean isRunning;
	private String title;
	private int fps;
	private long timePastFrame;
	private long timePastFps;
	private int fpsToDisplay;
	public static int clickedOn;

	public VeinsWindow() {
		isRunning = true;
		title = "Veins 3D";
		createDisplay();
		initWindowElements();
		setupWindow();
	}

	public VeinsWindow(String title) {
		isRunning = true;
		this.title = title;
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
			// Display.create();
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
			// OpenGL setup
			GL11.glClearStencil(0);
			renderer.setupView();

			// Theme setup
			ThemeManager themeManager = ThemeManager.createThemeManager(
					MainFrameRefactored.class.getResource("simple.xml"), renderer);
			gui.applyTheme(themeManager);

			// Sync
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
	public void mainLoop() {
		fps = 0;
		timePastFrame = (Sys.getTime() * 1000) / Sys.getTimerResolution();
		timePastFps = timePastFrame;
		fpsToDisplay = 0;
		renderer.setupView();
		while (!Display.isCloseRequested() && isRunning) {
			renderer.resetView();
			// TODO
			pollInput();
			renderer.render();
			hud.drawHUD();
			setTitle();
			gui.update();
			Display.update();
			// TODO
			// logic();
			Display.sync(MainFrameRefactored.settings.frequency);
		}
	}

	/**
	 * Updates title if FPS are shown
	 */
	private void setTitle() {
		if (MainFrameRefactored.settings.isFpsShown)
			Display.setTitle(title + " - FPS: " + fpsToDisplay);
		else
			Display.setTitle(title);
	}

	/**
	 * @since 0.1
	 * @version 0.4
	 */
	private void pollInput() {
		pollKeyboardInput();
		pollMouseInput();
	}

	private void pollKeyboardInput() {
		while (Keyboard.next()) {
			// if a key was pressed (vs.// released)
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_TAB) {
					if (MainFrameRefactored.settings.isFpsShown)
						MainFrameRefactored.settings.isFpsShown = false;
					else
						MainFrameRefactored.settings.isFpsShown = true;
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
			// create a vector representing the rotation axis
			Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(Camera.CAMERA_ROTATION_SPEED,
					new double[] { 1, 0, 0 });
			renderer.getCamera().cameraOrientation = Quaternion.quaternionMultiplication(
					renderer.getCamera().cameraOrientation, addRotation);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(Camera.CAMERA_ROTATION_SPEED,
					new double[] { -1, 0, 0 });
			renderer.getCamera().cameraOrientation = Quaternion.quaternionMultiplication(
					renderer.getCamera().cameraOrientation, addRotation);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(Camera.CAMERA_ROTATION_SPEED,
					new double[] { 0, 1, 0 });
			renderer.getCamera().cameraOrientation = Quaternion.quaternionMultiplication(
					renderer.getCamera().cameraOrientation, addRotation);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(Camera.CAMERA_ROTATION_SPEED,
					new double[] { 0, -1, 0 });
			renderer.getCamera().cameraOrientation = Quaternion.quaternionMultiplication(
					renderer.getCamera().cameraOrientation, addRotation);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
			Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(Camera.CAMERA_ROTATION_SPEED,
					new double[] { 0, 0, 1 });
			renderer.getCamera().cameraOrientation = Quaternion.quaternionMultiplication(
					renderer.getCamera().cameraOrientation, addRotation);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			Quaternion addRotation = Quaternion.quaternionFromAngleAndRotationAxis(Camera.CAMERA_ROTATION_SPEED,
					new double[] { 0, 0, -1 });
			renderer.getCamera().cameraOrientation = Quaternion.quaternionMultiplication(
					renderer.getCamera().cameraOrientation, addRotation);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			double v[] = new double[] { 0, 0, -1 };
			v = renderer.getCamera().cameraOrientation.rotateVector3d(v);
			renderer.getCamera().cameraX += (float) v[0];
			renderer.getCamera().cameraY += (float) v[1];
			renderer.getCamera().cameraZ += (float) v[2];
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			double v[] = new double[] { 0, 0, 1 };
			v = renderer.getCamera().cameraOrientation.rotateVector3d(v);
			renderer.getCamera().cameraX += (float) v[0];
			renderer.getCamera().cameraY += (float) v[1];
			renderer.getCamera().cameraZ += (float) v[2];
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			double v[] = new double[] { 1, 0, 0 };
			v = renderer.getCamera().cameraOrientation.rotateVector3d(v);
			renderer.getCamera().cameraX += (float) v[0];
			renderer.getCamera().cameraY += (float) v[1];
			renderer.getCamera().cameraZ += (float) v[2];
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			double v[] = new double[] { -1, 0, 0 };
			v = renderer.getCamera().cameraOrientation.rotateVector3d(v);
			renderer.getCamera().cameraX += (float) v[0];
			renderer.getCamera().cameraY += (float) v[1];
			renderer.getCamera().cameraZ += (float) v[2];
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
			double v[] = new double[] { 0, 1, 0 };
			v = renderer.getCamera().cameraOrientation.rotateVector3d(v);
			renderer.getCamera().cameraX += (float) v[0];
			renderer.getCamera().cameraY += (float) v[1];
			renderer.getCamera().cameraZ += (float) v[2];
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
			double v[] = new double[] { 0, -1, 0 };
			v = renderer.getCamera().cameraOrientation.rotateVector3d(v);
			renderer.getCamera().cameraX += (float) v[0];
			renderer.getCamera().cameraY += (float) v[1];
			renderer.getCamera().cameraZ += (float) v[2];
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
			renderer.getCamera().cameraX *= 0.8;
			renderer.getCamera().cameraY *= 0.8;
			renderer.getCamera().cameraZ *= 0.8;
		} else if (z < 0) {
			renderer.getCamera().cameraX *= 1.25;
			renderer.getCamera().cameraY *= 1.25;
			renderer.getCamera().cameraZ *= 1.25;
		}

		if (renderer.getVeinsModel() != null) {
			double[] veinsGrabbedAt = null;
			if (Mouse.isButtonDown(0)) {
				// figure out if clicked on the HUD first
				float w = MainFrameRefactored.settings.resWidth;
				float h = MainFrameRefactored.settings.resHeight;
				float r = w / 18;
				float offset = r * 2 / 3;
				float x = w - offset - r;
				float y = h - h / 18 - offset - r;
				float x2 = w - offset - r;
				float y2 = h - h / 18 - 2 * offset - 3 * r;
				float f = ELLIPSEF * r;

				if (clickedOn == CLICKED_ON_NOTHING) {
					float distanceToRotationCircle = (x - Mouse.getX()) * (x - Mouse.getX()) + (y - Mouse.getY())
							* (y - Mouse.getY());
					float distanceToMoveCircle = (x2 - Mouse.getX()) * (x2 - Mouse.getX()) + (y2 - Mouse.getY())
							* (y2 - Mouse.getY());
					float distanceToRotationFoci = (float) (Math.sqrt((x - -Mouse.getX()) * (x - f - Mouse.getX())
							+ (y - Mouse.getY()) * (y - Mouse.getY())) + Math.sqrt((x + f - Mouse.getX())
							* (x + f - Mouse.getX()) + (y - Mouse.getY()) * (y - Mouse.getY())));
					float distanceToMoveFoci = (float) (Math.sqrt((x2 - f - Mouse.getX()) * (x2 - f - Mouse.getX())
							+ (y2 - Mouse.getY()) * (y2 - Mouse.getY())) + Math.sqrt((x2 + f - Mouse.getX())
							* (x2 + f - Mouse.getX()) + (y2 - Mouse.getY()) * (y2 - Mouse.getY())));
					if (MainFrameRefactored.settings.resHeight - Mouse.getY() < MainFrameRefactored.settings.resHeight / 18) {
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
						veinsGrabbedAt = RayUtil.getRaySphereIntersection(Mouse.getX(), Mouse.getY(), renderer);
						renderer.setAddedModelOrientation(new Quaternion());
						if (veinsGrabbedAt != null)
							clickedOn = CLICKED_ON_VEINS_MODEL;
					}
				}

				if (clickedOn == CLICKED_ON_VEINS_MODEL) {
					double[] veinsHeldAt = RayUtil.getRaySphereIntersection(Mouse.getX(), Mouse.getY(), renderer);
					if (veinsHeldAt != null) {
						double[] rotationAxis = Vector.crossProduct(veinsGrabbedAt, veinsHeldAt);
						if (Vector.length(rotationAxis) > 0) {
							rotationAxis = Vector.normalize(rotationAxis);
							rotationAxis = Quaternion.quaternionReciprocal(renderer.getCurrentModelOrientation())
									.rotateVector3d(rotationAxis);
							double angle = Math.acos(Vector.dotProduct(veinsGrabbedAt, veinsHeldAt)
									/ (Vector.length(veinsGrabbedAt) * Vector.length(veinsHeldAt)));
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
				veinsGrabbedAt = null;
				Quaternion currentModelOrientation = Quaternion.quaternionMultiplication(
						renderer.getCurrentModelOrientation(), renderer.getAddedModelOrientation());
				renderer.setCurrentModelOrientation(currentModelOrientation);
				renderer.setAddedModelOrientation(new Quaternion());
			}
		}
	}
}
