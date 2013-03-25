package main;

import static org.lwjgl.opengl.GL11.GL_AMBIENT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DIFFUSE;
import static org.lwjgl.opengl.GL11.GL_EQUAL;
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_POSITION;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SPECULAR;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLight;
import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glStencilFunc;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glViewport;
import static tools.Tools.allocFloats;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import tools.Quaternion;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;

public class RendererPanel extends LWJGLRenderer{
	private HUD hud;

	// TODO check if this fits here
	private String title = "TITLE";
	private int fpsToDisplay = 15;

	private int fps;

	private long timePastFrame;

	private long timePastFps;

	private boolean isRunning = true;

	private Display GUI;

	private de.matthiasmann.twl.GUI gui;
	
	//Camera parameters
	static float fovy=45;
	static float zNear=10;
	static float zFar=10000;
	
	//camera pose
	// TODO CHANGE cameraOrientation
	static Quaternion cameraOrientation = new Quaternion();
	static float cameraX=0, cameraY=0, cameraZ=0, cameraMoveSpeed=1.667f;
	static double cameraRotationSpeed=(float)(72*Math.PI/180/60);

	/**
	 * Change gui update
	 * @param gui
	 * @throws LWJGLException
	 */
	public RendererPanel() throws LWJGLException {
		super();
	}
	
	// TODO Change this
	public void setGUI(GUI gui) {
		this.gui = gui;
	}

	/**
	 * @since 0.1
	 * @version 0.4
	 */
	public void mainLoop(){
		setupView();
		fps=0;
		timePastFrame=(Sys.getTime()*1000)/Sys.getTimerResolution();
		timePastFps=timePastFrame;
		fpsToDisplay=0;
		while(!Display.isCloseRequested() && isRunning){
			resetView();
			// TODO
			//pollInput();
			render();
			// TODO change updating
			gui.update();
			Display.update();
			// TODO
			//logic();
			Display.sync(MainFrameRefactored.settings.frequency);
		}
	}
	
	public void setHUD(HUD hud) {
		this.hud = hud;
	}
	
	/**
	 * @since 0.1
	 * @version 0.2
	 */
	private void setupView(){
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		glViewport(0, 0, MainFrameRefactored.settings.resWidth, MainFrameRefactored.settings.resHeight);
	    GL11.glMatrixMode(GL11.GL_PROJECTION);
	    GL11.glLoadIdentity();
	    GLU.gluPerspective(fovy, MainFrameRefactored.settings.resWidth/(float)MainFrameRefactored.settings.resHeight, zNear, zFar);
		glShadeModel(GL_SMOOTH);
		setCameraAndLight(0);
	}
	
	/**
	 * @since 0.1
	 * @version 0.1
	 */
	private void resetView(){
	    glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}
	
	/**
	 * @since 0.1
	 * @version 0.4
	 */
	private void render(){
	    if(MainFrameRefactored.settings.stereoEnabled){
	        float offset=MainFrameRefactored.settings.stereoValue/10f;
	        StencilMask.initStencil();
	        glStencilFunc(GL_EQUAL, 0, 0x01);
	        setCameraAndLight(offset);
            // TODO
	        //renderVeins();
            glStencilFunc(GL_EQUAL, 1, 0x01);
            setCameraAndLight(-offset);
            // TODO
            //renderVeins();
            glDisable(GL_STENCIL_TEST);
	    }
	    else{
	        setCameraAndLight(0);
	        // TODO
	        //renderVeins();
	    }
		//HUD
	    GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		if (hud != null)
			hud.drawHUD();
		if(MainFrameRefactored.settings.isFpsShown)Display.setTitle(title+" - FPS: "+fpsToDisplay);else	Display.setTitle(title);
	}
	
	/**
	 * @since 0.1
	 * @version 0.1
	 */
	private static void setCameraAndLight(float offset){
	    double v[]= new double[]{offset, 0, 0};
        v=cameraOrientation.rotateVector3d(v);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		Quaternion worldOrientation = Quaternion.quaternionReciprocal(cameraOrientation);
		glMultMatrix(worldOrientation.getRotationMatrix(false));
		glTranslatef(-cameraX+(float)v[0], -cameraY+(float)v[1], -cameraZ+(float)v[2]);
		
		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glLight(GL_LIGHT0,GL_POSITION,allocFloats(new float[]{0.0f, 1000.0f, 0.0f , 0.0f}));
		glLight(GL_LIGHT0,GL_DIFFUSE,allocFloats(new float[]{1f,1f,1f,1}));
		glLight(GL_LIGHT0,GL_AMBIENT,allocFloats(new float[]{0.3f,0.3f,0.3f,1}));
		glLight(GL_LIGHT0,GL_SPECULAR,allocFloats(new float[]{1.0f, 1.0f, 1.0f, 1.0f}));
	}
	
	
	
	
	
}
