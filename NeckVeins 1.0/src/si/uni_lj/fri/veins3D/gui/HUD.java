package si.uni_lj.fri.veins3D.gui;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_MODULATE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_ENV_MODE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexEnvf;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class HUD {
	private final String PATH = "res/imgs/";
	private final float ELLIPSEF = 1.1180339887498948482045868343656f;

	private Texture rotationCircle;
	private Texture circleGlow;
	private Texture movementCircle;
	private Texture rotationElipse;
	private Texture movementElipse;
	private Texture ellipseGlow;

	private int clickedOn;

	public int ellipseSide = 0;
	public float clickOnCircleAngle = 0;
	public float clickToCircleDistance = 0;

	/* Position and size */
	private float lastWindowWidth;
	private float lastWindowHeight;
	public float r;
	public float x1;
	public float y1;
	public float x2;
	public float y2;
	public float f;
	public float offset;

	
	//Font stuff
	private Texture font;
	private float fw = 0.065f, dx = 0.010f, fh = 0.13f, ff = 0.55f;
	public String title="";
	public String data="";
	public String tooltip="Odprite datoteko za testiranje, nato pritisnite Numpad 0";
	
	public HUD() {
		setHUDPositionAndSize();
		initHUDTextures();
	}

	public void setHUDPositionAndSize() {
		lastWindowWidth = VeinsWindow.settings.resWidth;
		lastWindowHeight = VeinsWindow.settings.resHeight;
		r = lastWindowWidth / 18;
		offset = r * 2 / 3;
		x1 = lastWindowWidth - offset - r;
		y1 = lastWindowHeight - lastWindowHeight / 18 - offset - r;
		x2 = lastWindowWidth - offset - r;
		y2 = lastWindowHeight - lastWindowHeight / 18 - 2 * offset - 3 * r;
		f = ELLIPSEF * r;
	}

	/**
	 * @since 0.1
	 * @version 0.4
	 */
	private void initHUDTextures() {
		// load textures
		try {
			rotationCircle = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream(PATH + "rotationCircle.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "rotationCircle.png unsuccessful");
			e.printStackTrace();
		}
		try {
			circleGlow = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(PATH + "circleGlow.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "circleGlow.png unsuccessful");
			e.printStackTrace();
		}
		try {
			movementCircle = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream(PATH + "movementCircle.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "movementCircle.png unsuccessful");
			e.printStackTrace();
		}
		try {
			rotationElipse = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream(PATH + "rotationElipse.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "rotationElipse.png unsuccessful");
			e.printStackTrace();
		}
		try {
			movementElipse = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream(PATH + "movementElipse.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "movementElipse.png unsuccessful");
			e.printStackTrace();
		}
		try {
			ellipseGlow = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(PATH + "ellipseGlow.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "ellipseGlow.png unsuccessful");
			e.printStackTrace();
		}
		
		//Load font
		try {
			font = TextureLoader.getTexture("PNG",
					ResourceLoader.getResourceAsStream(PATH + "font.png"));
		} catch (IOException e) {
			System.err.println("Loading texture " + PATH + "font.png unsuccessful");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @since 0.1
	 * @version 0.1
	 */
	public void drawHUD() {
		// check if window size has changed
		if (lastWindowWidth != VeinsWindow.settings.resWidth && lastWindowHeight != VeinsWindow.settings.resHeight)
			setHUDPositionAndSize();

		startHUD();

		glColor4f(1, 1, 1, 1);
		drawRotationEllipse();
		drawMovementEllipse();
		if (clickedOn == VeinsWindow.CLICKED_ON_MOVE_ELLIPSE || clickedOn == VeinsWindow.CLICKED_ON_ROTATION_ELLIPSE)
			drawEllipseGlow();

		glColor4f(1, 1, 1, 1);
		drawRotationCircle();
		drawMovementCircle();
		if (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_CIRCLE || clickedOn == VeinsWindow.CLICKED_ON_MOVE_CIRCLE)
			drawCircleGlow();

		endHUD();
	}

	private void startHUD() {
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		glOrtho(0, VeinsWindow.settings.resWidth, 0, VeinsWindow.settings.resHeight, 0.2f, 2);
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glLoadIdentity();
		glDisable(GL_LIGHTING);
		glEnable(GL_TEXTURE_2D);
		glClearColor(0f, 0f, 0f, 0f);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
	}

	private void drawRotationEllipse() {
		GL11.glBindTexture(GL_TEXTURE_2D, rotationElipse.getTextureID());
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x1 + 1.5f * r, y1 + r, -1.0f);
		glTexCoord2f(0, 0);
		glVertex3f(x1 - 1.5f * r, y1 + r, -1.0f);
		glTexCoord2f(0, 1);
		glVertex3f(x1 - 1.5f * r, y1 - r, -1.0f);
		glTexCoord2f(1, 1);
		glVertex3f(x1 + 1.5f * r, y1 - r, -1.0f);
		glEnd();
	}

	private void drawMovementEllipse() {
		GL11.glBindTexture(GL_TEXTURE_2D, movementElipse.getTextureID());
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x2 + 1.5f * r, y2 + r, -1.0f);
		glTexCoord2f(0, 0);
		glVertex3f(x2 - 1.5f * r, y2 + r, -1.0f);
		glTexCoord2f(0, 1);
		glVertex3f(x2 - 1.5f * r, y2 - r, -1.0f);
		glTexCoord2f(1, 1);
		glVertex3f(x2 + 1.5f * r, y2 - r, -1.0f);
		glEnd();
	}

	private void drawEllipseGlow() {
		float x = (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_ELLIPSE) ? x1 : x2;
		float y = (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_ELLIPSE) ? y1 : y2;

		glPushMatrix();
		glTranslatef(x, y, 0);
		if (ellipseSide == 0)
			glRotatef((float) (180), 0, 0, 1);
		glTranslatef(-x, -y, 0);
		GL11.glBindTexture(GL_TEXTURE_2D, ellipseGlow.getTextureID());
		glColor4f(1, 1, 1, 0.5f);
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x + 1.5f * r, y + r, -0.8f);
		glTexCoord2f(0, 0);
		glVertex3f(x - 1.5f * r, y + r, -0.8f);
		glTexCoord2f(0, 1);
		glVertex3f(x - 1.5f * r, y - r, -0.8f);
		glTexCoord2f(1, 1);
		glVertex3f(x + 1.5f * r, y - r, -0.8f);
		glEnd();
		glPopMatrix();
	}

	private void drawRotationCircle() {
		glColor4f(1, 1, 1, 1);
		GL11.glBindTexture(GL_TEXTURE_2D, rotationCircle.getTextureID());
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x1 + r, y1 + r, -0.6f);
		glTexCoord2f(0, 0);
		glVertex3f(x1 - r, y1 + r, -0.6f);
		glTexCoord2f(0, 1);
		glVertex3f(x1 - r, y1 - r, -0.6f);
		glTexCoord2f(1, 1);
		glVertex3f(x1 + r, y1 - r, -0.6f);
		glEnd();
	}

	private void drawMovementCircle() {
		GL11.glBindTexture(GL_TEXTURE_2D, movementCircle.getTextureID());
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x2 + r, y2 + r, -0.6f);
		glTexCoord2f(0, 0);
		glVertex3f(x2 - r, y2 + r, -0.6f);
		glTexCoord2f(0, 1);
		glVertex3f(x2 - r, y2 - r, -0.6f);
		glTexCoord2f(1, 1);
		glVertex3f(x2 + r, y2 - r, -0.6f);
		glEnd();
	}
	//FONT STUFF

	private void drawString(String text, int size){
		 GL11.glBindTexture(GL_TEXTURE_2D, font.getTextureID());  
		    int[] charPos = {0, 0};
		    int[] charCode = {0, 0};
		    
		    int len = text.length();
		    
		    glBegin(GL_QUADS);
		    for (int i = 0; i < len; i++) {
		      charCode = getCode(text.charAt(i));
		      
		      GL11.glTexCoord2f(dx+charCode[1]*fw, (charCode[0]+1)*fh);      GL11.glVertex3f(charPos[0], charPos[1], -0.6f);
		      GL11.glTexCoord2f(dx+(charCode[1]+1)*fw, (charCode[0]+1)*fh);  GL11.glVertex3f(charPos[0]+ff*size, charPos[1], -0.6f);
		      GL11.glTexCoord2f(dx+(charCode[1]+1)*fw, charCode[0]*fh);      GL11.glVertex3f(charPos[0]+ff*size, charPos[1]+size, -0.6f);
		      GL11.glTexCoord2f(dx+charCode[1]*fw, charCode[0]*fh);          GL11.glVertex3f(charPos[0], charPos[1]+size, -0.6f);
		      charPos[0]+=ff*size;
		    }
		    glEnd();
	}
	
	private float textWidth(String s, int size) {
		return s.length()*size*ff;
	}
	  
	public void drawText(){
		if (lastWindowWidth != VeinsWindow.settings.resWidth && lastWindowHeight != VeinsWindow.settings.resHeight)
			setHUDPositionAndSize();

		startHUD();
		glColor4f(1, 1, 1, 1);
		//drawRotationEllipse();
		GL11.glTranslatef(x1-r, y1-r, 0);
		
		glPushMatrix();
			GL11.glTranslatef(-(VeinsWindow.settings.resWidth/8.0f+textWidth(title,30)/2.0f), 150, 0);
			drawString(title, 30);
		glPopMatrix();
		
		glPushMatrix();
			GL11.glTranslatef(-(VeinsWindow.settings.resWidth/8.0f+textWidth(tooltip,25)/2.0f), 100, 0);
			drawString(tooltip, 25);
		glPopMatrix();
		
		glPushMatrix();
			GL11.glTranslatef(-(VeinsWindow.settings.resWidth/8.0f+textWidth(data,25)/2.0f), -400, 0);
			drawString(data, 25);
		glPopMatrix();
		
		endHUD();
	}
	
	
	private void drawCircleGlow() {
		float x = (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_CIRCLE) ? x1 : x2;
		float y = (clickedOn == VeinsWindow.CLICKED_ON_ROTATION_CIRCLE) ? y1 : y2;

		glPushMatrix();
		glTranslatef(x, y, 0);
		glRotatef((float) (180 * clickOnCircleAngle / Math.PI), 0, 0, 1);
		glTranslatef(-x, -y, 0);
		GL11.glBindTexture(GL_TEXTURE_2D, circleGlow.getTextureID());
		glColor4f(1, 1, 1, (float) clickToCircleDistance);
		glBegin(GL_QUADS);
		glTexCoord2f(1, 0);
		glVertex3f(x + r, y + r, -0.4f);
		glTexCoord2f(0, 0);
		glVertex3f(x - r, y + r, -0.4f);
		glTexCoord2f(0, 1);
		glVertex3f(x - r, y - r, -0.4f);
		glTexCoord2f(1, 1);
		glVertex3f(x + r, y - r, -0.4f);
		glEnd();
		glPopMatrix();
	}

	private void endHUD() {
		glDisable(GL_BLEND);
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_LIGHTING);
		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();
	}

	/**
	 * Sets the clickedOn so the HUD "knows" if it must response
	 * 
	 * @param clickedOn
	 */
	public void setClickedOn(int clickedOn) {
		this.clickedOn = clickedOn;
	}
	
	/*For fonts */
	private static int[] getCode(char c) {
	    switch(c) {
	      case 'a':
	        return new int[] {0, 0};
	      case 'b':
	        return new int[] {0, 1};
	      case 'c':
	        return new int[] {0, 2};
//	      case 'è':
//	        return new int[] {0, 3};
	      case 'd':
	        return new int[] {0, 4};
	      case 'e':
	        return new int[] {0, 5};
	      case 'f':
	        return new int[] {0, 6};
	      case 'g':
	        return new int[] {0, 7};
	      case 'h':
	        return new int[] {0, 8};
	      case 'i':
	        return new int[] {0, 9};
	      case 'j':
	        return new int[] {0, 10};
	      case 'k':
	        return new int[] {0, 11};
	      case 'l':
	        return new int[] {0, 12};
	      case 'm':
	        return new int[] {0, 13};
	      case 'n':
	        return new int[] {0, 14};
	      case 'o':
	        return new int[] {1, 0};
	      case 'p':
	        return new int[] {1, 1};
	      case 'q':
	        return new int[] {1, 2};
	      case 'r':
	        return new int[] {1, 3};
	      case 's':
	        return new int[] {1, 4};
//	      case 'š':
//	        return new int[] {1, 5};
	      case 't':
	        return new int[] {1, 6};
	      case 'u':
	        return new int[] {1, 7};
	      case 'v':
	        return new int[] {1, 8};
	      case 'w':
	        return new int[] {1, 9};
	      case 'x':
	        return new int[] {1, 10};
	      case 'y':
	        return new int[] {1, 11};
	      case 'z':
	        return new int[] {1, 12};
//	      case 'ž':
//	        return new int[] {1, 13};
	      case 'A':
	        return new int[] {2, 0};
	      case 'B':
	        return new int[] {2, 1};
	      case 'C':
	        return new int[] {2, 2};
//	      case 'È':
//	        return new int[] {2, 3};
	      case 'D':
	        return new int[] {2, 4};
	      case 'E':
	        return new int[] {2, 5};
	      case 'F':
	        return new int[] {2, 6};
	      case 'G':
	        return new int[] {2, 7};
	      case 'H':
	        return new int[] {2, 8};
	      case 'I':
	        return new int[] {2, 9};
	      case 'J':
	        return new int[] {2, 10};
	      case 'K':
	        return new int[] {2, 11};
	      case 'L':
	        return new int[] {2, 12};
	      case 'M':
	        return new int[] {2, 13};
	      case 'N':
	        return new int[] {2, 14};
	      case 'O':
	        return new int[] {3, 0};
	      case 'P':
	        return new int[] {3, 1};
	      case 'Q':
	        return new int[] {3, 2};
	      case 'R':
	        return new int[] {3, 3};
	      case 'S':
	        return new int[] {3, 4};
//	      case 'Š':
//	        return new int[] {3, 5};
	      case 'T':
	        return new int[] {3, 6};
	      case 'U':
	        return new int[] {3, 7};
	      case 'V':
	        return new int[] {3, 8};
	      case 'W':
	        return new int[] {3, 9};
	      case 'X':
	        return new int[] {3, 10};
	      case 'Y':
	        return new int[] {3, 11};
	      case 'Z':
	        return new int[] {3, 12};
//	      case 'Ž':
//	        return new int[] {3, 13};
	      case '0':
	        return new int[] {4, 0};
	      case '1':
	        return new int[] {4, 1};
	      case '2':
	        return new int[] {4, 2};
	      case '3':
	        return new int[] {4, 3};
	      case '4':
	        return new int[] {4, 4};
	      case '5':
	        return new int[] {4, 5};
	      case '6':
	        return new int[] {4, 6};
	      case '7':
	        return new int[] {4, 7};
	      case '8':
	        return new int[] {4, 8};
	      case '9':
	        return new int[] {4, 9};
	      case '!':
	        return new int[] {5, 0};
	      case '#':
	        return new int[] {5, 1};
	      case '$':
	        return new int[] {5, 2};
	      case '%':
	        return new int[] {5, 3};
	      case '&':
	        return new int[] {5, 4};
	      case '"':
	        return new int[] {5, 5};
	      case '/':
	        return new int[] {5, 6};
	      case '(':
	        return new int[] {5, 7};
	      case ')':
	        return new int[] {5, 8};
	      case '=':
	        return new int[] {5, 9};
	      case '?':
	        return new int[] {5, 10};
	      case '\'':
	        return new int[] {5, 11};
	      case '+':
	        return new int[] {5, 12};
	      case '*':
	        return new int[] {5, 13};
	      case '-':
	        return new int[] {5, 14};
	      case '_':
	        return new int[] {6, 0};
	      case '<':
	        return new int[] {6, 1};
	      case '>':
	        return new int[] {6, 2};
	      case '[':
	        return new int[] {6, 3};
	      case ']':
	        return new int[] {6, 4};
	      case '{':
	        return new int[] {6, 5};
	      case '}':
	        return new int[] {6, 6};
	      case ',':
	        return new int[] {6, 7};
	      case ';':
	        return new int[] {6, 8};
	      case '.':
	        return new int[] {6, 9};
	      case ':':
	        return new int[] {6, 10};
	      case '\\':
	        return new int[] {6, 11};
	      default:
	        return new int[] {1, 14};
	    }
	  }

}
